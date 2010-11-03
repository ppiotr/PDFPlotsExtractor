/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.plots.extractor.cli;

import de.intarsys.pdf.parser.COSLoadException;
import invenio.pdf.features.PlotsExtractorTools;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author piotr
 */
public class PlotsExtractorCli {
    public static void main(String[] args) throws IOException, COSLoadException {
//        String inputFileName = args[0];
//        File input = new File(inputFileName);
//        if (input.isDirectory()) {
//            File[] files = input.listFiles();
//            for (File file : files) {
//                if (file.getPath().toLowerCase().endsWith(".pdf")) {
//                    try {
//                        PlotsExtractorTools.processDocument(file.getPath(), file.getPath() + ".extracted");
//                    } catch (Exception ex) {
//                        Logger.getLogger(PlotsExtractorCli.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        } else {
//            try {
//                PlotsExtractorTools.processDocument(inputFileName, inputFileName + ".extracted");
//            } catch (Exception ex) {
//                Logger.getLogger(PlotsExtractorCli.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }
}
