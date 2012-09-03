package invenio.pdf.core.documentProcessing;

import java.util.Map;
import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;
import java.util.HashSet;

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
    private int recursionDepth;
    private HashSet<String> graphicalOperators;

    public ExtractorCSInterpreter(Map paramOptions, IGraphicsContext igc, PDFPageOperationsManager opManager) {
        super(paramOptions, igc);
        this.operationsManager = opManager;
        this.graphicalOperators = new HashSet<String>();

        this.recursionDepth = 0;


        // building a collection of all graphical operators
        String[] graphicalOperatorsArray = {"m", "l", "c", "v", "y", "h", "re", "s", "S", "f","F","f*","B","B*","b","b*","n","BI","DI","EI", "Do"};
        for (String op : graphicalOperatorsArray) {
            this.graphicalOperators.add(op);
        }
    }

    private boolean isGraphicalOperation(CSOperation op) {
        String operator = op.getOperator().toString();
        return this.graphicalOperators.contains(operator);
    }
    //// We override the operation processing so that it uses the operations manager

    @Override
    protected void process(CSOperation operation) throws CSException {

        if (this.isGraphicalOperation(operation)) {
            // we do this especially in deeper recursion calls to make a top level operation graphical based on presence of a graphical operator !
            this.operationsManager.enforceGraphicalOperation(operation);
        }
        // The recursion depth allows us to deal with the primary 
        // operations (not for example introduced by type3 fonts)
        if (this.recursionDepth == 0) {
            this.operationsManager.setCurrentOperation(operation);
        }

        /*
        try {
        this.operationsManager.contentStreamStateMachine.process(operation);
        } catch (Exception ex) {
        System.out.println("Something went wrong with parsing the stream of operations. " + ex.getMessage());
        }
         * 
         */

        this.recursionDepth++;

        super.process(operation);

        this.recursionDepth--;
        if (this.recursionDepth == 0) {
            this.operationsManager.unsetCurrentOperation();
        }
    }
    /**
     * Returns a stream of higher level objects
     * @return
     */
    /*
    public LinkedList<PDFObject> getObjects(){
    return this.operationsManager.contentStreamStateMachine.getObjects();
    }
     * 
     */
}
