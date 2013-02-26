/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import invenio.common.IterablesUtils;
import java.util.ArrayList;
import junit.framework.TestCase;

/**
 *
 * @author piotr
 */
public class IterableUtilsTest extends TestCase {

    public IterableUtilsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIterables() {

        ArrayList<Integer> l = new ArrayList<Integer>();
        l.add(1);
        l.add(2);
        l.add(3);
        l.add(4);
        l.add(5);
        l.add(6);
        Iterable<Iterable<Integer>> skipN = IterablesUtils.skipN(l, 0);
        
        for (Iterable<Integer> a: skipN){
            System.out.println("New configuration");
            for (Integer x: a){
                System.out.print(" " + x + ", ");
            }
            
            System.out.println();
                    
        }
    }
}
