/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.trainingset;

import invenio.common.Images;
import invenio.common.Pair;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.features.FigureCandidate;
import invenio.pdf.features.PlotsExtractorTools;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

/**
 *
 * @author piotr
 */
public class EvaluateDocument {

    File inputDirectory;

    private class Model {
        // enclosing together all the data read from the file

        public PDFDocumentManager documentManager; // the main document manager
        public PDFPageManager<Object> currentPage;
        public FigureCandidate currentPlot;
        public TreeMap<Integer, LinkedList<FigureCandidate>> plotsOnPages;

        public Model() {
            this.plotsOnPages = new TreeMap<Integer, LinkedList<FigureCandidate>>();
            this.documentManager = null;
            this.currentPage = null;
            this.currentPlot = null;
        }

        public int getTotalNumberOfPages() {
            return this.documentManager.getPagesNumber();
        }

        public int getTotalNumberOfPlots() {
            int res = 0;
            for (LinkedList<FigureCandidate> plots : this.plotsOnPages.values()) {
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
        public HashMap<Integer, BufferedImage> pageImages = new HashMap<Integer, BufferedImage>();

        private BufferedImage getRenderedPageImage(PDFPageManager pageManager) {
            return pageImages.get(pageManager.getPageNumber());
        }

        private void setRenderedPageImage(PDFPageManager pageManager, BufferedImage img) {
            this.pageImages.put(pageManager.getPageNumber(), img);
        }
    }
    Model model = new Model();

    /**
     * reading correct meta-data (plots positions) from the manually annotated directory
     * @param inputDirectory
     */
    private HashMap<Integer, LinkedList<FigureCandidate>> loadCorrectMetadataFromDirectory(File inputDirectory) {
        HashMap<Integer, LinkedList<FigureCandidate>> plotsOnPages = new HashMap<Integer, LinkedList<FigureCandidate>>();

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
                FigureCandidate plot = PlotsExtractorTools.readPlotMetadata(plotFile).get(0);
                int pageNum = plot.getPageNumber();

                //plot.setPageManager(model.documentManager.getPage(pageNum));
                if (!plotsOnPages.containsKey(pageNum)) {
                    plotsOnPages.put(pageNum, new LinkedList<FigureCandidate>());
                }
                plotsOnPages.get(pageNum).add(plot);
            } catch (Exception ex) {
                System.out.println(
                        "ERROR: unable to read the plot metadata description file "
                        + plotFile.getPath() + " because of the following error: "
                        + ex.getMessage());
            }
        }
        return plotsOnPages;
    }

