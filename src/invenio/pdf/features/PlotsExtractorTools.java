package invenio.pdf.features;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;


import invenio.pdf.core.PDFPageManager;
import java.awt.Rectangle;

public class PlotsExtractorTools {

    public static void annotateImage(Graphics2D graphics, List<Plot> plots,
            TextAreas textAreas) {
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

        return;
    }

    /** debug code -> rendering the entire page */
//    
//    public static void annotateImage(Graphics2D graphics,
//            PDFPageManager manager) {
//        /**
//         * Annotates image with the data from teh operation manager
//         */
//        graphics.setTransform(AffineTransform.getRotateInstance(0));
//        graphics.setPaintMode();
//
//        // drawing all the operations
//        graphics.setColor(Color.green);
//
////        Set<Operation> operations = opManager.getOperations();
////
////        for (Operation operation : operations) {
////            Rectangle2D opRec = opManager.getOperationBoundary2D(operation);
////            if (opRec != null) {
////                graphics.drawRect((int) opRec.getMinX(), (int) opRec.getMinY(), (int) opRec.getWidth(), (int) opRec.getHeight());
////            }
////        }
////
////        // drawing text operations
////
////        graphics.setColor(Color.red);
////
////        operations = opManager.getTextOperations();
////
////        for (CSOperation operation : operations) {
////            Rectangle2D opRec = opManager.getOperationBoundary2D(operation);
////            if (opRec != null) {
////                // TODO: take care of having all the text operators
////
////                graphics.drawRect((int) opRec.getMinX(), (int) opRec.getMinY(), (int) opRec.getWidth(), (int) opRec.getHeight());
////            }
////        }
//
//
//
//        graphics.setColor(Color.blue);
//        // now painting clustered operations
//        Map<Rectangle, List<Operation>> clustered = clusterOperations(manager);
//        for (Rectangle rec : clustered.keySet()) {
//            graphics.drawRect(rec.x, rec.y, rec.width, rec.height);
//        }
//    }
    /**
     * Render plot into a buffered image that can be later saved to the file
     * @param plot
     */
    private void renderPlotToImage(Plot plot) {
        //TODO: Implement
    }

    /**
     * Renders plot to a SVG format (if possible -> plot is not a raster graphics)
     * @param plot
     */
    private void renderPlotToSVG(Plot plot) {
        // TODO: Implement
    }

    /**
     * Finds all the textual references to all the plots
     * @param plots
     * @param managers
     */
    private void findPlotReferences(List<Plot> plots, List<PDFPageManager> managers) {
        //TODO: Implement
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
