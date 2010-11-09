/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core;

import java.awt.Rectangle;

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
      //  System.out.println("Margins: horizontal=" + hMargin + " vertical=" + vMargin);
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
       // System.out.println("Text margins: horizontal=" + hMargin + " vertical=" + vMargin);
        return new int[]{hMargin, vMargin};
    }
}
