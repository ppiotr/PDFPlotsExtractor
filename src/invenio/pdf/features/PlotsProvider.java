/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.pdf.features;

import invenio.common.ExtractorGeometryTools;
import invenio.pdf.core.FeatureNotPresentException;
import invenio.pdf.core.IPDFDocumentFeatureProvider;
import invenio.pdf.core.Operation;
import invenio.pdf.core.PDFDocumentManager;
import invenio.pdf.core.PDFPageManager;
import invenio.pdf.core.PDFCommonTools;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author piotr
 */
public class PlotsProvider implements IPDFDocumentFeatureProvider {

    private static String getPlotIdFromCaption(String caption) {
        //TODO: implement using regular expressions
        return Plot.getUniqueIdentifier();
    }

    @Override
    public Plots calculateFeature(PDFDocumentManager docManager) throws FeatureNotPresentException, Exception {
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
     * Finds all the plots present in the PDF page. Plots are extracted together
     * with captions but without references because captions appear
     * on the same page and textual references have to be found globally in the document.
     *
     * @param manager
     * @return List of plot descriptors
     */
    public static List<Plot> getPlotsFromPage(PDFPageManager manager) throws FeatureNotPresentException, Exception {
        List<Plot> plots = new LinkedList<Plot>();

        // first we generate algorithm parameters depending on the page parameters
        //TODO: extend this
        
        int[] margins = PDFCommonTools.calculateGraphicsMargins(manager);


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

        // we are done with plot images -> creating plot structures for every
        // selected region

        for (Rectangle area : plotRegions.keySet()) {
            Plot plot = new Plot();
            plot.setBoundary(area);
            plot.addOperations(plotRegions.get(area));
            plot.setPageNumber(manager.getPageNumber());
            plot.setCaption(getPlotCaption(plot, manager));
            plot.setPageManager(manager);
            plot.setId(getPlotIdFromCaption(plot.getCaption()));
            plots.add(plot);

        }

        return plots;
    }

    private static String getPlotCaption(Plot plot, PDFPageManager pageManager) throws FeatureNotPresentException, Exception {
        TextAreas textAreas =
                (TextAreas) pageManager.getPageFeature(TextAreas.featureName);
        // finding the first text area below the plot
        Rectangle currentArea = null;

        double plotEnding = plot.getBoundary().getMaxY();

        for (Rectangle textRegion : textAreas.areas.keySet()) {
            if (currentArea == null
                    || (currentArea.getMinY() > textRegion.getMinY()
                    && textRegion.getMinY() > plotEnding)) {
                currentArea = textRegion;
            }
        }

        if (currentArea == null) {
            return "";
        } else {
            // we have to determine if the area is really a plot caption !
            String candidate = textAreas.areas.get(currentArea).first;
            if (isPlotCaption(candidate)) {
                return candidate;
            } else {
                return "";
            }
        }
    }

    private static boolean isPlotCaption(String candidate) {
        String prepared = candidate.toLowerCase().trim();
        System.out.println("Processing a potential caption : " + candidate);
        return prepared.startsWith("fig")
                || prepared.startsWith("plot")
                || prepared.startsWith("image")
                || prepared.startsWith("table");
    }
}
