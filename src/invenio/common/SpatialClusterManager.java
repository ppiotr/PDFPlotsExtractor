/*
 * This class accepts rectangles clustering them into intersecting clusters
 */
package invenio.common;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author piotr
 */
public class SpatialClusterManager<StoredObjectType> {

    IntervalTree<Integer> xIntervalTree;
    IntervalTree<Integer> yIntervalTree;
    private Rectangle boundary;
    private int margin; //
    private Map<Integer, StoredObjectType> mappingsToObjects; // mapping numbers to the original objects
    private Map<Integer, Integer> mappingsToParents; // mapping numbers to list of objects contained in a given cathegory
    private int currentNumber = -1;

    private int getNextNumber() {
        currentNumber++;
        return currentNumber;
    }

    /**
     * Constructs a new spatial clusters manager
     * @param b the boundary of the entire region (everything has to be included in it)
     * @param m the margin by which each added rectangle will be extended
     */
    public SpatialClusterManager(Rectangle b, int m) {
        this.xIntervalTree = new IntervalTree<Integer>(b.x, b.x + b.width);
        this.yIntervalTree = new IntervalTree<Integer>(b.y, b.y + b.height);
        this.mappingsToObjects = new HashMap<Integer, StoredObjectType>();
        this.mappingsToParents = new HashMap<Integer, Integer>();
        this.margin = m;
    }

    /**
     * Return all the clusters being currently sotred in the manager
     *
     * @return a map identifier -> boundary
     */
    public Map<Integer, Rectangle> getFinalBoundaries() {
        Map<Integer, Rectangle> result = new HashMap<Integer, Rectangle>();
        Map<Integer, int[]> intervalsX = this.xIntervalTree.getAllIntervals();
        Map<Integer, int[]> intervalsY = this.yIntervalTree.getAllIntervals();
        // producing the cartesian products
        for (Integer i : intervalsX.keySet()) {
            if (!this.mappingsToParents.containsKey(i)) {
                int[] dx = intervalsX.get(i);
                int[] dy = intervalsY.get(i);

                result.put(i, new Rectangle(dx[0], dy[0], dx[1] - dx[0], dy[1] - dy[0]));
            }
        }

        return result;
    }

    /**
     * Adds a rectangle to the manager -> calculates all the intersections and
     * clusters intersecting elements together
     * @param rec
     * @param obj
     */
    public void addRectangle(Rectangle rec, StoredObjectType obj) {
        // update the original mapping

        int minX = rec.x - margin;
        int maxX = rec.x + rec.width + margin;
        int minY = rec.y - margin;
        int maxY = rec.y + rec.height + margin;

        int newObjectIdentifier = this.getNextNumber();
        this.mappingsToObjects.put(newObjectIdentifier, obj);
        Map<Integer, Boolean> intersecting = null;
        do {
            // finding intervals intersecting in x and y planes alone
            Map<Integer, int[]> xIntersecting = xIntervalTree.getIntersectingIntervals(
                    minX, maxX);

            Map<Integer, int[]> yIntersecting = yIntervalTree.getIntersectingIntervals(
                    minY, maxY);

            intersecting = new HashMap<Integer, Boolean>();
            for (Integer key : xIntersecting.keySet()) {
                if (yIntersecting.containsKey(key)) {
                    intersecting.put(key, Boolean.TRUE);
                    this.mappingsToParents.put(key, newObjectIdentifier); // fixing the parential relationship ..
                    // the new elements becomes a parent
                }
            }

            // find intersection of both sets of intervals ... and calculate the boundary of the intersection


            for (Integer intervalId : intersecting.keySet()) {
                int[] intervalX = xIntersecting.get(intervalId);
                if (minX > intervalX[0]) {
                    minX = intervalX[0];
                }

                if (maxX < intervalX[1]) {
                    maxX = intervalX[1];
                }

                int[] intervalY = yIntersecting.get(intervalId);
                if (minY > intervalY[0]) {
                    minY = intervalY[0];
                }

                if (maxY < intervalY[1]) {
                    maxY = intervalY[1];
                }
                // and finally we are removing intervals from both trees


                // temporarily, for the debigging reasons we want to show the tree before the operation
                //Images.writeImageToFile(this.xIntervalTree.renderTree(), "c:\\intervalTrees\\xtree_before_problem.png");
                // storing a copy of the tree... to render in case of problems and to be able to detect problems
                this.xIntervalTree.removeInterval(intervalX[0], intervalX[1], intervalId);
                this.yIntervalTree.removeInterval(intervalY[0], intervalY[1], intervalId);

            }
        } while (! intersecting.isEmpty()); // we finished only if there are no more intersecting areas

        this.xIntervalTree.addInterval(minX, maxX, newObjectIdentifier);
        this.yIntervalTree.addInterval(minY, maxY, newObjectIdentifier);
        // adding new intervals to the trees

    }
}
