/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.Pair;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.IPDFPageFeature;
import invenio.pdf.core.IPDFPageFeatureProvider;
import invenio.pdf.core.PDFPageManager;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author piotr
 */
public class PageLayoutProvider implements IPDFPageFeatureProvider {

    /**
     * Determines if the pixel can be considered empty. This is the case if
     * its distance from being white is not high
     * @param px
     * @return
     */
    private static boolean isPixelEmpty(int[] px) {
        int res = 0;
        for (int i = 0; i < 3; ++i) {
            res += (255 - px[i]) * (255 - px[i]);
        }
        return Math.sqrt(res) < 10;
    }

    /**
     * For each point of the raster calculate its distance to the closest non-empty box
     * 
     * @param preallocated
     * @return
     */
    private static int[][] calculateDistances(Raster raster, int[][] preallocated) {
        int[][] result;

        if (preallocated != null && preallocated.length == raster.getWidth()
                && preallocated[0].length == raster.getHeight()) {
            result = preallocated;
        } else {
            result = new int[raster.getWidth()][raster.getHeight()];
        }

        // clearing the array
        for (int x = 0; x < raster.getWidth(); ++x) {
            for (int y = 0; y < raster.getHeight(); ++y) {
                result[x][y] = -1;
            }
        }

        // initialisation finished, now real calculations-> first we have to fing all the non-zero pixels

        LinkedList<Pair<Integer, Integer>> bfsQueue = new LinkedList<Pair<Integer, Integer>>();
        int initiallyAdded = 0;
        int[] curPixel = new int[3]; // an array storing the current pixel
        for (int x = 0; x < raster.getWidth(); ++x) {
            for (int y = 0; y < raster.getHeight(); ++y) {
                if (!isPixelEmpty(raster.getPixel(x, y, curPixel))) {
                    initiallyAdded++;
                    bfsQueue.addLast(new Pair<Integer, Integer>(x, y));
                    result[x][y] = 0;
                }
            }
        }
        System.out.println("initially added " + initiallyAdded + " pixels");

        while (!bfsQueue.isEmpty()) {
            Pair<Integer, Integer> point = bfsQueue.removeFirst();
            int x = point.first;
            int y = point.second;

            // now treating all the neighbours of the point
            result[x][y] = raster.getHeight() + raster.getWidth();
            result[x][y] = updateMin(result[x][y], x - 1, y, result, raster);
            result[x][y] = updateMin(result[x][y], x + 1, y, result, raster);
            result[x][y] = updateMin(result[x][y], x, y - 1, result, raster);
            result[x][y] = updateMin(result[x][y], x, y + 1, result, raster);
            result[x][y]++;

            // conditionally adding adjacent points to the queue
            conditionallyAddIntoQueue(x - 1, y, result, raster, bfsQueue);
            conditionallyAddIntoQueue(x + 1, y, result, raster, bfsQueue);
            conditionallyAddIntoQueue(x, y - 1, result, raster, bfsQueue);
            conditionallyAddIntoQueue(x, y + 1, result, raster, bfsQueue);
        }

        return result;
    }

    /**
     * If this place has not been processed yet, add it to the queue
     * @param x
     * @param y
     * @param dists
     * @param raster
     */
    private static void conditionallyAddIntoQueue(int x, int y, int[][] dists,
            Raster raster, LinkedList<Pair<Integer, Integer>> queue) {
        if (x >= 0 && y >= 0 && x < raster.getWidth() && y < raster.getHeight()
                && dists[x][y] == -1) {
            queue.addLast(new Pair<Integer, Integer>(x, y));
        }
    }

    /**
     * update minimum -> don't take into account negative numbers
     * @param curMin
     * @param x
     * @param y
     * @param dists
     * @param raster
     * @return
     */
    private static int updateMin(int curMin, int x, int y, int[][] dists, Raster raster) {
        if (x < 0 || y < 0 || x >= raster.getWidth() || y >= raster.getHeight()) {
            return curMin;
        }
        if (dists[x][y] < 0) {
            return curMin;
        }
        if (curMin > dists[x][y]) {
            return dists[x][y];
        } else {
            return curMin;
        }
    }

