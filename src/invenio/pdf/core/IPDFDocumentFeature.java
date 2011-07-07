/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.core;

import java.io.FileNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface that has to be implemented by all classes describing global
 * document features.
 *
 * The difference between document and page features lies in informations
 * necessary to calculate them. In order to calculate page features, only
 * PDFPageManager has to be provided. In the case of document features, a global
 * information about the document is necessary
 * @author piotr
 */
public interface IPDFDocumentFeature {
    public void saveToXml(Document document, Element rootElement) throws FileNotFoundException, Exception;
}
