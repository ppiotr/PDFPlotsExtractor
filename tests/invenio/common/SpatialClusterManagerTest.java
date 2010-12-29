/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.common;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
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

        Map<Rectangle, List<Integer>> finalBoundaries = spatialManager.getFinalBoundaries();
        assertEquals("Wrong size of the resulting set", rectanglesToFind.length, finalBoundaries.size());
        // asserting that  correct the rectangles have been used
        for (Rectangle rec : finalBoundaries.keySet()) {
            //for (Integer key : finalBoundaries.keySet()) {
            List<Integer> vals = finalBoundaries.get(rec);
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
        SpatialClusterManager<Integer> instance =
                new SpatialClusterManager<Integer>(
                new Rectangle(0, 0, 1000, 1000), 1, 1);
        instance.addRectangle(new Rectangle(10, 10, 100, 10), 1);
        instance.addRectangle(new Rectangle(10, 10, 10, 100), 2);
        assertRectangles(instance, new Rectangle[]{new Rectangle(9, 9, 102, 102)});
    }

    @Test
    public void testGetFinalBoundariesSeries() throws Exception {
        SpatialClusterManager<Integer> instance =
                new SpatialClusterManager<Integer>(
                new Rectangle(0, 0, 1000, 1000), 0, 0);

        instance.addRectangle(new Rectangle(10, 10, 2, 1), 1);
        instance.addRectangle(new Rectangle(11, 11, 2, 1), 3);

        instance.addRectangle(new Rectangle(11, 10, 1, 2), 2);
        instance.addRectangle(new Rectangle(12, 11, 1, 2), 2);

        assertRectangles(instance, new Rectangle[]{new Rectangle(10, 10, 3, 3)});
    }

    @Test
    public void testGetFinalBoundariesIndirectSimple() throws Exception {
        SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(new Rectangle(0, 0, 1000, 1000), 0, 0);
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
        SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(new Rectangle(0, 0, 1000, 1000), 0, 0);
        instance.addRectangle(new Rectangle(20, 20, 100, 1), 1);
        instance.addRectangle(new Rectangle(20, 20, 1, 100), 2);
        instance.addRectangle(new Rectangle(10, 10, 100, 1), 3);
        instance.addRectangle(new Rectangle(10, 10, 1, 100), 4);

        assertRectangles(instance, new Rectangle[]{new Rectangle(10, 10, 110, 110)});
    }

    @Test
    public void anotherTestsFoundToBeFailing() throws Exception {
        SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(new Rectangle(36, 40, 89, 67), 0, 0);
        instance.addRectangle(new Rectangle(86, 74, 22, 1), 0);
        IntervalTreeTest.checkTreeSainty(instance.xIntervalTree);
        IntervalTreeTest.checkTreeSainty(instance.yIntervalTree);
        instance.addRectangle(new Rectangle(63, 79, 38, 12), 1);
        IntervalTreeTest.checkTreeSainty(instance.xIntervalTree);
        IntervalTreeTest.checkTreeSainty(instance.yIntervalTree);
        instance.addRectangle(new Rectangle(85, 96, 13, 4), 2);
        IntervalTreeTest.checkTreeSainty(instance.xIntervalTree);
        IntervalTreeTest.checkTreeSainty(instance.yIntervalTree);
        instance.addRectangle(new Rectangle(121, 63, 1, 19), 3);
        IntervalTreeTest.checkTreeSainty(instance.xIntervalTree);
        IntervalTreeTest.checkTreeSainty(instance.yIntervalTree);
        instance.addRectangle(new Rectangle(86, 80, 20, 18), 4);
        IntervalTreeTest.checkTreeSainty(instance.xIntervalTree);
        IntervalTreeTest.checkTreeSainty(instance.yIntervalTree);

    }

    /**
     * Returns a random rectangle lying inside a given area
     * @param area
     * @return
     */
    private Rectangle getRandomRectangle(Rectangle area) {
        int x = randomGenerator.nextInt(area.width - 3) + 1;
        int y = randomGenerator.nextInt(area.height - 3) + 1;
        int width = randomGenerator.nextInt(area.width - x - 2) + 1;
        int height = randomGenerator.nextInt(area.height - y - 2) + 1;
        return new Rectangle(area.x + x, area.y + y, width, height);
    }

    @Test
    public void bigRandomTest() throws Exception {
        int numberOfTests = 1000;
        int numberOfTrials = 300;
        for (int testNum = 0; testNum < numberOfTests; testNum++) {
            Rectangle boundary = null;
            do {
                boundary = getRandomRectangle(new Rectangle(0, 0, 150, 150));
            } while (boundary.width < 7 || boundary.height < 7);

            SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(boundary, 0, 0);

            List<Rectangle> recordedRectangles = new ArrayList<Rectangle>();

            for (int i = 0; i < numberOfTrials; i++) {
                Rectangle newRectangle = getRandomRectangle(boundary);
                recordedRectangles.add(newRectangle);
                instance.addRectangle(newRectangle, i);
                if (!IntervalTreeTest.isTreeSane(instance.xIntervalTree)) {
                    try {
                        //                       Images.writeImageToFile(instance.xIntervalTree.renderTree(), "c:\\intervalTrees\\failing_x_tree.png");
                        Images.writeImageToFile(instance.xIntervalTree.renderTree(), "/home/piotr/intervalTrees/failing_x_tree.png");

                    } catch (IOException ex) {
                        Logger.getLogger(SpatialClusterManagerTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Epic failure x;");
                }
                if (!IntervalTreeTest.isTreeSane(instance.yIntervalTree)) {
                    try {
//                        Images.writeImageToFile(instance.yIntervalTree.renderTree(), "c:\\intervalTrees\\failing_y_tree.png");
                        Images.writeImageToFile(instance.yIntervalTree.renderTree(), "/home/piotr/intervalTrees/failing_y_tree.png");

                    } catch (IOException ex) {
                        Logger.getLogger(SpatialClusterManagerTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Epic failure y;");
                }

                IntervalTreeTest.checkTreeSainty(instance.xIntervalTree);
                IntervalTreeTest.checkTreeSainty(instance.yIntervalTree);
            }
        }
    }
}
