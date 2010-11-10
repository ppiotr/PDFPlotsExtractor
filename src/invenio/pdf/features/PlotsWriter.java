/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.Images;
import invenio.pdf.core.ExtractorParameters;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.documentProcessing.PDFDocumentTools;
import java.awt.Color;
import java.awt.Rectangle;
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

    public static void writePlots(PDFDocumentManager document, String outputDirectory) throws FeatureNotPresentException, Exception {
        Plots plots = (Plots) document.getDocumentFeature(Plots.featureName);
        for (List<Plot> pagePlots : plots.plots) {
            for (Plot plot : pagePlots) {
                writePlot(plot, "");
            }
        }
    }

    public static void writePlot(Plot plot, String filePrefix) throws FileNotFoundException, IOException {
        setFileNames(plot);
        writePlotMetadata(plot);
        writePlotPng(plot);
        writePlotSvg(plot);
    }

    public static void writePlotMetadata(Plot plot) throws FileNotFoundException, IOException {
        FileOutputStream outputStream = new FileOutputStream(plot.getFileName("metadata"));
        PrintStream ps = new PrintStream(outputStream);
        ps.println("identifier= " + plot.getId());
        ps.println("caption=" + plot.getCaption());
        ps.println("filePng=" + plot.getFileName("png"));
        ps.println("fileSvg=" + plot.getFileName("svg"));
        ps.close();
        outputStream.close();
    }

    public static void writePlotPng(Plot plot) throws IOException {
        Rectangle b = plot.getBoundary();

        Images.writeImageToFile(plot.getPageManager().getRenderedPage().getSubimage(b.x, b.y, b.width, b.height), plot.getFileName("png"));
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
        PDFDocumentTools.renderSomePageOperations(plot.getPageManager(), plot.getOperations(), svgGenerator, params.getPageScale());
//        svgGenerator.setPaint(Color.red);
//        svgGenerator.fill(new Rectangle(10, 10, 100, 100));
//        svgGenerator.setPaint(Color.CYAN);
//
//        svgGenerator.fill(new Rectangle(20, 20, 30, 30));
        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        boolean useCSS = true; // we want to use CSS style attributes
        FileOutputStream fos = new FileOutputStream(plot.getFileName("svg"));
        Writer out = new OutputStreamWriter(fos, "UTF-8");
        svgGenerator.stream(out, useCSS);
        out.close();
        fos.close();
    }

    /**
     * Calculates names of files where the plot should be saved
     * @param plot
     */
    public static void setFileNames(Plot plot) {
        //TODO Create some more realistic file names
        plot.addFileName("metadata", "/home/piotr/pdf/" + plot.getId() + "metadata.txt");
        plot.addFileName("png", "/home/piotr/pdf/" + plot.getId() + ".png");
        plot.addFileName("svg", "/home/piotr/pdf/" + plot.getId() + ".svg");
    }
}
