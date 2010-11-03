/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.core;

/**
 *
 * @author piotr
 */
public interface IPDFPageFeatureProvider{
    IPDFPageFeature calculateFeature(PDFPageManager pageManager) throws FeatureNotPresentException;
    String getProvidedFeatureName();
}