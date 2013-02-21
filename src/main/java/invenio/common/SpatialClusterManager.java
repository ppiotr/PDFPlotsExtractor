/*
 * This class accepts rectangles clustering them into intersecting clusters
 */
package invenio.common;

import invenio.pdf.core.ExtractorLogger;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author piotr
 */
public class SpatialClusterManager<StoredObjectType> {

    IntervalTree<Integer> xIntervalTree;
    IntervalTree<Integer> yIntervalTree;
    private Rectangle boundary;
    private int hMargin, vMargin;
    private Map<Integer, StoredObjectType> mappingsToObjects; // mapping numbers to the original objects
    private Map<Integer, Integer> mappingsToParents; // mapping numbers to list of objects contained in a given cathegory
    private int currentNumber = -1;

    private int getNextNumber() {
        currentNumber++;
        return currentNumber;
    }

    /**
     * Constructs a new spatial clusters manager
     * @param b the boundary of the entire region (everything has to be included
     *          in it)
     * @param hM the margin by which each added rectangle will be extended
     *                in horizontal directions
     * @param vM the margin by which each added rectangle will be extended
     *                in vertical directions
     */
    public SpatialClusterManager(Rectangle b, int hM, int vM) {
        this.xIntervalTree = new IntervalTree<Integer>(b.x, b.x + b.width);
        this.yIntervalTree = new IntervalTree<Integer>(b.y, b.y + b.height);
        this.mappingsToObjects = new HashMap<Integer, StoredObjectType>();
        this.mappingsToParents = new HashMap<Integer, Integer>();
        this.hMargin = hM;
        this.vMargin = vM;
        this.boundary = b;
    }

    /**
     * Return all the clusters being currently sorted in the manager
     *
     * @return a map boundary -> list of objects  intersecting with the boundary
     */
    public Map<Rectangle, List<StoredObjectType>> getFinalBoundaries() {

        Map<Integer, Rectangle> partialResults =
                new HashMap<Integer, Rectangle>();
        Map<Integer, int[]> intervalsX = this.xIntervalTree.getAllIntervals();
        Map<Integer, int[]> intervalsY = this.yIntervalTree.getAllIntervals();

        // producing the cartesian products
        for (Integer i : intervalsX.keySet()) {
            if (!this.mappingsToParents.containsKey(i)) {
                int[] dx = intervalsX.get(i);
                int[] dy = intervalsY.get(i);

                if (dx == null || dy == null) {
                    ExtractorLogger.logMessage(1, "ERROR: Please increase the document scale ! some operations are rendered into no points !");
                } else {
                    partialResults.put(i, new Rectangle(
                            dx[0], dy[0], dx[1] - dx[0], dy[1] - dy[0]));
                }
            }
        }

        // now rewriting the results so that we have a list of objects 
        // associated with a given boundary arther than the identifier of the
        // parent

        Map<Rectangle, List<StoredObjectType>> result =
                new HashMap<Rectangle, List<StoredObjectType>>();

        for (Rectangle r : partialResults.values()) {
            result.put(r, new LinkedList<StoredObjectType>());
        }

        // now iterating over all the basic operations and associating them to
        // the parent region

        for (Integer internalIdent : this.mappingsToObjects.keySet()) {
            // find parent of this number
            int currentNum = internalIdent;
            Stack<Integer> visitednumbers = new Stack<Integer>();


            while (this.mappingsToParents.containsKey(currentNum)) {
                visitednumbers.push(currentNum);
                currentNum = this.mappingsToParents.get(currentNum);
            }
            // now we have the entire path on the stack -> we can repair links
            // for the futureuse - let them point directly to the root

            while (!visitednumbers.empty()) {
                int num = visitednumbers.pop();
                this.mappingsToParents.put(num, currentNum);
            }

            // we have the parent whose boundary is the boundary of the group
            if (partialResults.get(currentNum) == null){
                ExtractorLogger.logMessage(0, "FATAL: Error when clustering graphical areas ");
            }
            result.get(partialResults.get(currentNum)).add(
                    this.mappingsToObjects.get(internalIdent));
        }

        return result;
    }

    /**
     * Adds a rectangle to the manager -> calculates all the intersections and
     * clusters intersecting elements together
     * @param rec
     * @param obj
     */
    public void addRectangle(Rectangle rec, StoredObjectType obj) throws Exception {
        // a simple check allowing not to corrupt trees in case of incorrect data and to debug much more easily

        if (rec.getMinX() < this.boundary.getMinX()
                || rec.getMinY() < this.boundary.getMinY()
                || rec.getMaxX() > this.boundary.getMaxX()
                || rec.getMaxY() > this.boundary.getMaxY()) {
            throw new Exception("Trying to add a region that is outside the declared space boundary");
        }


        int minX = rec.x - this.hMargin;
        int maxX = rec.x + rec.width + this.hMargin;
        int minY = rec.y - this.vMargin;
        int maxY = rec.y + rec.height + this.vMargin;

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
        } while (!intersecting.isEmpty()); // we finished only if there are no more intersecting areas

        this.xIntervalTree.addInterval(minX, maxX, newObjectIdentifier);
        this.yIntervalTree.addInterval(minY, maxY, newObjectIdentifier);
        // adding new intervals to the trees

    }
    
}
