/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

import de.intarsys.pdf.content.CSOperation;

/**
 * A class describing any PDF operation on a higher level. When displaying a
 * PDF file, a series of operations are performed. Those operations are
 * described on a low level by the PDF manipulations library. Objects of this
 * class store informations that are less display-oriented.
 *
 * More specific types of operations are described by subclasses
 *
 * @author piotr
 */
public class Operation {

    private CSOperation originalOperation;

    public Operation(CSOperation orig) {
        this.setOriginalOperation(orig);
    }

    /**
     * set the original operation.
     * Original operation is an operation object from the PDF treatment library
     * @param op
     */
    public final void setOriginalOperation(CSOperation op) {
        this.originalOperation = op;
    }

    /**
     * Return the low level representation of the PDF operation
     * @return
     */
    public final CSOperation getOriginalOperation() {
        return this.originalOperation;
    }
}
