/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.plots;

import invenio.common.ExtractorGeometryTools;
import invenio.pdf.core.ExtractorParameters;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.IPDFDocumentFeatureProvider;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author piotr
 */
public class PlotsProvider implements IPDFDocumentFeatureProvider {

    @Override
    public Plots calculateFeature(PDFDocumentManager docManager) throws FeatureNotPresentException{
        // gathering all the plot descriptors from all the pages and generaing one collection
        Plots result = new Plots();
        for (int pageNum = 0; pageNum < docManager.getPagesNumber(); ++pageNum) {
            result.plots.add(getPlotsFromPage(docManager.getPage(pageNum)));
        }
        return result;
    }

    @Override
    public String getProvidedFeatureName() {
        return Plots.featureName;
    }

    /**
     * Based on the content of the page, calculate the most likely safe margins
     * between graphic operations inside one plot.
     *
     *
     * @param manager Page manager containing all the operations creating one
     *                document page
     * @return the integer array with two elements : horizontal and vertical
     *          margins.
     */
    private static int[] calculateGraphicsMargins(PDFPageManager manager) {
        ExtractorParameters parameters =
                ExtractorParameters.getExtractorParameters();
        double hPercentage = parameters.getHorizontalMargin();
        double vPercentage = parameters.getVerticalMargin();

        Rectangle boundary = manager.getPageBoundary();
        int hMargin = (int) (boundary.getWidth() * hPercentage);
        int vMargin = (int) (boundary.getHeight() * vPercentage);
        System.out.println("Margins: horizontal=" + hMargin + " vertical=" + vMargin);
        return new int[]{hMargin, vMargin};
    }

    /**
     * Finds all the plots present in the PDF page. Plots are extracted together
     * with captions but without references because captions appear
     * on the same page and textual references have to be found globally in the document.
     *
     * @param manager
     * @return List of plot descriptors
     */
    public static List<Plot> getPlotsFromPage(PDFPageManager manager) throws FeatureNotPresentException {
        List<Plot> plots = new LinkedList<Plot>();

        int[] margins = calculateGraphicsMargins(manager);

        /*************
         * Treating graphics operations - clustering them, filtering and
         * including appropriate text operations
         **************/
        GraphicalAreas graphicalAreas =
                (GraphicalAreas) manager.getPageFeature(GraphicalAreas.featureName);

        if (graphicalAreas == null) {
            throw new FeatureNotPresentException(GraphicalAreas.featureName);
        }

        Map<Rectangle, List<Operation>> shrinkedRegions =
                ExtractorGeometryTools.shrinkRectangleMap(graphicalAreas.areas,
                margins[0], margins[1]);


        Map<Rectangle, List<Operation>> graphicalPlotRegions =
                PlotHeuristics.removeFalsePlots(shrinkedRegions);


        Map<Rectangle, List<Operation>> plotRegions =
                PlotHeuristics.includeTextParts(graphicalPlotRegions, manager);

//        Map<Rectangle, List<CSOperation>> plotRegions = graphicalPlotRegions;

        // we are done with plot images -> creating plot structures for every
        // selected region

        for (Rectangle area : plotRegions.keySet()) {
            Plot plot = new Plot();
            plot.setBoundary(area);
            plot.addOperations(plotRegions.get(area));
            plot.setPageNumber(manager.getPageNumber());
            plots.add(plot);
        }

        // 2) too small areas/areas with too small aspect rations -> not plots !

        // Now including text operations that are overlapping with plots -> they
        // are part of a plot

//        for (TextOp operation : textOperations) {
//            Rectangle intersecting = findIntersectingRegion(operation, graphicalRegions);
//            if (intersecting != null) {
//                extendRegionByRectangle();
//            }
//        }



        /** Treating text operations - we want to recover the text flow and a \
         * very general view of its structure - division to blocks

         */
        // now clustering the text operations - we want to be able to detect
        // text blocks. For example caption is usually a separate block that is
        // separated by a bigger distance than others
        return plots;
    }
}
