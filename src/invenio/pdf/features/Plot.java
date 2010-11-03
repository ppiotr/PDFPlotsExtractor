/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.pdf.core.Operation;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *  A class describing single plot appearing inside a document.
 *  Among others, following informations are stored:
 *      - coordinates of the plot inside a rendered page.
 *      - resolution of the rendering page
 *      - list of PDF operations leading to the creation of a plot
 *      - caption
 *      - list of references from inside the text
 * @author piotr
 */
public class Plot {

    private Rectangle boundary; // The boundary inside the rendered page
    private Rectangle pageBoundary; // The boundary of the rendered page
    private String caption; // Detected caption of the plot
    private List<String> references; // a list of references to the plot, from the article text
    private List<Operation> operations; // a list of all the PDF operations creating this plot
    private int pageNumber;

    /**
     * A default constructor - creates a plot descriptor holding empty information
     */
    public Plot() {
        this.references = new ArrayList<String>();
        this.caption = "";
        this.boundary = new Rectangle(0, 0, 0, 0);
        this.pageBoundary = new Rectangle(0, 0, 0, 0);
        this.operations = new LinkedList<Operation>();
    }

    /**
     * Sets the page on which the plot is located
     * @param num
     */
    public void setPageNumber(int num) {
        this.pageNumber = num;
    }

    /**
     * returns the number of the page on which the plot is located
     * @return
     */
    public int setPageNumber(){
        return this.pageNumber;
    }

    /**
     * Return the boundary of the plot inside the rendered page;
     * @return
     */
    public Rectangle getBoundary() {
        return this.boundary;
    }

    /**
     * Sets the boundary of a described plot in the coordinates of the rendered page
     * @param rec The rectangle describing the new boundary
     */
    public void setBoundary(Rectangle rec) {
        this.boundary = rec;
    }

    /**
     * Sets the caption of the plot
     * @param cap
     */
    public void setCaption(String cap) {
        this.caption = cap;
    }

    /**
     * Return the caption of teh plot
     * @return
     */
    public String getCaption() {
        return this.caption;
    }

    /**
     * Registers a new CS operation (An operation present in the PDF content stream)
     * that is associated with the currently processed plot
     * @param op
     * @return
     */
    public void addOperation(Operation op) {
        this.operations.add(op);
    }

    /**
     * Return all the CS operations (Operations forming the PDF content stream)
     * associated with the current plot.
     * @return list of CSOperation instances
     */
    public List<Operation> getOperations() {
        return this.operations;
    }

    /**
     * Add the entire list of operations constituting the plot
     * @param operations list of CSOperation instances
     */
    public void addOperations(List<Operation> operations) {
        this.operations.addAll(operations);
    }
}
