/* A simple program allowing to traint the testing set
 * all the input is taken from the command line, only the image is displayed,
 * keyboard is used to navigate and select new plots
 * 
 */
package invenio.pdf.trainingset;

import invenio.common.Images;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.features.Plot;
import invenio.pdf.features.PlotsExtractorTools;
import invenio.pdf.features.PlotsWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.avalon.framework.activity.Executable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 *
 * @author piotr
 */
public class TestingSetDescriptor {

    private class Model {
        // enclosing together all the data read from the file

        public PDFDocumentManager documentManager; // the main document manager
        public PDFPageManager<Object> currentPage;
        public Plot currentPlot;
        public TreeMap<Integer, LinkedList<Plot>> plotsOnPages;

        public Model() {
            this.plotsOnPages = new TreeMap<Integer, LinkedList<Plot>>();
            this.documentManager = null;
            this.currentPage = null;
            this.currentPlot = null;
        }

        public int getTotalNumberOfPages() {
            return this.documentManager.getPagesNumber();
        }

        public int getTotalNumberOfPlots() {
            int res = 0;
            for (LinkedList<Plot> plots : this.plotsOnPages.values()) {
                res += plots.size();
            }
            return res;
        }

        public int getPageWidth() {
            return this.documentManager.getPage(0).getRenderedPage().getWidth();
        }

        public int getPageHeight() {
            return this.documentManager.getPage(0).getRenderedPage().getHeight();
        }
        public HashMap<Integer, Image> pageImages = new HashMap<Integer, Image>();

        private Image getRenderedPageImage(PDFPageManager pageManager) {
            return pageImages.get(pageManager.getPageNumber());
        }

        private void setRenderedPageImage(PDFPageManager pageManager, Image img) {
            this.pageImages.put(pageManager.getPageNumber(), img);
        }
    }
    Model model = new Model();
    private Shell shell;
    private Display display;
    private Canvas imageCanvas;
    ScrolledComposite canvasComposite;
    private boolean inOperation = false;
    public BufferedImage pageBufferedImage;
    public ImageData renderedPageData;
    public Image renderedPage;
    public GC currentGC;
    public File inputDirectory;

    private void readSWTImages() {
        // read all the page images in the SWT format rather than AWT as it is used in most of the framework
    }

    /**
     * Prints the summary of the current program state
     */
    private void printSummary() {
        System.out.println("Page: " + model.currentPage.getPageNumber() + "/" + model.getTotalNumberOfPages());
        if (model.currentPlot != null) {
            System.out.println("Plot: ");
            System.out.println("   rectangle = " + model.currentPlot.getBoundary().toString());
            System.out.println("   caption text = " + model.currentPlot.getCaption());
            System.out.println("   caption rectangle = " + model.currentPlot.getCaptionBoundary().toString());
        }
    }

    /**
     * Loading an output directory corresponding to a single file and
     * allowing to correct the plots description
     * @param inputDirectory
     */
    private void loadInitialDataFromDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;

        PDFDocumentManager documentManager = new PDFDocumentManager();
        // creating page managers from a directory with sample files
        if (!inputDirectory.isDirectory()) {
            System.out.println("ERROR: The path specified at the input is not a directory.");
        }
        // Searching the input directory for all the files that could

