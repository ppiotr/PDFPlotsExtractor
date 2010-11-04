/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.plots.extractor.cli;

import de.intarsys.pdf.parser.COSLoadException;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.documentProcessing.PDFDocumentPreprocessor;
import invenio.pdf.features.GraphicalAreasProvider;
import invenio.pdf.features.PlotsExtractorTools;
import invenio.pdf.features.PlotsProvider;
import invenio.pdf.features.PlotsWriter;
import invenio.pdf.features.TextAreasProvider;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author piotr
 */
public class PlotsExtractorCli {
    private static void processDocument(String inputFile, String outputDirectory) throws IOException{
        PDFDocumentManager document = PDFDocumentPreprocessor.readPDFDocument(inputFile);
        PlotsWriter.writePlots(document, outputDirectory);
    }

    public static void main(String[] args) throws IOException, COSLoadException {
        // registering all the necessary PDF document features
        PDFPageManager.registerFeatureProvider(new GraphicalAreasProvider());
        PDFPageManager.registerFeatureProvider(new TextAreasProvider());
        PDFDocumentManager.registerFeatureProvider(new PlotsProvider());


        String inputFileName = args[0];
        File input = new File(inputFileName);
        if (input.isDirectory()) {
            File[] files = input.listFiles();
            for (File file : files) {
                if (file.getPath().toLowerCase().endsWith(".pdf")) {
                    try {
                        //PlotsExtractorTools.processDocument(file.getPath(), file.getPath() + ".extracted");
                        processDocument(file.getPath(), file.getPath() + ".extracted");
                    } catch (Exception ex) {
                        Logger.getLogger(PlotsExtractorCli.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            try {
                //PlotsExtractorTools.processDocument(inputFileName, inputFileName + ".extracted");
                processDocument(inputFileName, inputFileName + ".extracted");
            } catch (Exception ex) {
                Logger.getLogger(PlotsExtractorCli.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
