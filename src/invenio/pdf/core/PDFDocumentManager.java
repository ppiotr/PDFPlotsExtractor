/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

import de.intarsys.pdf.pd.PDDocument;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author piotr
 */
public class PDFDocumentManager {

    private List<PDFPageManager> pages;
    private String fileName;
    private String resultsDirectory;
    private PDDocument pdDocument;

    public PDFDocumentManager(){
        this.pages = new ArrayList<PDFPageManager>();
        this.fileName = "";
        this.resultsDirectory = "";
    }

    /**
     * set the name of the file we are processing
     * @param name
     */
    public void setSourceFileName(String name) {
        this.fileName = name;
    }

    public String getSourceFileName() {
        throw new UnsupportedOperationException("Not yet implemented");
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
    public int getPagesNumber(){
        return this.pages.size();
    }

    public void addPage(PDFPageManager p) {
        this.pages.add(p);
    }

    public void setPDDocument(PDDocument doc) {
        this.pdDocument = doc;
    }
    
}
