/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.ExtractorGeometryTools;
import invenio.common.Pair;
import invenio.pdf.core.ExtractorLogger;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.IPDFDocumentFeatureProvider;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.PDFCommonTools;
import java.awt.Rectangle;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author piotr
 */
public class PlotsProvider implements IPDFDocumentFeatureProvider {

    private static String getPlotIdFromCaption(String caption) {
        //TODO: implement using regular expressions
        return Plot.getUniqueIdentifier();
    }

    @Override
    public Plots calculateFeature(PDFDocumentManager docManager) throws FeatureNotPresentException, Exception {
        // gathering all the plot descriptors from all the pages and generaing one collection
        Plots result = new Plots();
        for (int pageNum = 0; pageNum < docManager.getPagesNumber(); ++pageNum) {
            result.plots.add(getPlotsFromPage(docManager.getPage(pageNum)));
        }
        HashMap<Integer, HashMap<Integer, LinkedList<FigureCaption>>> captions = getAllCaptions(docManager);
        PlotsProvider.matchPlotsWithCaptions(docManager, result, captions);

        // Now we are left with some unmatched captions.... we should further investigate this



        /// Debugging code ... we show all captions found inside of the document
        
        return result;
    }

    @Override
    public String getProvidedFeatureName() {
        return Plots.featureName;
    }

    /** matches extracted captions with figure candidates 
     * 
     * @param plots
     * @param captions 
     */
    private static void matchPlotsWithCaptions(PDFDocumentManager docManager, Plots plots, HashMap<Integer, HashMap<Integer, LinkedList<FigureCaption>>> captions) throws FeatureNotPresentException, Exception {
        // we assume that caption will lie in the same layout element

        for (int pageNum = 0; pageNum < docManager.getPagesNumber(); ++pageNum) {
            PDFPageManager pageManager = docManager.getPage(pageNum);
            PageLayout pageLayout = (PageLayout) pageManager.getPageFeature(PageLayout.featureName);

            // for every plot of a page find the closest unassigned caption in teh same area

            // we consider plots in growing y coordinate order

            LinkedList<Plot> toIteratePlots = new LinkedList<Plot>(plots.plots.get(pageNum));
            java.util.Collections.sort(toIteratePlots, new Comparator<Plot>() {

                @Override
                public int compare(Plot o1, Plot o2) {
                    return o1.getBoundary().y - o2.getBoundary().y;
                }
            });

            for (Plot plot : toIteratePlots) {
                int area = pageLayout.getSingleBestIntersectingArea(plot.getBoundary());
                double minDist = 0;
                FigureCaption closestCaption = null;
                
                if (captions.containsKey(pageNum) && captions.get(pageNum).containsKey(area)) {
                    for (FigureCaption caption : captions.get(pageNum).get(area)) {
                        double distance = 0;
                        if (!caption.alreadyMatched){
                            if (caption.boundary.x > plot.getBoundary().x){
                                distance = caption.boundary.x - plot.getBoundary().x - plot.getBoundary().height;
                            } else {
                                distance = plot.getBoundary().x - caption.boundary.x - caption.boundary.height;
                            }
                        }
                        if (distance < minDist){
                            minDist = distance;
                            closestCaption = caption;
                        }
                        
                        if (distance < 0){
                            System.out.println("WARNING: Problem on page " + pageNum + " caption overlaping with figure");
                        }
                        
                    }
                }
                if (closestCaption != null){
                    plot.setCaption(closestCaption);
                }
            }
        }
    }

        /** Retrieve all captions appearing in the document, aggregated by page number, layout element number and the rectangle 
         * 
         * @param docManager the document manager describing currently processed PDF
         * @return 
         */
    

    

    public static HashMap<Integer, HashMap<Integer, LinkedList<FigureCaption>>> getAllCaptions(PDFDocumentManager docManager) throws FeatureNotPresentException, Exception {
        HashMap<Integer, HashMap<Integer, LinkedList<FigureCaption>>> captions =
                new HashMap<Integer, HashMap<Integer, LinkedList<FigureCaption>>>();

        for (int pageNum = 0; pageNum < docManager.getPagesNumber(); ++pageNum) {
            PDFPageManager pageManager = docManager.getPage(pageNum);
            captions.put(pageNum, new HashMap<Integer, LinkedList<FigureCaption>>());

            TextAreas textAreas =
                    (TextAreas) pageManager.getPageFeature(TextAreas.featureName);

            PageLayout pageLayout =
                    (PageLayout) pageManager.getPageFeature(PageLayout.featureName);

            for (Rectangle textRegion : textAreas.areas.keySet()) {
                FigureCaption caption = toFigureCaption(textAreas.areas.get(textRegion).first);
                if (caption != null) {
                    int bestArea = pageLayout.getSingleBestIntersectingArea(textRegion);


                    if (!captions.get(pageNum).containsKey(bestArea)) {
                        captions.get(pageNum).put(bestArea, new LinkedList<FigureCaption>());
                    }
                    caption.boundary = textRegion;
                    captions.get(pageNum).get(bestArea).add(caption);
                }
            }
            
            // now we sort caption within every area... by y coordinate
            for (int areaNum: captions.get(pageNum).keySet()){
                java.util.Collections.sort(captions.get(pageNum).get(areaNum), new Comparator<FigureCaption>(){

                    @Override
                    public int compare(FigureCaption o1, FigureCaption o2) {
                        return o1.boundary.y - o2.boundary.y;
                    }
                });
            }

        }
        
        
        return captions;
    }

