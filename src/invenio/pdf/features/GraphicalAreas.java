/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.features;

import invenio.pdf.core.IPDFPageFeature;
import invenio.pdf.core.Operation;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

/**
 *
 * @author piotr
 */
public class GraphicalAreas implements IPDFPageFeature {
    public static final String featureName = "GraphicalAreas";
    public Map<Rectangle, List<Operation>> areas;
}
