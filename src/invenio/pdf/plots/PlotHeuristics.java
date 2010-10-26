package invenio.pdf.plots;

import de.intarsys.pdf.content.CSOperation;
import invenio.common.ExtractorGeometryTools;
import invenio.common.SpatialClusterManager;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap;
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
public class PlotHeuristics {

    /**
     * Removes areas that can not be plots because of a wrong aspect ratio
     * (plots can not for example be half of the page hight and few pixels broad
     * @param areas
     */
    public static Map<Rectangle, List<CSOperation>> removeBasedOnAspectRatio(
            Map<Rectangle, List<CSOperation>> areas) {

        Map<Rectangle, List<CSOperation>> result = new HashMap<Rectangle, List<CSOperation>>();

        ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();

        double minAR = parameters.getMinimalAspectRatio();
        double maxAR = parameters.getMaximalAspectRatio();

        for (Rectangle curRec : areas.keySet()) {
            double aspectRatio = curRec.getWidth() / curRec.getHeight();
            if (minAR < aspectRatio && aspectRatio < maxAR) {
                result.put(curRec, areas.get(curRec)); // copying all the
                // operations belonging to a valid area
            } else {
                //TODO: remove this debugging code
                System.out.println("some areas filtered out !");
            }
        }

        return result;
    }

    /**
     * Remove irrelevant plots based on the number of graphical operations present inside.
     * This for example allows us to exclude tables and frames containing text.
     *
     * @return
     */
    public static Map<Rectangle, List<CSOperation>> removeBasedOnNumberOfOperations() {
        //Map<Rectangle, List<CSOperation>> result = new HashMap<Rectangle
        //TODO Implement... for the moment this heuristic seems not to be crucial in order
        //     for everything to work

        return null;
    }

    public static Map<Rectangle, List<CSOperation>> removeFalsePlots(Map<Rectangle, List<CSOperation>> areas) {
        // remove graphics with too small/too big aspect ratios
        return removeBasedOnAspectRatio(areas);
    }

    /**
     * If any text operation is intersecting with the plot area, include it inside !
     * We assume that areas are not overlapping !
     * @param areas
     * @param manager
     * @return
     */
    public static Map<Rectangle, List<CSOperation>> includeTextParts(
            Map<Rectangle, List<CSOperation>> areas, PDFPageManager manager) {
        // each region will be represented by one of CSOperation instances
        // constituting it
        HashMap<CSOperation, Rectangle> areaIdentifiers =
                new HashMap<CSOperation, Rectangle>();

        SpatialClusterManager<CSOperation> clusterManager =
                new SpatialClusterManager<CSOperation>(
                ExtractorGeometryTools.extendRectangle(
                manager.getPageBoundary(), 2, 2), 1, 1);

        for (Rectangle rec : areas.keySet()) {
            CSOperation representant = areas.get(rec).get(0);
            areaIdentifiers.put(representant, rec);
            clusterManager.addRectangle(rec, representant);
        }

        // now processing all the text operations
        for (CSOperation textOp : manager.getTextOperations()) {
            Rectangle2D boundary = manager.getOperationBoundary2D(textOp);
            if (boundary != null){
                clusterManager.addRectangle(boundary.getBounds(), textOp);
            }
        }

        Map<Rectangle, List<CSOperation>> newBoundaries = clusterManager.getFinalBoundaries();
        Map<Rectangle, List<CSOperation>> result = new HashMap<Rectangle, List<CSOperation>>();

        for (Rectangle rec: newBoundaries.keySet()){
            // find old rectangle corresponding to this new one
            Rectangle oldRect = null;
            List<CSOperation> toAdd = new LinkedList<CSOperation>();
            for (CSOperation op: newBoundaries.get(rec)){
                if (areaIdentifiers.containsKey(op)){
                    oldRect = areaIdentifiers.get(op);
                } else {
                    toAdd.add(op);
                }
            }
            if (oldRect != null){
                // we have a cluster containing a plot !
                toAdd.addAll(areas.get(oldRect));
                result.put(rec, toAdd);
            }

        }

        return result;
    }
}
