/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.core;

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

}
