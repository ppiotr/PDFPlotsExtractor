/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.ExtractorGeometryTools;
import invenio.common.Pair;
import invenio.common.SpatialClusterManager;
import invenio.pdf.core.DisplayedOperation;
import invenio.pdf.core.IPDFPageFeature;
import invenio.pdf.core.IPDFPageFeatureProvider;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.PDFCommonTools;
import java.awt.Rectangle;
import java.lang.Integer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author piotr
 */
public class GraphicalAreasProvider implements IPDFPageFeatureProvider {

    /**
     * Clusters all operations passed as parameter and returns all the clusters
     * @param operations
     * @param verticalMargin
     * @param horizontalMargin
     * @return rectangle -> (list of operations, area number)
     */
    private static Map<Rectangle, Pair<List<Operation>, Integer>> clusterOperations(
            Collection<Operation> operations, PDFPageManager manager, int horizontalMargin,
            int verticalMargin, PageLayout pageLayout) throws Exception {

        HashMap<Integer, SpatialClusterManager<Operation>> clusterManagers =
                new HashMap<Integer, SpatialClusterManager<Operation>>();

        for (Integer areaNum = 0; areaNum < pageLayout.areas.size(); ++areaNum) {
            clusterManagers.put(areaNum, new SpatialClusterManager<Operation>(
                    ExtractorGeometryTools.extendRectangle(manager.getPageBoundary(),
                    horizontalMargin * 2, verticalMargin * 2),
                    horizontalMargin, verticalMargin));
        }

// old code not taking into account the division into areas
//        SpatialClusterManager<Operation> clusterManager =
//                new SpatialClusterManager<Operation>(
//                ExtractorGeometryTools.extendRectangle(manager.getPageBoundary(),
//                horizontalMargin * 2, verticalMargin * 2),
//                horizontalMargin, verticalMargin);

        for (Operation op : operations) {
            if (op instanceof DisplayedOperation) {
//                if (clusterManager != null) {
                DisplayedOperation dOp = (DisplayedOperation) op;
                Rectangle srcRec = dOp.getBoundary();

                Set<Integer> intersectingAreas = pageLayout.getIntersectingAreas(srcRec);
                // we want to consider only the areas that are intersecting

                for (Integer areaNum : intersectingAreas) {
                    Rectangle rec = new Rectangle((int) srcRec.getX(), (int) srcRec.getY(),
                            (int) srcRec.getWidth(), (int) srcRec.getHeight());

                    clusterManagers.get(areaNum).addRectangle(rec, op);
                }

            } // else: the operation is either completely incorret( from outside a page) or some horrible error happened
        }


        // now merging informations from different cluster managers
        HashMap<Rectangle, Pair<List<Operation>, Integer>> results =
                new HashMap<Rectangle, Pair<List<Operation>, Integer>>();

        for (Integer areaNum : clusterManagers.keySet()) {
            Map<Rectangle, List<Operation>> partial =
                    clusterManagers.get(areaNum).getFinalBoundaries();
            for (Rectangle areaRectangle : partial.keySet()) {
                results.put(areaRectangle,
                        new Pair<List<Operation>, Integer>(partial.get(areaRectangle), areaNum));
            }
        }

        return results;

    }

    private static Map<Rectangle, Pair<List<Operation>, Integer>> clusterOperations(PDFPageManager manager, PageLayout pageLayout) throws Exception {
        Set<Operation> interestingOperations = manager.getGraphicalOperations();
        int[] margins = PDFCommonTools.calculateGraphicsMargins(manager);
        return clusterOperations(interestingOperations, manager, margins[0], margins[1], pageLayout);
    }

    @Override
    public <T> IPDFPageFeature calculateFeature(PDFPageManager<T> pageManager) throws Exception {
        PageLayout pageLayout = (PageLayout) pageManager.getPageFeature(PageLayout.featureName);
        GraphicalAreas result = new GraphicalAreas();

        result.areas = clusterOperations(pageManager, pageLayout);
        return result;
    }

    @Override
    public String getProvidedFeatureName() {
        return GraphicalAreas.featureName;
    }
}
