package invenio.pdf.features;

import invenio.pdf.core.ExtractorParameters;
import invenio.common.ExtractorGeometryTools;
import invenio.common.SpatialClusterManager;
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
public class PlotHeuristics {

    /**
     * Removes areas that can not be plots because of a wrong aspect ratio
     * (plots can not for example be half of the page hight and few pixels broad
     * @param areas
     */
    public static Map<Rectangle, List<Operation>> removeBasedOnAspectRatio(
            Map<Rectangle, List<Operation>> areas) {

        Map<Rectangle, List<Operation>> result = new HashMap<Rectangle, List<Operation>>();

        ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();

        double minAR = parameters.getMinimalAspectRatio();
        double maxAR = parameters.getMaximalAspectRatio();

        for (Rectangle curRec : areas.keySet()) {
            double aspectRatio = curRec.getWidth() / curRec.getHeight();
            if (minAR < aspectRatio && aspectRatio < maxAR) {
                result.put(curRec, areas.get(curRec)); // copying all the
                // operations belonging to a valid area
            }
//            } else {
//                //TODO: remove this debugging code
//              //  System.out.println("some areas filtered out !");
//            }
        }

        return result;
    }

    /**
     * Remove irrelevant plots based on the number of graphical operations present inside.
     * This for example allows us to exclude tables and frames containing text.
     *
     * @return
     */
    public static Map<Rectangle, List<Operation>> removeBasedOnNumberOfOperations() {
        //Map<Rectangle, List<CSOperation>> result = new HashMap<Rectangle
        //TODO Implement... for the moment this heuristic seems not to be crucial in order
        //     for everything to work

        return null;
    }

    public static Map<Rectangle, List<Operation>> removeFalsePlots(Map<Rectangle, List<Operation>> areas) {
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
    public static Map<Rectangle, List<Operation>> includeTextParts(
            Map<Rectangle, List<Operation>> areas, PDFPageManager manager) {
        // each region will be represented by one of CSOperation instances
        // constituting it
        HashMap<Operation, Rectangle> areaIdentifiers =
                new HashMap<Operation, Rectangle>();

        SpatialClusterManager<Operation> clusterManager =
                new SpatialClusterManager<Operation>(
                ExtractorGeometryTools.extendRectangle(
                manager.getPageBoundary(), 2, 2), 1, 1);

        for (Rectangle rec : areas.keySet()) {
            Operation representant = areas.get(rec).get(0);
            areaIdentifiers.put(representant, rec);
            clusterManager.addRectangle(rec, representant);
        }

        // now processing all the text operations
        for (Operation textOp : manager.getTextOperations()) {

            if (textOp instanceof TextOperation) {
                Rectangle boundary = ((TextOperation) textOp).getBoundary();
                clusterManager.addRectangle(boundary, textOp);
            }
        }

        Map<Rectangle, List<Operation>> newBoundaries = clusterManager.getFinalBoundaries();
        Map<Rectangle, List<Operation>> result = new HashMap<Rectangle, List<Operation>>();

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
                toAdd.addAll(areas.get(oldRect));
                result.put(rec, toAdd);
            }
        }
        return result;
    }
}
