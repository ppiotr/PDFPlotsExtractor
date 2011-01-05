/*
 * An obsolete code taking a raster and for each point calculating its distance
 * from the nearest non-empty point
 */

package invenio.pdf.plots.old;

import invenio.common.Pair;
import java.awt.image.Raster;
import java.util.LinkedList;

/**
 *
 * @author piotr
 */
public class RasterCalculateDistancesFromNonemptyPoints {
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
}
