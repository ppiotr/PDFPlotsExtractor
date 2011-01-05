/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.Images;
import invenio.pdf.core.ExtractorLogger;
import invenio.pdf.core.ExtractorParameters;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.documentProcessing.PDFDocumentTools;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;

/**
 * A class allowing to write plots together with meta-data into files
 * @author piotr
 */
public class PlotsWriter {

    public static void writePlots(PDFDocumentManager document, File outputDirectory)
            throws FeatureNotPresentException, Exception {
        Plots plots = (Plots) document.getDocumentFeature(Plots.featureName);
        for (List<Plot> pagePlots : plots.plots) {
            for (Plot plot : pagePlots) {
                writePlot(plot, outputDirectory);
            }
        }
    }

    public static void writePlot(Plot plot, File outputDirectory) throws FileNotFoundException, IOException {
        // first assure, the output directory exists

        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        setFileNames(plot, outputDirectory);
        writePlotMetadata(plot);
        writePlotPng(plot);
        writePlotSvg(plot);
    }

    public static void writePlotMetadata(Plot plot) throws FileNotFoundException, IOException {
        FileOutputStream outputStream = new FileOutputStream(plot.getFile("metadata"));
        PrintStream ps = new PrintStream(outputStream);
        ps.println("identifier= " + plot.getId());
        ps.println("caption=" + plot.getCaption());
        ps.println("filePng=" + plot.getFile("png").getPath());
        ps.println("fileSvg=" + plot.getFile("svg").getPath());
        ps.close();
        outputStream.close();
    }

    public static void writePlotPng(Plot plot) throws IOException {
        Rectangle b = plot.getBoundary();

        Images.writeImageToFile(plot.getPageManager().getRenderedPage().getSubimage(b.x, b.y, b.width, b.height), plot.getFile("png"));
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
        svgGenerator.setClip(0, 0, (int) plotBd.getWidth(), (int) plotBd.getHeight());
        svgGenerator.setTransform(AffineTransform.getTranslateInstance(-plotBd.getX(), -plotBd.getY()));
        svgGenerator.setSVGCanvasSize(new Dimension((int) plotBd.getWidth(), (int) plotBd.getHeight()));

        PDFDocumentTools.renderToCanvas(plot.getPageManager(), svgGenerator, params.getPageScale());

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

        plot.addFile("metadata", new File(outputDirectory.getPath(), plot.getId() + "metadata.txt"));
        plot.addFile("png", new File(outputDirectory.getPath(), plot.getId() + ".png"));
        plot.addFile("svg", new File(outputDirectory.getPath(), plot.getId() + ".svg"));
    }
}
