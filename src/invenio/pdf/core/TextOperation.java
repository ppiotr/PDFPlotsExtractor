/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.core;

import de.intarsys.pdf.content.CSOperation;
import java.awt.Rectangle;

/**
 *
 * @author piotr
 */
public class TextOperation extends DisplayedOperation{
    private String text;

    public TextOperation(CSOperation orig, Rectangle bnd){
        super(orig, bnd);
        this.text = "";
    }

    /**
     * gets the text associated with the operation
     * @return
     */
    public final String getText(){
        return this.text;
    }

    /**
     * Sets the text associated with an operation
     * @param nt
     */
    public final void setText(String nt){
        this.text = nt;
    }
}
