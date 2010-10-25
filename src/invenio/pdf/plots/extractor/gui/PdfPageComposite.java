/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.plots.extractor.gui;

import de.intarsys.pdf.content.CSOperation;
import invenio.pdf.plots.PDFPageManager;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 *
 * @author piotr
 */
public class PdfPageComposite extends Composite {

    /**
     * An implementation of the tab item used to display one pdf page
     */
    private Canvas imageCanvas;
    private Image renderedPage;
    private Table operationsTable;
    private TableColumn columnOperators;
    private TableColumn columnBoundaries;
    private TableColumn columnRenderingMethods;
    private TableColumn columnOperatorArguments;
    private TableColumn columnIsTextOperator;

    private PDFPageManager operationsManager;
    
    static ImageData convertToSWT(BufferedImage bufferedImage) {
        /**
         * This function has been downloaded from the Eclipse examples
         * (http://www.eclipse.org/swt/snippets/). There should not be any license problems
         */
        //TODO: check the license of this code !
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
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
        }
        return null;
    }
   


    public PdfPageComposite(TabFolder folder, PDFPageManager opManager) {
        super(folder, SWT.NONE);
        this.operationsManager = opManager;
        GridLayout pageLayout = new GridLayout();
        pageLayout.numColumns = 1;
        this.setLayout(pageLayout);

        BufferedImage pageImage = opManager.getRenderedPage();
        ScrolledComposite canvasComposite = new ScrolledComposite(this,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        canvasComposite.setLayout(new FillLayout());
        canvasComposite.setExpandHorizontal(true);
        canvasComposite.setExpandVertical(true);
        canvasComposite.setMinSize(pageImage.getWidth(), pageImage.getHeight());


        GridData imageLayoutData = new GridData();
        imageLayoutData.grabExcessVerticalSpace = true;
        imageLayoutData.grabExcessHorizontalSpace = true;
        imageLayoutData.verticalAlignment = SWT.FILL;
        imageLayoutData.horizontalAlignment = SWT.FILL;
        imageLayoutData.minimumHeight = 500;
        canvasComposite.setLayoutData(imageLayoutData);

        this.imageCanvas = new Canvas(canvasComposite, SWT.NONE);
        canvasComposite.setContent(this.imageCanvas);
        ImageData pageImageData = convertToSWT(pageImage);
        
        this.renderedPage = new Image(this.getDisplay(), pageImageData);

        this.imageCanvas.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                Rectangle bounds = renderedPage.getBounds();
                //System.out.println("Image parameters width: " + bounds.width + " height: " + bounds.height);
                e.gc.drawImage(renderedPage, 0, 0);
               
               // e.gc.drawImage(someImage, e.x, e.y, e.width, e.height, e.x, e.y, e.width, e.height);
                e.gc.drawRectangle(bounds);

                //e.gc.drawRectangle(0, 0, width, height);
                //  e.gc.drawImage(image, 0, 0, 100, 100, 200, 10, 200, 50);
            }
        });

        // now creating the grid displaying operation informations
        GridData tableLayout = new GridData();
        tableLayout.grabExcessHorizontalSpace = true;
        tableLayout.grabExcessVerticalSpace = true;
        tableLayout.verticalAlignment = SWT.FILL;
        tableLayout.horizontalAlignment = SWT.FILL;
        tableLayout.minimumHeight = 200;
        

        this.operationsTable = new Table(this, SWT.NONE);
        this.operationsTable.setLayoutData(tableLayout);
        this.operationsTable.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        this.operationsTable.setHeaderVisible(true);
        this.operationsTable.setLayout(new FillLayout());


        this.columnOperators = new TableColumn(this.operationsTable, SWT.NONE);
        this.columnOperators.setText("PDF Operator");
        this.columnIsTextOperator = new TableColumn(this.operationsTable, SWT.NONE);
        this.columnIsTextOperator.setText("text operator");
        this.columnBoundaries = new TableColumn(this.operationsTable, SWT.NONE);
        this.columnBoundaries.setText("result boundaries");
        this.columnOperatorArguments = new TableColumn(this.operationsTable, SWT.NONE);
        this.columnOperatorArguments.setText("operator arguments");
        this.columnRenderingMethods = new TableColumn(this.operationsTable, SWT.NONE);
        this.columnRenderingMethods.setText("rendering methods");
        /// sample item

        TableItem item = new TableItem(this.operationsTable, SWT.NONE);
	item.setText(new String[] {"operator name","results boundary",});
	//item.setForeground(red);
        this.columnBoundaries.pack();
        this.columnOperators.pack();
        this.columnRenderingMethods.pack();
        this.columnOperatorArguments.pack();

        this.fillOperationsTable();
    }

    private void fillOperationsTable(){
        for (CSOperation operation: this.operationsManager.getOperations()){
            TableItem item = new TableItem(this.operationsTable, SWT.NONE);
            // preparing the data about a particular operation
            String operator = operation.getOperator().toString();
            Rectangle2D boundaryRec = this.operationsManager.getOperationBoundary2D(operation);
            String boundary = (boundaryRec == null) ? "(null)" : boundaryRec.toString();
            String operatorArguments = operation.toString();
            String renderingMethods = "";
            String isTextOperator = (this.operationsManager.getTextOperations().contains(operation)) ? "yes" : "no";
            List<String> methods = this.operationsManager.getRenderingMethods(operation);
            if (methods != null){
                boolean first = true;
                for (String method : methods){
                    if (!first){
                        renderingMethods += ", ";
                    }
                    renderingMethods += method;
                    first = false;
                }
            }
            item.setText(new String[] {operator, isTextOperator, boundary, operatorArguments, renderingMethods});
        }
        this.columnOperatorArguments.pack();
        this.columnBoundaries.pack();
        this.columnOperators.pack();
        this.columnRenderingMethods.pack();
        this.columnIsTextOperator.pack();

    }

}
