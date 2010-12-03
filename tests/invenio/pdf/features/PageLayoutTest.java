/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import java.awt.Rectangle;
import java.util.ArrayList;
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

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void updateCurrentColumnsTest() {
        ArrayList<Integer> separatorPoints = new ArrayList<Integer>();
        separatorPoints.add(20);
        separatorPoints.add(30);

        ArrayList<Rectangle> columns = new ArrayList<Rectangle>();
        ArrayList<Rectangle> startedColumns = new ArrayList<Rectangle>();
        startedColumns.add(new Rectangle(0, 0, 0, 100));

        startedColumns = PageLayoutProvider.updateCurrentColumns(
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

        startedColumns = PageLayoutProvider.updateCurrentColumns(
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

        startedColumns = PageLayoutProvider.updateCurrentColumns(
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
        startedColumns = PageLayoutProvider.updateCurrentColumns(
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
        startedColumns = PageLayoutProvider.updateCurrentColumns(
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
        startedColumns = PageLayoutProvider.updateCurrentColumns(
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
}
