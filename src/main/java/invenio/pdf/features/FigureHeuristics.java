package invenio.pdf.features;

import invenio.common.ExtractorGeometryTools;
import invenio.common.Pair;
import invenio.common.SpatialClusterManager;
import invenio.pdf.core.DisplayedOperation;
import invenio.pdf.core.ExtractorParameters;
import invenio.pdf.core.GraphicalOperation;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.TextOperation;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class containing algorithms performing some additional filtering of
 * plot candidates. This includes:
 *  - removing false positives
 *  - joining parts of the same plot.
 *  - including textual parts being part of a plot
 * @author piotr
 */
public class FigureHeuristics {

    public abstract class Predicate<T, S> {

        public abstract boolean evaluate(T v1, S v2);
    }

    /**
     * @param sourceAreas
     * @param candidates
     * @param condition
     * @return
     */
    public static Map<Rectangle, List<Operation>> includeAreas(
            Map<Rectangle, List<Operation>> sourceAreas,
            Map<Rectangle, List<Operation>> candidates,
            Predicate condition) {
        return sourceAreas;
    }

    /**    public static Map<Rectangle, Pair<List<Operation>, Integer>> removeBasedOnAspectRatio(
    Map<Rectangle, Pair<List<Operation>, Integer>> areas) {
    return areas;
    }
     */
    /** Removes regions based on the ratio between number of graphical and text operations */
    public static void removeBasedOnTextOperationsProportion(List<FigureCandidate> plots) {
    
//         
//        for (Plot plot: plots.getPlots()){
//            int numText = 0;
//            int numGraph = 0;
//            for (Operation op : plot.getOperations()) {
//                if (op instanceof TextOperation) {
//                    numText++;
//                }
//                if (op instanceof GraphicalOperation) {
//                    numGraph++;
//                }
//
//                //TODO: contidions here !! and some real filtering out .... and then remove the initial return statement 
//                
//                //result.put(curRec, areas.get(curRec));
//            }
//        }
//        
    }

    /**
     * An attempt to remove mathematical formulas having only horizontal lines as graphical elements
     * @param areas
     * @return 
     */
    public static void removeBasedOnHavingOnlyHorizontalLines(List<FigureCandidate> plots) {
        for (FigureCandidate plot: plots){
            boolean containsNonHorizontal = false;
            for (Operation op : plot.getOperations()) {
                if (op instanceof GraphicalOperation) {
                    GraphicalOperation gop = (GraphicalOperation) op;
                    containsNonHorizontal = containsNonHorizontal || (gop.getBoundary().height > 3);
                }
            }
            if (!containsNonHorizontal) {
                plot.isApproved = false;
                System.out.println("Removed a figure candidate that contained only horizontal lines");
            }
        }
    }
    
    
   
   /**
     * Removes areas that can not be plots because of a wrong aspect ratio
     * (plots can not for example be half of the page hight and few pixels broad
     * @param areas
     */
   public static void removePlotsBasedOnAspectRatio(List<FigureCandidate> plots) {

        Map<Rectangle, Pair<List<Operation>, Integer>> result =
                new HashMap<Rectangle, Pair<List<Operation>, Integer>>();

        ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();

        double minAR = parameters.getMinimalAspectRatio();
        double maxAR = parameters.getMaximalAspectRatio();

        for (FigureCandidate plot: plots){
            Rectangle curRec = plot.getBoundary();
            double aspectRatio = curRec.getWidth() / curRec.getHeight();
            if (minAR >= aspectRatio || aspectRatio >= maxAR) {
                plot.isApproved = false; 
         
            }
        }
    }
   
