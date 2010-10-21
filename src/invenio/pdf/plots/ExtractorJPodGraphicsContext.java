package invenio.pdf.plots;

import java.awt.Graphics2D;
import java.awt.Shape;

import de.intarsys.cwt.awt.environment.CwtAwtGraphicsContext;

public class ExtractorJPodGraphicsContext extends CwtAwtGraphicsContext {
	public ExtractorJPodGraphicsContext(Graphics2D graphics){
		super(graphics);
	}
	
//	@Override
//	public void drawString(String text, float x, float y){
//		System.out.println("drawString(" + text + ", " + x + ", " + y + ");");
//	}
//
//	@Override
//	public void fill(Shape s){
//		System.out.println("fill(" + s.toString() + ");");
//	}
}
