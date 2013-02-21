/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

/**
 * An interface that has to be implemented by classes providinga particular feature
 * @author piotr
 */
public interface IPDFDocumentFeatureProvider {
    IPDFDocumentFeature calculateFeature(PDFDocumentManager docManager) throws FeatureNotPresentException, Exception;
    String getProvidedFeatureName();
}
