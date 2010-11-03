/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.ExtractorGeometryTools;
import invenio.common.Pair;
import invenio.common.SpatialClusterManager;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.IPDFPageFeature;
import invenio.pdf.core.IPDFPageFeatureProvider;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFCommonTools;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.TextOperation;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

/**
 *
 * @author piotr
 */
public class TextAreasProvider implements IPDFPageFeatureProvider {

    /** calculate all the text regions inside the page
     *
     * text regions are distinguished by their margins. The vertical margin
     * is much narrower than the horizontal one
     *
     * @param pageManager
     * @return
     * @throws FeatureNotPresentException
     */
    @Override
    public TextAreas calculateFeature(PDFPageManager pageManager) throws FeatureNotPresentException {
        TextAreas result = new TextAreas();

        int[] margins = PDFCommonTools.calculateTextMargins(pageManager);

        Rectangle spBd = ExtractorGeometryTools.extendRectangle(
                pageManager.getPageBoundary(),
                margins[0] * 2, margins[1] * 2);
        
        SpatialClusterManager<Operation> clusterManager = new SpatialClusterManager<Operation>(
                spBd, margins[0], margins[1]);


       // System.out.println("SpatialClusterManager<Integer> clustersManager = new SpatialClusterManager<Integer>(new Rectangle(" + spBd.x + ", " + spBd.y + ", " + spBd.width + ", " + spBd.height + "), " + margins[0] + ", " + margins[1] + ");");
        //int tmpNum = 0;

        for (Operation op : pageManager.getTextOperations()) {
            TextOperation textOp = (TextOperation) op;
            Rectangle bd = textOp.getBoundary();
          //  System.out.println("clustersManager.addRectangle(new Rectangle(" + bd.x + ", " + bd.y + ", " + bd.width + ", " + bd.height + "), " + tmpNum + ");");
           // tmpNum++;
            System.out.flush();

            clusterManager.addRectangle(textOp.getBoundary(), textOp);
        }
        Map<Rectangle, List<Operation>> tmpResults = clusterManager.getFinalBoundaries();
        for (Rectangle bd : tmpResults.keySet()) {
            List<Operation> operations = tmpResults.get(bd);
            result.areas.put(ExtractorGeometryTools.shrinkRectangle(bd, margins[0], margins[1]),
                    new Pair<String, List<Operation>>(getTextAreaString(operations), operations));
        }
        return result;
    }

    @Override
    public String getProvidedFeatureName() {
        return TextAreas.featureName;
    }

    /**
     * Return an uniform string from a list of text operations
     * @param operations
     * @return string representation of a block
     */
    private String getTextAreaString(List<Operation> operations) {
        StringBuilder sb = new StringBuilder();
        for (Operation op : operations) {
            TextOperation to = (TextOperation) op;
            sb.append(to.getText());
        }
        return sb.toString();
    }
}
