/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.core.documentProcessing;

import de.intarsys.cwt.environment.IGraphicsContext;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.CSOperation;
import de.intarsys.pdf.platform.cwt.rendering.CSPlatformRenderer;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author piotr
 */
public class ExtractorSelectiveInterpreter extends CSPlatformRenderer {

    private final Set<CSOperation> acceptedOperations;

    public ExtractorSelectiveInterpreter(Map paramOptions, IGraphicsContext igc, Set<CSOperation> accepted) {
        super(paramOptions, igc);
        this.acceptedOperations = accepted;
    }

    //// We override the operation processing so that it uses the operations manager
    @Override
    protected void process(CSOperation operation) throws CSException {
        if (this.acceptedOperations.contains(operation)) {
            super.process(operation);
        }
    }
}
