/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.common;

import java.awt.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author piotr
 */
public class XmlTools {
    public static void prepareDocumentWriter(){

    }
    
    public static void appendElementWithTextNode(Document doc, Element parent, String name, String value) {
        Element newElement = doc.createElement(name);
        newElement.appendChild(doc.createTextNode(value));
        parent.appendChild(newElement);
    }

    public static void appendRectangle(Document doc, Element parent, String name, Rectangle rec) {
        Element el = doc.createElement(name);
        parent.appendChild(el);
        if (rec == null) {
            return;
        }
        appendElementWithTextNode(doc, el, "x", "" + rec.x);
        appendElementWithTextNode(doc, el, "y", "" + rec.y);
        appendElementWithTextNode(doc, el, "width", "" + rec.width);
        appendElementWithTextNode(doc, el, "height", "" + rec.height);
    }

}
