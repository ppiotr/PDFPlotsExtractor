/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author piotr
 */
public class PDFPageManager {

    private List<Operation> operations;
    private Set<Operation> textOperations; // we need to be able to quicly acces the information if the opretation is graphical
    private Set<Operation> graphicalOperations;
    private Set<Operation> transformationOperations;
    private Rectangle pageBoundary;
    private BufferedImage renderedPage;

    // at some point we might need a mapping operation -> index
    public PDFPageManager() {
        this.operations = new ArrayList<Operation>();
        this.textOperations = new HashSet<Operation>();
        this.graphicalOperations = new HashSet<Operation>();
        this.transformationOperations = new HashSet<Operation>();
    }

    public void addTextOperation(TextOperation newOp) {
        this.addOperation(newOp);
        this.textOperations.add(newOp);
    }

    public void addOperation(Operation newOp) {
        this.operations.add(newOp);
    }

    public List<Operation> getOperations(){
        return this.operations;
    }


    public void addGraphicalOperation(GraphicalOperation newOp) {
        this.addOperation(newOp);
        this.graphicalOperations.add(newOp);
    }

    public void addTransformationOperation(TransformationOperation newOp) {
        this.addOperation(newOp);
        this.transformationOperations.add(newOp);
    }

    public Set<Operation> getGraphicalOperations() {
        return this.graphicalOperations;
    }

    public Rectangle getPageBoundary() {
        return this.pageBoundary;
    }

    public void setPageBoundary(Rectangle bd) {
        this.pageBoundary = bd;
    }

    public Set<Operation> getTextOperations() {
        return this.textOperations;
    }

    public BufferedImage getRenderedPage(){
        return this.renderedPage;
    }

    public void setRenderedPage(BufferedImage rp){
        this.renderedPage = rp;
    }
}
