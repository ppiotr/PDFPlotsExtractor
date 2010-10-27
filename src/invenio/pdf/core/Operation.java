/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

import de.intarsys.pdf.content.CSOperation;

/**
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
