/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.features;

import invenio.common.Pair;
import invenio.pdf.core.IPDFPageFeature;
import invenio.pdf.core.Operation;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author piotr
 */
public class GraphicalAreas implements IPDFPageFeature {
    public static final String featureName = "GraphicalAreas";
    public Map<Rectangle, Pair<List<Operation>, Integer>> areas; //rectangle -> (lsit of PDF operations, number of the area)

    @Override
    public void saveToXml(Document document, Element rootElement) {
        // graphical areas are not yet supposed to be saved... skip
    }
}
