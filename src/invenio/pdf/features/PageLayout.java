/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.pdf.core.IPDFPageFeature;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (results.isEmpty()){
            System.out.println("Strange enough !");
        }
        return results;
    }

    /**
     * Returns the area that fits the most
     * @return
     */
    public Integer getSingleBestIntersectingArea(){

    }
}
