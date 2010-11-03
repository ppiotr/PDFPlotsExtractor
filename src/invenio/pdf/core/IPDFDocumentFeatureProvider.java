/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

/**
 *
 * @author piotr
 */
public interface IPDFDocumentFeatureProvider {
    IPDFDocumentFeature calculateFeature(PDFDocumentManager docManager) throws FeatureNotPresentException;
    String getProvidedFeatureName();
}