    private void loadDetectedDataFromDirectory(File inputDirectory) {
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

            BufferedImage pageImage = Images.readImageFromFile(pageFile);

            model.setRenderedPageImage(pageManager, pageImage);

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
                FigureCandidate plot = PlotsExtractorTools.readPlotMetadata(plotFile).get(0);
                int pageNum = plot.getPageNumber();
                plot.setPageManager(model.documentManager.getPage(pageNum));
                if (!model.plotsOnPages.containsKey(pageNum)) {
                    model.plotsOnPages.put(pageNum, new LinkedList<FigureCandidate>());
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

    private int countPlots(Map<Integer, LinkedList<FigureCandidate>> plots) {
        int numPlots = 0;
        for (LinkedList<FigureCandidate> plist : plots.values()) {
            numPlots += plist.size();
        }
        return numPlots;
    }

    private boolean isWhite(int[] value) {
        return (value[0] > 250 && value[1] > 250 && value[2] > 250);
    }
    private int pixels[] = new int[3];

    private boolean isVerticalLineEmpty(int x, int miny, int maxy, WritableRaster img) {
        for (int y = miny; y <= maxy; ++y) {



            if (!isWhite(img.getPixel(x, y, pixels))) {
                return false;
            }
        }
        return true;
    }

    private boolean isHorizontalLineEmpty(int y, int minx, int maxx, WritableRaster img) {
        for (int x = minx; x <= maxx; ++x) {
            if (!isWhite(img.getPixel(x, y, pixels))) {
                return false;
            }
        }
        return true;
    }

    private Rectangle stripRectangle(Rectangle orig, WritableRaster data) {

        // moving the left line
        int minx = orig.x;
        int miny = orig.y;
        int maxx = orig.x + orig.width;
        int maxy = orig.y + orig.height;

        while (isVerticalLineEmpty(minx, miny, maxy, data) && minx <= maxx) {
            minx++;
        }

        while (isVerticalLineEmpty(maxx, miny, maxy, data) && minx <= maxx) {
            maxx--;
        }

        while (isHorizontalLineEmpty(miny, minx, maxx, data) && miny <= maxy) {
            miny++;
        }

        while (isHorizontalLineEmpty(maxy, minx, maxx, data) && miny <= maxy) {
            maxy--;
        }

        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    private double getVolume(Rectangle rec) {
        double val = rec.width * rec.height;
        return (val >= 0) ? val : -val;
    }

    /** Calculate the distance between two plots
     *
     * @param p1 correct plot
     * @param p2 detected plot
     * @param pageImage - image of the page within which plots sit
     * @return
     */
    private double plotsDistance(FigureCandidate p1, FigureCandidate p2) {
        // calculate the intersection
        Rectangle r1 = p1.getBoundary();
        Rectangle r2 = p2.getBoundary();

        Rectangle intersection = r1.intersection(r2);
        Rectangle union = r1.union(r2);

        double intVolume = getVolume(intersection);
        double unVolume = getVolume(union);

        double exclVolume = getVolume(r1) - intVolume;
        double inclVolume = getVolume(r2) - intVolume;

        if (unVolume == 0.0) {
            return 1.0;
        }


//        double weightOfIncorrectlyDetected =
//
//                inclVolume / getVolume(r2);
//        double weightOfExcluded = ;

        return 1 - (intVolume / unVolume);
    }

    /**
     * Returns following information: <<skipped, detected incorrectly>, evaluation>
     * @param correct
     * @param detected
     * @return
     */
    private Pair<Pair<Integer, Integer>, List<Double>> evaluatePage(List<FigureCandidate> correct, List<FigureCandidate> detected, int pageNumber) {
        if (correct == null) {
            int incorrectly = 0;
            if (detected != null) {
                incorrectly = detected.size();

            }
            return new Pair<Pair<Integer, Integer>, List<Double>>(new Pair<Integer, Integer>(0, incorrectly), new LinkedList<Double>());
        }

        if (detected == null) {
            int skipped = correct.size();
            return new Pair<Pair<Integer, Integer>, List<Double>>(new Pair<Integer, Integer>(skipped, 0), new LinkedList<Double>());
        }

        // end of preliminary conditions check

        int skipped = 0;
        int detectedIncorrectly = 0;
        double evaluation = 0.0;




        // stripping all rectangles from both sets detected and

        WritableRaster imData = model.getRenderedPageImage(model.documentManager.getPage(pageNumber)).getRaster();

        for (FigureCandidate plot : correct) {
            plot.setBoundary(stripRectangle(plot.getBoundary(), imData));
        }

        for (FigureCandidate plot : detected) {
            plot.setBoundary(stripRectangle(plot.getBoundary(), imData));
        }

        // matching plots with plots

        TreeSet<Pair<Pair<FigureCandidate, FigureCandidate>, Double>> distances = new TreeSet<Pair<Pair<FigureCandidate, FigureCandidate>, Double>>(new Comparator<Pair<Pair<FigureCandidate, FigureCandidate>, Double>>() {

            @Override
            public int compare(Pair<Pair<FigureCandidate, FigureCandidate>, Double> t, Pair<Pair<FigureCandidate, FigureCandidate>, Double> t1) {
                if (t.second == t1.second) {
                    return 0;
                }
                if (t.second < t1.second) {
                    return -1;
                }
                return 1;
            }
        });


        // matching step -> calculating distance for every possible pair
        for (FigureCandidate cPlot : correct) {
            for (FigureCandidate dPlot : detected) {
                distances.add(new Pair<Pair<FigureCandidate, FigureCandidate>, Double>(new Pair<FigureCandidate, FigureCandidate>(cPlot, dPlot), plotsDistance(cPlot, dPlot)));
            }
        }
        HashSet<FigureCandidate> usedPlots = new HashSet<FigureCandidate>(); // plots that have been already matched
        //List<Pair<Pair<Plot, Plot>, Double>> matched = new LinkedList<Pair<Pair<Plot, Plot>, Double>>();

        LinkedList<Double> result = new LinkedList<Double>();
        
        // iterating in the order of increasing distances... the treeset is sorted!
        for (Pair<Pair<FigureCandidate, FigureCandidate>, Double> possible : distances) {
            if (possible.second < 1.0 && !usedPlots.contains(possible.first.first) && !usedPlots.contains(possible.first.second)) {
                usedPlots.add(possible.first.first);
                usedPlots.add(possible.first.second);
          //      matched.add(possible);
                result.add(possible.second);
            }
        }



        // calculating the numebr of plots that have not been detected

        for (FigureCandidate p: correct){
            if (!usedPlots.contains(p)){
                skipped++;
            }
        }

        for (FigureCandidate p: detected){
            if (!usedPlots.contains(p)){
                detectedIncorrectly++;
            }
        }

        
        // calculating the number of plots detected incorrectly

        return new Pair<Pair<Integer, Integer>, List<Double>>(new Pair<Integer, Integer>(skipped, detectedIncorrectly), result);
    }

    public void run(String[] args) {
        HashMap<Integer, LinkedList<FigureCandidate>> correctPlots =
                loadCorrectMetadataFromDirectory(new File(args[0]));
        int numCorrectPlots = countPlots(correctPlots);
        loadDetectedDataFromDirectory(new File(args[1]));
        int numDetectedPlots = countPlots(model.plotsOnPages);

        System.out.println("read " + numCorrectPlots + " correct plots");
        System.out.println("read " + numDetectedPlots + " detected plots");

        int skipped = 0;
        int incorrectly = 0;
        double total = 0.0;
        int totalNum =0;
        // now evaluating for every page
        for (int pageNum = 0; pageNum < model.getTotalNumberOfPages(); pageNum++) {

            List<FigureCandidate> cPlots = correctPlots.get(pageNum);
            List<FigureCandidate> dPlots = model.plotsOnPages.get(pageNum);
            int nd = (dPlots == null) ? 0 : dPlots.size();
            int nc = (cPlots == null) ? 0 : cPlots.size();

            System.out.println("Evaluation of the page " + pageNum + " detected plots: " + nd + "  number of correct plots: " + nc);
            Pair<Pair<Integer, Integer>, List<Double>> pe = evaluatePage(cPlots, dPlots, pageNum);
            //Pair<Pair<Integer, Integer>, List<Double>> pe = evaluatePage(cPlots, cPlots, pageNum);
            for (double d : pe.second) {
                total += d;
                totalNum++;
            }
            skipped += pe.first.first;
            incorrectly += pe.first.second;
            System.out.println("   skipped:" + pe.first.first + "  incorrectly detected:" + pe.first.second);
        }

        System.out.println("Final document statistics: skipped=" + skipped + " incorrectly detected=" + incorrectly + " total sum of distances=" + total + "number of pairs="+totalNum);
        System.out.println("" + (total/ totalNum));
        double evaluation = 0.3 * ( ((double)skipped) / numCorrectPlots)+ 0.2 * ( ((double) incorrectly )/ numDetectedPlots ) + 0.5 * (total/ totalNum);
        System.out.println(" total evaluation:" + evaluation);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // read the detected path, the manually described path, match plots
        // and produce evaluation numbers
        EvaluateDocument doc = new EvaluateDocument();
        doc.run(args);
    }
}
