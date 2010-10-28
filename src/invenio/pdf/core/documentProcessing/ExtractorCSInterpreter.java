package invenio.pdf.core.documentProcessing;

import java.util.Map;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.cos.COSArray;
import de.intarsys.pdf.cos.COSObject;
import de.intarsys.pdf.cos.COSString;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;
import java.util.Iterator;
import java.util.List;

class ExtractorCSInterpreter extends CSPlatformRenderer {

    private PDFPageOperationsManager operationsManager;

    public ExtractorCSInterpreter(Map paramOptions, IGraphicsContext igc, PDFPageOperationsManager opManager) {
        super(paramOptions, igc);
        this.operationsManager = opManager;
    }

    //// We override the operation processing so that it uses the operations manager
    @Override
    protected void process(CSOperation operation) throws CSException {
        this.operationsManager.setCurrentOperation(operation);
        super.process(operation);
        this.operationsManager.unsetCurrentOperation();
    }

    @Override
    protected void render_Do(CSOperation operation) throws CSException {
        super.render_Do(operation);
    }
    // Handlers for 4 main text operators (we do not care about changing the text
    // state. only real drawin matters here)

    @Override
    protected void render_DoubleQuote(CSOperation operation) throws CSException {
        this.operationsManager.addTextOperation(operation);
        extractTextOperationString(operation);
        super.render_DoubleQuote(operation);
    }

    @Override
    protected void render_Quote(CSOperation operation) throws CSException {
        this.operationsManager.addTextOperation(operation);
        extractTextOperationString(operation);
        super.render_Quote(operation);
    }

    @Override
    protected void render_Tj(CSOperation operation) throws CSException {
        this.operationsManager.addTextOperation(operation);
        extractTextOperationString(operation);
        super.render_Tj(operation);
    }

    @Override
    protected void render_TJ(CSOperation operation) throws CSException {
        this.operationsManager.addTextOperation(operation);
        extractTextOperationString(operation);
        super.render_TJ(operation);
    }

    /**
     * Text is passed to text operations in more or less standard manner - this 
     * function tries to extract this text in a geenral manner
     * @param operation
     * @return
     */
    private String extractTextOperationString(CSOperation operation) {
        // interpreting the arguments -> reading the spring
        Iterator<COSObject> operandsIter = operation.getOperands();
        COSObject firstOperand = null;
        if (operandsIter.hasNext()) {
            firstOperand = operandsIter.next();
        }

        if (firstOperand instanceof COSArray) {
            COSArray operandsArray = (COSArray) firstOperand;
            for (COSObject operand : (List<COSObject>) operandsArray.getObjects()) {
                if (operand instanceof COSString) {
                    COSString stringArg = (COSString) operand;
                    System.out.print(stringArg.getValueString(""));
                }
            }
        } else if (firstOperand instanceof COSString){
            COSString arg = (COSString) firstOperand;
            System.out.print(arg.getValueString(""));
        }
        System.out.print(" ");
        return "";
    }
}
