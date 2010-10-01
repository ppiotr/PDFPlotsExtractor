/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.plots.extractor.cli;

import de.intarsys.pdf.parser.COSLoadException;
import invenio.pdf.plots.PlotsExtractor;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author piotr
 */
public class PlotsExtractorCli {
    public static void main(String[] args) throws IOException, COSLoadException {
        String inputFileName = args[0];
        File input = new File(inputFileName);
        if (input.isDirectory()) {
            File[] files = input.listFiles();
            for (File file : files) {
                if (file.getPath().toLowerCase().endsWith(".pdf")) {
                    PlotsExtractor.processDocument(file.getPath(), file.getPath()
                            + ".extracted");
                }
            }
        } else {
            PlotsExtractor.processDocument(inputFileName, inputFileName + ".extracted");
        }
    }
}
