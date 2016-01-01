/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.XmlTools;
import invenio.pdf.core.PDFPageManager;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;
/**
 *
 * @author piotr
 */
public class XMLDocumentReader {
    private static Log log = LogFactory.getLog(XMLDocumentReader.class);  
    
    public static class ParsingException extends Exception {

        public ParsingException(String msg) {
            super(msg);
        }
    }

    public static List<FigureCandidate> readDocument(File inputFile) throws ParserConfigurationException, SAXException, IOException, Exception {
        // saving document features
        List<FigureCandidate> res = new LinkedList<FigureCandidate>();

        Document document = XmlTools.readXmlDocument(inputFile);


        NodeList childNodes = document.getChildNodes();
        Element rootNode = document.getDocumentElement();
        if (!"publication".equals(rootNode.getNodeName())) {
            throw new ParsingException("Incorrect format of the document");
        }
        NodeList figures = rootNode.getElementsByTagName("figure");
        for (int figInd = 0; figInd < figures.getLength(); figInd++) {
            res.add(readFigure(figures.item(figInd)));
        }
        /// for compatibility with older formats, we search also the plots
        figures = rootNode.getElementsByTagName("plot");
        for (int figInd = 0; figInd < figures.getLength(); figInd++) {
            res.add(readFigure(figures.item(figInd)));
        }
        
        return res;
    }

    /**
     * Reads the caption from the provided caption node
     *
     * @param node
     * @return
     */
    public static FigureCaption readCaption(Node node) {
        FigureCaption caption = new FigureCaption();
        NodeList children = node.getChildNodes();
        for (int ind = 0; ind < children.getLength(); ++ind) {
            Node child = children.item(ind);
            if ("captionText".equals(child.getNodeName())) {
                try{
                caption.text = child.getFirstChild().getNodeValue();
            
                } catch (Exception e){
                    log.fatal("oops");
                }
            }
            if ("coordinates".equals(child.getNodeName())) {
                caption.boundary = readRectangle(child);
            }
        }
        return caption;
    }

    public static Rectangle readRectangle(Node node) {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        NodeList children = node.getChildNodes();
        for (int ind = 0; ind < children.getLength(); ++ind) {
            Node child = children.item(ind);
            if ("x".equals(child.getNodeName())) {
                x = Integer.parseInt(child.getFirstChild().getNodeValue());
            }
            if ("y".equals(child.getNodeName())) {
                y = Integer.parseInt(child.getFirstChild().getNodeValue());
            }
            if ("width".equals(child.getNodeName())) {
                width = Integer.parseInt(child.getFirstChild().getNodeValue());
            }
            if ("height".equals(child.getNodeName())) {
                height = Integer.parseInt(child.getFirstChild().getNodeValue());
            }
        }
        return new Rectangle(x, y, width, height);
    }

    public static void readFigureLocation(Node node, FigureCandidate figure) {
        NodeList children = node.getChildNodes();
        for (int ind = 0; ind < children.getLength(); ++ind) {
            Node child = children.item(ind);
            if ("pdf".equals(child.getNodeName())) {
                figure.addFile("png", new File(child.getFirstChild().getNodeValue()));
            }
            
            if ("pageResolution".equals(child.getNodeName())) {
                PDFPageManager pm = new PDFPageManager();
                pm.setPageBoundary(readRectangle(child));
                figure.setPageManager(pm);
            }
            
            if ("pageNumber".equals(child.getNodeName())) {
                figure.setPageNumber(Integer.parseInt(child.getFirstChild().getNodeValue()));
            }
            
            if ("pageCoordinates".equals(child.getNodeName())) {
                figure.setBoundary(readRectangle(child));
            }
        }
    }

    public static FigureCandidate readFigure(Node node) throws ParsingException {
        FigureCandidate result = new FigureCandidate();
        if (!"figure".equals(node.getNodeName()) && !"plot".equals(node.getNodeName())) {
            throw new ParsingException("Expected a figure node ... encountered " + node.getNodeName());
        }

        if ("plot".equals(node.getNodeName())){
            log.warn("Old format of XML description is used. (plot tags rather than figure). This might not be supported in the future");
        }
        NodeList children = node.getChildNodes();
        for (int ind = 0; ind < children.getLength(); ++ind) {
            Node child = children.item(ind);
            if ("caption".equals(child.getNodeName())) {
                result.setCaption(readCaption(child));
            }
            if ("identifier".equals(child.getNodeName())) {
                result.setId(child.getFirstChild().getNodeValue());
            }
            if ("location".equals(child.getNodeName())) {
                readFigureLocation(child, result);
            }
            if ("location".equals(child.getNodeName())) {
                readFigureLocation(child, result);
            }
            

        }
        return result;
    }   
}
