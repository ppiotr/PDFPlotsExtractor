/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.plots.extractor.gui;

import de.intarsys.pdf.parser.COSLoadException;
import invenio.common.Images;
import invenio.common.IntervalTree;
import invenio.pdf.plots.ExtractorOperationsManager;
import invenio.pdf.plots.PlotsExtractor;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

public class ExtractorInterface {
    // graphic controls
    private Shell shell;
    private Display display;
    // interface elements
    private Label openFileLabel;
    private Button openFileButton;
    private TabFolder pdfPagesTabFolder;
    // variables medling the data
    private String openFileName;

    private void init_interface() {
        /**
         * Creatign teh basic interface
         */
        this.display = new Display();
        this.shell = new Shell(display);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        shell.setLayout(layout);
        this.openFileLabel = new Label(shell, SWT.NONE);

        this.openFileButton = new Button(shell, SWT.NONE);
        openFileButton.setText("Open PDF File...");

        // Now the grid allowing us to view PDF pages

        GridData tabGData = new GridData();
        tabGData.verticalAlignment = GridData.FILL;
        tabGData.horizontalAlignment = GridData.FILL;
        tabGData.horizontalSpan = 2;
        tabGData.grabExcessHorizontalSpace = true;
        tabGData.grabExcessVerticalSpace = true;

        this.pdfPagesTabFolder = new TabFolder(shell, SWT.NONE);
        this.pdfPagesTabFolder.setLayoutData(tabGData);

        //     TabItem tabItem2 = new TabItem(this.pdfPagesTabFolder, SWT.NONE);

        // prefilling with data

        this.updateOpenFileLabel();
    }

    private void updateOpenFileLabel() {
        this.openFileLabel.setText("The file currently open in the editor: " + this.openFileName);
    }

    private TabItem createPdfPage(String title, ExtractorOperationsManager opManager){
        TabItem pageTab = new TabItem(this.pdfPagesTabFolder, SWT.NONE);
        PdfPageComposite content = new PdfPageComposite(this.pdfPagesTabFolder, opManager);
        pageTab.setControl(content);
        pageTab.setText(title);
        return pageTab;
    }
    
    protected void openDocument(String filename) throws IOException, COSLoadException{
        java.util.List<ExtractorOperationsManager> operationManagers;

        operationManagers = PlotsExtractor.renderDocumentPages(filename);
       
        this.openFileName = filename;
        this.updateOpenFileLabel();
        int page = 1;
        
        for (ExtractorOperationsManager opManager: operationManagers){
            TabItem tabPage = createPdfPage("Page " + page, opManager);
            page++;
        }
    }

    public ExtractorInterface() {
        this.openFileName = "";
        this.init_interface();
    }

    public void run() {
        /** 
         * run the application
         */
        this.shell.open();
        while (!this.shell.isDisposed()) {
            if (!this.display.readAndDispatch()) {
                this.display.sleep();
            }
        }
        this.display.dispose();
    }
    /// debug function: testing the interval tree implementation
    protected void testTheIntervalTrees() throws IOException{
        IntervalTree intervals = new IntervalTree<String>(-1000, 1000);
        Images.writeImageToFile(intervals.renderTree(), "c:\\intervalTrees\\1.png");
        intervals.addInterval(1, 5, "A");
        Images.writeImageToFile(intervals.renderTree(), "c:\\intervalTrees\\2.png");
//        intervals.addInterval(15.0, 20.0, "B");
//        Images.writeImageToFile(intervals.renderTree(), "c:\\intervalTrees\\3.png");
        intervals.addInterval(2, 7, "C");
        Images.writeImageToFile(intervals.renderTree(), "c:\\intervalTrees\\4.png");
       // intervals.removeInterval(1, 5, "A");
        Images.writeImageToFile(intervals.renderTree(), "c:\\intervalTrees\\5.png");
        Map a = intervals.getIntersectingIntervals(2, 3);
            

        intervals.removeInterval(2, 7, "C");
        Images.writeImageToFile(intervals.renderTree(), "c:\\intervalTrees\\6.png");
//        intervals.addInterval(0, 3, "D");
//        Images.writeImageToFile(intervals.renderTree(), "c:\\intervalTrees\\5.png");
//        intervals.performNecessaryRotations(intervals.root);
//        Images.writeImageToFile(intervals.renderTree(), "c:\\intervalTrees\\6.png");

    }

    public static void main(String[] args) throws IOException, COSLoadException {

        ExtractorInterface exInterface = new ExtractorInterface();
        exInterface.testTheIntervalTrees(); // debug only
        exInterface.openDocument("c:\\pdf\\tests\\proper_raster_image_one_page.pdf");
//         exInterface.openDocument("c:\\pdf\\tibor_1.pdf");
 //       exInterface.openDocument("c:\\pdf\\1007.0043.pdf");
        exInterface.run();
    }
}
