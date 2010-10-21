/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.common;

import java.util.List;
import java.util.ArrayList;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.eclipse.swt.internal.win32.HELPINFO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author piotr
 */
public class SpatialClusterManagerTest {

    private Random randomGenerator;

    public SpatialClusterManagerTest() {
        this.randomGenerator = new Random();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Asserts that only the appropriate rectangles are returned
     * @param spatialManager
     * @param rectanglesToFind
     */
    private void assertRectangles(SpatialClusterManager<Integer> spatialManager, Rectangle[] rectanglesToFind) {

        HashMap<Rectangle, Boolean> usedRectangles = new HashMap<Rectangle, Boolean>();
        for (Rectangle rec : rectanglesToFind) {
            usedRectangles.put(rec, Boolean.FALSE);
        }

        Map<Integer, Rectangle> finalBoundaries = spatialManager.getFinalBoundaries();
        assertEquals("Wrong size of the resulting set", rectanglesToFind.length, finalBoundaries.size());
        // asserting that  correct the rectangles have been used
        for (Integer key : finalBoundaries.keySet()) {
            Rectangle rec = finalBoundaries.get(key);
            assertEquals("Rectangle " + rec.toString() + " is not present or appeard more times in the result", Boolean.FALSE, usedRectangles.get(rec));
            usedRectangles.put(rec, Boolean.TRUE);
        }
        // asserting that all the correct rectangles have been used
        for (Rectangle key : usedRectangles.keySet()) {
            assertTrue("The rectangle " + key.toString() + " has not apprared in the result set", usedRectangles.get(key));
        }
    }

    /**
     * Test of getFinalBoundaries method, of class SpatialClusterManager.
     */
    @Test
    public void testGetFinalBoundaries() throws Exception {
        SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(new Rectangle(0, 0, 1000, 1000), 1);
        instance.addRectangle(new Rectangle(10, 10, 100, 10), 1);
        instance.addRectangle(new Rectangle(10, 10, 10, 100), 2);
        assertRectangles(instance, new Rectangle[]{new Rectangle(9, 9, 102, 102)});
    }

    @Test
    public void testGetFinalBoundariesSeries() throws Exception {
        SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(new Rectangle(0, 0, 1000, 1000), 0);

        instance.addRectangle(new Rectangle(10, 10, 2, 1), 1);
        instance.addRectangle(new Rectangle(11, 11, 2, 1), 3);

        instance.addRectangle(new Rectangle(11, 10, 1, 2), 2);
        instance.addRectangle(new Rectangle(12, 11, 1, 2), 2);

        assertRectangles(instance, new Rectangle[]{new Rectangle(10, 10, 3, 3)});
    }

    @Test
    public void testGetFinalBoundariesIndirectSimple() throws Exception {
        SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(new Rectangle(0, 0, 1000, 1000), 0);
        instance.addRectangle(new Rectangle(20, 20, 10, 1), 1);
        instance.addRectangle(new Rectangle(20, 20, 1, 10), 2);
        instance.addRectangle(new Rectangle(10, 10, 100, 1), 3);
        instance.addRectangle(new Rectangle(10, 10, 1, 100), 4);

        assertRectangles(instance, new Rectangle[]{new Rectangle(10, 10, 100, 100)});
    }

    /**
     *      ####################### <- insert second
     *      #
     *      #
     *      #
     *      #              #################
     *      #              #
     *      #              #
     *      #              #
     *                     #
     *                     #
     * insert first -->    #
     */
    @Test
    public void testGetFinalBoundariesIndirectMoreComplex() throws Exception {
        SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(new Rectangle(0, 0, 1000, 1000), 0);
        instance.addRectangle(new Rectangle(20, 20, 100, 1), 1);
        instance.addRectangle(new Rectangle(20, 20, 1, 100), 2);
        instance.addRectangle(new Rectangle(10, 10, 100, 1), 3);
        instance.addRectangle(new Rectangle(10, 10, 1, 100), 4);

        assertRectangles(instance, new Rectangle[]{new Rectangle(10, 10, 110, 110)});
    }

    /**
     * Returns a random rectangle lying inside a given area
     * @param area
     * @return
     */
    private Rectangle getRandomRectangle(Rectangle area) {
        int x = randomGenerator.nextInt(area.width - 1);
        int y = randomGenerator.nextInt(area.height - 1);
        int width = randomGenerator.nextInt(area.width - x - 1) + 1;
        int height = randomGenerator.nextInt(area.height - y - 1) + 1;
        return new Rectangle(area.x + x, area.y + y, width, height);
    }

    @Test
    public void bigRandomTest() {
        int numberOfTests = 1000;
        int numberOfTrials = 2;
        for (int testNum = 0; testNum < numberOfTests; testNum++) {
            Rectangle boundary = null;
            do {
                boundary = getRandomRectangle(new Rectangle(0, 0, 100, 100));
            } while (boundary.width < 10 || boundary.height < 10);

            SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(boundary, 0);

            List<Rectangle> recordedRectangles = new ArrayList<Rectangle>();

            for (int i = 0; i < numberOfTrials; i++) {
                Rectangle newRectangle = getRandomRectangle(boundary);
                recordedRectangles.add(newRectangle);
                instance.addRectangle(newRectangle, i);
                if (!IntervalTreeTest.isTreeSane(instance.xIntervalTree)){
                    System.out.println("Epic failure x;");
                }
                if (!IntervalTreeTest.isTreeSane(instance.yIntervalTree)){
                    System.out.println("Epic failure y;");
                }

                IntervalTreeTest.checkTreeSainty(instance.xIntervalTree);
                IntervalTreeTest.checkTreeSainty(instance.yIntervalTree);
            }
        }
    }
//
//    /**
//     * we are randomly generating a large number of rectangles from a given rectangle (the small rectangles are considerably smaller than the big one)
//     * by the law of big numbers we expect the big rectangle to be filled
//     */
//    @Test
//    public void testGetFinalBoundariesMany() {
//        Random rand = new Random();
//        SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(new Rectangle(0, 0, 10000, 10000), 0);
//        int width = 5;
//        int height = 5;
//        int maximalPiece = 3;
//        int minimalPiece = 1;
//
//        int x = rand.nextInt(1000) + 10;
//        int y = rand.nextInt(1000) + 10;
//
//        //int width = rand.nextInt(10) + 10;
//        //int height = rand.nextInt(10) + 10;
//
//        // the number of experiments depends on the total volume
//        int numberProbes = width * height;
//        System.out.println("Expecting the rectangle: (" + x + ", " + y + ", " + width + ", " + height + ")");
//        while (numberProbes > 0) {
//            int recWidth = rand.nextInt(maximalPiece - minimalPiece + 1) + minimalPiece;
//            int recHeight = rand.nextInt(maximalPiece - minimalPiece + 1) + 1;
//            int dx = rand.nextInt(width - recWidth + 1);
//            int dy = rand.nextInt(height - recHeight + 1);
//            instance.addRectangle(new Rectangle(x + dx, y + dy, recWidth, recHeight), numberProbes);
//            numberProbes--;
//            System.out.println("ädding the rectangle (" + (x + dx) + ", " + (y + dy) + ", " + recWidth + ", " + recHeight + ")");
//        }
//
//        // now checking if we have obtained the entire rectangle
//
//        Map<Integer, Rectangle> boundaries = instance.getFinalBoundaries();
//
//        assertEquals(1, boundaries.size());
//        Integer k1 = (Integer) boundaries.keySet().toArray()[0];
//
//        Rectangle r1 = boundaries.get(k1);
//        System.out.println("Obtained rectangle: (" + r1.x + ", " + r1.y + ", " + r1.width + ", " + r1.height + ")");
//
//        assertEquals(x, r1.x);
//        assertEquals(y, r1.y);
//        assertEquals(width, r1.width);
//        assertEquals(height, r1.height);
//    }
//    /**
//     * Test of addRectangle method, of class SpatialClusterManager.
//     */
//    @Test
//    public void testAddRectangle() {
//        System.out.println("addRectangle");
//        Rectangle rec = null;
//        Object obj = null;
//        SpatialClusterManager instance = null;
//        instance.addRectangle(rec, obj);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
