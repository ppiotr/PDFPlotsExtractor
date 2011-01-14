/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.common;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of methods implementing commong geometric operations that can be used
 * across the application
 * @author piotr
 */
public class ExtractorGeometryTools {

    /**
     * A method returning a rectangle extended by given margin values in respect
     * to the given one
     *
     * @param original original rectangle ro be extended
     * @param hMargin number by which the rectangle should grow into the left
     *                and into the right
     * @param vMargin number by which the original rectangle should grow to the
     *                top and to the bottom
     * @return a new rectangle
     */
    public static Rectangle extendRectangle(Rectangle original,
            int hMargin, int vMargin) {
        return new Rectangle((int) original.getX() - hMargin,
                (int) original.getY() - vMargin,
                original.width + (2 * hMargin),
                original.height + (2 * vMargin));
    }

    /**
     * Shrinks a given rectangle by margin sizes passed as arguments
     * @param original rectangle to be modified
     * @param hmargin margins in horizontal directions
     * @param vMargin margins in vertical directions
     * @return new rectangle shrinked by given values from each side
     */
    public static Rectangle shrinkRectangle(Rectangle original,
            int hMargin, int vMargin) {
        return new Rectangle((int) original.getX() + hMargin,
                (int) original.getY() + vMargin,
                (int) original.getWidth() - (2 * hMargin),
                (int) original.getHeight() - (2 * vMargin));
    }

    /**
     * Shrink all the keys of a dictionary indexed by rectangles
     * @param <T> some type constituting a value of the mapping, we do not
     *            assume anything about it
     * @param originals
     * @param hMargin
     * @param vMargin
     */
    public static <T> Map<Rectangle, T> shrinkRectangleMap(
            Map<Rectangle, T> originals, int hMargin, int vMargin) {
        HashMap<Rectangle, T> result = new HashMap<Rectangle, T>();
        for (Rectangle rec : originals.keySet()) {
            result.put(shrinkRectangle(rec, hMargin, vMargin),
                    originals.get(rec));
        }
        return result;
    }

    /**
     * Cropping in one dimension
     *
     * @param pos the value to be cropped 
     * @param bp boundary beginning value
     * @param bl boundary extent
     * @return
     */
    private static int cropPoint(int pos, int bp, int bl) {
        return (pos < bp) ? bp : ((pos > bp + bl) ? bp + bl : pos);
    }

    /** Cropp the rectangle rec so that it is equal to the intersection of
     * rec and bd
     *
     * @param rec The rectangle to crop
     * @param bd The boundary limit rectangle
     * @return
     */
    public static Rectangle cropRectangle(Rectangle rec,
            Rectangle bd) {

        int x = cropPoint(rec.x, bd.x, bd.width);
        int y = cropPoint(rec.y, bd.y, bd.height);
        int maxx = cropPoint(rec.x + rec.width, bd.x, bd.width);
        int maxy = cropPoint(rec.y + rec.height, bd.y, bd.height);
        return new Rectangle(x, y, maxx - x, maxy - y);
    }
}
