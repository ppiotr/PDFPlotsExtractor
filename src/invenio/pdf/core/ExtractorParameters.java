package invenio.pdf.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        FileInputStream fis = null;
        this.resetToDefaultValues();

        try {
            fis = new FileInputStream(new File(ExtractorParameters.confFilePath));
            this.load(fis);
            fis.close();
        } catch (Exception ex) {
            // oops... we need to read default values
            this.resetToDefaultValues();
        }
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
    private static String confFilePath;

    /**
     * Registers a path of the configuration file. 
     * This name will be always used to obtain the configuration.
     * @param fname 
     */
    public static void registerConfigurationFile(String fname) {
        ExtractorParameters.confFilePath = fname;
    }

    /**
     * Save parameters of the extractor into a file
     * 
     * @param fname Path of a file to save the parameters
     */
    public void saveExtractorParameters(String fname) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(new File(fname));
        this.store(fos, "This is a configuration file of the plots extractor");
    }

    /***** accessing the properties */
    /**
     * Return the comment that will be included in the configuration file - description of possible parameters
     * @return 
     */
    public static String getComment() {
        return "This is a configuration file for the PDF figures extractor Possible parameters (with types) are :"
                + "minimal_aspect_ratio"
                //    + "maximal_aspect_ratio"
                + "vertical_graphical_margin : a fraction of the page height that will be considered a margin of a graphical operation"
                + "horizontal_graphical_margin : A fraction of the page width that will be considered a margin of a graphical operation"
                + "vertical_text_margin : Returns a fraction of the page height that will be considered a vertical margin for all text operations being considered part of a plot"
                + ""
                + ""
                + ""
                + ""
                + "";
    }

    /**
     * Sets default values for all possible arguments
     */
    public final void resetToDefaultValues() {

        /**
         * Returns the maximal aspect ratio which is a double value
         * @return
         */
        this.setProperty("minimal_aspect_ratio", "0.1");
        //this.setProperty("maximal_aspect_ratio", "10");
        this.setProperty("vertical_graphical_margin", "0.02");
        this.setProperty("horizontal_graphical_margin", "0.1");
        this.setProperty("vertical_text_margin", "0.005");
        this.setProperty("horizontal_text_margin", "0.05");
        this.setProperty("vertical_plot_text_margin", "0.00065");
        this.setProperty("horizontal_plot_text_margin", "0.01");
        this.setProperty("page_scale", "2");
        this.setProperty("minimal_margin_width", "0.3");
        this.setProperty("horizontal_emptiness_radius", "0.01");
        this.setProperty("vertical_emptiness_radius", "0.005");
        this.setProperty("maximum_non_breaking_fraction", "0.005");
        this.setProperty("colour_emptiness_threshold", "10");
        this.setProperty("empty_pixel_colour_r", "255");
        this.setProperty("empty_pixel_colour_g", "255");
        this.setProperty("empty_pixel_colour_b", "255");

        this.setProperty("minimal_vertical_separator_height", "0.4");
        this.setProperty("generate_debug_information", "false");
        this.setProperty("generate_plot_provenance", "false");
        this.setProperty("generate_svg", "true");
    }

    /* return the maximal */
    public double getMinimalAspectRatio() {
        return Double.parseDouble(this.getProperty("minimal_aspect_ratio"));
    }

    /**
     * Returns the maximal aspect ratio which is a double value
     * @return
     */
    public double getMaximalAspectRatio() {
        return 1 / this.getMinimalAspectRatio();
    }

    /**
     * Returns a fraction of the page height that will be considered a margin of
     * a graphical operation
     * @return
     */
    public double getVerticalGraphicalMargin() {
        return Double.parseDouble(this.getProperty("vertical_graphical_margin"));
        // return 0.02;
        //return 0.003;
    }

    /**
     * Returns a fraction of the page height that will be considered
     * a horizontal margin of graphical operations
     * @return
     */
    public double getHorizontalGraphicalMargin() {
        //return 0.003;
        //return 0.10;
        return Double.parseDouble(this.getProperty("horizontal_graphical_margin"));
    }

    /**
     * Returns a fraction of the page height that will be considered a vertical
     * margin for all text operations being considered part of a plot
     *
     * @return
     */
    public double getVerticalTextMargin() {
        return Double.parseDouble(this.getProperty("vertical_text_margin"));
        //return 0.005;
        //return 0.007;
    }

    /**
     * Returns a fraction of the page height that will be considered a vertical
     * margin for all text operations
     * 
     * @return
     */
    public double getVerticalPlotTextMargin() {
        return Double.parseDouble(this.getProperty("vertical_plot_text_margin"));
        //return 0.00065;
        //return 0.003;
    }

    /**
     * Returns a fraction of the page width that will be considered the horizontal
     * margin of all text operations
     * @return
     */
    public double getHorizontalPlotTextMargin() {
//        return 0.01;
        return Double.parseDouble(this.getProperty("horizontal_plot_text_margin"));
    }

    /**
     * Returns a fraction of the page width that will be considered the horizontal
     * margin of all text operations
     * @return
     */
    public double getHorizontalTextMargin() {
        return Double.parseDouble(this.getProperty("horizontal_text_margin"));
        //return 0.05;
        //return 0.05; // this value is much bigger that the vertical margin
        // as it is unlikely that the text block will be lying next
        // to another text block (they are rather located below each other)
    }

    /**
     * Return the coefficient by which the page will be scaled
     * @return
     */
    public int getPageScale() {
        //return 2;
        return Integer.parseInt(this.getProperty("page_scale"));
    }

    ///// Parameters connected with detection of the page layout
    /**
     * Returns the minimal fraction of the page width that will not be broken into
     * @return
     */
    public double getMinimalMarginWidth() {
//        return 0.3;
        return Double.parseDouble(this.getProperty("minimal_margin_width"));
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
        //return 0.01;
        return Double.parseDouble(this.getProperty("horizontal_emptiness_radius"));
    }

    /**
     * Used to determine the minimal empty surrounding of a horizontal separator
     *
     * @return
     */
    public double getVerticalEmptinessRadius() {
//        return 0.005;
        return Double.parseDouble(this.getProperty("vertical_emptiness_radius"));
    }

    /**
     * Returns the fraction of the page height that can be covered with
     * non-empty pixels and not break a separator.
     *
     * @return
     */
    public double getMaximalNonBreakingFraction() {
//        return 0.005;
        return Double.parseDouble(this.getProperty("maximum_non_breaking_fraction"));
    }

    public int getColorEmptinessThreshold() {
//        return 10;
        return Integer.parseInt(this.getProperty("colour_emptiness_threshold"));
    }

    /**
     * Returns the color that can be considered an empty space inside an image
     * @return
     */
    public int[] getEmptyPixelColor() {
        int[] res = {Integer.parseInt(this.getProperty("empty_pixel_colour_r")),
            Integer.parseInt(this.getProperty("empty_pixel_colour_g")),
            Integer.parseInt(this.getProperty("empty_pixel_colour_b"))};
        return res;
    }

    /**
     * The percentage of hte page height that is considered to be the minimal
     * separator
     *
     * @return
     */
    public double getMinimalVerticalSeparatorHeight() {
//        return 0.4;
        return Double.parseDouble(this.getProperty("minimal_vertical_separator_height"));
    }

    public boolean generateDebugInformation() {
        //return false;
        return Boolean.parseBoolean(this.getProperty("generate_debug_information"));
    }

    public boolean generatePlotProvenance() {
//        return false;
        return Boolean.parseBoolean(this.getProperty("generate_plot_provenance"));
    }

    public boolean generateSVG() {
//        return false;
        return Boolean.parseBoolean(this.getProperty("generate_svg"));
    }
}
