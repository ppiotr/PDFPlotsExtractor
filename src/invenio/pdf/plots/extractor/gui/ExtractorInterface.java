/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.plots.extractor.gui;

import de.intarsys.pdf.parser.COSLoadException;
import invenio.pdf.plots.ExtractorOperationsManager;
import invenio.pdf.plots.PlotsExtractor;
import java.io.IOException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
  

    public static void main(String[] args) throws IOException, COSLoadException {

        ExtractorInterface exInterface = new ExtractorInterface();
        //exInterface.openDocument("c:\\pdf\\tests\\proper_raster_image_one_page.pdf");
//        exInterface.openDocument("c:\\pdf\\1007.0043.pdf");
       // exInterface.openDocument("c:\\pdf\\tests\\modified7_1007.0043.pdf");
exInterface.openDocument("/home/piotr/pdf/1007.0043.pdf");
        //     exInterface.openDocument("c:\\pdf\\tests\\problematic_page.pdf");

        //exInterface.openDocument("c:\\pdf\\tests\\two_plots_one_page.pdf");
//        exInterface.openDocument("c:\\pdf\\tests\\no_plots.pdf");
          //exInterface.openDocument("c:\\pdf\\tests\\some_math.pdf");

        //exInterface.openDocument("c:\\pdf\\tests\\overlaping_one_page.pdf");

//         exInterface.openDocument("c:\\pdf\\tibor_1.pdf");
 //       exInterface.openDocument("c:\\pdf\\1007.0043.pdf");
        exInterface.run();
    }
}
