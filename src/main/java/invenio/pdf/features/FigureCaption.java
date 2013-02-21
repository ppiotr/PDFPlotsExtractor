/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import java.awt.Rectangle;

/**
 * A class descibing figure caption extracted from PDF
 * @author piotr
 */
public class FigureCaption {
    public String text = ""; // the text of the caption
    public Rectangle boundary; // boundary inside of a page rectangle
    public boolean isTable; // Does this caption describe table ? 
    public String figureIdentifier; // Figure identifier derieved from the caption
    public boolean alreadyMatched; // has this caption been already matched with a figure ? 
}
