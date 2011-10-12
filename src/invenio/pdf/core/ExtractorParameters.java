package invenio.pdf.core;

import java.util.Properties;

/**
 * Handling parameters of the plots extractor - they are stored in an external
 * configuration file and this class is responsible of providing access to them.
 *
 * Properties object follow the singleton pattern
 * @author piotr
 */
public class ExtractorParameters extends Properties {

    private static ExtractorParameters properties = null;

    private ExtractorParameters() {
        // we do not want to create instances manually
    }

    /**
     * Getting an instance of the project properties
     * 
     * @return
     */
    public static ExtractorParameters getExtractorParameters() {
        if (properties == null) {
            // TODO : read this information from some configuration XML file
            properties = new ExtractorParameters();
        }
        return properties;
    }

    /* return the maximal */
    public double getMinimalAspectRatio() {
        return 0.1;
    }

    /**
     * Returns the maximal aspect ratio which is a double value
     * @return
     */
    public double getMaximalAspectRatio() {
        return 1 / getMinimalAspectRatio();
    }

    /**
     * Returns a fraction of the page height that will be considered a margin of
     * a graphical operation
     * @return
     */
    public double getVerticalGraphicalMargin() {
        return 0.02;
        //return 0.003;
    }

    /**
     * Returns a fraction of the page height that will be considered
     * a horizontal margin of graphical operations
     * @return
     */
    public double getHorizontalGraphicalMargin() {
        //return 0.003;
        return 0.10;
    }

    /**
     * Returns a fraction of the page height that will be considered a vertical
     * margin for all text operations being considered part of a plot
     *
     * @return
     */
    public double getVerticalTextMargin() {
        return 0.005;
        //return 0.007;
    }

    /**
     * Returns a fraction of the page height that will be considered a vertical
     * margin for all text operations
     * 
     * @return
     */
    public double getVerticalPlotTextMargin() {
        return 0.00065;
        //return 0.003;
    }

    /**
     * Returns a fraction of the page width that will be considered the horizontal
     * margin of all text operations
     * @return
     */
    public double getHorizontalPlotTextMargin() {
        return 0.01;
    }

    /**
     * Returns a fraction of the page width that will be considered the horizontal
     * margin of all text operations
     * @return
     */
    public double getHorizontalTextMargin() {
        return 0.05;
        //return 0.05; // this value is much bigger that the vertical margin
        // as it is unlikely that the text block will be lying next
        // to another text block (they are rather located below each other)
    }

    /**
     * Return the coefficient by which the page will be scaled
     * @return
     */
    public int getPageScale() {
        return 2;
    }

    ///// Parameters connected with detection of the page layout
    /**
     * Returns the minimal fraction of the page width that will not be broken into
     * @return
     */
    public double getMinimalMarginWidth() {
        return 0.3;
    }

    /**
     * When we check if a point is empty, the pixel must be empty together with
     * its surrounding of some radius.
     *
     * This function returns the fraction of the page width that is considered
     * to be this radius.
     * 
     * @return
     */
    public double getHorizontalEmptinessRadius() {
        return 0.01;
    }

    /**
     * Used to determine the minimal empty surrounding of a horizontal separator
     *
     * @return
     */
    public double getVerticalEmptinessRadius() {
        return 0.005;
    }

    /**
     * Returns the fraction of the page height that can be covered with
     * non-empty pixels and not break a separator.
     *
     * @return
     */
    public double getMaximalNonBreakingFraction() {
        return 0.005;
    }

    public int getColorEmptinessThreshold() {
        return 10;
    }

    /**
     * Returns the color that can be considered an empty space inside an image
     * @return
     */
    public int[] getEmptyPixelColor() {
        int[] res = {255, 255, 255};
        return res;
    }

    /**
     * The percentage of hte page height that is considered to be the minimal
     * separator
     *
     * @return
     */
    public double getMinimalVerticalSeparatorHeight() {
        return 0.4;
    }
    
    
    public boolean generateDebugInformation(){
        return false;
    }

    public boolean generatePlotProvenance() {
        return false;
    }
    
    public boolean generateSVG(){
        return false;
    }
}