        File[] pageFiles = inputDirectory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                String pureName = file.getName();
                return pureName.startsWith("raw") && pureName.endsWith(".png");
            }
        });

        SortedMap<Integer, PDFPageManager<Object>> pageManagers = new TreeMap<Integer, PDFPageManager<Object>>();
        ImageLoader loader = new ImageLoader();
        for (File pageFile : pageFiles) {
            // all the page managers created in this process do not contain operations, only 
            // meta description and provide a framework for adding and selecting plots

            PDFPageManager<Object> pageManager = new PDFPageManager<Object>();
            pageManager.setDocumentManager(documentManager);
            pageManager.setRawFileName(pageFile.getPath());

            //pageManager.setRenderedPage(Images.readImageFromFile(pageFile));


            //for (int pn = 0; pn < model.documentManager.getPagesNumber(); ++pn) {
            //  PDFPageManager<Object> pageManager = model.documentManager.getPage(pn);

            //}


            Pattern p = Pattern.compile("raw_output([0-9]+).png");
            Matcher m = p.matcher(pageFile.getName());
            if (m.matches()) {
                String pageId = m.group(1);
                pageManager.setPageNumber(Integer.parseInt(pageId));
            } else {
                System.out.println("ERROR: incorrect raw image file name: " + pageFile.getName());
            }

            // now reading the rendered image for each page
            pageManager.setRenderedPage(Images.readImageFromFile(pageFile));

            pageManagers.put(pageManager.getPageNumber(), pageManager);

            ImageData[] imagedatas = loader.load(pageFile.getPath());

            if (imagedatas.length != 1) {
                System.out.println("WARNING: a different number of ImageData instances has been read from one image file");
            } else {
                model.setRenderedPageImage(pageManager, new Image(this.imageCanvas.getDisplay(), imagedatas[0]));
            }
        }
        // now inserting pages in a correct order

        for (Integer page : pageManagers.keySet()) {
            documentManager.addPage(pageManagers.get(page));
        }

        model.documentManager = documentManager;


        // now we can proceed with reading plot descriptions from the directory
        File[] plotMetadataFiles = inputDirectory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                return fileName.startsWith("plot") && fileName.endsWith(".xml");
            }
        });

        for (File plotFile : plotMetadataFiles) {
            try {
                // reading metadata of one plot
                Plot plot = PlotsExtractorTools.readPlotMetadata(plotFile).get(0);
                int pageNum = plot.getPageNumber();
                plot.setPageManager(model.documentManager.getPage(pageNum));
                if (!model.plotsOnPages.containsKey(pageNum)) {
                    model.plotsOnPages.put(pageNum, new LinkedList<Plot>());
                }
                model.plotsOnPages.get(pageNum).add(plot);
            } catch (Exception ex) {
                System.out.println(
                        "ERROR: unable to read the plot metadata description file "
                        + plotFile.getPath() + " because of the following error: "
                        + ex.getMessage());
            }
        }

    }

    public Image annotateImage(Image img, Plot plot) {
        GC gc = new GC(img);

        gc.setForeground(new org.eclipse.swt.graphics.Color(gc.getDevice(), 0, 0, 255));
        Rectangle bd = plot.getBoundary();
        gc.drawRectangle(bd.x, bd.y, bd.width, bd.height);
        gc.setForeground(new org.eclipse.swt.graphics.Color(gc.getDevice(), 0, 255, 0));
        bd = plot.getCaptionBoundary();
        if (bd != null) {
            gc.drawRectangle(bd.x, bd.y, bd.width, bd.height);
        }
        return img;
    }