   /**
     * Removes areas that can not be plots because of a wrong aspect ratio
     * (plots can not for example be half of the page hight and few pixels broad
     * @param areas
     */
    public static Map<Rectangle, Pair<List<Operation>, Integer>> removeBasedOnAspectRatio(
            Map<Rectangle, Pair<List<Operation>, Integer>> areas) {

        Map<Rectangle, Pair<List<Operation>, Integer>> result =
                new HashMap<Rectangle, Pair<List<Operation>, Integer>>();

        ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();

        double minAR = parameters.getMinimalAspectRatio();
        double maxAR = parameters.getMaximalAspectRatio();

        for (Rectangle curRec : areas.keySet()) {
            double aspectRatio = curRec.getWidth() / curRec.getHeight();
            if (minAR < aspectRatio && aspectRatio < maxAR) {
                result.put(curRec, areas.get(curRec)); // copying all the
                // operations belonging to a valid area
            }
        }

        return result;
    }

    /**
     * Filters out areas where the total area covered by graphical operations is 
     * smaller than a certain fraction of the entire figure candidate
     * 
     * @param areas
     * @return 
     */
    public static Map<Rectangle, Pair<List<Operation>, Integer>> removeBrasedOnGraphicalArea(
            Map<Rectangle, Pair<List<Operation>, Integer>> areas) {
        Map<Rectangle, Pair<List<Operation>, Integer>> results = new HashMap<Rectangle, Pair<List<Operation>, Integer>>();
        double graphicalAreaThreshold = ExtractorParameters.getExtractorParameters().getMinimalGraphicalAreaFraction();

        for (Rectangle area : areas.keySet()) {
            double totalArea = area.width * area.height;
            double totalGraphicalArea = 0;

            for (Operation operation : areas.get(area).first) {
                if (operation instanceof GraphicalOperation) {
                    GraphicalOperation go = (GraphicalOperation) operation;
                    totalGraphicalArea += go.getBoundary().width * go.getBoundary().height;
                }
            }

            if (totalGraphicalArea / totalArea > graphicalAreaThreshold) {
                results.put(area, areas.get(area));
            }
        }
        return results;
    }

    /**
     * Remove false plots based on their sizes. Images having too small
     * dimensions can not be considered plots
     * @return
     */
    public static void removeBasedOnSize(List<FigureCandidate> plots, PDFPageManager manager) {
        ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();
        double minWidth = parameters.getMinimalFigureWidth() * manager.getRenderedPage().getHeight();
        double minHeight = parameters.getMinimalFigureHeight() * manager.getRenderedPage().getWidth();
        Map<Rectangle, Pair<List<Operation>, Integer>> results = new HashMap<Rectangle, Pair<List<Operation>, Integer>>();

        for (FigureCandidate plot: plots) {
             Rectangle area = plot.getBoundary();
            if (area.width <= minWidth || area.height <= minHeight) {
                plot.isApproved = false;
            }
        }
    }

    /**
     * Remove areas having too low number of operations
     * @param areas
     * @return 
     */
    public static void removeBasedOnOperationsNumber(
           List<FigureCandidate> plots) {
        HashMap<Rectangle, Pair<List<Operation>, Integer>> result = new HashMap<Rectangle, Pair<List<Operation>, Integer>>();

        ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();
        int minNum = parameters.getMinimalFiguresOperationsNumber();
        int minGraphical = parameters.getMinimalFiguresGraphicalOperationsNumber();
        double graphicalAreaThreshold = parameters.getMinimalGraphicalAreaFraction();
        double minGraphicalFraction = parameters.getMinimalFiguresGraphicalOperationsFraction();
        
        for (FigureCandidate plot: plots) {
            // Operators to consider non-blocking: Do BI, ID, EI 
            Rectangle area = plot.getBoundary();
            boolean acceptAnyway = false;
            int numGraphical = 0;
            double totalGraphicalArea = 0;
            double totalArea = area.width * area.height;
            
            for (Operation op : plot.getOperations()) {
                
                String operator = op.getOriginalOperation().getOperator().toString();
                if ("Do".equals(operator) || "BI".equals(operator) || "ID".equals(operator) || "EI".equals(operator)) {
                    // we do not ommit this reference
                    DisplayedOperation dop = (DisplayedOperation) op;
                    
                        acceptAnyway = (dop.getBoundary().width > 5 && dop.getBoundary().height > 5); // very small graphical operations should be treated as ordinary operations .... like lines
                    
                }
                if (op instanceof GraphicalOperation) {
                    numGraphical += 1;
                    GraphicalOperation go = (GraphicalOperation) op;
                    totalGraphicalArea += go.getBoundary().width * go.getBoundary().height;
                }
            }

            boolean addArea = false;
            if (acceptAnyway) {
                // the inline/external graphics has been painted ... we apply the criterion on teh area covered by graphics operations
                plot.isApproved = plot.isApproved && ((totalGraphicalArea / totalArea) > graphicalAreaThreshold);
            } else {
                // no inline/external graphics... we apply the criteria on the number of operatio               
                plot.isApproved = plot.isApproved && (plot.getOperationsNumber() >= minNum && numGraphical > minGraphical && ((double) numGraphical / (double) plot.getOperationsNumber() > minGraphicalFraction));
            }
        }
    }

