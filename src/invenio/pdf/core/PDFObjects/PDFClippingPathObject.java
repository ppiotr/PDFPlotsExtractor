/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.core.PDFObjects;

/**
 *
 * @author ppraczyk
 */
public class PDFClippingPathObject extends PDFObject{
    private PDFPathObject path;
    public PDFClippingPathObject(PDFPathObject pathObject){
        this.path = pathObject;
    }
}
