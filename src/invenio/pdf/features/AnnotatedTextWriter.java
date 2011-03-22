/* A module writing the text with all necessary annotations
 *
 * The structure of the structured text document:
 * <fulltext>
 *    <text xml:space="preserve">
 *       *** HERE THE TEXT OF THE DOCUMENT ***
 *    </text>
 *    <fragments>
 *       <fragment begin="2" length="1"
 *    </fragments>
 * </fulltext>
 */
package invenio.pdf.features;

import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.TextOperation;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnnotatedTextWriter {

    /**
     * Prepares a string to be passed as JSON
     * @param in
     * @return
     */
    private static String escapeJSON(String in) {
        //TODO: Implement !
        return in;
    }

    private static String getJSONRectangle(Rectangle rec){
        String result = "{\n";
        result += "   x: " + rec.x + ",\n";
        result += "   y: " + rec.y + ",\n";
        result += "   width: " + rec.width + ",\n";
        result += "   height: " + rec.height + "\n";
        result += "}\n";
        return result;
    }

    /**
     * Writing a JSON representation of the document full-text
     *
     * sample text
     * @param outputFile
     * @param pdfDoc
     * @throws FileNotFoundException
     */
    public static void writeStructuredTextAsJSON(File outputFile,
            PDFDocumentManager pdfDoc)
            throws FileNotFoundException {
        /** in the future this shoudl probably be replaced by an XSLT
         * transformation and having data exported in only one format*/
        PrintStream outputStream = new PrintStream(outputFile);
        outputStream.print("{");

        for (int pageNum = 0; pageNum < pdfDoc.getPagesNumber(); ++pageNum) {
            PDFPageManager pageManager = pdfDoc.getPage(pageNum);
            String pageText = pageManager.getPageText();
            Set<Operation> operations = (Set<Operation>) pageManager.getTextOperations();
            outputStream.print("\"" + pageNum + "\" : {");
            outputStream.print("fullText: \"" + pageText + "\", boxes: [ ");

            boolean notFirst = false;
            for (Operation operation : operations) {
                TextOperation op = (TextOperation) operation;
                if (notFirst){
                    outputStream.print(", ");
                } else {
                    notFirst = true;
                }

                outputStream.print("{");
                outputStream.print("  from: " + op.getTextBeginning() + ", ");
                outputStream.print("  until: " + op.getTextEnding() + ", ");
                outputStream.print("  boundary: " + getJSONRectangle(op.getBoundary()));
                // here additional annotations

                outputStream.print("}");
            }
            outputStream.print("]");
            outputStream.print("}");
        }
        outputStream.print("}");
        outputStream.close();
    }

    public static void writeStructuredText(File outputFile, PDFDocumentManager pdfDoc)
            throws FileNotFoundException, ParserConfigurationException, TransformerConfigurationException, TransformerException, IOException {
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        PrintStream ps = new PrintStream(outputStream);

        DocumentBuilderFactory documentBuilderFactory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document document = documentBuilder.newDocument();
        Element plotsCollectionElement = document.createElement("plots");
        document.appendChild(plotsCollectionElement);

        Element rootElement = document.createElement("plot");
        plotsCollectionElement.appendChild(rootElement);

//        // plot identifier
//
//        appendElementWithTextNode(document, rootElement, "identifier", plot.getId());
//
//        //        ps.println("identifier= " + plot.getId());
//
//        // plot  image files
//        appendElementWithTextNode(document, rootElement, "png", plot.getFile("png").getAbsolutePath());
//        appendElementWithTextNode(document, rootElement, "svg", plot.getFile("svg").getAbsolutePath());
//
//        // location of the source
//
//        Element locationElement = document.createElement("location");
//        rootElement.appendChild(locationElement);
//        // main document file
//        appendElementWithTextNode(document, locationElement, "pdf", plot.getPageManager().getDocumentManager().getSourceFileName());
//        // document scale
//        appendElementWithTextNode(document, locationElement, "scale", "" + ExtractorParameters.getExtractorParameters().getPageScale());
//        // current page resulution
//        Rectangle pb = plot.getPageManager().getPageBoundary();
//        if (pb != null) {
//            Element pageResolution = document.createElement("pageResolution");
//
//            locationElement.appendChild(pageResolution);
//            appendElementWithTextNode(document, pageResolution, "width", "" + pb.width);
//            appendElementWithTextNode(document, pageResolution, "height", "" + pb.height);
//        }
//        // main document page (indexed from 0)
//        appendElementWithTextNode(document, locationElement, "pageNumber", "" + plot.getPageManager().getPageNumber());
//
//        // coordinates in the main document
//        appendRectangle(document, locationElement, "pageCoordinates", plot.getBoundary());
//
//
//
//
//        Element captionEl = document.createElement("caption");
//        rootElement.appendChild(captionEl);
//        // caption coordinates
//        appendRectangle(document, captionEl, "coordinates", plot.getCaptionBoundary());
//        // caption text
//        appendElementWithTextNode(document, captionEl, "captionText", "" + plot.getCaption());
//        // caption image
//        appendElementWithTextNode(document, rootElement, "captionImage", plot.getFile("captionImage").getAbsolutePath());
//        // debug image
//        appendElementWithTextNode(document, rootElement, "annotatedImage", plot.getFile("annotatedImage").getAbsolutePath());
//
//        // saving the output into a file

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);

        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);

        outputStream.close();
    }
}
