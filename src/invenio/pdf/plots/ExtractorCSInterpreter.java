package invenio.pdf.plots;

import java.util.Map;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.cos.COSNumber;
import de.intarsys.pdf.cos.COSString;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;

public class ExtractorCSInterpreter extends CSPlatformRenderer {

    private ExtractorOperationsManager operationsManager;

    public ExtractorCSInterpreter(Map paramOptions, IGraphicsContext igc, ExtractorOperationsManager opManager) {
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

    /// reimplementation of the text operations
   

    @Override
    protected void render_BT(CSOperation operation) {
        System.out.println("render_BT(" + operation.toString() + ") - begin text block");
        super.render_BT(operation);
    }

    @Override
    protected void render_Do(CSOperation operation) throws CSException {
        System.out.println("render named xobject");
        System.out.println("");
    //    super.render_Do(operation);
    }
    // Handlers for 4 main text operators (we do not care about changing the text
    // state. only real drawin matters here)

    @Override
    protected void render_DoubleQuote(CSOperation operation) throws CSException {
        System.out.println("Rendering text (\" operator)");
        super.render_DoubleQuote(operation);
    }

    @Override
    protected void render_Quote(CSOperation operation) throws CSException {
        System.out.println("Rendering text (\' operator)");
        super.render_Quote(operation);
    }

    @Override
    protected void render_Tj(CSOperation operation) throws CSException {
        System.out.println("Rendering text (Tj operator)");
        this.operationsManager.addTextOperation(operation);
        super.render_Tj(operation);
    }

    @Override
    protected void render_TJ(CSOperation operation) throws CSException {
     //   System.out.println("Rendering text (TJ operator)");
        this.operationsManager.addTextOperation(operation);
        super.render_TJ(operation);
    }
}
