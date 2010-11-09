package invenio.pdf.core.documentProcessing;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import java.awt.geom.Rectangle2D;
/**
 * The purpose of this class is to emulate a standard Java Graphics2D.
 * When the renderer passes direct graphical operations necessary to render
 * a PDF operator, we have access to the exact boundaries of the result.
 *
 * This implementation allows to completely abstract from the internal way
 * of PDF commands processing done by the PDF library (jPod) and even from the
 * nuances of the PDF specification itself.
 *
 * @author piotr
 */
class ExtractorGraphics2D extends Graphics2D {

    private Graphics2D originalGraphics;
    private PDFPageOperationsManager operationsManager; // keeping track of the current operation and of its parameters

    /**
     * A standard constructor - requies one more parameter being a
     * PDFPageOperatonsManager operating on the currently processed page.
     * @param original
     * @param opManager
     */
    public ExtractorGraphics2D(Graphics2D original, PDFPageOperationsManager opManager) {
        this.originalGraphics = original;
        this.operationsManager = opManager;
    }

    protected void processOperatorBoundary(Rectangle2D bounds) {
              AffineTransform currentTransform = this.originalGraphics.getTransform();
        double origin[] = {bounds.getMinX(), bounds.getMinY(),
            bounds.getMinX(), bounds.getMaxY(),
            bounds.getMaxX(), bounds.getMaxY(),
            bounds.getMaxX(), bounds.getMinY()};
        double transformedPoints[] = new double[8];
        currentTransform.transform(origin, 0, transformedPoints, 0, 4);
        //currentTransform.

        double minX = transformedPoints[0];
        double maxX = transformedPoints[0];

        for (int pos = 0; pos < 8; pos += 2) {
            if (minX > transformedPoints[pos]) {
                minX = transformedPoints[pos];
            }
            if (maxX < transformedPoints[pos]) {
                maxX = transformedPoints[pos];
            }
        }
        double minY = transformedPoints[1];
        double maxY = transformedPoints[1];
        for (int pos = 1; pos < 8; pos += 2) {
            if (minY > transformedPoints[pos]) {
                minY = transformedPoints[pos];
            }
            if (maxY < transformedPoints[pos]) {
                maxY = transformedPoints[pos];
            }
        }
        // after a rotation, this might be something different that a rectanglr
        // we have to find the boudary
        Rectangle finalBoundingRectangle = new Rectangle((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY));
        this.operationsManager.extendCurrentOperationBoundary(finalBoundingRectangle);
    }

    @Override
    public void addRenderingHints(Map<?, ?> arg0) {
        this.originalGraphics.addRenderingHints(arg0);
    }

    @Override
    public void clip(Shape arg0) {
        this.originalGraphics.clip(arg0);
    }

    @Override
    public void draw(Shape arg0) {        
        this.processOperatorBoundary(arg0.getBounds2D());
        this.operationsManager.addRenderingMethod("draw");
        this.originalGraphics.draw(arg0);
    }

    @Override
    public void drawGlyphVector(GlyphVector arg0, float arg1, float arg2) {
        this.originalGraphics.drawGlyphVector(arg0, arg1, arg2);
        this.operationsManager.addRenderingMethod("drawGlyphVector");

    }

    @Override
    public boolean drawImage(Image arg0, AffineTransform arg1,
            ImageObserver arg2) {
        int width = arg0.getWidth(null);
        int height = arg0.getHeight(null);
        double originals[] = {0.0, 0.0, 0.0, (double) height,
                            (double) width, (double) height,
                            (double) width, 0.0};
        double transformed[] = new double[8];
        arg1.transform(originals, 0, transformed, 0, 4);

        return this.originalGraphics.drawImage(arg0, arg1, arg2);
    }

    @Override
    public void drawImage(BufferedImage arg0, BufferedImageOp arg1, int arg2,
            int arg3) {
        this.originalGraphics.drawImage(arg0, arg1, arg2, arg3);
    }

    @Override
    public void drawRenderableImage(RenderableImage arg0, AffineTransform arg1) {
        System.out.println("drawRenderableImage(RenderableImage arg0, AffineTransform arg1);");
        this.originalGraphics.drawRenderableImage(arg0, arg1);
    }

    @Override
    public void drawRenderedImage(RenderedImage arg0, AffineTransform arg1) {
        System.out.println("drawRenderedImage(RenderedImage arg0, AffineTransform arg1);");
        this.originalGraphics.drawRenderedImage(arg0, arg1);
    }

    @Override
    public void drawString(String arg0, int arg1, int arg2) {
        System.out.println("drawString(String arg0, int arg1, int arg2);");
        this.originalGraphics.drawString(arg0, arg1, arg2);
    }

