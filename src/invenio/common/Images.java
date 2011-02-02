/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.common;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;


/**
 *
 * @author piotr
 */
public class Images {

    public static BufferedImage readImageFromFile(File inputFile) {
        try {
            return ImageIO.read(inputFile);
        } catch (IOException ex) {
            System.out.println("Error: Can not read the image file");
            return null;
        }
    }

    /**
     * Save a given BufferedImage instance into a file
     *
     * @param image
     *            The object describing the image to be saved
     * @param filename
     *            Name of the file, where results should be saved. If this
     *            parameter designs a directory, a new file with the unique
     *            name is created
     * @return The name of the file, where image has been saved
     */
    public static String writeImageToFile(BufferedImage image, File outputFile)
            throws IOException {

        ImageWriter writer = null;
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
        if (iter.hasNext()) {
            writer = iter.next();
        }

        if (outputFile.isDirectory()) {
            outputFile = File.createTempFile("plot", ".png", outputFile);
        }

        ImageOutputStream ios = null;

        try {
            ios = ImageIO.createImageOutputStream(outputFile);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ios != null) {
                try {
                    ios.flush();
                } catch (IOException e) {
                }
                try {
                    ios.close();
                } catch (IOException e) {
                }
            }
            writer.reset();
        }
        writer.dispose();
        return outputFile.getPath();
    }

    /** Copy the buffered image, we are currently dealing with into a new one,
     * which can be modifed without affecting the original one
     * @param src
     * @return
     */
    public static BufferedImage copyBufferedImage(BufferedImage img) {
        BufferedImage result = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        result.getGraphics().drawImage(img, 0, 0, null);
        return result;
    }

    public static ImageData convertToSWT(BufferedImage bufferedImage) {
        /**
         * This function has been downloaded from the Eclipse examples
         * (http://www.eclipse.org/swt/snippets/). There should not be any license problems
         */
        //TODO: check the license of this code !
        //ImageData data = new ImageData(width, height, depth, null);
        //bufferedImage.getRGB(i, i1);

        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
        //ColorModel colorModel = bufferedImage.getColorModel();

            PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
 //           PalletteData p = new PaletteData()
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                    data.setPixel(x, y, pixel);
                    if (colorModel.hasAlpha()) {
                        data.setAlpha(x, y, (rgb >> 24) & 0xFF);
                    }
                }
            }
            return data;
        } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        } else if (bufferedImage.getColorModel() instanceof ComponentColorModel){
            //ComponentColorModel colorModel = (ComponentColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(16711680, 65280, 255);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), 32, palette);

            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    data.setPixel(x, y, rgb);
                }
            }
            return data;

        }

        return null;
    }
}
