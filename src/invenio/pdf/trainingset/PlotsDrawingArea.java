/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package invenio.pdf.trainingset;

import invenio.common.Pair;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author piotr
 */
public class PlotsDrawingArea extends JPanel{
    private BufferedImage backingImage;
    private List<Pair<Rectangle, Rectangle>> plots;

    protected void paintComponent(){
        Graphics2D gc = (Graphics2D) this.getGraphics();
        gc.drawLine(0, 0, 1000, 1000);
    }

    protected void changeImage(BufferedImage newImage, Rectangle plotRec, Rectangle capRec){

    }
}
