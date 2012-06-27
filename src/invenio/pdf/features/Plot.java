/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFPageManager;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
    private FigureCaption caption; // Detected caption of the plot
    private List<String> references; // a list of references to the plot, from the article text
    private List<Operation> operations; // a list of all the PDF operations creating this plot
    private int pageNumber;
    private String id; // Plot identifier iside the document
    private HashMap<String, File> files;
    private PDFPageManager pageManager;
    
    private static int identifierFactory = 0;
    
    public Boolean isApproved = false;
    public Boolean isToplevelPlot = true;
    /**
     * A default constructor - creates a plot descriptor holding empty information
     */
    public Plot() {
        this.references = new ArrayList<String>();
        this.caption = new FigureCaption();
        this.boundary = new Rectangle(0, 0, 0, 0);
        this.pageBoundary = new Rectangle(0, 0, 0, 0);
        this.operations = new LinkedList<Operation>();
        this.files = new HashMap<String, File>();
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
    public int getPageNumber() {
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
    public void setCaption(FigureCaption cap) {
        this.caption = cap;
    }

    /**
     * Return the caption of teh plot
     * @return
     */
    public FigureCaption getCaption() {
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

    public File getFile(String type) {
        return this.files.get(type);
    }

    public void addFile(String type, File f) {
        this.files.put(type, f);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String i) {
        this.id = i;
    }

    public PDFPageManager getPageManager() {
        return this.pageManager;
    }

    public void setPageManager(PDFPageManager mng) {
        this.pageManager = mng;
    }
    
    /**
     * Returns an unique identifier generated for a plot. This identifier is 
     * useful in case, we are not able to detect the internal plot identification
     * @return
     */
    public static String getUniqueIdentifier(){
        identifierFactory++;
        return "plot" + identifierFactory;
    }

    int getOperationsNumber() {
        return this.operations.size();
    }
}
