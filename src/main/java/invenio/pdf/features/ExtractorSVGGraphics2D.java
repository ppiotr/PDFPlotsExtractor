/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import java.awt.font.GlyphVector;
import java.text.AttributedCharacterIterator;
import org.apache.batik.svggen.ExtensionHandler;
import org.apache.batik.svggen.ImageHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author piotr
 */
public class ExtractorSVGGraphics2D extends SVGGraphics2D {
    private static Log log = LogFactory.getLog(ExtractorSVGGraphics2D.class); 

    public ExtractorSVGGraphics2D(Document domFactory) {
        super(domFactory);
    }

    public ExtractorSVGGraphics2D(Document domFactory, ImageHandler imageHandler, ExtensionHandler extensionHandler, boolean textAsShapes) {
        super(domFactory, imageHandler, extensionHandler, textAsShapes);
    }

    public ExtractorSVGGraphics2D(SVGGeneratorContext generatorCtx, boolean textAsShapes) {
        super(generatorCtx, textAsShapes);
    }

    public ExtractorSVGGraphics2D(SVGGraphics2D g) {
        super(g);
    }

    @Override
    public void drawString(String arg0, int arg1, int arg2) {
        log.debug("drawString(String arg0=" + arg0 + ", int arg1=" + arg1 + ", int arg2 = " + arg2 + ")");
    }

    @Override
    public void drawString(String arg0, float arg1, float arg2) {
        log.debug("drawString(String arg0=" + arg0 + ", float arg1=" + arg1 + ", float arg2 = " + arg2 + ")");
    }

    @Override
    public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2) {
        log.debug("drawString(AttributedCharacterIterator arg0=" + arg0 + ", int arg1 = " + arg1 + ", int arg2=" + arg2 + ")");
    }

    @Override
    public void drawString(AttributedCharacterIterator arg0, float arg1,
            float arg2) {
        log.debug("drawString(AttributedCharacterIterator arg0=" + arg0 + ", float arg1 = " + arg1 + ", float arg2=" + arg2 + ")");

    }


    @Override
    public void drawGlyphVector(GlyphVector arg0, float arg1, float arg2) {
        log.debug("drawGlyphVector(GlyphVector arg0=" + arg0 + ", float arg1 = " + arg1 + ", float arg2=" + arg2 + ")");
    }
}
