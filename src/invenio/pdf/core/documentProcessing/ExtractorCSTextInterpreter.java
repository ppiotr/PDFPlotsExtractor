/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core.documentProcessing;

import de.intarsys.pdf.content.CSDeviceBasedInterpreter;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.content.ICSDevice;
import de.intarsys.pdf.content.text.CSTextExtractor;
import java.util.Map;

/**
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
        super.process(operation);        
        int finalIndex = extractor.getContent().length();
        this.operationsManager.setOperationTextIndices(operation, new int[]{initialIndex, finalIndex});
    }
}
