/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.core;

import de.intarsys.pdf.content.CSOperation;
import java.awt.Rectangle;

/**
 * A description of a text operation - an operation that paints text.
 * @author piotr
 */
public class TextOperation extends DisplayedOperation{
    private String text;
    private int textBeginning;
    private int textEnding;

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

    /**
     * Sets the text range within which we should search
     * @param b
     * @param e
     */
    
    public void setTextRange(int b, int e){
        this.setTextBeginning(b);
        this.setTextEnding(e);
    }

    /**
     * Sets the index of the begining of the text inside the entire page text
     * @param b
     */
    public void setTextBeginning(int b){
        this.textBeginning = b;
    }

    public void setTextEnding(int e){
        this.textEnding = e;
    }


    public int getTextBeginning(){
        return this.textBeginning;
    }

    /** Returns the index of the end of text of this operation inside the page text

     * @return
     *
     */

    public int getTextEnding(){
        return this.textEnding;
    }
    
}
