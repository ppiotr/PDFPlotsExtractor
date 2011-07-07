/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.common;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author piotr
 */
public class XmlTools {

    public static void prepareDocumentWriter() {
    }

    public static void appendElementWithTextNode(Document doc, Element parent, String name, String value) {
        Element newElement = doc.createElement(name);
        newElement.appendChild(doc.createTextNode(value));
        parent.appendChild(newElement);

        //CDATASection section = doc.createCDATASection(name);
        //section.setData(value);
        //parent.appendChild(section);
    }

    public static void appendRectangle(Document doc, Element parent, String name, Rectangle rec) {
        Element el = doc.createElement(name);
        parent.appendChild(el);
        if (rec == null) {
            return;
        }
        appendElementWithTextNode(doc, el, "x", "" + rec.x);
        appendElementWithTextNode(doc, el, "y", "" + rec.y);
        appendElementWithTextNode(doc, el, "width", "" + rec.width);
        appendElementWithTextNode(doc, el, "height", "" + rec.height);
    }

    public static Document createXmlDocument() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        document.setXmlVersion("1.1");
        return document;
    }

    public static void saveXmlDocument(Document document, File outputFile) throws TransformerConfigurationException, FileNotFoundException, TransformerException, IOException {
        // writePlotsMetadata(plots, rootElement, document);

        // saving the output into a file

        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        FileOutputStream outputStream = new FileOutputStream(outputFile);

        StreamResult result = new StreamResult(outputStream);
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.1");


        transformer.transform(source, result);

        outputStream.close();
    }
}
