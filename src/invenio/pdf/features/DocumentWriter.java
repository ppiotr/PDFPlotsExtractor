/* Writes information about document pages into the markup
 */
package invenio.pdf.features;

import invenio.common.XmlTools;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.IPDFDocumentFeature;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author piotr
 */
public class DocumentWriter {

    public static void writePage(Document document, Element rootElement,
            PDFPageManager pManager) {
        Element mainPageEl = document.createElement("page");
        rootElement.appendChild(mainPageEl);

        XmlTools.appendElementWithTextNode(document, mainPageEl, "fullText", pManager.getPageText());
        XmlTools.appendElementWithTextNode(document, mainPageEl, "renderedFile", pManager.getRawFileName());
        XmlTools.appendElementWithTextNode(document, mainPageEl, "pageNumber", "" + pManager.getPageNumber());
        XmlTools.appendRectangle(document, mainPageEl, "boundary", pManager.getPageBoundary());
        // now saving page reatures

    }

    public static void writeDocumentToFile(PDFDocumentManager dManager,
            File outputFile) throws ParserConfigurationException, TransformerConfigurationException, FileNotFoundException, TransformerException, IOException, FeatureNotPresentException, Exception {

        Document document = XmlTools.createXmlDocument();
        Element rootElement = document.createElement("publications");
        document.appendChild(rootElement);
        writeDocument(document, rootElement, dManager);
        XmlTools.saveXmlDocument(document, outputFile);
    }

    public static void writeDocument(Document document, Element rootElement,
            PDFDocumentManager dManager) throws ParserConfigurationException, FeatureNotPresentException, Exception {
        // saving document features

        Element docElement = document.createElement("publication");
        rootElement.appendChild(docElement);
        //TODO: now writing the global desceription of the publication


        // writing document pages
        Element pagesElement = document.createElement("pages");
        docElement.appendChild(pagesElement);

        for (PDFPageManager page : dManager.getPages()) {
            writePage(document, pagesElement, page);
        }

        // writing document features
        for (String featureName : dManager.getFeatureNames()) {
            IPDFDocumentFeature docFeature = dManager.getDocumentFeature(featureName);
            docFeature.saveToXml(document, docElement);
        }
    }
}