/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;
import invenio.pdf.core.DisplayedOperation;
import invenio.pdf.core.GraphicalOperation;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.documentProcessing.OperationTools;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Random;
import sun.awt.SunToolkit;

/**
 * A class allowing selective rendering of the content. The selectiveness can be
 * expressed in following ways:
 *
 * -By providing an operations manager and a rectangle which has to be
 * intersected by processed graphical operations
 *
 * - By disabling the rendering of text operations completely
 *
 * @author piotr
 */
public class CSSelectiveRenderer extends CSPlatformRenderer {

    protected PDFPageManager<PDPage> currentPageManager;
    private boolean ignoreText;
    private Rectangle2D clippingRect;

    /**
     *
     * @param paramOptions
     * @param igc
     */
    public CSSelectiveRenderer(Map paramOptions, IGraphicsContext igc) {
        super(paramOptions, igc);
    }

    /**
     * A method allowing to process the entire page by providing only the page
     * manager
     *
     * @param pageManager
     */
    public void process(PDFPageManager pageManager) throws CSException {
        this.process(pageManager, false, null);
    }

    /**
     * A method allowing to process the entire page by providing only the page
     * manager
     *
     * @param pageManager
     * @param ignoreText Indicates if text operations should be ignored during
     * the rendering
     * @throws CSException
     */
    public void process(PDFPageManager pageManager, boolean ignoreText)
            throws CSException {
        this.process(pageManager, ignoreText, null);
    }

    /**
     * A method allowing to process the entire page by providing only the page
     * manager
     *
     * @param pageManager
     * @param clippingRectangle Indicated the rectangle inside of a page, which
     * has to be intersected by all the rendered operations. The coordinate
     * system is the same as in the PDFPageManager specified by the pageManager
     * argument
     * @throws CSException
     */
    public void process(PDFPageManager pageManager, Rectangle2D clippingRectangle)
            throws CSException {
        this.process(pageManager, false, clippingRectangle);
    }

    /**
     * Processes a stream of PDF operations allowing to ignore some
     *
     * @param pContent
     * @param pResources
     * @param ignoreText
     * @throws CSException
     */
    public void process(PDFPageManager pageManager, boolean ignoreText, Rectangle2D clippingRectangle) throws CSException {
        this.currentPageManager = pageManager;
        this.ignoreText = ignoreText;
        this.clippingRect = clippingRectangle;

        PDPage page = (PDPage) pageManager.getInternalPage();
        CSContent content = page.getContentStream();

        if (content != null) {
            super.process(content, page.getResources());
        }
    }

    @Override
    protected void process(CSOperation operation) throws CSException {
        Operation op = this.currentPageManager.getHigherLevelOperation(operation);
        if (op instanceof DisplayedOperation) {
            DisplayedOperation dop = (DisplayedOperation) op;
            if (this.clippingRect == null || dop.getBoundary().intersects(this.clippingRect)) {
                if (!this.ignoreText || !OperationTools.getInstance().isTextOperation(operation.getOperator().toString())) {
                    super.process(operation);
                } else {
                    // We remember the current effective transformation (so that we can apply it when really renderingthe text
                    AffineTransform effectiveTransform = super.getDevice().getGraphicsState().transform;
                    this.currentPageManager.setEffectiveTransform(operation, effectiveTransform);
                }
            }
        } else {
            super.process(operation);
        }
    }
}
