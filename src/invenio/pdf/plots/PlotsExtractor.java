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

public class PlotsExtractor {

    public static int SCALE = 2;

    /**
     * Clusters all operations passed as parameter and returns all the clusters
     * @param operations
     * @param verticalMargin 
     * @param horizontalMargin 
     * @return
     */
    private static Map<Rectangle, List<Operation>> clusterOperations(
            Set<Operation> operations, PDFPageManager manager, int horizontalMargin,
            int verticalMargin) {

        SpatialClusterManager<Operation> clusterManager =
                new SpatialClusterManager<Operation>(
                ExtractorGeometryTools.extendRectangle(manager.getPageBoundary(),
                horizontalMargin * 2, verticalMargin * 2),
                horizontalMargin, verticalMargin);

        for (Operation op : operations) {
            if (op instanceof DisplayedOperation) {
                DisplayedOperation dOp = (DisplayedOperation) op;
                Rectangle srcRec = dOp.getBoundary();

                Rectangle rec = new Rectangle((int) srcRec.getX(), (int) srcRec.getY(), (int) srcRec.getWidth(), (int) srcRec.getHeight());
                clusterManager.addRectangle(rec, op);
            }
        }
        return clusterManager.getFinalBoundaries();
    }

    /**
     * while (!constant point){
     *    Cluster all operations. graphical together and text together.
     *    Add text clusters overlaping with graphical clusters to the graphical clusters
     * }
     * 
     * Every cluster is represented by the rectangular area in the space
     *
     * @param opManager
     */
    private static Map<Rectangle, List<Operation>> clusterOperations(PDFPageManager manager) {
        Set<Operation> interestingOperations = manager.getGraphicalOperations();

        return clusterOperations(interestingOperations, manager, 100, 50);
    }

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
    
    public static void annotateImage(Graphics2D graphics,
            PDFPageManager manager) {
        /**
         * Annotates image with the data from teh operation manager
         */
        graphics.setTransform(AffineTransform.getRotateInstance(0));
        graphics.setPaintMode();

        // drawing all the operations
        graphics.setColor(Color.green);

//        Set<Operation> operations = opManager.getOperations();
//
//        for (Operation operation : operations) {
//            Rectangle2D opRec = opManager.getOperationBoundary2D(operation);
//            if (opRec != null) {
//                graphics.drawRect((int) opRec.getMinX(), (int) opRec.getMinY(), (int) opRec.getWidth(), (int) opRec.getHeight());
//            }
//        }
//
//        // drawing text operations
//
//        graphics.setColor(Color.red);
//
//        operations = opManager.getTextOperations();
//
//        for (CSOperation operation : operations) {
//            Rectangle2D opRec = opManager.getOperationBoundary2D(operation);
//            if (opRec != null) {
//                // TODO: take care of having all the text operators
//
//                graphics.drawRect((int) opRec.getMinX(), (int) opRec.getMinY(), (int) opRec.getWidth(), (int) opRec.getHeight());
//            }
//        }



        graphics.setColor(Color.blue);
        // now painting clustered operations
        Map<Rectangle, List<Operation>> clustered = clusterOperations(manager);
        for (Rectangle rec : clustered.keySet()) {
            graphics.drawRect(rec.x, rec.y, rec.width, rec.height);
        }
    }

    /**
     * Based on the content of the page, calculate the most likely safe margins
     * between graphic operations inside one plot.
     *
     *
     * @param manager Page manager containing all the operations creating one
     *                document page
     * @return the integer array with two elements : horizontal and vertical
     *          margins.
     */
    private static int[] calculateGraphicsMargins(PDFPageManager manager) {
        ExtractorParameters parameters =
                ExtractorParameters.getExtractorParameters();
        double hPercentage = parameters.getHorizontalMargin();
        double vPercentage = parameters.getVerticalMargin();

        Rectangle boundary = manager.getPageBoundary();
        int hMargin = (int) (boundary.getWidth() * hPercentage);
        int vMargin = (int) (boundary.getHeight() * vPercentage);
        System.out.println("Margins: horizontal=" + hMargin + " vertical=" + vMargin);
        return new int[]{hMargin, vMargin};
    }

    /**
     * Finds all the plots present in the PDF page. Plots are extracted together
     * with captions but without references because captions appear
     * on the same page and textual references have to be found globally in the document.
     *
     * @param manager
     * @return List of plot descriptors
     */
    public static List<Plot> getPlotsFromPage(PDFPageManager manager) {
        List<Plot> plots = new LinkedList<Plot>();

        int[] margins = calculateGraphicsMargins(manager);


        /*************
         * Treating graphics operations - clustering them, filtering and
         * including appropriate text operations
         **************/
        Set<Operation> graphicalOperations = manager.getGraphicalOperations();

//        graphicOperations.removeAll(manager.getTextOperations());

        Map<Rectangle, List<Operation>> graphicalRegions = clusterOperations(
                graphicalOperations, manager, margins[0], margins[1]);

        Map<Rectangle, List<Operation>> shrinkedRegions =
                ExtractorGeometryTools.shrinkRectangleMap(graphicalRegions,
                margins[0], margins[1]);


        Map<Rectangle, List<Operation>> graphicalPlotRegions =
                PlotHeuristics.removeFalsePlots(shrinkedRegions);


        Map<Rectangle, List<Operation>> plotRegions =
                PlotHeuristics.includeTextParts(graphicalPlotRegions, manager);

//        Map<Rectangle, List<CSOperation>> plotRegions = graphicalPlotRegions;

        // we are done with plot images -> creating plot structures for every
        // selected region

        for (Rectangle area : plotRegions.keySet()) {
            Plot plot = new Plot();
            plot.setBoundary(area);
            plot.addOperations(plotRegions.get(area));
            plots.add(plot);
        }

        // 2) too small areas/areas with too small aspect rations -> not plots ! 

        // Now including text operations that are overlapping with plots -> they
        // are part of a plot

//        for (TextOp operation : textOperations) {
//            Rectangle intersecting = findIntersectingRegion(operation, graphicalRegions);
//            if (intersecting != null) {
//                extendRegionByRectangle();
//            }
//        }



        /** Treating text operations - we want to recover the text flow and a \
         * very general view of its structure - division to blocks

         */
        // now clustering the text operations - we want to be able to detect
        // text blocks. For example caption is usually a separate block that is
        // separated by a bigger distance than others
        return plots;
    }

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
    public static List<Plot> processDocument(String fileName, String outputDirectory)
            throws IOException, COSLoadException {
        LinkedList<Plot> result = new LinkedList<Plot>();
        FileLocator locator = new FileLocator(fileName);
        PDDocument doc = PDDocument.createFromLocator(locator);

        File cosLogFile = new File(fileName + ".cos");
        PrintStream cosStream = new PrintStream(cosLogFile);

        //List<PDFPageOperationsManager> documentPages = getOperationsFromDocument(doc, fileName);
        PDFDocumentManager processedDocument = PDFDocumentPreprocessor.readPDFDocument(fileName);

        for (int i = 0; i < processedDocument.getPagesNumber(); ++i) {
            // Now we want to process document page by page and detect potential plots
            System.out.println("page processed");
            List<Plot> currentPlots = getPlotsFromPage(processedDocument.getPage(i));
            result.addAll(currentPlots);
        }

        return result;
    }
}
