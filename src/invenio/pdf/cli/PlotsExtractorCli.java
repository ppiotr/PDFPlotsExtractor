/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.cli;

import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDPage;
import invenio.common.Images;
import invenio.pdf.core.DisplayedOperation;
import invenio.pdf.core.ExtractorLogger;
import invenio.pdf.core.ExtractorParameters;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.GraphicalOperation;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.TextOperation;
import invenio.pdf.core.TransformationOperation;
import invenio.pdf.core.documentProcessing.PDFDocumentTools;
import invenio.pdf.features.AnnotatedTextWriter;
import invenio.pdf.features.DocumentWriter;
import invenio.pdf.features.GraphicalAreasProvider;
import invenio.pdf.features.PageLayout;
import invenio.pdf.features.PageLayoutProvider;
import invenio.pdf.features.Figure;
import invenio.pdf.features.FiguresExtractorTools;
import invenio.pdf.features.FiguresProvider;
import invenio.pdf.features.FiguresWriter;
import invenio.pdf.features.TextAreas;
import invenio.pdf.features.TextAreasProvider;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
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

        System.out.println("Processing document: " + inputFile.getAbsolutePath());
        PDFDocumentManager document = PDFDocumentTools.readPDFDocument(inputFile);
        ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();

        // writing annotated pages of the document
        Figure plots = (Figure) document.getDocumentFeature(Figure.featureName);

        for (int i = 0; i < document.getPagesNumber(); ++i) {
            PDFPageManager<PDPage> pageMgr = document.getPage(i);

            if (parameters.generateDebugInformation()) {
                BufferedImage img = pageMgr.getRenderedPage();


                BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                img2.getGraphics().drawImage(img, 0, 0, null);

                FiguresExtractorTools.annotateImage((Graphics2D) img2.getGraphics(),
                        plots.figures.get(i),
                        (TextAreas) pageMgr.getPageFeature(TextAreas.featureName),
                        (PageLayout) pageMgr.getPageFeature(PageLayout.featureName),
                        null, null);


                Images.writeImageToFile(img2, new File(outputDirectory.getPath(), "output" + i + ".png"));

                File rawFile = new File(outputDirectory.getPath(), "raw_output" + i + ".png");
                Images.writeImageToFile(img, rawFile);
                pageMgr.setRawFileName(rawFile.getAbsolutePath());

                // Now saving the layout for all pages ... a lot of files so only diring the very debugging phase
                PageLayout pageLayout = (PageLayout) pageMgr.getPageFeature(PageLayout.featureName);
                for (int layoutNum = 0; layoutNum < pageLayout.areas.size(); ++layoutNum) {
                    BufferedImage layout_img = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                    layout_img.getGraphics().drawImage(img, 0, 0, null);
                    FiguresExtractorTools.annotateWithLayout((Graphics2D) layout_img.getGraphics(), pageLayout, layoutNum);
                    Images.writeImageToFile(layout_img, new File(outputDirectory.getPath(), "layout" + i + "_" + layoutNum + ".png"));
                }

                // writing preliminary rectangles of the page layout ... those before having separators moved
                PageLayoutProvider provider = new PageLayoutProvider();
                Raster raster = pageMgr.getRenderedPage().getData();
                LinkedList<Rectangle> verticalSeparators = new LinkedList<Rectangle>();
                List<Rectangle> preliminaryColumns = provider.getPageColumns(raster, verticalSeparators);

                BufferedImage pre_layout_img = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                pre_layout_img.getGraphics().drawImage(img, 0, 0, null);
                FiguresExtractorTools.annotateWithRectangles((Graphics2D) pre_layout_img.getGraphics(), preliminaryColumns);
                Images.writeImageToFile(pre_layout_img, new File(outputDirectory.getPath(), "preliminary_layout" + i + ".png"));

                /* verifying that rectangles are not intersecting !! */
                int int_num = 0;
                for (Rectangle r1 : preliminaryColumns) {
                    for (Rectangle r2 : preliminaryColumns) {
                        if (r1.intersects(r2) && r1 != r2) {
                            // something went terribly wrong... initial rectangles intersect
                            LinkedList<Rectangle> intRecs = new LinkedList<Rectangle>();
                            intRecs.add(r1);
                            intRecs.add(r2);
                            
                            BufferedImage int_layout_img = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                            int_layout_img.getGraphics().drawImage(img, 0, 0, null);
                            FiguresExtractorTools.annotateWithEmptyRectangles((Graphics2D) int_layout_img.getGraphics(), intRecs);
                            Images.writeImageToFile(int_layout_img, new File(outputDirectory.getPath(), "preliminary_layout_" + i +"intersection_" + int_num +".png"));
                            int_num++;
                        }
                    }
                }

                // saving annotated graphical operations -> checking their boundaries

                BufferedImage img3 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
                img3.getGraphics().drawImage(img, 0, 0, null);
                File pageOperationsDumpFile = new File(outputDirectory.getPath(), "page_dump_" + i + ".txt");
                FileOutputStream operationsDumpFile = new FileOutputStream(pageOperationsDumpFile);
                PrintStream ps = new PrintStream(operationsDumpFile);

                LinkedList<Operation> graphicalOperations = new LinkedList();
                int lineNumber = 1;
                for (Operation op : pageMgr.getOperations()) {

                    ps.print(lineNumber);
                    ps.print(" ");
                    ps.print(op.getOriginalOperation().toString());
                    ++lineNumber;
                    if (op instanceof TransformationOperation) {
                        ps.print(" transformation");
                    }
                    if (op instanceof TextOperation) {
                        ps.print(" text");
                    }

                    if (op instanceof GraphicalOperation) {
                        ps.print(" graphical");
                        GraphicalOperation go = (GraphicalOperation) op;
                        if (pageMgr.getPageNumber() == 3 && go.getBoundary().x < 300 && go.getBoundary().y < 505) {
                            //System.out.println("our operation");
                            ps.print("    <---------------");
                        }
                        graphicalOperations.add(op);
                    }
                    ps.println("");
                }

                operationsDumpFile.close();

                FiguresExtractorTools.annotateImage((Graphics2D) img3.getGraphics(),
                        null,
                        null,
                        null, graphicalOperations, null);
                File graphicalAnnotatedFile = new File(outputDirectory.getPath(), "graphical_output" + i + ".png");
                Images.writeImageToFile(img3, graphicalAnnotatedFile);
                /*
                // annotating with detected PDFObjects
                BufferedImage img4 = Images.copyBufferedImage(img);
                PlotsExtractorTools.annotateImage((Graphics2D) img4.getGraphics(),
                        null,
                        null,
                        null, null, pageMgr.getPDFObjects());
                
                
                File pdfObjectsFile = new File(outputDirectory.getPath(), "pdfobjects_output" + i + ".png");
                Images.writeImageToFile(img4, pdfObjectsFile);
*/
                BufferedImage img40 = Images.copyBufferedImage(img);
                FiguresExtractorTools.annotateImage((Graphics2D) img40.getGraphics(),
                        null,
                        null,
                        null, pageMgr.getOperations(), null);

                File pdfOperations = new File(outputDirectory.getPath(), "pdfops_output" + i + ".png");
                Images.writeImageToFile(img40, pdfOperations);

                /** Searching for operations intersecting the layout in a very bad manner */
                PageLayout layout = (PageLayout) pageMgr.getPageFeature(PageLayout.featureName);
                for (Operation op : pageMgr.getOperations()) {
                    if (op instanceof DisplayedOperation) {
                        DisplayedOperation dop = (DisplayedOperation) op;
                        if (layout.getIntersectingAreas(dop.getBoundary()).size() > 1) {
                            System.out.println("operation that intersects too many layout areas !");
                        }
                    }
                }

                /*
                System.out.println("Statistics about objects stored in the PDF");
                // now dealing with operations ..
                for (PDFObject object : pageMgr.getPDFObjects()) {
                    if (object instanceof PDFPathObject) {
                        System.out.print("PATH            ");
                    }
                    if (object instanceof PDFClippingPathObject) {
                        System.out.print("CLIPPING        ");
                    }
                    if (object instanceof PDFTextObject) {
                        System.out.print("TEXT            ");
                    }
                    if (object instanceof PDFPageDescriptionObject) {
                        System.out.print("PAGE DESCRIPTION");
                    }
                    if (object instanceof PDFExternalObject) {
                        System.out.print("EXTERNAL        ");
                    }
                    if (object instanceof PDFInlineImageObject) {
                        System.out.print("INLINE          ");
                    }
                    if (object instanceof PDFShadingObject) {
                        System.out.print("SHADING         ");
                    }

                    System.out.print("(" + object.getOperations().size() + ") ");
                    Rectangle bd = object.getBoundary();
                    if (bd != null) {
                        System.out.print("(" + bd.x + ", " + bd.y + ", " + bd.width + ", " + bd.height + ")");
                    }

                    System.out.println("");
                }*/
            }
        }

        FiguresWriter.writePlots(document, outputDirectory, true);
        File annotatedTextFile = new File(outputDirectory.getPath(), "annotatedText.json");
        AnnotatedTextWriter.writeStructuredTextAsJSON(annotatedTextFile, document);

        // writing the global metadata of all the plots collectively
        File completemetadataFile = new File(outputDirectory.getPath(), "completeMetadata.xml");

        FiguresWriter.writePlotsMetadataToFile(plots.getToplevelPlots(), completemetadataFile);

        File extractorOutputFile = new File(outputDirectory.getPath(), "description.xml");
        DocumentWriter.writeDocumentToFile(document, extractorOutputFile);

        File extractorJSONOutputFile = new File(outputDirectory.getPath(), "extracted.json");

        FiguresWriter.writePlotsMetadataToFileJSON(plots.getToplevelPlots(), extractorJSONOutputFile);
        //writeDocumentToFileJSON(document, extractorJSONOutputFile);
    }

    /** Setup paths to the configuration file based on the execution path */
    private static void setConfigurationFile() {
        //TODO: Search for the configuration fiel in more locations
        String fname = PlotsExtractorCli.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "extractor.conf";
        ExtractorParameters.registerConfigurationFile(fname);

    }

    public static void main(String[] args) throws IOException, COSLoadException {
        // registering all the necessary PDF document features

        setConfigurationFile();

        PDFPageManager.registerFeatureProvider(new GraphicalAreasProvider());
        PDFPageManager.registerFeatureProvider(new TextAreasProvider());
        PDFPageManager.registerFeatureProvider(new PageLayoutProvider());

        PDFDocumentManager.registerFeatureProvider(new FiguresProvider());
        File outputFolder;


        /** Saving extractor parameters to file... to see the format */
        ExtractorParameters par = ExtractorParameters.getExtractorParameters();
        OutputStream ost = new ByteArrayOutputStream(10000);
        par.store(ost, "This is a comments string");

        System.out.println(PlotsExtractorCli.class.getProtectionDomain().getCodeSource().getLocation().getPath());


        System.out.println(ost.toString());
        if (args.length < 1 || args.length > 2) {
            usage();
            return;
        }

        File input = new File(args[0]);

        if (args.length >= 2) {
            outputFolder = new File(args[1]);
        } else {
            if (input.isDirectory()) {
                outputFolder = input;
            } else {
                outputFolder = input.getParentFile();
            }

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
