
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
     *  Returns page areas lying on the left of a given area
     * @param areaNum number of the area to be tested
     * @return 
     */
    public HashSet<Integer> getLeftAreas(int areaNum) {
        HashSet<Integer> onTheLeft = new HashSet<Integer>();
        for (Rectangle rec : this.areas.get(areaNum)) {
            // for each rectangle of an area generate a flat rectangle on the left
            Rectangle tester = new Rectangle(rec.x - 1, rec.y, 1, rec.height);
            for (Integer intArea : this.getIntersectingAreas(tester)) {
                if (intArea != areaNum) {
                    onTheLeft.add(intArea);
                }
            }
        }
        return onTheLeft;
    }

    /**
     *  Returns page areas lying on the right of a given area
     * @param areaNum number of the area to be tested
     * @return 
     */
    public HashSet<Integer> getRightAreas(int areaNum) {
        HashSet<Integer> onTheRight = new HashSet<Integer>();
        for (Rectangle rec : this.areas.get(areaNum)) {
            // for each rectangle of an area generate a flat rectangle on the left
            Rectangle tester = new Rectangle(rec.x + rec.width, rec.y, 1, rec.height);
            for (Integer intArea : this.getIntersectingAreas(tester)) {
                if (intArea != areaNum) {
                    onTheRight.add(intArea);
                }
            }
        }
        return onTheRight;
    }
    /**
     *  Returns page areas lying on the top of a given area
     * @param areaNum number of the area to be tested
     * @return 
     */
    public HashSet<Integer> getTopAreas(int areaNum) {
        HashSet<Integer> onTheTop = new HashSet<Integer>();
        for (Rectangle rec : this.areas.get(areaNum)) {
            // for each rectangle of an area generate a flat rectangle on the left
            Rectangle tester = new Rectangle(rec.x, rec.y - 1, rec.width, 1);
            for (Integer intArea : this.getIntersectingAreas(tester)) {
                if (intArea != areaNum) {
                    onTheTop.add(intArea);
                }
            }
        }
        return onTheTop;
    }

    /**
     *  Returns page areas lying on the bottom of a given area
     * @param areaNum number of the area to be tested
     * @return 
     */
    public HashSet<Integer> getBottomAreas(int areaNum) {
        HashSet<Integer> onTheBottom = new HashSet<Integer>();
        for (Rectangle rec : this.areas.get(areaNum)) {
            // for each rectangle of an area generate a flat rectangle on the left
            Rectangle tester = new Rectangle(rec.x, rec.y + rec.height, rec.width, 1);
            for (Integer intArea : this.getIntersectingAreas(tester)) {
                if (intArea != areaNum) {
                    onTheBottom.add(intArea);
                }
            }
        }
        return onTheBottom;
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
