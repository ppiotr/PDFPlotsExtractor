package invenio.pdf.features;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;


import invenio.pdf.core.PDFPageManager;
import java.awt.Rectangle;

public class PlotsExtractorTools {

    public static void annotateImage(Graphics2D graphics, List<Plot> plots,
            TextAreas textAreas, PageLayout layout) {
        // annotate image with plot information
        graphics.setTransform(AffineTransform.getRotateInstance(0));
        graphics.setPaintMode();

        graphics.setColor(Color.blue);
        
        for (Plot plot : plots) {
            Rectangle boundary = plot.getBoundary();
            graphics.drawRect((int) boundary.getX(), (int) boundary.getY(),
                    (int) boundary.getWidth(), (int) boundary.getHeight());
        }

        graphics.setColor(Color.green);
        for (Rectangle bd : textAreas.areas.keySet()) {
            graphics.drawRect((int) bd.getX(), (int) bd.getY(),
                    (int) bd.getWidth(), (int) bd.getHeight());
        }
        // drawing column rectangles

        Color[] columnColors = new Color[]{Color.magenta, Color.pink, Color.red, Color.blue, Color.gray, Color.orange};
        int colorIndex = 0;
        if (layout.areas.isEmpty()) {
            System.out.println("Page without a layout ! ");
        }

        for (List<Rectangle> area : layout.areas) {
            if (colorIndex == columnColors.length) {
                graphics.setColor(Color.black);
            } else {
                graphics.setColor(columnColors[colorIndex]);
                colorIndex++;
            }

            for (Rectangle bd : area) {
                graphics.drawRect(bd.x, bd.y, bd.width-1, bd.height-1);
                graphics.drawRect(bd.x + 1, bd.y + 1, bd.width - 3, bd.height - 3);
            }
        }
        return;
    }
    /**
     * Perform a complete processing for one document
     *
     * @param fileName
     *            The name of the pdf file to extract plots from
     * @param outputDirectory
     *            The name of the directory, where the results should be
     *            saved
     *  @return list of plots found in the document
     */
//    public static List<Plot> processDocument(String fileName, String outputDirectory)
//            throws IOException, COSLoadException {
//        LinkedList<Plot> result = new LinkedList<Plot>();
//
//        //List<PDFPageOperationsManager> documentPages = getOperationsFromDocument(doc, fileName);
//        PDFDocumentManager processedDocument = PDFDocumentPreprocessor.readPDFDocument(fileName);
//
//        for (int i = 0; i < processedDocument.getPagesNumber(); ++i) {
//            // Now we want to process document page by page and detect potential plots
//            System.out.println("page processed");
//            List<Plot> currentPlots = getPlotsFromPage(processedDocument.getPage(i));
//            result.addAll(currentPlots);
//        }
//
//        return result;
//    }
}
