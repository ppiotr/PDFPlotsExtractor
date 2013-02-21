/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

import de.intarsys.pdf.content.CSOperation;
import java.awt.Rectangle;

/**
 *  A representation of a PDF operation that explicitly displays something
 * @author piotr
 */
public class DisplayedOperation extends Operation {

    private Rectangle boundary;

    public DisplayedOperation(CSOperation orig, Rectangle bnd) {
        super(orig);
        this.setBoundary(bnd);

    }

    /**
     * Sets the boundary of a current operation
     * @param bnd
     */
    public final void setBoundary(Rectangle bnd) {
        this.boundary = bnd;
    }

    /**
     * returns the boundary of a current operation
     * @return
     */
    public final Rectangle getBoundary() {
        return this.boundary;
    }
}
