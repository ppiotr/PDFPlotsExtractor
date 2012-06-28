/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.Pair;
import invenio.pdf.core.IPDFDocumentFeature;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** 
 *  A class allowing to manage plots together with plot candidates 
 * @author piotr
 */
public class Plots implements IPDFDocumentFeature {

    public static final String featureName = "Plots";
    public ArrayList<List<Plot>> plots;
    // pageNum -> list of Plots

    public Plots() {
        this.plots = new ArrayList<List<Plot>>();
    }

    /** Return an unstructured list of all plots of a given document
     *  we do not return plot candidates
     *
     */
    public List<Plot> getPlots() {
        LinkedList<Plot> result = new LinkedList<Plot>();
        for (List<Plot> partialList : this.plots) {
            for (Plot plot : partialList) {
                if (plot.isApproved) {
                    result.add(plot);
                }
            }

        }
        return result;
    }

    /** returns all stored plot candidates
     * 
     * @return 
     */
    public List<Plot> getPlotcandidates() {
        LinkedList<Plot> result = new LinkedList<Plot>();
        for (List<Plot> partialList : this.plots) {
            result.addAll(partialList);
        }
        return result;
    }

    @Override
    public void saveToXml(Document document, Element rootElement) throws FileNotFoundException, Exception {
        // first flatten the array
        LinkedList<Plot> toWrite = new LinkedList<Plot>();
        for (List<Plot> partial : this.plots) {
            toWrite.addAll(partial);
        }
        PlotsWriter.writePlotsMetadata(document, rootElement, toWrite);
    }

    public List<Plot> getPlotCandidatesByPage(int pageNum) {
        return this.plots.get(pageNum);
    }
}
