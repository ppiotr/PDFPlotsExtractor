/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

import de.intarsys.pdf.pd.PDDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A class whose objects describe one PDF document.
 * Besides storing global metadata of the document, a list of PDFPageManagers is
 * maintained.
 * @author piotr
 */
public class PDFDocumentManager {

    private List<PDFPageManager> pages;
    private String fileName;
    private String resultsDirectory;
    private PDDocument pdDocument;
    private HashMap<String, IPDFDocumentFeature> documentFeatures;

    public PDFDocumentManager() {
        this.pages = new ArrayList<PDFPageManager>();
        this.fileName = "";
        this.resultsDirectory = "";
        this.documentFeatures = new HashMap<String, IPDFDocumentFeature>();
    }

    /**
     * set the name of the file we are processing
     * @param name
     */
    public void setSourceFileName(String name) {
        this.fileName = name;
    }

    public String getSourceFileName() {
        return this.fileName;
    }

    /**
     * Return mananger of the page of a given number
     * @param Num
     * @return
     */
    public PDFPageManager getPage(int num) {
        return this.pages.get(num);
    }

    /**
     * Return the number of pages present in the document
     * @return
     */
    public int getPagesNumber() {
        return this.pages.size();
    }

    public List<PDFPageManager> getPages() {
        return this.pages;
    }

    public void addPage(PDFPageManager p) {
        this.pages.add(p);
    }

    public void setPDDocument(PDDocument doc) {
        this.pdDocument = doc;
    }
    private static HashMap<String, IPDFDocumentFeatureProvider> featureProviders =
            new HashMap<String, IPDFDocumentFeatureProvider>();

    public static void registerFeatureProvider(IPDFDocumentFeatureProvider provider) {
        PDFDocumentManager.featureProviders.put(provider.getProvidedFeatureName(), provider);
    }

    public IPDFDocumentFeature getDocumentFeature(String featureName) throws FeatureNotPresentException, Exception {
        if (this.documentFeatures.containsKey(featureName)) {
            // the feature is alreadsy precalculated and we can jsut return it
            return this.documentFeatures.get(featureName);
        } else {
            if (featureProviders.containsKey(featureName)) {
                this.documentFeatures.put(featureName,
                        featureProviders.get(featureName).calculateFeature(this));
                return this.documentFeatures.get(featureName);
            }
        }
        return null;
    }

    /** return names of all registered features */
    public Set<String> getFeatureNames() {
        return this.documentFeatures.keySet();
    }
    
}
