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
public class Figure implements IPDFDocumentFeature {

    public static final String featureName = "Figures";
    public ArrayList<List<FigureCandidate>> figures;
    // pageNum -> list of Plots

    public Figure() {
        this.figures = new ArrayList<List<FigureCandidate>>();
    }

    /** Return an unstructured list of all plots of a given document
     *  we do not return plot candidates
     *
     */
    public List<FigureCandidate> getToplevelPlots() {
        LinkedList<FigureCandidate> result = new LinkedList<FigureCandidate>();
        for (List<FigureCandidate> partialList : this.figures) {
            for (FigureCandidate plot : partialList) {
                if (plot.isApproved && plot.isToplevelPlot) {
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
    public List<FigureCandidate> getPlotcandidates() {
        LinkedList<FigureCandidate> result = new LinkedList<FigureCandidate>();
        for (List<FigureCandidate> partialList : this.figures) {
            result.addAll(partialList);
        }
        return result;
    }

    @Override
    public void saveToXml(Document document, Element rootElement) throws FileNotFoundException, Exception {
        // first flatten the array
        /**LinkedList<FigureCandidate> toWrite = new LinkedList<FigureCandidate>();
        
        for (List<FigureCandidate> partial : this.plots) {
            toWrite.addAll(partial);
        }
        */
        List<FigureCandidate> toWrite = this.getToplevelPlots();
        
        FiguresWriter.writePlotsMetadata(document, rootElement, toWrite);
    }

    public List<FigureCandidate> getFigureCandidatesByPage(int pageNum) {
        return this.figures.get(pageNum);
    }
}