    @Override
    public void drawString(String arg0, float arg1, float arg2) {
        System.out.println("drawString(String arg0=" + arg0 + " , float arg1, float arg2)");
        this.originalGraphics.drawString(arg0, arg1, arg2);
    }

    @Override
    public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2) {
        System.out.println("drawString(AttributedCharacterIterator arg0, int arg1, int arg2);");
        this.originalGraphics.drawString(arg0, arg1, arg2);
    }

    @Override
    public void drawString(AttributedCharacterIterator arg0, float arg1,
            float arg2) {
        System.out.println("drawString(AttributedCharacterIterator arg0, float arg1, float arg2)");
        this.originalGraphics.drawString(arg0, arg1, arg2);
    }

    @Override
    public void fill(Shape arg0) {
        // String opString = "(null)";
        //CSOperation currentOp = this.operationsManager.getCurrentOperation();
        //if (currentOp != null) {
        //    opString = currentOp.toString();
        // }

        // System.out.println("fill(Shape arg0 = " + arg0.toString() + ") current operation: " + opString);
        this.operationsManager.addRenderingMethod("fill");
        this.processOperatorBoundary(arg0.getBounds2D());
        this.originalGraphics.fill(arg0);
    }

    @Override
    public Color getBackground() {
        return this.originalGraphics.getBackground();
    }

    @Override
    public Composite getComposite() {
        return this.originalGraphics.getComposite();
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return this.originalGraphics.getDeviceConfiguration();
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return this.originalGraphics.getFontRenderContext();
    }

    @Override
    public Paint getPaint() {
        return this.originalGraphics.getPaint();
    }

    @Override
    public Object getRenderingHint(Key arg0) {
        return this.originalGraphics.getRenderingHint(arg0);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return this.originalGraphics.getRenderingHints();
    }

    @Override
    public Stroke getStroke() {
        return this.originalGraphics.getStroke();
    }

    @Override
    public AffineTransform getTransform() {
        return this.originalGraphics.getTransform();
    }

    @Override
    public boolean hit(Rectangle arg0, Shape arg1, boolean arg2) {
        System.out.println("hit(Rectangle arg0=" + arg0.toString() + ", Shape arg1=" + arg1.toString() + ", boolean arg2=" + arg2 + ");");
        return this.originalGraphics.hit(arg0, arg1, arg2);
    }

    @Override
    public void rotate(double arg0) {
        this.originalGraphics.rotate(arg0);
    }

    @Override
    public void rotate(double arg0, double arg1, double arg2) {
        this.originalGraphics.rotate(arg0, arg1, arg2);
    }

    @Override
    public void scale(double arg0, double arg1) {
        System.out.println("scale(double arg0, double arg1)");
        this.originalGraphics.scale(arg0, arg1);
    }

    @Override
    public void setBackground(Color arg0) {
        this.originalGraphics.setBackground(arg0);
    }

    @Override
    public void setComposite(Composite arg0) {
        this.originalGraphics.setComposite(arg0);
    }

    @Override
    public void setPaint(Paint arg0) {
        this.originalGraphics.setPaint(arg0);
    }

    @Override
    public void setRenderingHint(Key arg0, Object arg1) {
        this.originalGraphics.setRenderingHint(arg0, arg1);
    }

    @Override
    public void setRenderingHints(Map<?, ?> arg0) {
        this.originalGraphics.setRenderingHints(arg0);
    }

    @Override
    public void setStroke(Stroke arg0) {
        this.operationsManager.addRenderingMethod("setStroke");
        this.originalGraphics.setStroke(arg0);
    }

    @Override
    public void setTransform(AffineTransform arg0) {
        this.originalGraphics.setTransform(arg0);
    }

    @Override
    public void shear(double arg0, double arg1) {
        this.originalGraphics.shear(arg0, arg1);
    }

    @Override
    public void transform(AffineTransform arg0) {
        this.originalGraphics.transform(arg0);
    }

    @Override
    public void translate(int arg0, int arg1) {
        this.originalGraphics.translate(arg0, arg1);
    }

    @Override
    public void translate(double arg0, double arg1) {
        this.originalGraphics.translate(arg0, arg1);
    }

    @Override
    public void clearRect(int arg0, int arg1, int arg2, int arg3) {
        this.originalGraphics.clearRect(arg0, arg1, arg2, arg3);
    }

    @Override
    public void clipRect(int arg0, int arg1, int arg2, int arg3) {
        this.originalGraphics.clipRect(arg0, arg1, arg2, arg3);
    }

    @Override
    public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        this.originalGraphics.copyArea(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public Graphics create() {
        return this.originalGraphics.create();
    }

    @Override
    public void dispose() {
        this.originalGraphics.dispose();
    }

    @Override
    public void drawArc(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        System.out.println("drawArc(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5)");
        this.operationsManager.addRenderingMethod("drawArc");
        this.originalGraphics.drawArc(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
        System.out.println("drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3)");
        this.operationsManager.addRenderingMethod("drawImage");
        this.processOperatorBoundary(new Rectangle2D.Double((double) arg1, (double) arg2, (double) arg0.getWidth(arg3), (double) arg0.getHeight(arg3)));
        return this.originalGraphics.drawImage(arg0, arg1, arg2, arg3);
    }

    @Override
    public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3,
            ImageObserver arg4) {
        System.out.println("drawImage(Image arg0, int arg1, int arg2, Color arg3,ImageObserver arg4)");
        return this.originalGraphics.drawImage(arg0, arg1, arg2, arg3, arg4);
    }

    @Override
    public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
            int arg4, ImageObserver arg5) {
        System.out.println("drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, ImageObserver arg5);");
        return this.originalGraphics.drawImage(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
            int arg4, Color arg5, ImageObserver arg6) {
        System.out.println("drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, Color arg5, ImageObserver arg6);");
        return this.originalGraphics.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    @Override
    public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
            int arg4, int arg5, int arg6, int arg7, int arg8, ImageObserver arg9) {
        System.out.println("drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, ImageObserver arg9)");
        return this.originalGraphics.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
    }

    @Override
    public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
            int arg4, int arg5, int arg6, int arg7, int arg8, Color arg9,
            ImageObserver arg10) {
        System.out.println("drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7, int arg8, Color arg9, ImageObserver arg10)");
        return this.originalGraphics.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
    }

    @Override
    public void drawLine(int arg0, int arg1, int arg2, int arg3) {
        System.out.println("drawLine(int arg0, int arg1, int arg2, int arg3)");
        this.originalGraphics.drawLine(arg0, arg1, arg2, arg3);
    }

    @Override
    public void drawOval(int arg0, int arg1, int arg2, int arg3) {
        System.out.println("drawOval(int arg0, int arg1, int arg2, int arg3)");
        this.originalGraphics.drawOval(arg0, arg1, arg2, arg3);
    }

    @Override
    public void drawPolygon(int[] arg0, int[] arg1, int arg2) {
        System.out.println("drawPolygon(int[] arg0, int[] arg1, int arg2)");
        this.originalGraphics.drawPolygon(arg0, arg1, arg2);
    }

    @Override
    public void drawPolyline(int[] arg0, int[] arg1, int arg2) {
        System.out.println("drawPolyline(int[] arg0, int[] arg1, int arg2)");
        this.originalGraphics.drawPolyline(arg0, arg1, arg2);
    }

    @Override
    public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        System.out.println("drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5)");
        this.originalGraphics.drawRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        System.out.println("fillArc(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5)");
        this.originalGraphics.fillArc(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public void fillOval(int arg0, int arg1, int arg2, int arg3) {
        System.out.println("fillOval(int arg0, int arg1, int arg2, int arg3)");
        this.originalGraphics.fillOval(arg0, arg1, arg2, arg3);
    }

    @Override
    public void fillPolygon(int[] arg0, int[] arg1, int arg2) {
        System.out.println("fillPolygon(int[] arg0, int[] arg1, int arg2);");
        this.originalGraphics.fillPolygon(arg0, arg1, arg2);
    }

    @Override
    public void fillRect(int arg0, int arg1, int arg2, int arg3) {
        System.out.println("fillRect(int arg0, int arg1, int arg2, int arg3)");
        this.originalGraphics.fillRect(arg0, arg1, arg2, arg3);
    }

    @Override
    public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        System.out.println("fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,	int arg5)");
        this.originalGraphics.fillRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public Shape getClip() {
        return this.originalGraphics.getClip();
    }

    @Override
    public Rectangle getClipBounds() {
        return this.originalGraphics.getClipBounds();
    }

    @Override
    public Color getColor() {
        return this.originalGraphics.getColor();
    }

    @Override
    public Font getFont() {
        return this.originalGraphics.getFont();
    }

    @Override
    public FontMetrics getFontMetrics(Font arg0) {
        return this.originalGraphics.getFontMetrics(arg0);
    }

    @Override
    public void setClip(Shape arg0) {
        this.originalGraphics.setClip(arg0);
    }

    @Override
    public void setClip(int arg0, int arg1, int arg2, int arg3) {
        this.originalGraphics.setClip(arg0, arg1, arg2, arg3);
    }

    @Override
    public void setColor(Color arg0) {
        this.originalGraphics.setColor(arg0);
    }

    @Override
    public void setFont(Font arg0) {
        System.out.println("setFont(Font arg0)");
        this.originalGraphics.setFont(arg0);
    }

    @Override
    public void setPaintMode() {
        this.originalGraphics.setPaintMode();
    }

    @Override
    public void setXORMode(Color arg0) {
        this.originalGraphics.setXORMode(arg0);
    }
}
