/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.cli;

import de.intarsys.pdf.parser.COSLoadException;
import invenio.common.Images;
import invenio.pdf.core.ExtractorLogger;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.documentProcessing.PDFDocumentTools;
import invenio.pdf.features.GraphicalAreasProvider;
import invenio.pdf.features.PageLayout;
import invenio.pdf.features.PageLayoutProvider;
import invenio.pdf.features.Plots;
import invenio.pdf.features.PlotsExtractorTools;
import invenio.pdf.features.PlotsProvider;
import invenio.pdf.features.PlotsWriter;
import invenio.pdf.features.TextAreas;
import invenio.pdf.features.TextAreasProvider;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author piotr
 */
public class PlotsExtractorCli {

    /**
     * Processes one PDF document of a given file name
     * 
     * @param inputFile
     * @param outputDirectory 
     * @throws IOException
     * @throws FeatureNotPresentException
     * @throws Exception
     */
    private static void processDocument(File inputFile, File outputDirectory)
            throws IOException, FeatureNotPresentException, Exception {

        PDFDocumentManager document = PDFDocumentTools.readPDFDocument(inputFile);
        PlotsWriter.writePlots(document, outputDirectory);

        // writing annotated pages of the document
        Plots plots = (Plots) document.getDocumentFeature(Plots.featureName);
        for (int i = 0; i < document.getPagesNumber(); ++i) {
            PDFPageManager pageMgr = document.getPage(i);
            BufferedImage img = pageMgr.getRenderedPage();
            Images.writeImageToFile(img, new File(outputDirectory.getPath(), "raw_output" + i + ".png"));

            PlotsExtractorTools.annotateImage((Graphics2D) img.getGraphics(),
                    plots.plots.get(i),
                    (TextAreas) pageMgr.getPageFeature(TextAreas.featureName),
                    (PageLayout) pageMgr.getPageFeature(PageLayout.featureName));
            Images.writeImageToFile(img, new File(outputDirectory.getPath(), "output" + i + ".png"));
        }
    }

    public static void main(String[] args) throws IOException, COSLoadException {
        // registering all the necessary PDF document features

        PDFPageManager.registerFeatureProvider(new GraphicalAreasProvider());
        PDFPageManager.registerFeatureProvider(new TextAreasProvider());
        PDFPageManager.registerFeatureProvider(new PageLayoutProvider());

        PDFDocumentManager.registerFeatureProvider(new PlotsProvider());
        File outputFolder;
        if (args.length < 1 || args.length > 2) {
            usage();
            return;
        }

        File input = new File(args[0]);

        if (args.length >= 2) {
            outputFolder = new File(args[1]);
        } else {
            outputFolder = input;
        }

        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }

        if (input.isDirectory()) {
            File[] files = input.listFiles();
            for (File file : files) {
                if (file.getPath().toLowerCase().endsWith(".pdf")) {
                    try {
                        //PlotsExtractorTools.processDocument(file.getPath(), file.getPath() + ".extracted");
                        ExtractorLogger.logMessage(1, "Processing " + file.getPath());
                        processDocument(file, getOutputDirectory(outputFolder, file));
                    } catch (Exception ex) {
                        Logger.getLogger(PlotsExtractorCli.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            try {
                //PlotsExtractorTools.processDocument(inputFileName, inputFileName + ".extracted");
                processDocument(input, getOutputDirectory(outputFolder, input));
            } catch (Exception ex) {
                Logger.getLogger(PlotsExtractorCli.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static File getOutputDirectory(File outputDirectory, File inputFile) {
        File result = new File(outputDirectory.getPath(), inputFile.getName() + ".extracted");
        if (!result.exists()) {
            result.mkdir();
        }
        return result;
    }

    public static void usage() {
        System.out.println("Invalid number of arguments");
        System.out.println("Usage:");
        System.out.println("   PDFExtractor pdffile|folder [output_folder]");
    }
}
