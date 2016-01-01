/* A state machine clustering operations into groups corresponding
 * to PDF graphical objects -> transforming the raw content stream
 * into higher level content stream
 *
 * For more information on the construction fo this state machine
 * please refer to the PDF Reference version 1.6, page 197
 */
package invenio.pdf.core.PDFObjects;

import de.intarsys.pdf.content.CSOperation;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;

public class ContentStreamStateMachine {
    private static Log log = LogFactory.getLog(ContentStreamStateMachine.class);  

    private PDFObject currentObject;
    private LinkedList<PDFObject> storedObjects;
    private final String[] operationsListPathPainting = new String[]{"S", "s", "f", "F", "f*", "B", "B*", "b", "b*", "n"};
    private final String[] operationsListGeneralGraphicsState = new String[]{"w", "j", "J", "M", "d", "ri", "i", "gs"};
    private final String[] operationsListSpecialGraphicsState = new String[]{"q", "Q", "cm"};
    private final String[] operationsListPathConstruction = new String[]{"m", "l", "c", "v", "y", "h", "re"};
    private final String[] operationsListClippingPaths = new String[]{"W", "W*"};
    private final String[] operationsListTextObjects = new String[]{"BT", "ET"};
    private final String[] operationsListTextState = new String[]{"Tc", "Tw", "Tz", "TL", "Tf", "Tr", "Ts"};
    private final String[] operationsListTextPositioning = new String[]{"Td", "TD", "Tm", "T*"};
    private final String[] operationsListTextShowing = new String[]{"Tj", "TJ", "'", "\""};
    private final String[] operationsListType3Fonts = new String[]{"d0", "d1"};
    private final String[] operationsListColor = new String[]{"CS", "cs", "SC", "SCN", "sc", "scn", "G", "g", "RG", "rg", "K", "k"};
    private final String[] operationsListShadingPatterns = new String[]{"sh"};
    private final String[] operationsListInlineImages = new String[]{"BI", "ID", "EI"};
    private final String[] operationsListXObjects = new String[]{"Do"};
    private final String[] operationsListMarkedContent = new String[]{"MP", "DP", "BMC", "BDC", "EMC"};
    private final String[] operationsListCompatibility = new String[]{"BC", "EX"};

    private PDFObject currentBoundaryObject;

    public ContentStreamStateMachine() {
        this.currentObject = new PDFPageDescriptionObject();
        this.storedObjects = new LinkedList<PDFObject>();
    }

    ///// common infrastructure functions
    private void incorrectOperation() {
        log.info("The PDF operation appeared in a wrong context");
        //TODO: possibly throw some exception here, for the moment there is no need
    }

    private String getOperationIdentifier(CSOperation operation) {
        return operation.getOperator().toString();
    }

    /**
     * Adds current object and adds it at the end of the list of already detected entities
     */
    private boolean checkListOfOperations(String operationType, String[] possibleValues) {
        for (String possibility : possibleValues) {
            if (possibility.equals(operationType)) {
                return true;
            }
        }
        return false;
    }

    private void acceptObject() {
        this.storedObjects.add(this.currentObject);
    }

    /**
     * processing if we are in the state "Path object"
     */
    private void processPath(CSOperation operation) {
        String operationId = getOperationIdentifier(operation);
        if (checkListOfOperations(operationId, new String[]{"W", "W*"})) {
            //TODO: Verify if W* is a correct string according to the toString() method
            // entering the state of a path object

            PDFObject tmpObject = new PDFClippingPathObject((PDFPathObject) this.currentObject);


            tmpObject.addOperation(operation);
            tmpObject.setBoundary(this.currentObject.getBoundary());
            
            this.currentObject = tmpObject;
            this.currentBoundaryObject = this.currentObject;
            

        } else if (checkListOfOperations(operationId, operationsListPathPainting)) {
            this.currentObject.addOperation(operation);
            this.acceptObject();
            this.currentObject = new PDFPageDescriptionObject();
        } else if (checkListOfOperations(operationId, operationsListPathConstruction)){
            this.currentObject.addOperation(operation);
        } else {
            log.warn("Incorrect operator in the Path state: " + operationId);
        }
    }

    /**
     * Clipping path object
     */
    private void processClipping(CSOperation operation) {
        String operationId = getOperationIdentifier(operation);
        if (checkListOfOperations(operationId, operationsListPathPainting)) {
            this.currentObject.addOperation(operation);
            this.acceptObject();
            this.currentObject = new PDFPageDescriptionObject();
        } else {
            log.warn("Incorrect PDF operation in the Path clipping state");
        }
    }

