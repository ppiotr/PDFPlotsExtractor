/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.Images;
import invenio.common.XmlTools;
import invenio.pdf.core.ExtractorLogger;
import invenio.pdf.core.ExtractorParameters;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.documentProcessing.PDFDocumentTools;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class allowing to write plots together with meta-data into files
 * @author piotr
 */
public class PlotsWriter {

    public static void writePlots(PDFDocumentManager document, File outputDirectory, boolean saveAttachments)
            throws FeatureNotPresentException, Exception {
        Plots plots = (Plots) document.getDocumentFeature(Plots.featureName);
        for (List<Plot> pagePlots : plots.plots) {
            for (Plot plot : pagePlots) {
                writePlot(plot, outputDirectory, saveAttachments);
            }
        }
    }

    public static void writePlot(Plot plot, File outputDirectory, boolean saveAttachments) throws FileNotFoundException, Exception {
        // first assure, the output directory exists

        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        setFileNames(plot, outputDirectory);

        writePlotMetadataToFile(plot);
        if (saveAttachments) {
            writePlotPng(plot);
            writePlotSvg(plot);
            writePlotAnnotatedPage(plot);
            writePlotCaptionImage(plot);
        }
    }

    public static void writePlotMetadataToFile(Plot plot) throws FileNotFoundException, Exception {
        LinkedList<Plot> plots = new LinkedList<Plot>();
        plots.add(plot);
        writePlotsMetadataToFile(plots, plot.getFile("metadata"));
    }