//    public BufferedImage annotateImage(BufferedImage img, Plot plot) {
//        // annotate current image with plot rectangles
//
//        Graphics2D g = (Graphics2D) img.getGraphics();
//
//        g.setColor(Color.blue);
//
//        // drawing plot boundary
//        Rectangle bd = plot.getBoundary();
//        g.drawRect(bd.x, bd.y, bd.width, bd.height);
//        g.setColor(Color.green);
//        bd = plot.getCaptionBoundary();
//        if (bd != null) {
//            g.drawRect(bd.x, bd.y, bd.width, bd.height);
//        }
//        return img;
//    }
    private Image copySWTImage(Image src) {
        return new Image(Display.getDefault(), src.getImageData());
    }

    /**
     * switching interface to a different plot
     * @param plot
     */
    private void setCurrentPlot(Plot plot) {
        model.currentPage = plot.getPageManager();
        model.currentPlot = plot;

        // regenerating current image -> getting image of the page and annotating it with the plot metadata
        // TODO: finish

        //updateCurrentImage(annotateImage(Images.copyBufferedImage(plot.getPageManager().getRenderedPage()), plot));

        updateCurrentImage(annotateImage(copySWTImage(model.getRenderedPageImage(plot.getPageManager())), plot));

//        imageCanvas.redraw();
        imageCanvas.setBackgroundImage(renderedPage);

        printSummary();
    }

    /** Setting the current page
     * 
     * @param page
     */
    private void setCurrentPage(PDFPageManager<Object> page) {
        if (page == null) {
            return;
        }

        List<Plot> plotsOnPage = model.plotsOnPages.get(page.getPageNumber());

        if (plotsOnPage != null && !(plotsOnPage.isEmpty())) {
            // we are setting in fact the first plot from this page
            setCurrentPlot(plotsOnPage.get(0));

        } else {

            // really switching to a page
            model.currentPlot = null;
            model.currentPage = page;
            // here producing the correct image ! ... the one we are using should be copied
            // TODO: finish

            updateCurrentImage(model.getRenderedPageImage(page));
//            imageCanvas.redraw();
            imageCanvas.setBackgroundImage(renderedPage);

            printSummary();
        }
    }

    // now a set of interface based functions
    private void printHelp() {
        // this function should print the help file
    }

    private void selectPlot() {
        inOperation = true;
        System.out.println("Started selecting new plot data. please select the plot on the image");
        if (model.currentPlot != null) {
            selectRectangle(new RectangleCallback() {

                @Override
                public void execute() throws Exception {
                    model.currentPlot.setBoundary(this.rectangle);
                    setCurrentPlot(model.currentPlot);
                    System.out.println("Finished selecting new plot data");
                    inOperation = false;
                }
            });

        }
        // for now exiting, but the callback will follow
    }

    private void inputCaptionText() {
        System.out.println("reading the caption text");
        // here additional operations

        System.out.println("updated the caption text");
    }

    private void selectCaption() {
        inOperation = true;
        System.out.println("Started selecting new caption data. please select the plot on the image");

        if (model.currentPlot != null) {
            selectRectangle(new RectangleCallback() {

                @Override
                public void execute() throws Exception {
                    model.currentPlot.setCaptionBoundary(this.rectangle);
                    setCurrentPlot(model.currentPlot);
                    System.out.println("Finished selecting new plot data");
                    inOperation = false;
                }
            });
        }
    }

    private void newPlot() {
        inOperation = true;
        if (model.currentPage != null) {
            Plot newPlot = new Plot();
            newPlot.setPageManager(model.currentPage);
            newPlot.setCaptionBoundary(new Rectangle(0, 0, 0, 0));
            newPlot.setCaption("");
            setCurrentPlot(newPlot);
            newPlot.setPageNumber(model.currentPage.getPageNumber());
            if (model.plotsOnPages.get(model.currentPage.getPageNumber()) == null) {
                model.plotsOnPages.put(model.currentPage.getPageNumber(), new LinkedList<Plot>());
            }

            model.plotsOnPages.get(model.currentPage.getPageNumber()).add(newPlot);

            System.out.println("created a new plot");
        }
        inOperation = false;
    }

    private void deletePlot() {
        inOperation = true;
        System.out.println("deleting the current plot");
        inOperation = false;
    }

    private Plot getNextPlot(Plot curPlot) {
        if (curPlot == null) {
            return null;
        }

        int pageNumber = curPlot.getPageNumber();
        for (int consideredPage : model.plotsOnPages.tailMap(pageNumber).keySet()) {
            List<Plot> plots = model.plotsOnPages.get(consideredPage);

            if (plots != null && !plots.isEmpty()) {
                if (consideredPage == pageNumber) {
                    boolean canChoose = false;
                    for (Plot plot : plots) {
                        if (plot == curPlot) {
                            // from now on, we can select
                            canChoose = true;
                        } else if (canChoose) {
                            return plot;
                        }
                    }
                } else {
                    return plots.get(0);
                }
            }
        }
        return null;
    }

    private Plot getPreviousPlot(Plot curPlot) {
        if (curPlot == null) {
            return null;
        }
        int pageNumber = curPlot.getPageNumber();
        Plot resPlot = null;

        for (Integer consideredPage : model.plotsOnPages.keySet()) {
            List<Plot> plots = model.plotsOnPages.get(consideredPage);
            if (plots != null && (!plots.isEmpty())) {
                if (consideredPage > pageNumber) {
                    return null; // this should not happen, but in such a situation a correct answer would be null
                }

                if (consideredPage == pageNumber) {
                    for (Plot plot : plots) {
                        if (plot == curPlot) {
                            return resPlot;
                        } else {
                            resPlot = plot;
                        }
                    }
                } else {
                    resPlot = plots.get(plots.size() - 1);
                }
            }
        }

        return resPlot;
    }

    private void nextPlot() {
        inOperation = true;
        Plot curPlot = getNextPlot(model.currentPlot);
        if (curPlot != null) {
            setCurrentPlot(curPlot);
        }
        System.out.println("No further plot to open");
        inOperation = false;
    }

    private void previousPlot() {
        inOperation = true;
        Plot curPlot = getPreviousPlot(model.currentPlot);

        if (curPlot != null) {
            setCurrentPlot(curPlot);
        }
        System.out.println("Moved to previous plot");
        inOperation = false;
    }

    private void nextPage() {
        inOperation = true;
        int pageNum = model.currentPage.getPageNumber();
        PDFPageManager<Object> page = null;

        while (page == null && pageNum < model.documentManager.getPagesNumber()) {
            page = model.documentManager.getPage(pageNum);
            pageNum++;
        }

        setCurrentPage(page);

        System.out.println("Moved to next page");
        inOperation = false;
    }

    private void previousPage() {
        inOperation = true;

        int pageNum = model.currentPage.getPageNumber();
        PDFPageManager<Object> page = null;

        while (page == null && pageNum >= 0) {
            page = model.documentManager.getPage(pageNum);
            pageNum--;
        }

        setCurrentPage(page);

        System.out.println("Moved to previous page");
        inOperation = false;
    }

    private void saveResults() {

        File outputDirectory = new File(inputDirectory, "manual");
        for (Integer pageNum : model.plotsOnPages.keySet()) {
            for (Plot plot : model.plotsOnPages.get(pageNum)) {
                try {
                    PlotsWriter.writePlot(plot, outputDirectory, false);
                } catch (FileNotFoundException ex) {
                    System.out.println("ERROR: Filesystem error");
                } catch (Exception ex) {
                    System.out.println("ERROR: Some other error " + ex.getMessage());
                }
            }
        }
    }

    private void quit() {
        System.out.println("Exiting the program");
        System.out.println("Saving results");
        saveResults();
        display.close();
    }

    private void openFirstPlot() {
        //1) determining where the first plot is\

        for (Integer page : model.plotsOnPages.keySet()) {
            if (model.plotsOnPages.get(page) != null && model.plotsOnPages.get(page).size() > 0) {
                Plot plot = model.plotsOnPages.get(page).get(0);
                setCurrentPlot(plot);
                return;
            }
        }

        // there are no plots ...
        setCurrentPage(model.documentManager.getPage(0));
    }

