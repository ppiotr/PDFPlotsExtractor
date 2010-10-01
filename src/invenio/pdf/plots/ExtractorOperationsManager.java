package invenio.pdf.plots;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Set;

import de.intarsys.pdf.content.CSOperation;

/**
 * 
 * @author Piotr Praczyk
 *
 */

public class ExtractorOperationsManager {
	/**
	 * Managing operations from the document operations stream (remembering attributes for them).
	 * Contains informations about:
	 *    current operation : operation that has been last started
	 *    operation boundries : rectangles affected by the execution of an operation
	 */
	
	private CSOperation currentOperation; // currently performed operation
	private HashMap<CSOperation, Rectangle2D> operationBoundaries; // Boundries of areas affected by PS operations
//	private Set<CSOperation> textOperations; // operations drawing the text
	
	public ExtractorOperationsManager(){
		this.currentOperation = null;
		this.operationBoundaries = new HashMap<CSOperation, Rectangle2D>();
	};
	
	public void setCurrentOperation(CSOperation op){
		this.currentOperation = op;
	};
	
	public void unsetCurrentOperation(){
		/**
		 * Remove the current operation (none is being performed)
		 */
		this.currentOperation = null;
	};
	
	public CSOperation getCurrentOperation(){
		/**
		 * get the last started CSOperation
		 */
		return this.currentOperation;
	};
	
	public void setOperationBoundary(CSOperation op, Rectangle2D rec){
		this.operationBoundaries.put(op, rec);
	};
	
	public Rectangle2D getOperationBoundary(CSOperation op){
		return this.operationBoundaries.get(op); // will return null if key is not present
	};
	
	public void extendCurrentOperationBoundary(Rectangle2D rec){
		/**
		 *  Extend the boundary of a current operation by a given rectangle. 
		 *  (find a minimal rectangle containing current boundary and the rectangle passed as a parameter)
		 */
		//TODO: implement
		this.setOperationBoundary(this.getCurrentOperation(), rec);
	};
};