    /**
     * Finds all the plots present in the PDF page. Plots are extracted together
     * with captions but without references because captions appear
     * on the same page and textual references have to be found globally in the document.
     *
     * @param manager
     * @return List of plot descriptors
     */
    public static List<Plot> getPlotsFromPage(PDFPageManager manager) throws FeatureNotPresentException, Exception {
        List<Plot> plots = new LinkedList<Plot>();

        // first we generate algorithm parameters depending on the page parameters
        //TODO: extend this

        int[] margins = PDFCommonTools.calculateGraphicsMargins(manager);


        /*************
         * Treating graphics operations - clustering them, filtering and
         * including appropriate text operations
         **************/
        GraphicalAreas graphicalAreas =
                (GraphicalAreas) manager.getPageFeature(GraphicalAreas.featureName);

        if (graphicalAreas == null) {
            throw new FeatureNotPresentException(GraphicalAreas.featureName);
        }

        Map<Rectangle, Pair<List<Operation>, Integer>> areas;


        areas = graphicalAreas.areas;

        areas = ExtractorGeometryTools.shrinkRectangleMap(areas, margins[0], margins[1]);
        areas = PlotHeuristics.removeIncorrectGraphicalRegions(areas);
        areas = PlotHeuristics.includeTextParts(areas, manager);
        areas = PlotHeuristics.removeFalsePlots(areas, manager);

        // we are done with plot images -> creating plot structures for every
        // selected region

        for (Rectangle area : areas.keySet()) {
            Plot plot = new Plot();
            plot.setBoundary(area);
            plot.addOperations(areas.get(area).first);
            plot.setPageNumber(manager.getPageNumber());
            
            
            /* We do not assign caption at this stage !
            Pair<String, Rectangle> caption = getPlotCaption(plot, manager);        
            plot.setCaption(caption.first);
            plot.setCaptionBoundary(caption.second);
            */
            
            plot.setPageManager(manager);
            plot.setId(getPlotIdFromCaption(plot.getCaption().figureIdentifier));
            plots.add(plot);
        }

        return plots;
    }

    private static Pair<String, Rectangle> getPlotCaption(Plot plot, PDFPageManager pageManager)
            throws FeatureNotPresentException, Exception {

        TextAreas textAreas =
                (TextAreas) pageManager.getPageFeature(TextAreas.featureName);

        PageLayout pageLayout =
                (PageLayout) pageManager.getPageFeature(PageLayout.featureName);

        // finding the first text area next to the plot
        Rectangle currentArea = null;

        double plotEnding = plot.getBoundary().getMaxY();
        int plotArea = pageLayout.getSingleBestIntersectingArea(plot.getBoundary());

        // now filtering all the text areas. We want to consider only correct captions
        // layout area number -> rectangle -> string
        HashMap<Integer, HashMap<Rectangle, String>> possibleCaptions = new HashMap<Integer, HashMap<Rectangle, String>>();

        for (Rectangle textRegion : textAreas.areas.keySet()) {
            if (isPlotCaption(textAreas.areas.get(textRegion).first)) {
                int bestArea = pageLayout.getSingleBestIntersectingArea(textRegion);

                if (!possibleCaptions.containsKey(bestArea)) {
                    possibleCaptions.put(bestArea, new HashMap<Rectangle, String>());
                }

                possibleCaptions.get(bestArea).put(textRegion,
                        textAreas.areas.get(textRegion).first);
            }
        }
        // until now should be precalculated for every page

        HashMap<Rectangle, String> captions = possibleCaptions.get(plotArea);
        if (captions == null) {
            return new Pair<String, Rectangle>("", null);
        }

        for (Rectangle textRegion : captions.keySet()) {
            if (currentArea == null
                    || (currentArea.getMinY() > textRegion.getMinY()
                    && textRegion.getMinY() > plotEnding)) {
                currentArea = textRegion;
            }
        }

        if (currentArea == null) {
            return new Pair<String, Rectangle>("", null);
        } else {
            return new Pair<String, Rectangle>(captions.get(currentArea), currentArea);
        }
    }

    private static FigureCaption toFigureCaption(String candidate) {
        String prepared = candidate.toLowerCase().trim();
        ExtractorLogger.logMessage(2, "Processing a potential caption : " + candidate);


        Pattern p = Pattern.compile("(figure|fig\\.|fig|plot|image|img.|img|table|tab.|tab)([^:]{1,5}+):");
        Matcher m = p.matcher(prepared);

        if (m.matches()) {
            FigureCaption caption = new FigureCaption();
            caption.text = candidate;
            caption.figureIdentifier = m.group(0); // the entire word
            caption.isTable = m.group(1).startsWith("tab");
            caption.alreadyMatched = false;
            return caption;
        } else {
            return null;
        }
        // m.group(2) - identifier of the figure (has to be trimmed before usage)
    }
}