    /**
     * Mark some plot candidates as incorrect by using heuristic techniques
     * @param plots
     * @return 
     */
    
    public static void removeFalsePlots(
            /*Map<Rectangle, Pair<List<Operation>, Integer>> areas,
            PDFPageManager manager) {*/
            List<FigureCandidate> plots,
            PDFPageManager manager){
        
        // remove graphics with too small/too big aspect ratios
        removePlotsBasedOnAspectRatio(plots);
        removeBasedOnTextOperationsProportion(plots);
        removeBasedOnHavingOnlyHorizontalLines(plots);
        removeBasedOnOperationsNumber(plots);
        removeBasedOnSize(plots, manager);
    }

    /**
     * Filter out graphical regions that should not be extended into plots by
     * the inclusion of small surrounding text parts
     * @param areas
     * @return
     */
    static Map<Rectangle, Pair<List<Operation>, Integer>> removeIncorrectGraphicalRegions(Map<Rectangle, Pair<List<Operation>, Integer>> areas) {
        return removeBasedOnAspectRatio(areas);
    }

    /**
     * Includes text parts that are more distant that margin settings would
     * allow, but are small enough and so, unlikely to be part of the text corpus
     * 
     * @param <T>
     * @param areas
     * @param manager
     * @return
     * @throws Exception
     */
    public static <T> Map<Rectangle, List<Operation>> includeLooseTextParts(
            Map<Rectangle, List<Operation>> areas, PDFPageManager<T> manager) throws Exception {
        return null;

    }

    /**
     * Takes a list of areas and includes all the text areas overlapping with them.
     * Possibly joins input areas
     * @param areas
     * @param manager
     * @return
     */
    public static <T> Map<Rectangle, List<Operation>> includeTextAreas(
            Map<Rectangle, List<Operation>> areas, PDFPageManager<T> manager) throws Exception {
        return areas;
    }

    /**
     * Include text areas that are located farther than the threshold, but
     * are small enough that it is not likely, they will live on their own.
     * @param <T>
     * @param areas
     * @param manager
     * @return
     * @throws Exception
     */
    public static <T> Map<Rectangle, List<Operation>> includeLooseTextAreas(
            Map<Rectangle, List<Operation>> areas, PDFPageManager<T> manager) throws Exception {
        // if there is a text part in a bigger radius than searched before and the region is small enough,
        // include it in the plot

        return areas;
    }

    /**
     * Include areas satisfying some criterias 
     * @param <T>
     * @param areas
     * @param manager
     * @return
     * @throws Exception
     */
    public static <T> Map<Rectangle, List<Operation>> includeLooseAreas(
            Map<Rectangle, List<Operation>> areas, PDFPageManager<T> manager) throws Exception {

        return areas;
    }

