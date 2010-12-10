/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.ExtractorGeometryTools;
import invenio.common.SpatialClusterManager;
import invenio.pdf.core.DisplayedOperation;
import invenio.pdf.core.IPDFPageFeature;
import invenio.pdf.core.IPDFPageFeatureProvider;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.PDFCommonTools;
import java.awt.Rectangle;
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
     * @return
     */
    private static Map<Rectangle, List<Operation>> clusterOperations(
            Collection<Operation> operations, PDFPageManager manager, int horizontalMargin,
            int verticalMargin, PageLayout pageLayout) throws Exception {

        HashMap<Rectangle, SpatialClusterManager<Operation>> clusterManagers =
                new HashMap<Rectangle, SpatialClusterManager<Operation>>();

//        for (Rectangle column : pageLayout.columns) {
//            clusterManagers.put(column, new SpatialClusterManager<Operation>(
//                    ExtractorGeometryTools.extendRectangle(manager.getPageBoundary(),
//                    horizontalMargin * 2, verticalMargin * 2),
//                    horizontalMargin, verticalMargin));
//        }
        
        // to be commented out later!
        SpatialClusterManager<Operation> clusterManager =
                new SpatialClusterManager<Operation>(
                    ExtractorGeometryTools.extendRectangle(manager.getPageBoundary(),
                    horizontalMargin * 2, verticalMargin * 2),
                    horizontalMargin, verticalMargin);

        for (Operation op : operations) {
            if (op instanceof DisplayedOperation) {
//                SpatialClusterManager<Operation> clusterManager = null;
//                for (Rectangle column: pageLayout.columns){
//
//                }
                
                if (clusterManager != null) {
                    DisplayedOperation dOp = (DisplayedOperation) op;
                    Rectangle srcRec = dOp.getBoundary();

                    Rectangle rec = new Rectangle((int) srcRec.getX(), (int) srcRec.getY(),
                            (int) srcRec.getWidth(), (int) srcRec.getHeight());
                    clusterManager.addRectangle(rec, op);

                } // else: the operation is either completely incorret( from outside a page) or some horrible error happened
            }
        }

        // now merging informations from different cluster managers
        return clusterManager.getFinalBoundaries();

    }

    private static Map<Rectangle, List<Operation>> clusterOperations(PDFPageManager manager, PageLayout pageLayout) throws Exception {
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