    /**
     * Checks if the point and its surroundings are empty
     * @param x
     * @param y
     * @param raster
     * @return
     */
    private static boolean isPointEmpty(int x, int y, Raster raster, int[][] distances) {
        double emptinessRadius = 0.002;

        int r = (int) (emptinessRadius * raster.getWidth());
        return distances[x][y] >= r;
    }

    private static boolean isPointEmpty(int x, int y, Raster raster) {
        int[] currentPixel = new int[3];
        double emptinessRadius = 0.01;

        int r = (int) (emptinessRadius * raster.getWidth());
        for (int dx = -r; dx < r; ++dx) {
            currentPixel = raster.getPixel(x + dx, y, currentPixel);
            if (!isPixelEmpty(currentPixel)) {
                if (x == 622) {
                    System.out.println("here we go !");
                }

                return false;
            }
        }
        return true;
    }
    private static int[][] distancesArray = null; // array of distences from non-empty pixels

    /** Detects a layout of the page by searching high consistent blocks of
     *  empty space in the rendered version of the page. (a minimal width and
     *  height are set.
     *
     *  Each column is a rectangle
     *
     * @param pageManager
     * @return
     */
    public static List<Rectangle> getPageColumns(PDFPageManager pageManager) {
        double minimalFractionOfColumnSeparatorHeight = 0.4; // minimal height of an area separating columns
        //       double minimalFractionOfColumnSeparatorWidth = 0.01; // minimal height of an area separating columns
        double minimalFractionOfMarginWidth = 0.1; // minimal height of an area separating columns

        Raster raster = pageManager.getRenderedPage().getData();

        int minimalHeight = (int) (raster.getHeight() * minimalFractionOfColumnSeparatorHeight);
        //       int minimalWidth = (int) (raster.getWidth() * minimalFractionOfColumnSeparatorWidth);
        int minimalLeft = (int) (raster.getWidth() * minimalFractionOfMarginWidth);
        int maximalLeft = (int) (raster.getWidth() * (1 - minimalFractionOfMarginWidth));


        //ArrayList<ArrayList<Pair<Integer, Integer>>>  = new ArrayList<ArrayList<Pair<Integer, Integer>>>();

        ArrayList<Rectangle> columns = new ArrayList<Rectangle>();
        ArrayList<Rectangle> startedColumns = new ArrayList<Rectangle>(); // started columns. Wodth is invalid as we do not know them yet
        ArrayList<Integer> emptyAreas = new ArrayList<Integer>();
//        distancesArray = calculateDistances(raster, distancesArray);
        startedColumns.add(new Rectangle(0, 0, raster.getWidth(), raster.getHeight()));

        // for debugging ... we want this data to be visible

        int debug_page_width = raster.getWidth();
        int debug_page_height = raster.getHeight();


        boolean inMaximisationMode = false; // the mode where we choose the biggest separator.
        int currentColumnEvaluation = 0;

        int xOfMaximum = 0;
        int maximalWeight = 0;

        ArrayList<Integer> maximalSeparators = new ArrayList<Integer>();

        for (int x = minimalLeft; x < maximalLeft; ++x) { // iterating over the columns
            // calculating signature of a line
            int emptyAreaBeginning = 0;

            emptyAreas.clear();
            boolean isEmpty = true;

            for (int y = 0; y < raster.getHeight(); ++y) {
                if (!isPointEmpty(x, y, raster) || (y == (raster.getHeight() - 1))) {
                    if ((y - emptyAreaBeginning) > minimalHeight) {
                        emptyAreas.add(emptyAreaBeginning);
                        emptyAreas.add(y);
                        currentColumnEvaluation += y - emptyAreaBeginning;
                    }
                    emptyAreaBeginning = y + 1;
                }
            }

            // we want to maintain the information of the longest separator line
            if (maximalWeight < currentColumnEvaluation) {
                maximalWeight = currentColumnEvaluation;
                maximalSeparators.clear();
                maximalSeparators.addAll(emptyAreas);
                xOfMaximum = x;
            }

            // now we have to look, what impact do our lines have on columns that have been begun
            // we have to find begun rectangles intersecting with the line.

            if (inMaximisationMode) {
                if (emptyAreas.isEmpty()) {
                    // apply the maximal separator
                    startedColumns = updateCurrentColumns(columns, maximalSeparators, startedColumns, xOfMaximum);

                    // now reseting maximal values

                    maximalWeight = 0;

                    inMaximisationMode = false;
                }
            } else {
                if (!emptyAreas.isEmpty()) {
                    inMaximisationMode = true;
                }
            }
        }

// now closing all the unfinished columns -> equivalent to a separator going from the top to the bottom
        ArrayList<Integer> finalSeparator = new ArrayList<Integer>();

        finalSeparator.add(0);

        finalSeparator.add(raster.getHeight()
                - 1);

        updateCurrentColumns(columns, finalSeparator, startedColumns, raster.getWidth()); // we do not care
        // of the currently
        // open columns any more


        return columns;

    }

