package invenio.pdf.features;

import invenio.pdf.core.ExtractorLogger;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.List;


import invenio.pdf.core.PDFPageManager;
import java.awt.Rectangle;
import java.io.File;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class PlotsExtractorTools {

    public static void annotateImage(Graphics2D graphics, List<Plot> plots,
            TextAreas textAreas, PageLayout layout) {
        // annotate image with plot information
        graphics.setTransform(AffineTransform.getRotateInstance(0));
        graphics.setPaintMode();

        graphics.setColor(Color.blue);

        for (Plot plot : plots) {
            Rectangle boundary = plot.getBoundary();
            graphics.drawRect((int) boundary.getX(), (int) boundary.getY(),
                    (int) boundary.getWidth(), (int) boundary.getHeight());
        }

        graphics.setColor(Color.green);
        for (Rectangle bd : textAreas.areas.keySet()) {
            graphics.drawRect((int) bd.getX(), (int) bd.getY(),
                    (int) bd.getWidth(), (int) bd.getHeight());
        }
        // drawing column rectangles

        Color[] columnColors = new Color[]{Color.magenta, Color.pink, Color.red, Color.blue, Color.gray, Color.orange};
        int colorIndex = 0;
        if (layout.areas.isEmpty()) {
            ExtractorLogger.logMessage(0, "ERROR: no page layout detected");
        }

        for (List<Rectangle> area : layout.areas) {
            if (colorIndex == columnColors.length) {
                graphics.setColor(Color.black);
            } else {
                graphics.setColor(columnColors[colorIndex]);
                colorIndex++;
            }

            for (Rectangle bd : area) {
                graphics.drawRect(bd.x, bd.y, bd.width - 1, bd.height - 1);
                graphics.drawRect(bd.x + 1, bd.y + 1, bd.width - 3, bd.height - 3);
            }
        }
        return;
    }

    private static Rectangle readRectangleFromXmlNode(Element recElement) {
        Element xEl = (Element) recElement.getElementsByTagName("x").item(0);
        Element yEl = (Element) recElement.getElementsByTagName("y").item(0);
        Element widthEl = (Element) recElement.getElementsByTagName("width").item(0);
        Element heightEl = (Element) recElement.getElementsByTagName("height").item(0);
        int x = Integer.parseInt(xEl.getFirstChild().getNodeValue().trim());
        int y = Integer.parseInt(yEl.getFirstChild().getNodeValue().trim());
        int width = Integer.parseInt(widthEl.getFirstChild().getNodeValue().trim());
        int height = Integer.parseInt(heightEl.getFirstChild().getNodeValue().trim());

        return new Rectangle(x, y, width, height);
    }

    private static void readPlotLocationFromXmlElement(Element locationElement, Plot plot) {

        Element pdfNode = (Element) locationElement.getElementsByTagName("pdf").item(0);
        Element scaleNode = (Element) locationElement.getElementsByTagName("scale").item(0);

        Element resolutionNode = (Element) locationElement.getElementsByTagName("pageResolution").item(0);
        Element pageNumberNode = (Element) locationElement.getElementsByTagName("pageNumber").item(0);
        Element pageCoordinatesNode = (Element) locationElement.getElementsByTagName("pageCoordinates").item(0);

        plot.setBoundary(readRectangleFromXmlNode(pageCoordinatesNode));
        plot.setPageNumber(Integer.parseInt(pageNumberNode.getFirstChild().getNodeValue().trim()));
    }

    private static void readPlotCaptionFromXmlElements(Element captionElement, Plot plot){
        Element coordElement = (Element) captionElement.getElementsByTagName("coordinates").item(0);
        Element textElement = (Element) captionElement.getElementsByTagName("captionText").item(0);

        try{
            plot.setCaptionBoundary(readRectangleFromXmlNode(coordElement));
            plot.setCaption(textElement.getFirstChild().getNodeValue().trim());
        } catch(Exception e){
            plot.setCaptionBoundary(new Rectangle(0,0,0,0));
            plot.setCaption("");
        }
    }

    private static Plot readPlotFromXmlElement(Element plotElement) {
        Plot result = new Plot();
        // here we assume the correctness

        Element idNode = (Element) plotElement.getElementsByTagName("identifier").item(0);
        Element pngNode = (Element) plotElement.getElementsByTagName("png").item(0);
        Element svgNode = (Element) plotElement.getElementsByTagName("svg").item(0);
        Element locationNode = (Element) plotElement.getElementsByTagName("location").item(0);
        Element captionNode = (Element) plotElement.getElementsByTagName("caption").item(0);
        Element annotatedImgNode = (Element) plotElement.getElementsByTagName("annotatedImage").item(0);
        Element captionImgNode = (Element) plotElement.getElementsByTagName("captionImage").item(0);

        result.setId(idNode.getFirstChild().getNodeValue().trim());
        result.addFile("png", new File(pngNode.getFirstChild().getNodeValue().trim()));
        result.addFile("svg", new File(svgNode.getFirstChild().getNodeValue().trim()));
        result.addFile("annotatedImage", new File(annotatedImgNode.getFirstChild().getNodeValue().trim()));
        result.addFile("captionImage", new File(captionImgNode.getFirstChild().getNodeValue().trim()));
        readPlotLocationFromXmlElement(locationNode, result);
        readPlotCaptionFromXmlElements(captionNode, result);
        return result;
    }

    /** Reading the file describing one plot metadata
     *
     * @return
     */
    public static List<Plot> readPlotMetadata(File plotFile) throws Exception {
        LinkedList<Plot> result = new LinkedList<Plot>();
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(plotFile);

        // normalize text representation
        doc.getDocumentElement().normalize();

        Element rel = (Element) doc.getDocumentElement();

        NodeList listOfPlots = rel.getElementsByTagName("plot");
        
        for (int s = 0; s < listOfPlots.getLength(); s++) {
            Node plotNode = listOfPlots.item(s);
            if (plotNode.getNodeType() == Node.ELEMENT_NODE) {
                Element plotElement = (Element) plotNode;
                result.add(readPlotFromXmlElement(plotElement));
            }
        }


        return result;
    }
}
