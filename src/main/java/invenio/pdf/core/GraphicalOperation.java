/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.core;

import de.intarsys.pdf.content.CSOperation;
import java.awt.Rectangle;

/**
 * Class describing operation that is purely graphical
 * @author piotr
 */
public class GraphicalOperation extends DisplayedOperation {
    public GraphicalOperation(CSOperation orig, Rectangle bnd){
        super(orig, bnd);
    }
}
