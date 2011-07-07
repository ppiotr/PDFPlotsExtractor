/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.features;

import invenio.pdf.core.IPDFDocumentFeature;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    /** Return an unstructured list of all plots of a given document
     *
     */
    public List<Plot> getPlots(){
        LinkedList<Plot> result = new LinkedList<Plot>();
        for (List<Plot> partialList : this.plots){
            result.addAll(partialList);
        }
        return result;
    }

    @Override
    public void saveToXml(Document document, Element rootElement) throws FileNotFoundException, Exception {
        // first flatten the array
        LinkedList<Plot> toWrite = new LinkedList<Plot>();
        for (List<Plot> partial: this.plots){
            toWrite.addAll(partial);
        }
        PlotsWriter.writePlotsMetadata(document, rootElement, toWrite);
    }
}