//    private void updateCurrentImage(BufferedImage image) {
//        pageBufferedImage = Images.copyBufferedImage(image);
//        renderedPageData = Images.convertToSWT(pageBufferedImage);
//        this.renderedPage = new Image(shell.getDisplay(), renderedPageData);
//        imageCanvas.redraw();
//    }
    private void updateCurrentImage(Image image) {
//        pageBufferedImage = Images.copyBufferedImage(image);

        //    renderedPageData = Images.convertToSWT(pageBufferedImage);
        this.renderedPage = image;
//        imageCanvas.redraw();
        imageCanvas.setBackgroundImage(renderedPage);


    }

    private void xorRectangle(GC gc, int x, int y, int width, int height) {
//        if (width < 0) {
//            x -= width;
//            width = -width;
//        }
//        if (height < 0) {
//            y -= height;
//            height = -height;
//        }
        //GC
//                gc = new GC(this.renderedPage);
//        gc.setXORMode(true);
//        gc.setForeground(new org.eclipse.swt.graphics.Color(imageCanvas.getDisplay(), 255, 255, 255));
//        gc.drawRectangle(x, y, width, height);
//        renderedPageData = renderedPage.getImageData();
//        for (int dy = 0; dy < height; ++dy) {
//            int pixel = renderedPageData.getPixel(x + width, y + dy) ^ 16777215;
//            renderedPageData.setPixel(x + width, y + dy, pixel);
//
//            pixel = renderedPageData.getPixel(x, y + dy) ^ 16777215;
//            renderedPageData.setPixel(x, y + dy, pixel);
//
//        }
//        for (int dx = 0; dx < width; ++dx) {
//            int pixel = renderedPageData.getPixel(x + dx, y + height) ^ 16777215;
//            renderedPageData.setPixel(x + dx, y + height, pixel);
//            pixel = renderedPageData.getPixel(x + dx, y) ^ 16777215;
//            renderedPageData.setPixel(x + dx, y, pixel);
//        }
//        renderedPage = new Image(Display.getDefault(), renderedPageData);
    }

    /// Java is UGLY !
    abstract class RectangleCallback implements Executable {

        public Rectangle rectangle;
    }

    /**
     * A function selecting a rectangle using the mouse
     * @return
     */
    private void selectRectangle(RectangleCallback onSel) {
        class MyMouseListener implements MouseListener {

            public final Integer block = 0;
            RectangleCallback onSelected;

            class MyMouseMoveListener implements MouseMoveListener {
                //TestingSetDescriptor desc;

                public int x = 0;
                public int y = 0;
                public int width = 0;
                public int height = 0;

                public MyMouseMoveListener(int sx, int sy) {
                    //this.desc = tsdesc;
                    this.x = sx;
                    this.y = sy;
                }

                @Override
                public void mouseMove(MouseEvent e) {
                    if (width != 0 && height != 0) {
                        System.out.println("clearing previous selection");
                        xorRectangle(currentGC, x, y, width, height);

                    }
                    width = e.x - this.x;
                    height = e.y - this.y;

                    xorRectangle(currentGC, x, y, width, height);
//                    renderedPage.dispose();
//                    renderedPage = new Image(imageCanvas.getDisplay(), renderedPageData);

//                    imageCanvas.redraw();
                    imageCanvas.setBackgroundImage(renderedPage);

                }
            }
            private MyMouseMoveListener mouseMoveListner;
            public int x = 0;
            public int y = 0;
            public int fx = 0;
            public int fy = 0;

            @Override
            public void mouseDoubleClick(MouseEvent e) {
            }

            @Override
            public void mouseDown(MouseEvent e) {
                System.out.println("starting selection");
                System.out.println("Mouse has been pressed at the location: x=" + e.x + "y=" + e.y);
                x = e.x;
                y = e.y;

                mouseMoveListner = new MyMouseMoveListener(x, y);
                imageCanvas.addMouseMoveListener(mouseMoveListner);
            }

            @Override
            public void mouseUp(MouseEvent e) {
                this.fx = e.x;
                this.fy = e.y;

                System.out.println("finishing selection");
                System.out.println("Mouse has been released at the location: x=" + e.x + "y=" + e.y);
                imageCanvas.removeMouseListener(this);
                imageCanvas.removeMouseMoveListener(mouseMoveListner);

                xorRectangle(currentGC, x, y, this.mouseMoveListner.width, this.mouseMoveListner.height);
                // renderedPage.dispose();
                // renderedPage = new Image(imageCanvas.getDisplay(), renderedPageData);
//                imageCanvas.redraw();
                imageCanvas.setBackgroundImage(renderedPage);

                // and finally notify the caller
                int ex = (x < fx) ? x : fx;
                int ey = (y < fy) ? y : fy;

                int efx = (x < fx) ? fx : x;
                int efy = (y < fy) ? fy : y;

                onSelected.rectangle = new Rectangle(ex, ey, efx - ex, efy - ey);
                try {
                    onSelected.execute();
                } catch (Exception ex1) {
                    System.out.println("ERROR: Failed to execute the callback");
                }
            }
        }

        MyMouseListener mouseListener = new MyMouseListener();
        mouseListener.onSelected = onSel;


        imageCanvas.addMouseListener(mouseListener);
    }

    /**initialization of the main window and the most important part of the interface
     * 
     */
    private void create_interface() {

        this.display = new Display();
        this.shell = new Shell(display);
        this.canvasComposite = new ScrolledComposite(shell,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        this.imageCanvas = new Canvas(canvasComposite, SWT.NONE);

    }

    private void init_interface() {

        this.shell.setLayout(new FillLayout());

        canvasComposite.setLayout(new FillLayout());
        canvasComposite.setExpandHorizontal(true);
        canvasComposite.setExpandVertical(true);
        canvasComposite.setMinSize(model.getPageWidth(), model.getPageHeight());


        canvasComposite.setContent(this.imageCanvas);

        //canvasComposite.setMinSize(pageImage.getWidth(), pageImage.getHeight());

        this.shell.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (!inOperation) {
                    switch (e.character) {
                        case 'z':
                            previousPlot();
                            break;

                        case 'x':
                            nextPlot();
                            break;
                        case 'n':
                            newPlot();
                            break;
                        case 'p':
                            selectPlot();
                            break;
                        case 'd':
                            deletePlot();
                            break;
                        case 'c':
                            selectCaption();
                            break;
                        case 'a':
                            previousPage();
                            break;
                        case 's':
                            nextPage();
                            break;
                        case 'q':
                            quit();
                            break;
                    }
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println("key released " + e.character);
            }
        });


        openFirstPlot();
        //updateCurrentImage(Images.readImageFromFile(new File("/home/piotr/samplepng.png")));



