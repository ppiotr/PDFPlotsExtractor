/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * A set of useful operations used across the PDF extractor
 * @author piotr
 */
public class PDFCommonTools {

    /**
     * Based on the content of the page, calculate the most likely safe margins
     * between graphic operations inside one plot.
     *
     *
     * @param manager Page manager containing all the operations creating one
     *                document page
     * @return the integer array with two elements : horizontal and vertical
     *          margins.
     */
    public static int[] calculateGraphicsMargins(PDFPageManager manager) {
        ExtractorParameters parameters =
                ExtractorParameters.getExtractorParameters();
        double hPercentage = parameters.getHorizontalGraphicalMargin();
        double vPercentage = parameters.getVerticalGraphicalMargin();

        Rectangle boundary = manager.getPageBoundary();
        int hMargin = (int) (boundary.getWidth() * hPercentage);
        int vMargin = (int) (boundary.getHeight() * vPercentage);
        return new int[]{hMargin, vMargin};
    }

    /**
     * Calculates margins separating textual areas -
     * @param manager
     * @return
     */
    public static int[] calculateTextMargins(PDFPageManager manager) {
        ExtractorParameters parameters =
                ExtractorParameters.getExtractorParameters();
        double hPercentage = parameters.getHorizontalTextMargin();
        double vPercentage = parameters.getVerticalTextMargin();

        Rectangle boundary = manager.getPageBoundary();
        int hMargin = (int) (boundary.getWidth() * hPercentage);
        int vMargin = (int) (boundary.getHeight() * vPercentage);
        return new int[]{hMargin, vMargin};
    }

    public static BufferedImage renderPDFPage(PDPage page) {
        Rectangle2D rect = page.getCropBox().toNormalizedRectangle();
        BufferedImage image = null;
        IGraphicsContext graphics = null;
        ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();


        Rectangle pageBoundary = new Rectangle(0, 0,
                (int) rect.getWidth() * parameters.getPageScale(),
                (int) rect.getHeight() * parameters.getPageScale());

        //Constructing the graphics context
        image = new BufferedImage((int) pageBoundary.getWidth(),
                (int) pageBoundary.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = (Graphics2D) image.createGraphics();
        graphics = new CwtAwtGraphicsContext(g2);

        // setup user space
        AffineTransform imgTransform = graphics.getTransform();
        //imgTransform.scale(sx, sy)
        imgTransform.scale(parameters.getPageScale(), -parameters.getPageScale());
        imgTransform.translate(-rect.getMinX(), -rect.getMaxY());

        graphics.setTransform(imgTransform);
        graphics.setBackgroundColor(Color.WHITE);
        graphics.fill(rect);
        CSContent content = page.getContentStream();

        if (content != null) {
            // we inject our own implementation of the renderer -> before performing an operation, it is
            // marked as currently performed. In such a way, out graphics device will be able to detect the operation
            // and assign its attributes

            CSPlatformRenderer renderer = new CSPlatformRenderer(null,
                    graphics);

            renderer.process(content, page.getResources());
        }
        return image;
    }
}