    /**
     * If any text operation is intersecting with the plot area, include it inside !
     * We assume that areas are not overlapping !
     * @param areas
     * @param manager
     * @return
     */
    public static Map<Rectangle, Pair<List<Operation>, Integer>> includeTextParts(
            Map<Rectangle, Pair<List<Operation>, Integer>> areas, PDFPageManager<?> manager) throws Exception {

        // each region will be represented by one of CSOperation instances
        // constituting it

        ExtractorParameters params = ExtractorParameters.getExtractorParameters();

        int hPlotTextMargin = (int) (params.getHorizontalPlotTextMargin()
                * manager.getPageBoundary().getWidth());
        int vPlotTextMargin = (int) (params.getVerticalPlotTextMargin()
                * manager.getPageBoundary().getHeight());


        HashMap<Operation, Rectangle> areaIdentifiers =
                new HashMap<Operation, Rectangle>();

        // we keep only one cluster per area

        PageLayout layout = (PageLayout) manager.getPageFeature(PageLayout.featureName);
        Map<Integer, SpatialClusterManager<Operation>> clusterManagers =
                new HashMap<Integer, SpatialClusterManager<Operation>>();

        for (int areaNum = 0; areaNum < layout.areas.size(); ++areaNum) {
            clusterManagers.put(areaNum, new SpatialClusterManager<Operation>(
                    ExtractorGeometryTools.extendRectangle(
                    manager.getPageBoundary(), 2, 2), 1, 1));

        }

        // now taking every graphical area and electing a representant operation for it ... adding to cluster managers


        for (Rectangle rec : areas.keySet()) {
            Operation representant = areas.get(rec).first.get(0);
            Integer pageArea = areas.get(rec).second;

            areaIdentifiers.put(representant, rec);

            clusterManagers.get(pageArea).addRectangle(
                    ExtractorGeometryTools.cropRectangle(
                    ExtractorGeometryTools.extendRectangle(
                    rec, hPlotTextMargin, vPlotTextMargin), manager.getPageBoundary()),
                    representant);
        }

        // now processing all the text operations

        for (Operation textOp : manager.getTextOperations()) {

            if (textOp instanceof TextOperation) {
                // determining, which columns is thgaze operation intersecting

                int intersecting = layout.getSingleBestIntersectingArea(((TextOperation) textOp).getBoundary());
                if (intersecting >= 0) {
                    Rectangle boundary = ExtractorGeometryTools.cropRectangle(
                            ExtractorGeometryTools.extendRectangle(
                            ((TextOperation) textOp).getBoundary(),
                            hPlotTextMargin, vPlotTextMargin), manager.getPageBoundary());


                    clusterManagers.get(intersecting).addRectangle(
                            boundary, textOp);
                }
            }
        }
        Map<Rectangle, Pair<List<Operation>, Integer>> result =
                new HashMap<Rectangle, Pair<List<Operation>, Integer>>();

        for (Integer areaNum = 0; areaNum < layout.areas.size(); ++areaNum) {


            Map<Rectangle, List<Operation>> newBoundaries = clusterManagers.get(areaNum).getFinalBoundaries();


            for (Rectangle rec : newBoundaries.keySet()) {
                // find old rectangle corresponding to this new one
                Rectangle oldRect = null;
                List<Operation> toAdd = new LinkedList<Operation>();
                for (Operation op : newBoundaries.get(rec)) {
                    if (areaIdentifiers.containsKey(op)) {
                        oldRect = areaIdentifiers.get(op);
                    } else {
                        toAdd.add(op);
                    }
                }

                if (oldRect != null) {
                    // we have a cluster containing a plot !
                    toAdd.addAll(areas.get(oldRect).first);
                    result.put(ExtractorGeometryTools.shrinkRectangle(rec,
                            hPlotTextMargin, vPlotTextMargin), new Pair<List<Operation>, Integer>(toAdd, areaNum));
                }
            }

        }

        return result;
    }
}