    /**
     * Updates information about current columns and unfinished columns based on
     * the currently detected separators and on existing unfinished columns
     * 
     * @param columns
     * @param separatorPoints
     * @param startedColumns
     * @param curX - x coordinate at which we are currently
     * @return
     */
    public static ArrayList<Rectangle> updateCurrentColumns(ArrayList<Rectangle> columns, ArrayList<Integer> separatorPoints,
            ArrayList<Rectangle> startedColumns, int curX) {

        if (separatorPoints.isEmpty()) {
            return startedColumns;
        }

        ArrayList<Rectangle> newColumns = new ArrayList<Rectangle>();
        boolean onSeparator = false;
        int curPoint = separatorPoints.get(0);
        int curPointIndex = 0;
        Rectangle curRectangle = new Rectangle(curX, 0, 0, 0); // this is used to generate newcomming rectangles


        for (Rectangle curCol : startedColumns) {
            int curY = curCol.y; // this is used to close existing parts of rectangles
            while (curPoint >= curCol.y && curPoint <= curCol.y + curCol.height) {
                if (onSeparator) { // the sparator is just ending
                    // we are at the separator... we have to split our current rectangle until the current point
                    /// adding height to a new rectangle
                    /// adding width to the rectangle being closed

                    curRectangle.setBounds(curRectangle.x, curRectangle.y, 0, curPoint - curRectangle.y);
                    if (curRectangle.height > 0) {
                        newColumns.add(curRectangle);
                    }
                    columns.add(new Rectangle(curCol.x, curY, curX - curCol.x, curPoint - curY));

                } else { // the separator is just beginning
                    if (curPoint > curY) {
                        newColumns.add(new Rectangle(curCol.x, curY, 0, curPoint - curY)); // a rectangle starting at teh x of old one
                    }
                    curRectangle = new Rectangle(curX, curPoint, 0, 0); // width and height are filled later
                }

                onSeparator = !onSeparator;
                curY = curPoint;
                curPointIndex++;

                if (curPointIndex < separatorPoints.size()) {
                    curPoint = separatorPoints.get(curPointIndex);
                } else {
                    // we are choosing a point from behind the page - it will never be processed
                    curPoint = (int) startedColumns.get(
                            startedColumns.size() - 1).getMaxY() + 10;
                }
            }

            // do something with the remainder of this column
            if (onSeparator) {
                // we have to split the remainder of the interval
                columns.add(new Rectangle(curCol.x, curY, curX - curCol.x, curCol.y + curCol.height - curY));
            } else {
                // we have to add the remainder of the interval intact
                curCol.setBounds(curCol.x, curY, curCol.width, curCol.height + curCol.y - curY);
                if (curCol.height > 0) {
                    newColumns.add(curCol);
                }
            }
        }
        // current rectangle should not exist at this time
        return newColumns;

    }

    @Override
    public <T> IPDFPageFeature calculateFeature(PDFPageManager<T> pageManager) throws FeatureNotPresentException, Exception {
        PageLayout layout = new PageLayout();
        layout.columns = getPageColumns(pageManager);
        return layout;
    }

    @Override
    public String getProvidedFeatureName() {
        return PageLayout.featureName;
    }
}
