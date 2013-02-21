package invenio.pdf.plots.old;

import de.intarsys.pdf.pd.PDXObject;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class PlotDescription {

    private BufferedImage image;
    private String caption;
    private List<String> contexts;
    private PDXObject xobject;

    public BufferedImage getImage() {
        /**
         * @return The buffered image of the plot
         */
        return this.image;
    }

    public void setImage(BufferedImage img) {
        /**
         * Sets the image
         * @param img BufferedImage instance to be set
         */
        this.image = img;
    }

    public String getCaption() {
        /**
         * @return Caption of the image or null
         */
        return this.caption;
    }

    public void setCaption(String cap) {
        /**
         * @param cap A caption to be set
         */
        this.caption = cap;
    }

    public List<String> getContexts() {
        /**
         * @return A list of contexts of a given image
         */
        return this.contexts;
    }

    public void addContext(String context) {
        /**
         * @param context A new context to be added to the list
         */
        this.contexts.add(context);
    }

    public PlotDescription() {
        this.image = null;
        this.contexts = new ArrayList<String>();
        this.caption = null;
        this.xobject = null;
    }

    public void setXObject(PDXObject xobject) {
        this.xobject = xobject;
    }

    public PDXObject getXObject() {
        /**
         * @return returns the current xobject
         */
        return this.xobject;
    }
}
