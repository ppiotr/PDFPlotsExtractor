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
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author piotr
 */
public class TextAreas implements IPDFPageFeature{
    public static final String featureName = "TextAreas";

    public Map<Rectangle, Pair<String, List<Operation>>> areas;

    public TextAreas(){
        this.areas = new HashMap<Rectangle, Pair<String, List<Operation>>>();
    }

    @Override
    public void saveToXml(Document document, Element rootElement) {
        throw new UnsupportedOperationException("Not supported yet.... "
                + "implement using the annmotated text writer");
    }
}
