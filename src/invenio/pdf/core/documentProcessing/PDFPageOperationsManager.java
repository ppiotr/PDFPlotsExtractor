package invenio.pdf.core.documentProcessing;

import java.util.HashMap;
import java.util.Set;

import de.intarsys.pdf.content.CSOperation;
import invenio.pdf.core.ExtractorLogger;
import invenio.pdf.core.GraphicalOperation;
import invenio.pdf.core.PDFObjects.ContentStreamStateMachine;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.TextOperation;
import invenio.pdf.core.TransformationOperation;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * Managing operations from the document operations stream (remembering attributes for them).
 * Contains informations about:
 *    current operation : operation that has been last started
 *    operation boundaries : rectangles affected by the execution of an operation
 * 
 * @author Piotr Praczyk
 *
 */
class PDFPageOperationsManager {

    private CSOperation currentOperation; // currently performed operation
    private HashMap<CSOperation, Rectangle> operationBoundaries; // Boundries of areas affected by PS operations
    private HashSet<CSOperation> textOperations; // operations drawing the text
    private List<CSOperation> operations; // operations drawing the text
    //   private HashMap<CSOperation, List<String>> renderingMethods; // Methods
    // called in order to execute an operation
    private Rectangle pageBoundary;
    private Map<CSOperation, int[]> operationTextPositions;
    private String pageText;
    public ContentStreamStateMachine contentStreamStateMachine;

    /**
     * Creates a new instance of the page manager for a page of a given boundary
     * (rectangle starting at 0,0 and having some non-zero width and height)
     *
     * @param pgBound a rectangle defining the page boundary
     */
    public PDFPageOperationsManager(Rectangle pgBound) {
        this.currentOperation = null;
        this.operationBoundaries = new HashMap<CSOperation, Rectangle>();
        this.textOperations = new HashSet<CSOperation>();
        //     this.renderingMethods = new HashMap<CSOperation, List<String>>();
        this.operations = new ArrayList<CSOperation>();
        this.pageBoundary = pgBound;
        this.operationTextPositions = new HashMap<CSOperation, int[]>();
        this.pageText = "";

        this.contentStreamStateMachine = new ContentStreamStateMachine();
    }

    /**
     * Returns the boundary of the page described by this manager
     * @return
     */
    public Rectangle getPageBoundary() {
        return this.pageBoundary;
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
//        List<String> methods = this.renderingMethods.get(op);
//        if (methods == null) {
//            this.renderingMethods.put(op, new ArrayList<String>());
//        }
//        this.renderingMethods.get(op).add(method);
    }

    public List<String> getRenderingMethods(CSOperation op) {
        /**
         * Returns methods used to render a particular operation
         */
        //return this.renderingMethods.get(op);\
        return new LinkedList<String>();
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

    public void setOperationBoundary(CSOperation op, Rectangle rec) {
        this.operationBoundaries.put(op, new Rectangle(rec));
        if (rec.x == 109 && rec.y == 840 && rec.width == 1015 && rec.height == 610) {
            System.out.println("Wiedz, ze cos sie dzieje");
        }
    }

    public Rectangle getOperationBoundary(CSOperation op) {
        return this.operationBoundaries.get(op); // will return null if key is not present
    }

    public void extendCurrentOperationBoundary(Rectangle rec) {
        /**
         *  Extend the boundary of a current operation by a given rectangle.
         *  (find a minimal rectangle containing current boundary and the rectangle passed as a parameter)
         */
        Rectangle currentBoundary = this.getOperationBoundary(this.getCurrentOperation());
        
        if (currentBoundary != null) {
            Rectangle bnd = currentBoundary.createUnion(rec.getBounds2D()).getBounds();
            if (bnd.x == 109 && bnd.y == 840 && bnd.width == 1015 && bnd.height == 610) {
                System.out.println("Wiedz, ze cos sie dzieje");
            }
            this.setOperationBoundary(this.getCurrentOperation(), bnd);
        } else {
            this.setOperationBoundary(this.getCurrentOperation(), new Rectangle(rec));
            if (rec.x == 109 && rec.y == 840 && rec.width == 1015 && rec.height == 610) {
                System.out.println("Wiedz, ze cos sie dzieje");
            }
        }

        this.contentStreamStateMachine.extendCurrentBoundary(rec);
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

    /**
     * transforms the current instance into the instance of PDFPageManager
     * @return
     */
    public PDFPageManager transformToPDFPageManager() {
        PDFPageManager result = new PDFPageManager();
        result.setPageBoundary(this.getPageBoundary());
        result.setPageText(this.getPageText());
        for (CSOperation op : this.operations) {
            if (this.isTextOperation(op)) {
                TextOperation newOp = new TextOperation(op,
                        this.getOperationBoundary(op));
                Rectangle bd = this.getOperationBoundary(op);
                if (bd != null) {
                    int[] substrInd = this.getOperationTextIndices(op);
                    if (substrInd == null || this.getPageText() == null) {
                        ExtractorLogger.logMessage(0, "FATAL: failed to extract the page text");
                    }
                    String operationString = this.getPageText().substring(
                            substrInd[0], substrInd[1]);
                    newOp.setText(operationString);
                    newOp.setTextRange(substrInd[0], substrInd[1]);
                    result.addTextOperation(newOp);
                    // check if text operation fits inside the page !
                }

            } else if (this.isGraphicalOperation(op)) {
                GraphicalOperation newOp = new GraphicalOperation(op, this.getOperationBoundary(op));
                result.addGraphicalOperation(newOp);
            } else {
                TransformationOperation newOp = new TransformationOperation(op);
                result.addTransformationOperation(newOp);
            }
        }
        return result;
    }

    private boolean isTextOperation(CSOperation op) {
        return this.textOperations.contains(op);
    }

    public boolean isGraphicalOperation(CSOperation op) {
        return (this.getOperationBoundary(op) != null) && (!this.isTextOperation(op));
    }

    public void setOperationTextIndices(CSOperation operation, int[] i) {
        this.operationTextPositions.put(operation, i);
    }

    public int[] getOperationTextIndices(CSOperation operation) {
        return this.operationTextPositions.get(operation);
    }

    public void setPageText(String text) {
        this.pageText = text;
    }

    public String getPageText() {
        return this.pageText;
    }
};
