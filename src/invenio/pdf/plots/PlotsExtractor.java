package invenio.pdf.plots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.tools.locator.FileLocator;
import invenio.common.ExtractorGeometryTools;
import invenio.common.SpatialClusterManager;
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
    private static Map<Rectangle, List<CSOperation>> clusterOperations(
            Set<CSOperation> operations, PDFPageManager manager, int horizontalMargin,
            int verticalMargin) {

        SpatialClusterManager<CSOperation> clusterManager =
                new SpatialClusterManager<CSOperation>(
                ExtractorGeometryTools.extendRectangle(manager.getPageBoundary(),
                horizontalMargin * 2, verticalMargin * 2),
                horizontalMargin, verticalMargin);

        for (CSOperation op : operations) {
            Rectangle2D srcRec = manager.getOperationBoundary2D(op);
            if (srcRec != null) {
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
    private static Map<Rectangle, List<CSOperation>> clusterOperations(PDFPageManager opManager) {
        Set<CSOperation> interestingOperations = opManager.getOperations();
        interestingOperations.removeAll(opManager.getTextOperations());

        return clusterOperations(interestingOperations, opManager, 100, 50);
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
            PDFPageManager opManager) {
        /**
         * Annotates image with the data from teh operation manager
         */
        graphics.setTransform(AffineTransform.getRotateInstance(0));
        graphics.setPaintMode();

        // drawing all the operations
        graphics.setColor(Color.green);

        Set<CSOperation> operations = opManager.getOperations();

        for (CSOperation operation : operations) {
            Rectangle2D opRec = opManager.getOperationBoundary2D(operation);
            if (opRec != null) {
                graphics.drawRect((int) opRec.getMinX(), (int) opRec.getMinY(), (int) opRec.getWidth(), (int) opRec.getHeight());
            }
        }

        // drawing text operations

        graphics.setColor(Color.red);

        operations = opManager.getTextOperations();

        for (CSOperation operation : operations) {
            Rectangle2D opRec = opManager.getOperationBoundary2D(operation);
            if (opRec != null) {
                // TODO: take care of having all the text operators

                graphics.drawRect((int) opRec.getMinX(), (int) opRec.getMinY(), (int) opRec.getWidth(), (int) opRec.getHeight());
            }
        }
        graphics.setColor(Color.blue);
        // now painting clustered operations
        Map<Rectangle, List<CSOperation>> clustered = clusterOperations(opManager);
        for (Rectangle rec : clustered.keySet()) {
            graphics.drawRect(rec.x, rec.y, rec.width, rec.height);
        }

    }

    private static PDFPageManager getOperationsFromPage(PDPage page) {
        Rectangle2D rect = page.getCropBox().toNormalizedRectangle();
        BufferedImage image = null;
        IGraphicsContext graphics = null;
        try {
            Rectangle pageBoundary = new Rectangle(0, 0,
                    (int) rect.getWidth() * SCALE,
                    (int) rect.getHeight() * SCALE);

            //Constructing the graphics context
            image = new BufferedImage((int) pageBoundary.getWidth(),
                    (int) pageBoundary.getHeight(), BufferedImage.TYPE_INT_RGB);

            Graphics2D g2 = (Graphics2D) image.getGraphics();

            //Our wrapper around the 2D device allowing to extract informations
            // about the device

            PDFPageManager opManager = new PDFPageManager(pageBoundary);

            ExtractorGraphics2D g2proxy = new ExtractorGraphics2D(g2, opManager);

            // now we use our wrapper in order to construct standard mechanisms
            graphics = new ExtractorJPodGraphicsContext(g2proxy);

            // setup user space
            AffineTransform imgTransform = graphics.getTransform();
            imgTransform.scale(SCALE, -SCALE);
            imgTransform.translate(-rect.getMinX(), -rect.getMaxY());
            graphics.setTransform(imgTransform);
            graphics.setBackgroundColor(Color.WHITE);
            graphics.fill(rect);
            CSContent content = page.getContentStream();
            CSOperation[] operations = content.getOperations();

            if (content != null) {

                //CSPlatformRenderer renderer = new CSPlatformRenderer(null,
                //		graphics);

                // we inject our own implementation of the renderer -> before performing an operation, it is
                // marked as currently performed. In such a way, out graphics device will be able to detect the operation
                // and assign its attributes

                ExtractorCSInterpreter renderer = new ExtractorCSInterpreter(null,
                        graphics, opManager);

                renderer.process(content, page.getResources());
                //  annotateImage(g2, opManager);
                opManager.setRenderedPage(image);
            }
            return opManager;
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }
    }

    private static List<PDFPageManager> getOperationsFromDocument(PDDocument doc,
            String fileName) throws IOException {

        //ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
        ArrayList<PDFPageManager> results = new ArrayList<PDFPageManager>();

        PDPageTree pages = doc.getPageTree();
        PDPage page = pages.getFirstPage();

//        int i = 0;
        while (page != null) {
//            System.out.println("Processing page " + i);
            PDFPageManager currentOperationsManager = getOperationsFromPage(page);
//            Images.writeImageToFile(currentOperationsManager.getRenderedPage(), fileName + "." + i + ".png");
            results.add(currentOperationsManager);
//            i++;
            page = page.getNextPage();
        }
        return results;
    }

    public static List<PDFPageManager> getOperationsFromDocument(String filename)
            throws IOException, COSLoadException {
        FileLocator locator = new FileLocator(filename);
        PDDocument doc = PDDocument.createFromLocator(locator);
        List<PDFPageManager> opManagers = getOperationsFromDocument(doc, filename);
        doc.close();
        return opManagers;
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
        Set<CSOperation> graphicOperations = manager.getOperations();
        graphicOperations.removeAll(manager.getTextOperations());

        Map<Rectangle, List<CSOperation>> graphicalRegions = clusterOperations(
                graphicOperations, manager, margins[0], margins[1]);

        Map<Rectangle, List<CSOperation>> shrinkedRegions =
                ExtractorGeometryTools.shrinkRectangleMap(graphicalRegions,
                margins[0], margins[1]);


        Map<Rectangle, List<CSOperation>> graphicalPlotRegions =
                PlotHeuristics.removeFalsePlots(shrinkedRegions);


        Map<Rectangle, List<CSOperation>> plotRegions =
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

        List<PDFPageManager> documentPages = getOperationsFromDocument(doc, fileName);
        for (PDFPageManager manager : documentPages) {
            // Now we want to process document page by page and detect potential plots
            System.out.println("page processed");
            List<Plot> currentPlots = getPlotsFromPage(manager);
            result.addAll(currentPlots);
        }

        return result;
    }
}
