package invenio.pdf.core.documentProcessing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author piotr
 */
public class OperationTools {

    /** A collection of utility methods allowing to determine properties of PDF operations 
    Build based on PDF reference v. 1.7, page 196
     */
    private static OperationTools instance = null;
    private HashMap<String, HashSet<String>> operators;
    private HashSet<String> operatorCategories;
    private HashSet<String> textCategories;
    private HashSet<String> graphicalCategories;

    private HashSet<String> arrayToSet(String[] array) {
        HashSet<String> result = new HashSet<String>();
        result.addAll(Arrays.asList(array));
        return result;
    }

    private OperationTools() {
        // we have only one instance as data has to be initialised but this has to happen only once

        this.operatorCategories = this.arrayToSet(new String[]{"generalGraphicsState", "specialGraphicsState",
                    "pathConstruction", "pathPainting", "clippingPaths", "textObjects", "textState",
                    "textPositioning", "textShowing", "type3Fonts", "color", "shadingPatterns",
                    "inlineImages", "xObjects", "markedContent", "compatibility"});
        
        this.textCategories = this.arrayToSet(new String[]{"textObjects", "textState",
                    "textPositioning", "textShowing", "type3Fonts"});
        
        this.graphicalCategories = this.arrayToSet(new String[]{"generalGraphicsState", "specialGraphicsState",
                    "pathConstruction", "pathPainting", "clippingPaths", "color", "shadingPatterns",
                    "inlineImages", "xObjects"});
         
        this.operators = new HashMap<String, HashSet<String>>();
        this.operators.put("generalGraphicsState", this.arrayToSet(new String[]{"w", "j", "j", "M", "d", "ri", "i", "gs"}));
        this.operators.put("specialGraphicsState", this.arrayToSet(new String[]{"Q", "q", "cm"}));
        this.operators.put("pathConstruction", this.arrayToSet(new String[]{"m", "l", "c", "v", "y", "h", "re"}));
        this.operators.put("pathPainting", this.arrayToSet(new String[]{"s", "S", "f", "F", "f*", "B", "B*", "b", "b*", "n"}));
        this.operators.put("clippingPaths", this.arrayToSet(new String[]{"W", "W*"}));
        this.operators.put("textObjects", this.arrayToSet(new String[]{"BT", "ET"}));
        this.operators.put("textState", this.arrayToSet(new String[]{"Tc", "Tw", "Tz", "TL", "Tf", "Tr", "Ts"}));
        this.operators.put("textPositioning", this.arrayToSet(new String[]{"TD", "Td", "Tm", "T*"}));
        this.operators.put("textShowing", this.arrayToSet(new String[]{"Tj", "TJ", "'", "\""}));
        this.operators.put("type3Fonts", this.arrayToSet(new String[]{"d0", "d1"}));
        this.operators.put("color", this.arrayToSet(new String[]{"CS", "cs", "SC", "SCN", "sc", "scn", "G", "g", "RG", "rg", "K", "k"}));
        this.operators.put("shadingPatterns", this.arrayToSet(new String[]{"sh"}));
        this.operators.put("inlineImages", this.arrayToSet(new String[]{"BI", "DI", "EI"}));
        this.operators.put("xObjects", this.arrayToSet(new String[]{"Do"}));
        this.operators.put("markedContent", this.arrayToSet(new String[]{"MP", "DP", "BMC", "BDC", "EMC"}));
        this.operators.put("compatibility", this.arrayToSet(new String[]{"BX", "EX"}));

    }

    public static OperationTools getInstance() {
        if (OperationTools.instance == null) {
            OperationTools.instance = new OperationTools();
        }
        return OperationTools.instance;
    }
    
    private boolean isPresentInCategories(HashSet<String> categories, String operation){
        /**Determine if instructions are present in one of given categories*/
        for (String category: categories){
            if (this.operators.get(category).contains(operation)){
                return true;
            }
        }
        return false;
    }
    /** interface methods */
    
    public boolean isValidOperation(String op){
        /** Determines if instruction is valid (present in any of dictionaries */
        return this.isPresentInCategories(this.operatorCategories, op);
    }
    
    public boolean isTextOperation(String op){
        /** Determines if instruction is valid (present in any of dictionaries */
        return this.isPresentInCategories(this.textCategories, op);
    }
    
    public boolean isGraphicalOperation(String op){
        /** Determines if instruction is valid (present in any of dictionaries */
        return this.isPresentInCategories(this.graphicalCategories, op);
    }
}
