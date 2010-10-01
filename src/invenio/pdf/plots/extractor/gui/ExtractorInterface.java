/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.plots.extractor.gui;

import de.intarsys.pdf.parser.COSLoadException;
import invenio.pdf.plots.PlotsExtractor;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

    private TabItem createPdfPage(BufferedImage image, String title){
        TabItem pageTab = new TabItem(this.pdfPagesTabFolder, SWT.NONE);
        PdfPageComposite content = new PdfPageComposite(this.pdfPagesTabFolder, image);
        pageTab.setControl(content);
        pageTab.setText(title);
        return pageTab;
    }
    
    protected void openDocument(String filename) throws IOException, COSLoadException{
        BufferedImage i;
        java.util.List<BufferedImage> images =
                  PlotsExtractor.renderDocumentPages(filename);
        this.openFileName = filename;
        this.updateOpenFileLabel();
        int page = 1;
        for (BufferedImage image: images){
            createPdfPage(image, "Page " + page);
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
        exInterface.openDocument("c:\\pdf\\tests\\proper_raster_image_one_page.pdf");
 //       exInterface.openDocument("c:\\pdf\\1007.0043.pdf");
        exInterface.run();
    }
}
