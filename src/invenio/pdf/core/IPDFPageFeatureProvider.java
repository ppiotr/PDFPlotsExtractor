/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.core;

/**
 * An interface that has to be implemented by all classes providing a
 * particular page feature
 * @author piotr
 */
public interface IPDFPageFeatureProvider{
    <T> IPDFPageFeature calculateFeature(PDFPageManager<T> pageManager)
            throws FeatureNotPresentException, Exception;
    String getProvidedFeatureName();
}
