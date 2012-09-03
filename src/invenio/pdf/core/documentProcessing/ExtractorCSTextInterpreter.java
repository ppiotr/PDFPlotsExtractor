
package invenio.pdf.core.documentProcessing;

import de.intarsys.pdf.content.CSDeviceBasedInterpreter;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.content.text.CSTextExtractor;
import java.awt.Rectangle;
import java.util.Map;

/**
 * A class allowing the extraction of the text painted by text operators.
 * This information can not be retrieved inside the ExtractorGraphics2D calss
 * because all the text information is lost there in favour of painting a
 * particular shape
 * 
 * @author piotr
 */
class ExtractorCSTextInterpreter extends CSDeviceBasedInterpreter {

    protected PDFPageOperationsManager operationsManager;
    protected CSTextExtractor extractor;

    public ExtractorCSTextInterpreter(Map paramOptions, CSTextExtractor device, PDFPageOperationsManager manager) {
        super(paramOptions, device);
        this.operationsManager = manager;
        this.extractor = device;
    }

    @Override
    protected void process(CSOperation operation) throws CSException {
        int initialIndex = extractor.getContent().length();

        //TODO: This is an ugly hack to avoid the library error
        // This should be fixed in the jPod library and then try{}catch should be removed
        // bug report: https://sourceforge.net/tracker/?func=detail&aid=3103823&group_id=203731&atid=986772
        
        try {
            super.process(operation);
        } catch (CSException e) {
            // oops ... error but we have to continue !
        } catch (Exception e){
            // throw new Exception("Ugly workaround exception !");
        }
        int finalIndex = extractor.getContent().length();
        if (initialIndex != finalIndex){
            // mark current operation as a text operation
            this.operationsManager.setOperationTextIndices(operation, new int[]{initialIndex, finalIndex});
            this.operationsManager.addTextOperation(operation);
//            Rectangle r = this.operationsManager.getOperationBoundary(operation);
//            if (r == null){
//                System.out.println("Empty boundary ... something wrong, text detected but no boundary");
//            }
        }

    }
}
