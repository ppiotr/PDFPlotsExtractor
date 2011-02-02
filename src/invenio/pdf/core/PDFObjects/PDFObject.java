/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core.PDFObjects;

import de.intarsys.pdf.content.CSOperation;
import java.awt.Rectangle;
import java.util.LinkedList;

/**
 *
 * @author ppraczyk
 */
public class PDFObject {

    private LinkedList<CSOperation> operations = new LinkedList<CSOperation>();
    private Rectangle boundary = null;

    public void addOperation(CSOperation op) {
        this.operations.add(op);
    }

    public void setBoundary(Rectangle b) {
        this.boundary = b;
    }

    public Rectangle getBoundary() {
        return this.boundary;
    }

    public void extendBoundary(Rectangle addition) {
        if (this.boundary == null) {
            this.boundary = addition;
        } else {
            this.boundary.add(addition);
        }
    }

    public LinkedList<CSOperation> getOperations() {
        return this.operations;
    }
}
