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
import java.util.HashMap;
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
import invenio.common.Images;
import invenio.common.SpatialClusterManager;
import java.awt.Rectangle;
import java.util.LinkedList;

public class PlotsExtractor {

    /**
     * @param args
     */
    public static int SCALE = 2;

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
    private static Map<Integer, Rectangle> clusterOperations(PDFPageManager opManager) {
        SpatialClusterManager<CSOperation> clusterManager =
                new SpatialClusterManager<CSOperation>(new Rectangle(-10000, -10000, 30000, 30000), 5);

        Set<CSOperation> allOperations = opManager.getOperations();
        Set<CSOperation> textOperations = opManager.getTextOperations();

        for (CSOperation op : allOperations) {
            if (!textOperations.contains(op) && opManager.getOperationBoundary2D(op) != null) {
                Rectangle2D srcRec = opManager.getOperationBoundary2D(op);
                Rectangle rec = new Rectangle((int) srcRec.getX(), (int) srcRec.getY(), (int) srcRec.getWidth(), (int) srcRec.getHeight());

                clusterManager.addRectangle(rec, op);
            }
        }
        return clusterManager.getFinalBoundaries();
    }

    /** debug code -> rendering the entire page */
    private static void annotateImage(Graphics2D graphics,
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
        Map<Integer, Rectangle> clustered = clusterOperations(opManager);
        for (Integer i : clustered.keySet()) {
            Rectangle rec = clustered.get(i);
            graphics.drawRect(rec.x, rec.y, rec.width, rec.height);
        }

    }

    private static PDFPageManager getOperationsFromPage(PDPage page) {
        Rectangle2D rect = page.getCropBox().toNormalizedRectangle();
        BufferedImage image = null;
        IGraphicsContext graphics = null;
        try {

            image = new BufferedImage((int) rect.getWidth() * SCALE, (int) rect.getHeight()
                    * SCALE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = (Graphics2D) image.getGraphics();

            // Our wrapper around the 2D device allowing to extract informations about the device
            PDFPageManager opManager = new PDFPageManager();

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
                annotateImage(g2, opManager);
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
     * Finds all the plots present in the PDF page. Plots are extracted together
     * with captions but without references because captions appear
     * on the same page and textual references have to be found globally in the document.
     *
     * @param manager
     * @return List of plot descriptors
     */
    private static List<Plot> getPlotsFromPage(PDFPageManager manager) {
        List<Plot> plots = new LinkedList<Plot>();
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
