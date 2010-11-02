/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.core;

/**
 *
 * @author piotr
 */
public class FeatureNotPresentException extends Exception{

    public FeatureNotPresentException(String featureName) {
        super("Feature not present: " + featureName + " probably the provider has not been registered or dependent features could not have been calculated !");
    }

}
