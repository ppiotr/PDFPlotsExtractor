/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.features;

import invenio.pdf.core.IPDFPageFeature;
import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

/**
 *
 * @author piotr
 */
public class PageLayout implements IPDFPageFeature {
    public static final String featureName = "PageLayout";
    public List<List<Rectangle>> areas;
    public List<Rectangle> columns;
}