    private void processText(CSOperation operation) {
        String operationId = getOperationIdentifier(operation);
        if (checkListOfOperations(operationId, new String[]{"ET"})) {
            this.currentObject.addOperation(operation);
            
            this.acceptObject();
            this.currentObject = new PDFPageDescriptionObject();
        } else if (checkListOfOperations(operationId, operationsListGeneralGraphicsState)
                || checkListOfOperations(operationId, operationsListColor)
                || checkListOfOperations(operationId, operationsListTextState)
                || checkListOfOperations(operationId, operationsListTextShowing)
                || checkListOfOperations(operationId, operationsListTextPositioning)
                || checkListOfOperations(operationId, operationsListMarkedContent)) {
            this.currentObject.addOperation(operation);
        } else {
            log.warn("Incorrect PDF operation in the state of text inputing");
        }
    }

    private void processShading(CSOperation operation) {
        log.warn("Weird... this should not happen ! (calling the ContentStreamStateMachine::processShading()");
    }

    private void processExternal(CSOperation operation) {
       log.warn("Weird... this should not happen ! (calling the ContentStreamStateMachine::processExternal()");
    }

    private void processInline(CSOperation operation) {
        String operationId = getOperationIdentifier(operation);
        if (checkListOfOperations(operationId, new String[]{"ID"})) {
            this.currentObject.addOperation(operation);            
        } else if (checkListOfOperations(operationId, new String[]{"EI"})) {
            this.currentObject.addOperation(operation);

            this.acceptObject();
            this.currentObject = new PDFPageDescriptionObject();

        } else{
            log.error("incorrect operation");
        }
    }

    /** we are on the page description level
     * 
     * @param operation
     */
    private void processPageDescription(CSOperation operation) {
        String operationId = getOperationIdentifier(operation);
        if (checkListOfOperations(operationId, new String[]{"m", "re"})) {
            // entering the state of a path object
            this.acceptObject();
            this.currentObject = new PDFPathObject();
            this.currentObject.addOperation(operation);
        } else if (checkListOfOperations(operationId, new String[]{"BT"})) {
            this.acceptObject();
            this.currentObject = new PDFTextObject();
            this.currentObject.addOperation(operation);
        } else if (checkListOfOperations(operationId, new String[]{"sh"})) {
            this.acceptObject();
            this.currentObject = new PDFShadingObject();
            this.currentObject.addOperation(operation);
            this.acceptObject();
            this.currentObject = new PDFPageDescriptionObject();
        } else if (checkListOfOperations(operationId, new String[]{"Do"})) {
            this.acceptObject();
            this.currentObject = new PDFExternalObject();
            this.currentObject.addOperation(operation);
            this.acceptObject();
            this.currentObject = new PDFPageDescriptionObject();
        } else if (checkListOfOperations(operationId, new String[]{"BI"})) {
            this.acceptObject();
            this.currentObject = new PDFInlineImageObject();
            this.currentObject.addOperation(operation);
        } else {
            this.currentObject.addOperation(operation);
        }

        this.currentBoundaryObject = this.currentObject;
    }

    /**
     *
     * @param operation
     * @throws Exception
     */
    public void process(CSOperation operation) throws Exception {
        this.currentBoundaryObject = this.currentObject;
        
        if (this.currentObject instanceof PDFPageDescriptionObject) {
            processPageDescription(operation);
        } else if (this.currentObject instanceof PDFClippingPathObject) {
            processClipping(operation);
        } else if (this.currentObject instanceof PDFExternalObject) {
            processExternal(operation);
        } else if (this.currentObject instanceof PDFInlineImageObject) {
            processInline(operation);
        } else if (this.currentObject instanceof PDFPathObject) {
            processPath(operation);
        } else if (this.currentObject instanceof PDFShadingObject) {
            processShading(operation);
        } else if (this.currentObject instanceof PDFTextObject) {
            processText(operation);
        } else {
            throw new Exception("Incorrect state of the PDF content stream state machine");
        }
    }

    public LinkedList<PDFObject> getObjects() {
        return this.storedObjects;
    }

    public void extendCurrentBoundary(Rectangle rec) {
        if (this.currentBoundaryObject != null){
            this.currentBoundaryObject.extendBoundary(rec);
        }
    }
}