//        this.imageCanvas.addPaintListener(new PaintListener() {
//
//            @Override
//            public void paintControl(PaintEvent e) {
//                org.eclipse.swt.graphics.Rectangle bounds = renderedPage.getBounds();
//                //System.out.println("Image parameters width: " + bounds.width + " height: " + bounds.height);
//                e.gc.drawImage(renderedPage, 0, 0);
//
//                // e.gc.drawImage(someImage, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
//                e.gc.drawRectangle(bounds);
//
//                //e.gc.drawRectangle(0, 0, width, height);
//                //  e.gc.drawImage(image, 0, 0, 100, 100, 200, 10, 200, 50);
//            }
//        });
        imageCanvas.setBackgroundImage(renderedPage);
    }

    private void run() {

        this.shell.open();
        while (!this.shell.isDisposed()) {
            if (!this.display.readAndDispatch()) {
                this.display.sleep();
            }
        }
        this.display.dispose();
    }

    public static void main(String[] args) {
        // TODO code application logic here
        if (args.length != 1) {
            printUsage();
            return;
        }

        TestingSetDescriptor desc = new TestingSetDescriptor();
        desc.create_interface();

        System.out.println("Loading the source directory");
        desc.loadInitialDataFromDirectory(new File(args[0]));
        System.out.println("finished loading");


        desc.init_interface();




        desc.run();
    }

    public static void printUsage() {
        System.out.println("A tool allowing a manual annotation of documents with plots");
        System.out.println("Usage:");
        System.out.println("      executable source_directory_name");
    }
}
