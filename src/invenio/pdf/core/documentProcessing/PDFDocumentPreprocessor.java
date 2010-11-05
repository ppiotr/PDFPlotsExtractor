/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core.documentProcessing;

import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.content.text.CSTextExtractor;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.pdf.tools.kernel.PDFGeometryTools;
import de.intarsys.tools.locator.FileLocator;
import invenio.pdf.core.ExtractorParameters;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * This class provides interface for the external world, producing an instance
 * if PDFDocumentManager based on a PDF file name;
 * @author piotr
 */
public class PDFDocumentPreprocessor {

    public static PDFDocumentManager readPDFDocument(String fileName) throws IOException {
        FileLocator locator = new FileLocator(fileName);
        PDDocument doc;
        try {
            doc = PDDocument.createFromLocator(locator);
        } catch (COSLoadException ex) {
            throw new IOException("Wrong PDF file");
        }

        PDFDocumentManager documentManager = getOperationsFromDocument(doc, fileName);
        //doc.close();
        documentManager.setPDDocument(doc);

        return documentManager;
    }

    private static PDFPageManager getOperationsFromPage(PDPage page) {
        Rectangle2D rect = page.getCropBox().toNormalizedRectangle();
        BufferedImage image = null;
        IGraphicsContext graphics = null;
        ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();

        try {
            Rectangle pageBoundary = new Rectangle(0, 0,
                    (int) rect.getWidth() * parameters.getPageScale(),
                    (int) rect.getHeight() * parameters.getPageScale());

            //Constructing the graphics context
            image = new BufferedImage((int) pageBoundary.getWidth(),
                    (int) pageBoundary.getHeight(), BufferedImage.TYPE_INT_RGB);

            Graphics2D g2 = (Graphics2D) image.getGraphics();

            //Our wrapper around the 2D device allowing to extract informations
            // about the device

            PDFPageOperationsManager opManager = new PDFPageOperationsManager(pageBoundary);

            ExtractorGraphics2D g2proxy = new ExtractorGraphics2D(g2, opManager);

            // now we use our wrapper in order to construct standard mechanisms
            graphics = new CwtAwtGraphicsContext(g2proxy);

            // setup user space
            AffineTransform imgTransform = graphics.getTransform();
            //imgTransform.scale(sx, sy)
            imgTransform.scale(parameters.getPageScale(), -parameters.getPageScale());
            imgTransform.translate(-rect.getMinX(), -rect.getMaxY());

            graphics.setTransform(imgTransform);
            graphics.setBackgroundColor(Color.WHITE);
            graphics.fill(rect);
            CSContent content = page.getContentStream();
            //CSOperation[] operations = content.getOperations();

            PDFPageManager result = new PDFPageManager();

            // now the text extraction part
            CSTextExtractor extractor = new CSTextExtractor();
            AffineTransform pageTx = new AffineTransform();
            PDFGeometryTools.adjustTransform(pageTx, page);
            extractor.setDeviceTransform(pageTx);



            if (content != null) {
                //CSPlatformRenderer renderer = new CSPlatformRenderer(null,
                //		graphics);

                // we inject our own implementation of the renderer -> before performing an operation, it is
                // marked as currently performed. In such a way, out graphics device will be able to detect the operation
                // and assign its attributes

                ExtractorCSInterpreter renderer = new ExtractorCSInterpreter(null,
                        graphics, opManager);

                ExtractorCSTextInterpreter textInterpreter =
                        new ExtractorCSTextInterpreter(null, extractor, opManager);

                renderer.process(content, page.getResources());
                textInterpreter.process(content, page.getResources());
                opManager.setPageText(extractor.getContent());
                // System.out.println("Page text: " + extractor.getContent());
                //  annotateImage(g2, opManager);
                result = opManager.transformToPDFPageManager();
                result.setRenderedPage(image);
            }
            return result;
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }
    }

    private static PDFDocumentManager getOperationsFromDocument(PDDocument doc,
            String fileName) throws IOException {
        PDFDocumentManager documentManager = new PDFDocumentManager();
        documentManager.setSourceFileName(fileName);

        PDPageTree pages = doc.getPageTree();
        PDPage page = pages.getFirstPage();

        int pageNum = 0;
        while (page != null) {
            PDFPageManager currentPageManager = getOperationsFromPage(page);
            currentPageManager.setPageNumber(pageNum);
            documentManager.addPage(currentPageManager);
            page = page.getNextPage();
            pageNum++;

            // debug... writing the page text... trying to figure out the encoding things
            try {
                //Map<String, Charset> sets = Charset.availableCharsets();
                FileOutputStream os = new FileOutputStream("/home/piotr/pagedump_" + pageNum + ".txt");
                OutputStreamWriter str = new OutputStreamWriter(os, Charset.forName("UTF8"));
                str.write(currentPageManager.getPageText());
                str.close();
                os.close();
//                  BufferedWriter writer = new BufferedWriter(new FileWriter("/home/piotr/pagedump_" + pageNum + ".txt"));
//                writer.write(currentPageManager.getPageText().toCharArray()); // cc is a char[] that stores the characters
//                writer.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return documentManager;
    }
}
