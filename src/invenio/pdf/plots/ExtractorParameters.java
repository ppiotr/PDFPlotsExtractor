package invenio.pdf.plots;

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
     *
     * @return
     */
    public double getVerticalMargin() {
        return 0.03;
    }

    /**
     * 
     * @return
     */
    public double getHorizontalMargin() {
        return 0.15;
    }
}
