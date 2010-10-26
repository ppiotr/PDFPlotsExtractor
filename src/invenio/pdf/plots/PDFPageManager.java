package invenio.pdf.plots;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Set;

import de.intarsys.pdf.content.CSOperation;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * Managing operations from the document operations stream (remembering attributes for them).
 * Contains informations about:
 *    current operation : operation that has been last started
 *    operation boundries : rectangles affected by the execution of an operation
 * 
 * @author Piotr Praczyk
 *
 */
public class PDFPageManager {

    private CSOperation currentOperation; // currently performed operation
    private HashMap<CSOperation, Rectangle2D> operationBoundaries2D; // Boundries of areas affected by PS operations
    private HashSet<CSOperation> textOperations; // operations drawing the text
    private HashSet<CSOperation> operations; // operations drawing the text
    private HashMap<CSOperation, List<String>> renderingMethods; // Methods
    // called in order to execute an operation
    private BufferedImage renderedPage; // the image of a completely rendered page
    private Rectangle pageBoundary;

    /**
     * Creates a new instance of the page manager for a page of a given boundary
     * (rectangle starting at 0,0 and having some non-zero width and height)
     *
     * @param pgBound a rectangle defining the page boundary
     */
    public PDFPageManager(Rectangle pgBound) {
        this.currentOperation = null;
        this.operationBoundaries2D = new HashMap<CSOperation, Rectangle2D>();
        this.textOperations = new HashSet<CSOperation>();
        this.renderingMethods = new HashMap<CSOperation, List<String>>();
        this.operations = new HashSet<CSOperation>();
        this.pageBoundary = pgBound;
    }

    /**
     * Returns the boundary of the page described by this manager
     * @return
     */
    public Rectangle getPageBoundary() {
        return this.pageBoundary;
    }

    public BufferedImage getRenderedPage() {
        return this.renderedPage;
    }

    public void setRenderedPage(BufferedImage im) {
        this.renderedPage = im;
    }

    public void addRenderingMethod(String method) {
        /**
         *  Add a rendering method to the current operation
         */
        this.addRenderingMethod(this.getCurrentOperation(), method);
    }

    public Set<CSOperation> getOperations() {
        /**return all the operations managed by this manager*/
        return new HashSet<CSOperation>(this.operations);
    }

    public void addOperation(CSOperation op) {
        /**
         * Add an operation to the set of processed operations
         */
        this.operations.add(op);
    }

    public void addRenderingMethod(CSOperation op, String method) {
        List<String> methods = this.renderingMethods.get(op);
        if (methods == null) {
            this.renderingMethods.put(op, new ArrayList<String>());
        }
        this.renderingMethods.get(op).add(method);
    }

    public List<String> getRenderingMethods(CSOperation op) {
        /**
         * Returns methods used to render a particular operation
         */
        return this.renderingMethods.get(op);
    }

    public void setCurrentOperation(CSOperation op) {
        this.currentOperation = op;
        this.addOperation(op);
    }

    public void unsetCurrentOperation() {
        /**
         * Remove the current operation (none is being performed)
         */
        this.currentOperation = null;
    }

    public CSOperation getCurrentOperation() {
        /**
         * get the last started CSOperation
         */
        return this.currentOperation;
    }

    public void setOperationBoundary2D(CSOperation op, Rectangle2D rec) {
        this.operationBoundaries2D.put(op, rec);
    }

    public Rectangle2D getOperationBoundary2D(CSOperation op) {
        return this.operationBoundaries2D.get(op); // will return null if key is not present
    }

    public void extendCurrentOperationBoundary2D(Rectangle2D rec) {
        /**
         *  Extend the boundary of a current operation by a given rectangle.
         *  (find a minimal rectangle containing current boundary and the rectangle passed as a parameter)
         */
        Rectangle2D currentBoundary = this.getOperationBoundary2D(this.getCurrentOperation());
        if (currentBoundary != null) {
            this.setOperationBoundary2D(this.getCurrentOperation(), currentBoundary.createUnion(rec));
        } else {
            this.setOperationBoundary2D(this.getCurrentOperation(), rec);
        }
    }

    public void addTextOperation(CSOperation op) {
        /**
         * Mark an operation as a tree operation
         */
        this.textOperations.add(op);
    }

    public Set<CSOperation> getTextOperations() {
        /**
         * Returns all the operations causing the text to be drawn
         */
        return this.textOperations;
    }
};
