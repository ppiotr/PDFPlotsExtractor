package invenio.pdf.core.documentProcessing;

import java.util.Map;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;
import invenio.pdf.core.PDFObjects.PDFObject;
import java.util.LinkedList;

/**
 * An implementation of the jPod CSPlatformRenderer - a class responsible for
 * processing PDF operators.
 *
 * All the operations are passed to the super class implementation.
 * The only purpose of this class is keeping track of which operation is
 * currentlly processed.
 *
 * This is achieved by sharing an instance of PDFPageOperationsManager with
 * an instance of ExtractorGraphics2D. When an operation is about to be
 * processed, it is marked as a current operation inside the manager.
 * After processing an operation, the current operation is reseted in the
 * manager. As a result, all the ExtractorGraphics2D calls executed by the
 * superclass between the beginning of processing of an operation and ending,
 * do have access to the currently processed operation and so, are able to
 * assign various parameters to it.
 *
 * WARNING:
 *    This implementation assumes that all the graphical operations are
 *    immediately passed to the Graphics2D instance, which is the case with
 *    jPod.
 * @author piotr
 */
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

        try {
            this.operationsManager.contentStreamStateMachine.process(operation);
        } catch (Exception ex) {
            System.out.println("Something went wrong with parsing the stream of operations. " + ex.getMessage());
        }

        super.process(operation);

        this.operationsManager.unsetCurrentOperation();
    }

    /**
     * Returns a stream of higher level objects
     * @return
     */
    public LinkedList<PDFObject> getObjects(){
        return this.operationsManager.contentStreamStateMachine.getObjects();
    }
}
