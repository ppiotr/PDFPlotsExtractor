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
    public void testGetFinalBoundariesIndirectMoreComplex() {
        SpatialClusterManager<Integer> instance = new SpatialClusterManager<Integer>(new Rectangle(0, 0, 1000, 1000), 0, 0);
        instance.addRectangle(new Rectangle(20, 20, 100, 1), 1);
        instance.addRectangle(new Rectangle(20, 20, 1, 100), 2);
        instance.addRectangle(new Rectangle(10, 10, 100, 1), 3);
        instance.addRectangle(new Rectangle(10, 10, 1, 100), 4);

        assertRectangles(instance, new Rectangle[]{new Rectangle(10, 10, 110, 110)});
    }

    @Test
    public void anotherTestsFoundToBeFailing() {
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
    public void bigRandomTest() {
        int numberOfTests = 10;
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

    @Test
    public void someTest() {
        SpatialClusterManager<Integer> manager = new SpatialClusterManager<Integer>(new Rectangle(-182, -182, 976, 820), 91, 7);
        manager.addRectangle(new Rectangle(357, 607, 3, 5), 0);
        manager.addRectangle(new Rectangle(140, 378, 2, 11), 1);
        manager.addRectangle(new Rectangle(220, 695, 24, 8), 2);
        manager.addRectangle(new Rectangle(285, 502, 182, 10), 3);
        manager.addRectangle(new Rectangle(72, 395, 329, 11), 4);
        manager.addRectangle(new Rectangle(352, 681, 9, 5), 5);
        manager.addRectangle(new Rectangle(384, 488, 6, 7), 6);
        manager.addRectangle(new Rectangle(72, 536, 384, 11), 7);
        manager.addRectangle(new Rectangle(467, 450, 9, 5), 8);
        manager.addRectangle(new Rectangle(471, 541, 6, 5), 9);
        manager.addRectangle(new Rectangle(134, 377, 3, 5), 10);
        manager.addRectangle(new Rectangle(353, 485, 27, 8), 11);
        manager.addRectangle(new Rectangle(338, 519, 200, 8), 12);
    }

    @Test
    public void longerCrashingTest() {
        SpatialClusterManager<Integer> clustersManager = new SpatialClusterManager<Integer>(new Rectangle(-182, -182, 976, 820), 91, 7);
        clustersManager.addRectangle(new Rectangle(72, 535, 59, 10), 0);
        clustersManager.addRectangle(new Rectangle(198, 148, 2, 5), 1);
        clustersManager.addRectangle(new Rectangle(204, 164, 5, 5), 2);
        clustersManager.addRectangle(new Rectangle(289, 693, 0, 11), 3);
        clustersManager.addRectangle(new Rectangle(151, 627, 2, 5), 4);
        clustersManager.addRectangle(new Rectangle(298, 500, 114, 11), 5);
        clustersManager.addRectangle(new Rectangle(221, 330, 8, 8), 6);
        clustersManager.addRectangle(new Rectangle(218, 124, 2, 11), 7);
        clustersManager.addRectangle(new Rectangle(196, 182, 6, 5), 8);
        clustersManager.addRectangle(new Rectangle(358, 341, 5, 5), 9);
        clustersManager.addRectangle(new Rectangle(376, 540, 7, 5), 10);
        clustersManager.addRectangle(new Rectangle(269, 143, 24, 8), 11);
        clustersManager.addRectangle(new Rectangle(82, 609, 6, 5), 12);
        clustersManager.addRectangle(new Rectangle(205, 211, 3, 5), 13);
        clustersManager.addRectangle(new Rectangle(365, 327, 2, 5), 14);
        clustersManager.addRectangle(new Rectangle(176, 305, 5, 5), 15);
        clustersManager.addRectangle(new Rectangle(388, 259, 1, 3), 16);
        clustersManager.addRectangle(new Rectangle(254, 325, 5, 5), 17);
        clustersManager.addRectangle(new Rectangle(283, 499, 8, 7), 18);
        clustersManager.addRectangle(new Rectangle(239, 302, 24, 10), 19);
        clustersManager.addRectangle(new Rectangle(518, 434, 6, 5), 20);
        clustersManager.addRectangle(new Rectangle(190, 159, 2, 11), 21);
        clustersManager.addRectangle(new Rectangle(526, 429, 8, 7), 22);
        clustersManager.addRectangle(new Rectangle(468, 384, 1, 3), 23);
        clustersManager.addRectangle(new Rectangle(400, 107, 57, 11), 24);
        clustersManager.addRectangle(new Rectangle(127, 552, 59, 11), 25);
        clustersManager.addRectangle(new Rectangle(263, 125, 37, 9), 26);
        clustersManager.addRectangle(new Rectangle(72, 570, 118, 10), 27);
        clustersManager.addRectangle(new Rectangle(316, 325, 5, 5), 28);
        clustersManager.addRectangle(new Rectangle(149, 449, 389, 10), 29);
        clustersManager.addRectangle(new Rectangle(503, 659, 0, 11), 30);
        clustersManager.addRectangle(new Rectangle(150, 659, 15, 8), 31);
        clustersManager.addRectangle(new Rectangle(223, 302, 4, 8), 32);
        clustersManager.addRectangle(new Rectangle(349, 178, 15, 8), 33);
        clustersManager.addRectangle(new Rectangle(140, 449, 4, 8), 34);
        clustersManager.addRectangle(new Rectangle(488, 259, 1, 1), 35);
        clustersManager.addRectangle(new Rectangle(178, 128, 6, 5), 36);
        clustersManager.addRectangle(new Rectangle(297, 659, 15, 8), 37);
        clustersManager.addRectangle(new Rectangle(177, 212, 18, 11), 38);
        clustersManager.addRectangle(new Rectangle(342, 521, 4, 5), 39);
        clustersManager.addRectangle(new Rectangle(250, 107, 61, 11), 40);
        clustersManager.addRectangle(new Rectangle(271, 213, 19, 8), 41);
        clustersManager.addRectangle(new Rectangle(365, 343, 3, 5), 42);
        clustersManager.addRectangle(new Rectangle(316, 343, 2, 5), 43);
        clustersManager.addRectangle(new Rectangle(72, 659, 72, 8), 44);
        clustersManager.addRectangle(new Rectangle(358, 252, 29, 8), 45);
        clustersManager.addRectangle(new Rectangle(314, 609, 7, 5), 46);
        clustersManager.addRectangle(new Rectangle(309, 341, 5, 5), 47);
        clustersManager.addRectangle(new Rectangle(411, 542, 1, 1), 48);
        clustersManager.addRectangle(new Rectangle(424, 178, 10, 8), 49);
        clustersManager.addRectangle(new Rectangle(422, 90, 15, 8), 50);
        clustersManager.addRectangle(new Rectangle(246, 570, 9, 8), 51);
        clustersManager.addRectangle(new Rectangle(203, 107, 34, 11), 52);
        clustersManager.addRectangle(new Rectangle(287, 681, 6, 5), 53);
        clustersManager.addRectangle(new Rectangle(510, 659, 29, 8), 54);
        clustersManager.addRectangle(new Rectangle(341, 343, 3, 5), 55);
        clustersManager.addRectangle(new Rectangle(197, 557, 7, 5), 56);
        clustersManager.addRectangle(new Rectangle(356, 535, 13, 11), 57);
        clustersManager.addRectangle(new Rectangle(191, 194, 21, 11), 58);
        clustersManager.addRectangle(new Rectangle(373, 552, 165, 9), 59);
        clustersManager.addRectangle(new Rectangle(197, 215, 6, 7), 60);
        clustersManager.addRectangle(new Rectangle(358, 325, 5, 5), 61);
        clustersManager.addRectangle(new Rectangle(178, 198, 6, 7), 62);
        clustersManager.addRectangle(new Rectangle(72, 396, 466, 11), 63);
        clustersManager.addRectangle(new Rectangle(502, 522, 1, 3), 64);
        clustersManager.addRectangle(new Rectangle(348, 257, 3, 5), 65);
        clustersManager.addRectangle(new Rectangle(232, 535, 4, 8), 66);
        clustersManager.addRectangle(new Rectangle(136, 677, 4, 8), 67);
        clustersManager.addRectangle(new Rectangle(119, 306, 7, 2), 68);
        clustersManager.addRectangle(new Rectangle(368, 716, 6, 5), 69);
        clustersManager.addRectangle(new Rectangle(323, 343, 7, 0), 70);
        clustersManager.addRectangle(new Rectangle(261, 570, 277, 10), 71);
        clustersManager.addRectangle(new Rectangle(72, 252, 238, 8), 72);
        clustersManager.addRectangle(new Rectangle(334, 327, 3, 5), 73);
        clustersManager.addRectangle(new Rectangle(362, 694, 176, 10), 74);
        clustersManager.addRectangle(new Rectangle(352, 90, 9, 8), 75);
        clustersManager.addRectangle(new Rectangle(211, 130, 5, 5), 76);
        clustersManager.addRectangle(new Rectangle(420, 213, 19, 8), 77);
        clustersManager.addRectangle(new Rectangle(455, 382, 9, 5), 78);
        clustersManager.addRectangle(new Rectangle(499, 523, 2, 5), 79);
        clustersManager.addRectangle(new Rectangle(414, 535, 102, 10), 80);
        clustersManager.addRectangle(new Rectangle(163, 301, 3, 5), 81);
        clustersManager.addRectangle(new Rectangle(277, 680, 9, 5), 82);
        clustersManager.addRectangle(new Rectangle(246, 659, 45, 8), 83);
        clustersManager.addRectangle(new Rectangle(136, 535, 4, 8), 84);
        clustersManager.addRectangle(new Rectangle(237, 540, 7, 5), 85);
        clustersManager.addRectangle(new Rectangle(411, 194, 36, 11), 86);
        clustersManager.addRectangle(new Rectangle(440, 501, 98, 8), 87);
        clustersManager.addRectangle(new Rectangle(263, 194, 36, 11), 88);
        clustersManager.addRectangle(new Rectangle(521, 535, 4, 8), 89);
        clustersManager.addRectangle(new Rectangle(325, 107, 61, 11), 90);
        clustersManager.addRectangle(new Rectangle(445, 252, 19, 10), 91);
        clustersManager.addRectangle(new Rectangle(162, 110, 6, 5), 92);
        clustersManager.addRectangle(new Rectangle(270, 339, 4, 7), 93);
        clustersManager.addRectangle(new Rectangle(105, 629, 1, 1), 94);
        clustersManager.addRectangle(new Rectangle(380, 711, 159, 9), 95);
        clustersManager.addRectangle(new Rectangle(476, 522, 7, 2), 96);
        clustersManager.addRectangle(new Rectangle(221, 714, 6, 5), 97);
        clustersManager.addRectangle(new Rectangle(155, 626, 1, 3), 98);
        clustersManager.addRectangle(new Rectangle(274, 177, 16, 8), 99);
        clustersManager.addRectangle(new Rectangle(72, 587, 43, 8), 100);
        clustersManager.addRectangle(new Rectangle(186, 123, 8, 7), 101);
        clustersManager.addRectangle(new Rectangle(197, 124, 2, 11), 102);
        clustersManager.addRectangle(new Rectangle(523, 252, 15, 8), 103);
        clustersManager.addRectangle(new Rectangle(393, 252, 46, 10), 104);
        clustersManager.addRectangle(new Rectangle(269, 324, 7, 7), 105);
        clustersManager.addRectangle(new Rectangle(109, 622, 27, 8), 106);
        clustersManager.addRectangle(new Rectangle(329, 556, 4, 5), 107);
        clustersManager.addRectangle(new Rectangle(245, 501, 15, 8), 108);
        clustersManager.addRectangle(new Rectangle(244, 711, 109, 8), 109);
        clustersManager.addRectangle(new Rectangle(296, 335, 7, 0), 110);
        clustersManager.addRectangle(new Rectangle(244, 272, 6, 5), 111);
        clustersManager.addRectangle(new Rectangle(209, 212, 2, 11), 112);
        clustersManager.addRectangle(new Rectangle(72, 676, 58, 9), 113);
        clustersManager.addRectangle(new Rectangle(241, 334, 7, 2), 114);
        clustersManager.addRectangle(new Rectangle(347, 330, 9, 8), 115);
        clustersManager.addRectangle(new Rectangle(488, 662, 4, 5), 116);
        clustersManager.addRectangle(new Rectangle(99, 622, 4, 8), 117);
        clustersManager.addRectangle(new Rectangle(478, 379, 61, 9), 118);
        clustersManager.addRectangle(new Rectangle(72, 448, 63, 9), 119);
        clustersManager.addRectangle(new Rectangle(234, 657, 8, 7), 120);
        clustersManager.addRectangle(new Rectangle(121, 587, 0, 11), 121);
        clustersManager.addRectangle(new Rectangle(378, 384, 2, 5), 122);
        clustersManager.addRectangle(new Rectangle(72, 466, 466, 10), 123);
        clustersManager.addRectangle(new Rectangle(329, 257, 1, 3), 124);
        clustersManager.addRectangle(new Rectangle(465, 384, 2, 5), 125);
        clustersManager.addRectangle(new Rectangle(418, 504, 9, 5), 126);
        clustersManager.addRectangle(new Rectangle(308, 608, 4, 5), 127);
        clustersManager.addRectangle(new Rectangle(168, 125, 8, 8), 128);
        clustersManager.addRectangle(new Rectangle(228, 308, 8, 5), 129);
        clustersManager.addRectangle(new Rectangle(335, 536, 15, 8), 130);
        clustersManager.addRectangle(new Rectangle(489, 521, 9, 5), 131);
        clustersManager.addRectangle(new Rectangle(406, 605, 28, 8), 132);
        clustersManager.addRectangle(new Rectangle(537, 438, 1, 3), 133);
        clustersManager.addRectangle(new Rectangle(72, 431, 444, 10), 134);
        clustersManager.addRectangle(new Rectangle(188, 146, 9, 5), 135);
        clustersManager.addRectangle(new Rectangle(381, 384, 1, 3), 136);
        clustersManager.addRectangle(new Rectangle(338, 194, 36, 11), 137);
        clustersManager.addRectangle(new Rectangle(171, 659, 54, 8), 138);
        clustersManager.addRectangle(new Rectangle(460, 605, 78, 8), 139);
        clustersManager.addRectangle(new Rectangle(163, 308, 5, 5), 140);
        clustersManager.addRectangle(new Rectangle(358, 714, 9, 5), 141);
        clustersManager.addRectangle(new Rectangle(183, 307, 8, 5), 142);
        clustersManager.addRectangle(new Rectangle(141, 625, 9, 5), 143);
        clustersManager.addRectangle(new Rectangle(485, 659, 0, 11), 144);
        clustersManager.addRectangle(new Rectangle(297, 699, 7, 5), 145);
        clustersManager.addRectangle(new Rectangle(183, 164, 4, 5), 146);
        clustersManager.addRectangle(new Rectangle(287, 327, 3, 5), 147);
        clustersManager.addRectangle(new Rectangle(414, 160, 31, 8), 148);
        clustersManager.addRectangle(new Rectangle(72, 608, 9, 5), 149);
        clustersManager.addRectangle(new Rectangle(72, 500, 115, 9), 150);
        clustersManager.addRectangle(new Rectangle(72, 359, 415, 11), 151);
        clustersManager.addRectangle(new Rectangle(186, 194, 3, 5), 152);
        clustersManager.addRectangle(new Rectangle(186, 180, 9, 5), 153);
        clustersManager.addRectangle(new Rectangle(411, 125, 36, 8), 154);
        clustersManager.addRectangle(new Rectangle(201, 128, 9, 5), 155);
        clustersManager.addRectangle(new Rectangle(72, 556, 9, 5), 156);
        clustersManager.addRectangle(new Rectangle(86, 625, 7, 5), 157);
        clustersManager.addRectangle(new Rectangle(129, 592, 7, 5), 158);
        clustersManager.addRectangle(new Rectangle(341, 160, 31, 8), 159);
        clustersManager.addRectangle(new Rectangle(351, 257, 1, 3), 160);
        clustersManager.addRectangle(new Rectangle(182, 107, 2, 11), 161);
        clustersManager.addRectangle(new Rectangle(176, 163, 6, 5), 162);
        clustersManager.addRectangle(new Rectangle(72, 414, 220, 10), 163);
        clustersManager.addRectangle(new Rectangle(117, 558, 2, 5), 164);
        clustersManager.addRectangle(new Rectangle(208, 587, 9, 8), 165);
        clustersManager.addRectangle(new Rectangle(520, 259, 1, 1), 166);
        clustersManager.addRectangle(new Rectangle(280, 325, 5, 5), 167);
        clustersManager.addRectangle(new Rectangle(339, 517, 0, 11), 168);
        clustersManager.addRectangle(new Rectangle(491, 252, 15, 8), 169);
        clustersManager.addRectangle(new Rectangle(228, 709, 9, 7), 170);
        clustersManager.addRectangle(new Rectangle(197, 570, 4, 8), 171);
        clustersManager.addRectangle(new Rectangle(106, 305, 5, 5), 172);
        clustersManager.addRectangle(new Rectangle(72, 623, 9, 7), 173);
        clustersManager.addRectangle(new Rectangle(229, 301, 3, 5), 174);
        clustersManager.addRectangle(new Rectangle(493, 664, 7, 5), 175);
        clustersManager.addRectangle(new Rectangle(147, 301, 14, 11), 176);
        clustersManager.addRectangle(new Rectangle(106, 556, 9, 5), 177);
        clustersManager.addRectangle(new Rectangle(131, 305, 9, 5), 178);
        clustersManager.addRectangle(new Rectangle(249, 535, 80, 9), 179);
        clustersManager.addRectangle(new Rectangle(168, 677, 9, 8), 180);
        clustersManager.addRectangle(new Rectangle(275, 504, 6, 5), 181);
        clustersManager.addRectangle(new Rectangle(440, 608, 9, 5), 182);
        clustersManager.addRectangle(new Rectangle(323, 327, 2, 5), 183);
        clustersManager.addRectangle(new Rectangle(315, 255, 9, 5), 184);
        clustersManager.addRectangle(new Rectangle(72, 711, 147, 10), 185);
        clustersManager.addRectangle(new Rectangle(152, 107, 8, 8), 186);
        clustersManager.addRectangle(new Rectangle(391, 379, 56, 9), 187);
        clustersManager.addRectangle(new Rectangle(263, 277, 1, 1), 188);
        clustersManager.addRectangle(new Rectangle(327, 325, 5, 5), 189);
        clustersManager.addRectangle(new Rectangle(221, 587, 317, 10), 190);
        clustersManager.addRectangle(new Rectangle(265, 500, 8, 8), 191);
        clustersManager.addRectangle(new Rectangle(229, 335, 7, 5), 192);
        clustersManager.addRectangle(new Rectangle(397, 605, 4, 8), 193);
        clustersManager.addRectangle(new Rectangle(252, 267, 8, 7), 194);
        clustersManager.addRectangle(new Rectangle(345, 552, 13, 11), 195);
        clustersManager.addRectangle(new Rectangle(94, 605, 208, 10), 196);
        clustersManager.addRectangle(new Rectangle(371, 538, 4, 5), 197);
        clustersManager.addRectangle(new Rectangle(100, 274, 7, 5), 198);
        clustersManager.addRectangle(new Rectangle(450, 610, 2, 5), 199);
        clustersManager.addRectangle(new Rectangle(120, 557, 1, 3), 200);
        clustersManager.addRectangle(new Rectangle(326, 257, 2, 5), 201);
        clustersManager.addRectangle(new Rectangle(363, 553, 4, 8), 202);
        clustersManager.addRectangle(new Rectangle(124, 590, 4, 5), 203);
        clustersManager.addRectangle(new Rectangle(72, 694, 211, 10), 204);
        clustersManager.addRectangle(new Rectangle(295, 560, 1, 1), 205);
        clustersManager.addRectangle(new Rectangle(262, 327, 2, 5), 206);
        clustersManager.addRectangle(new Rectangle(342, 694, 15, 8), 207);
        clustersManager.addRectangle(new Rectangle(335, 557, 7, 5), 208);
        clustersManager.addRectangle(new Rectangle(384, 607, 7, 5), 209);
        clustersManager.addRectangle(new Rectangle(95, 272, 4, 5), 210);
        clustersManager.addRectangle(new Rectangle(266, 160, 31, 8), 211);
        clustersManager.addRectangle(new Rectangle(313, 694, 23, 8), 212);
        clustersManager.addRectangle(new Rectangle(72, 483, 466, 10), 213);
        clustersManager.addRectangle(new Rectangle(72, 302, 29, 8), 214);
        clustersManager.addRectangle(new Rectangle(303, 741, 4, 8), 215);
        clustersManager.addRectangle(new Rectangle(279, 90, 4, 8), 216);
        clustersManager.addRectangle(new Rectangle(386, 535, 14, 11), 217);
        clustersManager.addRectangle(new Rectangle(335, 341, 5, 5), 218);
        clustersManager.addRectangle(new Rectangle(113, 269, 129, 11), 219);
        clustersManager.addRectangle(new Rectangle(327, 605, 52, 8), 220);
        clustersManager.addRectangle(new Rectangle(347, 523, 7, 5), 221);
        clustersManager.addRectangle(new Rectangle(299, 676, 239, 11), 222);
        clustersManager.addRectangle(new Rectangle(469, 252, 16, 8), 223);
        clustersManager.addRectangle(new Rectangle(170, 105, 8, 7), 224);
        clustersManager.addRectangle(new Rectangle(185, 110, 9, 5), 225);
        clustersManager.addRectangle(new Rectangle(318, 659, 160, 10), 226);
        clustersManager.addRectangle(new Rectangle(142, 301, 3, 5), 227);
        clustersManager.addRectangle(new Rectangle(161, 622, 59, 8), 228);
        clustersManager.addRectangle(new Rectangle(307, 693, 0, 11), 229);
        clustersManager.addRectangle(new Rectangle(313, 552, 14, 11), 230);
        clustersManager.addRectangle(new Rectangle(193, 163, 9, 5), 231);
        clustersManager.addRectangle(new Rectangle(513, 252, 4, 8), 232);
        clustersManager.addRectangle(new Rectangle(368, 382, 9, 5), 233);
        clustersManager.addRectangle(new Rectangle(145, 677, 17, 8), 234);
        clustersManager.addRectangle(new Rectangle(112, 307, 2, 5), 235);
        clustersManager.addRectangle(new Rectangle(94, 557, 7, 2), 236);
        clustersManager.addRectangle(new Rectangle(210, 553, 82, 8), 237);
        clustersManager.addRectangle(new Rectangle(453, 609, 1, 3), 238);
        clustersManager.addRectangle(new Rectangle(95, 379, 265, 10), 239);
        clustersManager.addRectangle(new Rectangle(302, 412, 8, 7), 240);
        clustersManager.addRectangle(new Rectangle(139, 587, 0, 11), 241);
        clustersManager.addRectangle(new Rectangle(72, 269, 17, 8), 242);
        clustersManager.addRectangle(new Rectangle(360, 518, 88, 10), 243);
        clustersManager.addRectangle(new Rectangle(405, 536, 4, 8), 244);
        clustersManager.addRectangle(new Rectangle(292, 697, 4, 5), 245);
        clustersManager.addRectangle(new Rectangle(315, 414, 223, 10), 246);
        clustersManager.addRectangle(new Rectangle(145, 536, 81, 7), 247);
        clustersManager.addRectangle(new Rectangle(530, 536, 8, 7), 248);
        clustersManager.addRectangle(new Rectangle(82, 557, 6, 5), 249);
        clustersManager.addRectangle(new Rectangle(346, 213, 19, 8), 250);
        clustersManager.addRectangle(new Rectangle(298, 553, 11, 8), 251);
        clustersManager.addRectangle(new Rectangle(526, 330, 12, 11), 252);
        clustersManager.addRectangle(new Rectangle(338, 125, 36, 8), 253);
        clustersManager.addRectangle(new Rectangle(170, 309, 1, 3), 254);
        clustersManager.addRectangle(new Rectangle(182, 677, 90, 10), 255);
        clustersManager.addRectangle(new Rectangle(226, 662, 6, 5), 256);
        clustersManager.addRectangle(new Rectangle(206, 570, 33, 8), 257);
        clustersManager.addRectangle(new Rectangle(196, 302, 25, 8), 258);
        clustersManager.addRectangle(new Rectangle(331, 542, 1, 1), 259);
        clustersManager.addRectangle(new Rectangle(95, 642, 443, 10), 260);
        clustersManager.addRectangle(new Rectangle(464, 523, 6, 5), 261);
        clustersManager.addRectangle(new Rectangle(509, 259, 1, 3), 262);
        clustersManager.addRectangle(new Rectangle(208, 500, 32, 9), 263);
        clustersManager.addRectangle(new Rectangle(428, 505, 6, 5), 264);
        clustersManager.addRectangle(new Rectangle(454, 521, 9, 5), 265);
        clustersManager.addRectangle(new Rectangle(72, 518, 260, 10), 266);
        clustersManager.addRectangle(new Rectangle(211, 159, 2, 11), 267);
        clustersManager.addRectangle(new Rectangle(403, 612, 1, 1), 268);
        clustersManager.addRectangle(new Rectangle(145, 587, 57, 10), 269);
        clustersManager.addRectangle(new Rectangle(191, 552, 4, 8), 270);
        clustersManager.addRectangle(new Rectangle(294, 417, 6, 5), 271);
    }
}
