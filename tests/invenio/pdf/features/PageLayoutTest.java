/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import java.util.Random;
import java.util.Comparator;
import invenio.common.Pair;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import invenio.common.Images;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
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
public class PageLayoutTest {

    public PageLayoutTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private PageLayoutProvider provider = new PageLayoutProvider();
    private Random random = new Random();
    
    @Before
    public void setUp() {
        /**
         *  separators must be at least 1% of height/width
         *  left/right margins are considered 0.2 of the width
         *  column separators have to be at least 0.3 of the page height
         */
        this.provider = new PageLayoutProvider(0.01, 0.005, 0.3, 0.2);
    }

    /* Creation of a set of sample rasters */
    /**
     * Create the simplest case of a two column layout
     * @return
     */
    private BufferedImage createSampleImage1() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);
        graphics.fillRect(10, 10, 40, 180);
        graphics.fillRect(51, 10, 40, 180);

        return image;
    }

    private BufferedImage createSampleImage2() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);
        graphics.fillRect(10, 10, 81, 40);
        graphics.fillRect(10, 51, 40, 140);
        graphics.fillRect(51, 51, 40, 140);


        return image;
    }

    private BufferedImage createSampleImage3() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);
        graphics.fillRect(10, 10, 40, 140);
        graphics.fillRect(51, 10, 40, 140);
        graphics.fillRect(10, 151, 81, 40);

        return image;
    }

    private BufferedImage createSampleImage4() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);

        graphics.fillRect(10, 10, 81, 40);
        graphics.fillRect(10, 51, 40, 100);
        graphics.fillRect(51, 51, 40, 100);
        graphics.fillRect(10, 152, 81, 40);

        return image;
    }

    /**
     * The case of an area that requires the second algorithm to run
     * @return
     */
    private BufferedImage createSampleImage5() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);


        graphics.fillRect(10, 10, 40, 45);
        graphics.fillRect(50, 10, 41, 40);

        graphics.fillRect(10, 56, 40, 95);
        graphics.fillRect(51, 51, 40, 100);
        graphics.fillRect(10, 152, 81, 40);

        return image;
    }

    private BufferedImage createSampleImage6() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);


        graphics.fillRect(10, 10, 40, 45);
        graphics.fillRect(50, 10, 41, 40);

        graphics.fillRect(10, 56, 40, 90);
        graphics.fillRect(51, 51, 40, 100);

        graphics.fillRect(10, 147, 40, 45);
        graphics.fillRect(50, 152, 41, 40);

        return image;
    }

    private BufferedImage createSampleImage7() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);


        graphics.fillRect(10, 10, 40, 90);
        graphics.fillRect(50, 10, 41, 40);


        graphics.fillRect(51, 51, 40, 100);

        graphics.fillRect(10, 101, 40, 91);
        graphics.fillRect(50, 152, 41, 40);

        return image;
    }

    private BufferedImage createSampleImage8() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);
        graphics.fillRect(10, 10, 40, 182);
        graphics.fillRect(50, 10, 41, 40);
        graphics.fillRect(51, 51, 40, 100);
        graphics.fillRect(50, 152, 41, 40);

        return image;
    }

    private BufferedImage createSampleImage9() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);

        graphics.fillRect(10, 10, 41, 40);
        graphics.fillRect(51, 10, 40, 45);

        graphics.fillRect(10, 51, 40, 100);
        graphics.fillRect(51, 56, 40, 95);

        graphics.fillRect(10, 152, 81, 40);

        return image;
    }

    private BufferedImage createSampleImage10() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);

        graphics.fillRect(10, 10, 41, 40);
        graphics.fillRect(10, 51, 40, 100);
        graphics.fillRect(10, 152, 81, 40);
        graphics.fillRect(51, 10, 40, 141);
        return image;
    }

    private BufferedImage createSampleImage11() {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);

        graphics.fillRect(10, 10, 81, 40);

        graphics.fillRect(10, 51, 40, 100);
        graphics.fillRect(51, 51, 40, 141);

        graphics.fillRect(10, 152, 41, 40);


        return image;
    }
    // now cases with having two columns above each other

    private BufferedImage createSampleImage12() {

        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);

        graphics.fillRect(10, 10, 40, 85);
        graphics.fillRect(51, 10, 39, 85);

        graphics.fillRect(10, 96, 80, 10);

        graphics.fillRect(10, 107, 40, 85);
        graphics.fillRect(51, 107, 39, 85);


        return image;
    }

    private BufferedImage createSampleImage13() {

        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);
        graphics.fillRect(10, 10, 40, 85);
        graphics.fillRect(51, 10, 39, 85);
        graphics.fillRect(10, 96, 80, 11);
        graphics.fillRect(10, 107, 40, 85);
        graphics.fillRect(51, 107, 39, 85);

        return image;
    }

    /** Simple three columns */
    private BufferedImage createSampleImage14() {

        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        // first creating a white background
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, 100, 200);

        // now the painting -> in order to stimulate data we paint black rectangles
        graphics.setColor(Color.BLACK);
        graphics.fillRect(10, 10, 40, 85);
        graphics.fillRect(51, 10, 39, 85);
        graphics.fillRect(10, 96, 80, 11);
        graphics.fillRect(10, 107, 40, 85);
        graphics.fillRect(51, 107, 39, 85);

        return image;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void writeSampleRastersToFile() {
        try {
            Images.writeImageToFile(createSampleImage1(), "/home/piotr/pdf/sample1.png");
            Images.writeImageToFile(createSampleImage2(), "/home/piotr/pdf/sample2.png");
            Images.writeImageToFile(createSampleImage3(), "/home/piotr/pdf/sample3.png");
            Images.writeImageToFile(createSampleImage4(), "/home/piotr/pdf/sample4.png");
            Images.writeImageToFile(createSampleImage5(), "/home/piotr/pdf/sample5.png");
            Images.writeImageToFile(createSampleImage6(), "/home/piotr/pdf/sample6.png");
            Images.writeImageToFile(createSampleImage7(), "/home/piotr/pdf/sample7.png");
            Images.writeImageToFile(createSampleImage8(), "/home/piotr/pdf/sample8.png");
            Images.writeImageToFile(createSampleImage9(), "/home/piotr/pdf/sample9.png");
            Images.writeImageToFile(createSampleImage10(), "/home/piotr/pdf/sample10.png");
            Images.writeImageToFile(createSampleImage11(), "/home/piotr/pdf/sample11.png");

            Images.writeImageToFile(createSampleImage12(), "/home/piotr/pdf/sample12.png");
            Images.writeImageToFile(createSampleImage13(), "/home/piotr/pdf/sample13.png");


        } catch (IOException ex) {
            Logger.getLogger(PageLayoutTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private BufferedImage annotateImageWithColumns(BufferedImage img, List<Rectangle> columns) {
        Graphics graphics = img.getGraphics();
        graphics.setColor(Color.CYAN);
        for (Rectangle column : columns) {
            graphics.drawRect(column.x + 1, column.y + 1, column.width - 2, column.height - 2);
        }
        return img;
    }

    private BufferedImage annotateImageWithAreas(BufferedImage img, List<List<Rectangle>> areas) {
        Color[] colours = new Color[]{Color.CYAN, Color.RED, Color.GREEN, Color.BLUE, Color.PINK, Color.YELLOW, Color.darkGray};
        int colourIndex = 0;
        Graphics graphics = img.getGraphics();

        for (List<Rectangle> area : areas) {
            if (colourIndex < colours.length) {
                graphics.setColor(colours[colourIndex]);
            } else {
                // no predefiend colour-> generatign a random one
                
                graphics.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            }

            colourIndex++;


            for (Rectangle rec : area) {
                graphics.drawRect(rec.x + 1, rec.y + 1, rec.width - 2, rec.height - 2);
            }
        }
        return img;
    }

    /**
     *  Detect preliminary columns and annotate image
     */
    private BufferedImage detectAndAnnotate(BufferedImage img) {
        List columns = provider.getPageColumns(img.getData());
        return annotateImageWithColumns(img, columns);
    }

    private BufferedImage detectAdvancedAndAnnotate(BufferedImage img) {
        List<Rectangle> verticalSeparators = new LinkedList<Rectangle>();
        List<Rectangle> columns = provider.getPageColumns(img.getData(), verticalSeparators);

        PageLayout layout = provider.fixHorizontalSeparators(columns, verticalSeparators, img.getData());
        return annotateImageWithAreas(img, layout.areas);
    }

    /**
     * Test of the detection of columns - the method detecting vertical separator
     * not caring about the
     */
    @Test
    public void testPreliminaryColumnDetection() {
        try {
            Images.writeImageToFile(detectAndAnnotate(createSampleImage1()), "/home/piotr/pdf/sample1_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage2()), "/home/piotr/pdf/sample2_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage3()), "/home/piotr/pdf/sample3_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage4()), "/home/piotr/pdf/sample4_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage5()), "/home/piotr/pdf/sample5_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage6()), "/home/piotr/pdf/sample6_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage7()), "/home/piotr/pdf/sample7_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage8()), "/home/piotr/pdf/sample8_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage9()), "/home/piotr/pdf/sample9_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage10()), "/home/piotr/pdf/sample10_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage11()), "/home/piotr/pdf/sample11_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage12()), "/home/piotr/pdf/sample12_pdetected.png");
            Images.writeImageToFile(detectAndAnnotate(createSampleImage13()), "/home/piotr/pdf/sample13_pdetected.png");
            //List<Rectangle> getPageColumns(Raster raster)
        } catch (IOException ex) {
            Logger.getLogger(PageLayoutTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testAdvancedColumnDetection() {
        try {
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage1()), "/home/piotr/pdf/sample1_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage2()), "/home/piotr/pdf/sample2_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage3()), "/home/piotr/pdf/sample3_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage4()), "/home/piotr/pdf/sample4_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage5()), "/home/piotr/pdf/sample5_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage6()), "/home/piotr/pdf/sample6_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage7()), "/home/piotr/pdf/sample7_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage8()), "/home/piotr/pdf/sample8_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage9()), "/home/piotr/pdf/sample9_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage10()), "/home/piotr/pdf/sample10_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage11()), "/home/piotr/pdf/sample11_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage12()), "/home/piotr/pdf/sample12_pdetecteda.png");
            Images.writeImageToFile(detectAdvancedAndAnnotate(createSampleImage13()), "/home/piotr/pdf/sample13_pdetecteda.png");
            //List<Rectangle> getPageColumns(Raster raster)
        } catch (IOException ex) {
            Logger.getLogger(PageLayoutTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ////// testing the function for updating current horizontal separators
    @Test
    public void updateHorizontalSeparators1() {
        HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>> existingSeparators = new HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>>();
        int consideredY = 10;

        TreeMap<Rectangle, Pair<Integer, Integer>> currentLine =
                new TreeMap<Rectangle, Pair<Integer, Integer>>(new Comparator<Rectangle>() {

            @Override
            public int compare(Rectangle t, Rectangle t1) {
                return t.x - t1.x;
            }
        });

        existingSeparators.put(consideredY, currentLine);
//        currentLine.put(new Rectangle(10, 10, 30, 0),
//                new Pair<Integer, Integer>(-1, 1));
//        
        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(0, 10, 100, 0), 1, -1);
        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(0, 10, 50, 0), -1, 2);
        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(50, 10, 50, 0), -1, 3);
        // now assertions

        Rectangle r1 = new Rectangle(0, 10, 50, 0);
        Rectangle r2 = new Rectangle(50, 10, 50, 0);

        assertEquals(2, currentLine.size());

        assertTrue(currentLine.containsKey(r1));
        assertTrue(currentLine.containsKey(r2));

        assertEquals(1, (int) currentLine.get(r1).first);
        assertEquals(2, (int) currentLine.get(r1).second);

        assertEquals(1, (int) currentLine.get(r2).first);
        assertEquals(3, (int) currentLine.get(r2).second);

        //provider.updateHorizontalSeparators(existingSeparators, new Rectangle(12, 10, 4, 0), 45 , -1);

    }

    @Test
    public void updateHorizontalSeparators2() {
        HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>> existingSeparators = new HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>>();
        int consideredY = 10;

        TreeMap<Rectangle, Pair<Integer, Integer>> currentLine =
                new TreeMap<Rectangle, Pair<Integer, Integer>>(new Comparator<Rectangle>() {

            @Override
            public int compare(Rectangle t, Rectangle t1) {
                return t.x - t1.x;
            }
        });

        existingSeparators.put(consideredY, currentLine);

        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(0, 10, 50, 0), -1, 2);
        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(50, 10, 50, 0), -1, 3);
        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(0, 10, 100, 0), 1, -1);
        // now assertions

        Rectangle r1 = new Rectangle(0, 10, 50, 0);
        Rectangle r2 = new Rectangle(50, 10, 50, 0);

        assertEquals(2, currentLine.size());

        assertTrue(currentLine.containsKey(r1));
        assertTrue(currentLine.containsKey(r2));

        assertEquals(1, (int) currentLine.get(r1).first);
        assertEquals(2, (int) currentLine.get(r1).second);

        assertEquals(1, (int) currentLine.get(r2).first);
        assertEquals(3, (int) currentLine.get(r2).second);

    }

    @Test
    public void updateHorizontalSeparators3() {
        HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>> existingSeparators = new HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>>();
        int consideredY = 10;

        TreeMap<Rectangle, Pair<Integer, Integer>> currentLine =
                new TreeMap<Rectangle, Pair<Integer, Integer>>(new Comparator<Rectangle>() {

            @Override
            public int compare(Rectangle t, Rectangle t1) {
                return t.x - t1.x;
            }
        });

        existingSeparators.put(consideredY, currentLine);

        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(0, 10, 50, 0), -1, 2);
        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(0, 10, 100, 0), 1, -1);
        // now assertions

        Rectangle r1 = new Rectangle(0, 10, 50, 0);
        Rectangle r2 = new Rectangle(50, 10, 50, 0);

        assertEquals(2, currentLine.size());

        assertTrue(currentLine.containsKey(r1));
        assertTrue(currentLine.containsKey(r2));

        assertEquals(1, (int) currentLine.get(r1).first);
        assertEquals(2, (int) currentLine.get(r1).second);

        assertEquals(1, (int) currentLine.get(r2).first);
        assertEquals(-1, (int) currentLine.get(r2).second);
    }

    @Test
    public void updateHorizontalSeparators4() {
        HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>> existingSeparators = new HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>>();
        int consideredY = 10;

        TreeMap<Rectangle, Pair<Integer, Integer>> currentLine =
                new TreeMap<Rectangle, Pair<Integer, Integer>>(new Comparator<Rectangle>() {

            @Override
            public int compare(Rectangle t, Rectangle t1) {
                return t.x - t1.x;
            }
        });

        existingSeparators.put(consideredY, currentLine);

        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(30, 10, 30, 0), -1, 2);
        provider.updateHorizontalSeparators(existingSeparators, new Rectangle(0, 10, 100, 0), 1, -1);
        // now assertions

        Rectangle r1 = new Rectangle(0, 10, 30, 0);
        Rectangle r2 = new Rectangle(30, 10, 30, 0);
        Rectangle r3 = new Rectangle(60, 10, 40, 0);

        assertEquals(3, currentLine.size());

        assertTrue(currentLine.containsKey(r1));
        assertTrue(currentLine.containsKey(r2));
        assertTrue(currentLine.containsKey(r3));


        assertEquals(1, (int) currentLine.get(r1).first);
        assertEquals(-1, (int) currentLine.get(r1).second);

        assertEquals(1, (int) currentLine.get(r2).first);
        assertEquals(2, (int) currentLine.get(r2).second);

        assertEquals(1, (int) currentLine.get(r3).first);
        assertEquals(-1, (int) currentLine.get(r3).second);
    }

    @Test
    public void updateCurrentColumnsTest() {

        ArrayList<Integer> separatorPoints = new ArrayList<Integer>();
        separatorPoints.add(20);
        separatorPoints.add(30);

        ArrayList<Rectangle> columns = new ArrayList<Rectangle>();
        ArrayList<Rectangle> startedColumns = new ArrayList<Rectangle>();
        startedColumns.add(new Rectangle(0, 0, 0, 100));

        startedColumns = provider.updateCurrentColumns(
                columns, separatorPoints, startedColumns, 10);

        // now we should have three entries in current columns
        // the y axis should be slited in a following way:
        // (0, 20) (20, 30) (30, 100)
        assertEquals(3, startedColumns.size());
        assertEquals(0, startedColumns.get(0).y);
        assertEquals(0, startedColumns.get(0).x);
        assertEquals(20, startedColumns.get(0).height);
        assertEquals(20, startedColumns.get(1).y);
        assertEquals(10, startedColumns.get(1).x);
        assertEquals(10, startedColumns.get(1).height);
        assertEquals(30, startedColumns.get(2).y);
        assertEquals(0, startedColumns.get(2).x);
        assertEquals(70, startedColumns.get(2).height);

        // checking the columns that have been already successfully detected
        assertEquals(1, columns.size());
        assertEquals(20, columns.get(0).y);
        assertEquals(0, columns.get(0).x);
        assertEquals(10, columns.get(0).height);
        assertEquals(10, columns.get(0).width);

        // now cutting all the intervals by providing a separator going through
        // the entire page height
        separatorPoints.clear();
        separatorPoints.add(0);
        separatorPoints.add(100);

        startedColumns = provider.updateCurrentColumns(
                columns, separatorPoints, startedColumns, 20);

        assertEquals(1, startedColumns.size());
        assertEquals(20, startedColumns.get(0).x);
        assertEquals(0, startedColumns.get(0).y);
        assertEquals(100, startedColumns.get(0).height);

        assertEquals(4, columns.size());

    }

    @Test
    public void updateCurrentColumnsSecondTest() {
        ArrayList<Integer> separatorPoints = new ArrayList<Integer>();
        separatorPoints.add(2);
        separatorPoints.add(3);
        separatorPoints.add(4);
        separatorPoints.add(6);

        ArrayList<Rectangle> columns = new ArrayList<Rectangle>();
        ArrayList<Rectangle> startedColumns = new ArrayList<Rectangle>();
        startedColumns.add(new Rectangle(0, 0, 0, 10));

        startedColumns = provider.updateCurrentColumns(
                columns, separatorPoints, startedColumns, 10);

        assertEquals(5, startedColumns.size());
        assertEquals(0, startedColumns.get(0).y);
        assertEquals(0, startedColumns.get(0).x);
        assertEquals(2, startedColumns.get(0).height);
        assertEquals(2, startedColumns.get(1).y);
        assertEquals(10, startedColumns.get(1).x);
        assertEquals(1, startedColumns.get(1).height);
        assertEquals(3, startedColumns.get(2).y);
        assertEquals(0, startedColumns.get(2).x);
        assertEquals(1, startedColumns.get(2).height);
        assertEquals(4, startedColumns.get(3).y);
        assertEquals(10, startedColumns.get(3).x);
        assertEquals(2, startedColumns.get(3).height);
        assertEquals(6, startedColumns.get(4).y);
        assertEquals(0, startedColumns.get(4).x);
        assertEquals(4, startedColumns.get(4).height);
        assertEquals(2, columns.size());


        separatorPoints.clear();
        separatorPoints.add(0);
        separatorPoints.add(7);
        startedColumns = provider.updateCurrentColumns(
                columns, separatorPoints, startedColumns, 11);

        assertEquals(2, startedColumns.size());
        assertEquals(0, startedColumns.get(0).y);
        assertEquals(11, startedColumns.get(0).x);
        assertEquals(7, startedColumns.get(0).height);
        assertEquals(7, startedColumns.get(1).y);
        assertEquals(0, startedColumns.get(1).x);
        assertEquals(3, startedColumns.get(1).height);
        assertEquals(7, columns.size());

        separatorPoints.clear();
        separatorPoints.add(4);
        separatorPoints.add(10);
        startedColumns = provider.updateCurrentColumns(
                columns, separatorPoints, startedColumns, 12);

        assertEquals(2, startedColumns.size());
        assertEquals(0, startedColumns.get(0).y);
        assertEquals(11, startedColumns.get(0).x);
        assertEquals(4, startedColumns.get(0).height);
        assertEquals(4, startedColumns.get(1).y);
        assertEquals(12, startedColumns.get(1).x);
        assertEquals(6, startedColumns.get(1).height);
        assertEquals(9, columns.size());

        separatorPoints.clear();
        separatorPoints.add(0);
        separatorPoints.add(10);
        startedColumns = provider.updateCurrentColumns(
                columns, separatorPoints, startedColumns, 20);

        assertEquals(1, startedColumns.size());
        assertEquals(0, startedColumns.get(0).y);
        assertEquals(20, startedColumns.get(0).x);
        assertEquals(10, startedColumns.get(0).height);
        assertEquals(11, columns.size());

    }

    @Test
    public void updateCurrentColumnsImmediateClosureTest() {
        ArrayList<Integer> separatorPoints = new ArrayList<Integer>();
        separatorPoints.add(0);
        separatorPoints.add(12345);

        ArrayList<Rectangle> columns = new ArrayList<Rectangle>();
        ArrayList<Rectangle> startedColumns = new ArrayList<Rectangle>();
        startedColumns.add(new Rectangle(0, 0, 0, 12345));
    }

    // now tests for moving the separators
    @Test
    public void testMoveOnCorrectData() {
        // in this test no move should be performed becasue all the data is correct
    }
}
