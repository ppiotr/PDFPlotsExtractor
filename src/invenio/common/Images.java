/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author piotr
 */
public class Images {

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
}
