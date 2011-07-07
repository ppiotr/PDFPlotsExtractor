
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

//~--- non-JDK imports --------------------------------------------------------
import invenio.pdf.core.IPDFPageFeature;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Rectangle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author piotr
 */
public class PageLayout implements IPDFPageFeature {

    public static final String featureName = "PageLayout";
    public List<List<Rectangle>> areas;

    /**
     * Return a list of areas intersecting with the given rectangle
     * @param srcRec
     * @return
     */
    public HashSet<Integer> getIntersectingAreas(Rectangle srcRec) {
        HashSet<Integer> results = new HashSet<Integer>();

        for (Integer areaNum = 0; areaNum < areas.size(); areaNum++) {
            for (Rectangle areaPart : areas.get(areaNum)) {
                if (areaPart.intersects(srcRec)) {
                    results.add(areaNum);
                }
            }
        }

        return results;
    }

    /**
     * Returns the area that fits the most
     * @return
     */
    public int getSingleBestIntersectingArea(Rectangle srcRec) {
        int maximalArea = -1;
        int maximalIntersection = 0; // maximal intersection area

        Set<Integer> intersecting = getIntersectingAreas(srcRec);

        for (int areaNum : intersecting) {
            // for every intersecting area, calculate the intersection area and
            // compare to the previous maximum
            int currentIntersection = 0;
            for (Rectangle areaPart : this.areas.get(areaNum)) {
                Rectangle is = srcRec.intersection(areaPart);
                int added = is.width * is.height;
                currentIntersection += (added > 0) ? added : -added;
            }

            if (currentIntersection > maximalIntersection) {
                // we want to update the area intersecting the most
                maximalArea = areaNum;
                maximalIntersection = currentIntersection;
            }
        }

        return maximalArea;
    }

    @Override
    public void saveToXml(Document document, Element rootElement) {
        // we do not want to save page layout information yet... maybe in the future
    }
}
