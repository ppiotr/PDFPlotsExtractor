/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.features;

import invenio.pdf.core.IPDFDocumentFeature;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author piotr
 */
public class Plots implements IPDFDocumentFeature{
    public static final String featureName = "Plots";
    public ArrayList<List<Plot>> plots;

    public Plots(){
        this.plots = new ArrayList<List<Plot>>();
    }
}