    public static void writePlotsMetadataToFile(List<Plot> plots, File plotMetadataFile)
            throws FileNotFoundException, Exception {

        FileOutputStream outputStream = new FileOutputStream(plotMetadataFile);

        PrintStream ps = new PrintStream(outputStream);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element plotsCollectionElement = document.createElement("plots");
        document.appendChild(plotsCollectionElement);

        for (Plot plot : plots) {
            writePlotMetadata(document, plotsCollectionElement, plot);
        }
        // saving the output into a file

        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);

        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);

        outputStream.close();
    }

    public static void writePlotMetadata(Document document, Element containerElement, Plot plot)
            throws FileNotFoundException, Exception {


        Element rootElement = document.createElement("plot");
        containerElement.appendChild(rootElement);

        // plot identifier

        XmlTools.appendElementWithTextNode(document, rootElement, "identifier", plot.getId());

        //        ps.println("identifier= " + plot.getId());

        // plot  image files
        XmlTools.appendElementWithTextNode(document, rootElement, "png", plot.getFile("png").getAbsolutePath());
        XmlTools.appendElementWithTextNode(document, rootElement, "svg", plot.getFile("svg").getAbsolutePath());

        // location of the source

        Element locationElement = document.createElement("location");
        rootElement.appendChild(locationElement);
        // main document file
        XmlTools.appendElementWithTextNode(document, locationElement, "pdf", plot.getPageManager().getDocumentManager().getSourceFileName());
        // document scale
        XmlTools.appendElementWithTextNode(document, locationElement, "scale", "" + ExtractorParameters.getExtractorParameters().getPageScale());
        // current page resulution
        Rectangle pb = plot.getPageManager().getPageBoundary();
        if (pb != null) {
            Element pageResolution = document.createElement("pageResolution");

            locationElement.appendChild(pageResolution);
            XmlTools.appendElementWithTextNode(document, pageResolution, "width", "" + pb.width);
            XmlTools.appendElementWithTextNode(document, pageResolution, "height", "" + pb.height);
        }
        // main document page (indexed from 0)
        XmlTools.appendElementWithTextNode(document, locationElement, "pageNumber", "" + plot.getPageManager().getPageNumber());

        // coordinates in the main document
        XmlTools.appendRectangle(document, locationElement, "pageCoordinates", plot.getBoundary());




        Element captionEl = document.createElement("caption");
        rootElement.appendChild(captionEl);
        // caption coordinates
        XmlTools.appendRectangle(document, captionEl, "coordinates",
                plot.getCaptionBoundary());
        // caption text
        XmlTools.appendElementWithTextNode(document, captionEl, "captionText",
                "" + plot.getCaption());
        // caption image
        XmlTools.appendElementWithTextNode(document, rootElement,
                "captionImage", plot.getFile("captionImage").getAbsolutePath());
        // debug image
        XmlTools.appendElementWithTextNode(document, rootElement,
                "annotatedImage",
                plot.getFile("annotatedImage").getAbsolutePath());
    }

    /** Prepare and write the image of an annotated plot
     *
     */
    public static void writePlotAnnotatedPage(Plot plot) throws Exception {
        BufferedImage pageImg = Images.copyBufferedImage(plot.getPageManager().getRenderedPage());
        Graphics2D gr = (Graphics2D) pageImg.getGraphics();
        gr.setTransform(AffineTransform.getTranslateInstance(0, 0));
        gr.setColor(Color.blue);
        Rectangle bd = plot.getBoundary();
        gr.drawRect(bd.x, bd.y, bd.width, bd.height);
        gr.setColor(Color.green);
        bd = plot.getCaptionBoundary();
        if (bd != null) {
            gr.drawRect(bd.x, bd.y, bd.width, bd.height);
        }
        Images.writeImageToFile(pageImg, plot.getFile("annotatedImage"));
    }

    public static void writePlotPng(Plot plot) throws IOException {
        Rectangle b = plot.getBoundary();
        if (plot.getPageManager().getRenderedPage() != null) {
            Images.writeImageToFile(plot.getPageManager().getRenderedPage().getSubimage(b.x, b.y, b.width, b.height), plot.getFile("png"));
        }
    }

    public static void writePlotCaptionImage(Plot plot) throws IOException {
        Rectangle b = plot.getCaptionBoundary();
        if (plot.getPageManager().getRenderedPage() != null) {
            if (b == null) {
                return;
            }
            Images.writeImageToFile(plot.getPageManager().getRenderedPage().getSubimage(b.x, b.y, b.width, b.height), plot.getFile("captionImage"));
        }
    }

    public static void writePlotSvg(Plot plot) throws UnsupportedEncodingException, SVGGraphics2DIOException, FileNotFoundException, IOException {
        // Get a DOMImplementation.
        DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        // Ask the test to render into the SVG Graphics2D implementation.

        // paint
        ExtractorParameters params = ExtractorParameters.getExtractorParameters();

        Rectangle plotBd = plot.getBoundary();
        svgGenerator.setClip(0, 0, (int) plotBd.getWidth(),
                (int) plotBd.getHeight());
        svgGenerator.setTransform(AffineTransform.getTranslateInstance(-plotBd.getX(), -plotBd.getY()));
        svgGenerator.setSVGCanvasSize(new Dimension((int) plotBd.getWidth(),
                (int) plotBd.getHeight()));

        PDFDocumentTools.renderToCanvas(plot.getPageManager(), svgGenerator,
                params.getPageScale());

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        boolean useCSS = true; // we want to use CSS style attributes
        FileOutputStream fos = new FileOutputStream(plot.getFile("svg"));
        Writer out = new OutputStreamWriter(fos, "UTF-8");
        svgGenerator.stream(out, useCSS);
        out.close();
        fos.close();
    }

    /**
     * Calculates names of files where the plot should be saved
     * @param plot
     */
    public static void setFileNames(Plot plot, File outputDirectory) {
        //TODO Create some more realistic file names
        ExtractorLogger.logMessage(2, "Saving a plot from page "
                + plot.getPageManager().getPageNumber()
                + " number of operations: " + plot.getOperations().size());

        plot.addFile("metadata", new File(outputDirectory.getPath(),
                plot.getId() + "_metadata.xml"));
        plot.addFile("png", new File(outputDirectory.getPath(),
                plot.getId() + ".png"));
        plot.addFile("svg", new File(outputDirectory.getPath(),
                plot.getId() + ".svg"));
        plot.addFile("annotatedImage", new File(outputDirectory.getPath(),
                plot.getId() + "_annotated.png"));
        plot.addFile("captionImage", new File(outputDirectory.getPath(),
                plot.getId() + "caption.png"));

    }
}
