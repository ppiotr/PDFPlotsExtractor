package invenio.pdf.features;

import invenio.pdf.core.DisplayedOperation;
import invenio.pdf.core.ExtractorLogger;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFObjects.PDFClippingPathObject;
import invenio.pdf.core.PDFObjects.PDFObject;
import invenio.pdf.core.PDFObjects.PDFPathObject;
import invenio.pdf.core.PDFObjects.PDFTextObject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;


import java.awt.Rectangle;
import java.io.File;
import java.util.LinkedList;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FiguresExtractorTools {

    /** draw a number of rectangles on the canvas*/
    private static Random random = null;

    public static void annotateWithEmptyRectangles(Graphics2D graphics, List<Rectangle> rectangles) {
        graphics.setTransform(AffineTransform.getRotateInstance(0));
        graphics.setPaintMode();
        graphics.setColor(Color.blue);

        for (Rectangle bd : rectangles) {
            graphics.drawRect(bd.x, bd.y, bd.width - 1, bd.height - 1);
            graphics.drawRect(bd.x + 1, bd.y + 1, bd.width - 3, bd.height - 3);
        }
    }

    /** draw a number of rectangles on the canvas*/
    public static void annotateWithRectangles(Graphics2D graphics, List<Rectangle> rectangles) {
        graphics.setTransform(AffineTransform.getRotateInstance(0));
        graphics.setPaintMode();


        for (int rectInd = 0; rectInd < rectangles.size(); ++rectInd) {
            Rectangle bd = rectangles.get(rectInd);
            Color c = FiguresExtractorTools.getRandomColor();
            graphics.setColor(c);
            graphics.setFont(new Font("helvetica", 0, 40));

            //graphics.fillRect(bd.x, bd.y, bd.width - 1, bd.height - 1);
            graphics.fillRect(bd.x + 1, bd.y + 1, bd.width - 2, bd.height - 2);

            graphics.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 255));

            graphics.drawString(String.valueOf(rectInd), bd.x + Math.round(bd.width / 2), bd.y + Math.round(bd.height / 2));
        }
    }

    private static Color getRandomColor() {
        if (FiguresExtractorTools.random == null) {
            FiguresExtractorTools.random = new Random();
        }
        int r = FiguresExtractorTools.random.nextInt(256);
        int g = FiguresExtractorTools.random.nextInt(256);
        int b = FiguresExtractorTools.random.nextInt(256);
        return new Color(r, g, b, 100);
    }

    /**
     * Annotate the canvas with single layout area
     * @param graphics
     * @param layout
     * @param areaNum 
     */
    public static void annotateWithLayout(Graphics2D graphics, PageLayout layout, int areaNum) {
        graphics.setTransform(AffineTransform.getRotateInstance(0));
        graphics.setPaintMode();
        List<Rectangle> area = layout.areas.get(areaNum);

        Color c = FiguresExtractorTools.getRandomColor();
        graphics.setColor(c);
        graphics.setFont(new Font("helvetica", 0, 40));

        // we use temporary variables to calculate the center of the largest rectangle of the layout region
        int maxVol = 0;
        int centerX = 0;
        int centerY = 0;

        for (Rectangle bd : area) {
            //graphics.fillRect(bd.x, bd.y, bd.width - 1, bd.height - 1);
            graphics.fillRect(bd.x + 1, bd.y + 1, bd.width - 2, bd.height - 2);
            if (bd.width * bd.height > maxVol) {
                maxVol = bd.width * bd.height;
                centerX = bd.x + Math.round(bd.width / 2);
                centerY = bd.y + Math.round(bd.height / 2);
            }
        }

        // now write the area number inside

        graphics.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 255));

        graphics.drawString(String.valueOf(areaNum), centerX, centerY);
    }

    public static void annotateImage(Graphics2D graphics, List<FigureCandidate> plots,
            TextAreas textAreas, PageLayout layout, List<Operation> operations, List<PDFObject> pdfObjects) {
        // annotate image with plot information
        graphics.setTransform(AffineTransform.getRotateInstance(0));
        graphics.setPaintMode();



        if (plots != null) {
            graphics.setColor(Color.blue);
            for (FigureCandidate plot : plots) {
                Rectangle boundary = plot.getBoundary();
                graphics.drawRect((int) boundary.getX(), (int) boundary.getY(),
                        (int) boundary.getWidth(), (int) boundary.getHeight());
            }
        }

        if (textAreas != null) {
            graphics.setColor(Color.green);
            for (Rectangle bd : textAreas.areas.keySet()) {
                graphics.drawRect((int) bd.getX(), (int) bd.getY(),
                        (int) bd.getWidth(), (int) bd.getHeight());
            }
        }
        // drawing column rectangles
        if (layout != null) {
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
        }

        // annotating additional operations
        graphics.setColor(Color.CYAN);
        graphics.setColor(Color.BLUE);

        if (operations != null) {
            for (Operation operation : operations) {
                if (operation instanceof DisplayedOperation) {
                    DisplayedOperation op = (DisplayedOperation) operation;
                    Rectangle bd = op.getBoundary();
                    graphics.drawRect(bd.x, bd.y, bd.width, bd.height);
                }
            }
        }

        if (pdfObjects != null) {
            for (PDFObject object : pdfObjects) {
                if (object instanceof PDFPathObject) {
                    graphics.setColor(Color.PINK);
                }
                if (object instanceof PDFTextObject) {
                    graphics.setColor(Color.GREEN);
                }
                if (object instanceof PDFClippingPathObject) {
                    graphics.setColor(Color.BLUE);
                }
                Rectangle bd = object.getBoundary();

                if (bd != null) {
                    graphics.drawRect(bd.x, bd.y, bd.width, bd.height);
                }
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

    private static void readPlotLocationFromXmlElement(Element locationElement, FigureCandidate plot) {

        Element pdfNode = (Element) locationElement.getElementsByTagName("pdf").item(0);
        Element scaleNode = (Element) locationElement.getElementsByTagName("scale").item(0);

        Element resolutionNode = (Element) locationElement.getElementsByTagName("pageResolution").item(0);
        Element pageNumberNode = (Element) locationElement.getElementsByTagName("pageNumber").item(0);
        Element pageCoordinatesNode = (Element) locationElement.getElementsByTagName("pageCoordinates").item(0);

        plot.setBoundary(readRectangleFromXmlNode(pageCoordinatesNode));
        plot.setPageNumber(Integer.parseInt(pageNumberNode.getFirstChild().getNodeValue().trim()));
    }

    private static void readPlotCaptionFromXmlElements(Element captionElement, FigureCandidate plot) {
        Element coordElement = (Element) captionElement.getElementsByTagName("coordinates").item(0);
        Element textElement = (Element) captionElement.getElementsByTagName("captionText").item(0);

        try {
            plot.getCaption().boundary = readRectangleFromXmlNode(coordElement);
            plot.getCaption().text = textElement.getFirstChild().getNodeValue().trim();
        } catch (Exception e) {
            plot.getCaption().boundary = new Rectangle(0, 0, 0, 0);
            plot.getCaption().text = "";
        }
    }

    private static FigureCandidate readPlotFromXmlElement(Element plotElement) {
        FigureCandidate result = new FigureCandidate();
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
    public static List<FigureCandidate> readPlotMetadata(File plotFile) throws Exception {
        LinkedList<FigureCandidate> result = new LinkedList<FigureCandidate>();
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
