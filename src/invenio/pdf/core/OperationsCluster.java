package invenio.pdf.core;

import java.util.List;

/**
 * A class representing a cluster of operations.
 * @author Piotr
 */
public class OperationsCluster {
    public List<Operation> operations; // the content stream of this cluster
    public int layoutRegion;
};
