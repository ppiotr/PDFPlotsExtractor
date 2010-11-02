package invenio.pdf.plots;

import invenio.pdf.core.ExtractorParameters;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;


import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.tools.locator.FileLocator;
import invenio.common.ExtractorGeometryTools;
import invenio.common.SpatialClusterManager;
import invenio.pdf.core.DisplayedOperation;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.documentProcessing.PDFDocumentPreprocessor;
import java.awt.Rectangle;
import java.util.LinkedList;

public class PlotsExtractorTools {
   




    public static void annotateImage(Graphics2D graphics, List<Plot> plots) {
        // annotate image with plot information
        graphics.setTransform(AffineTransform.getRotateInstance(0));
        graphics.setPaintMode();
        graphics.setColor(Color.blue);
        for (Plot plot : plots) {
            Rectangle boundary = plot.getBoundary();
            graphics.drawRect((int) boundary.getX(), (int) boundary.getY(),
                    (int) boundary.getWidth(), (int) boundary.getHeight());
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
    private void findPlotreferences(List<Plot> plots, List<PDFPageManager> managers) {
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
