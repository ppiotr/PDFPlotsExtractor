/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package invenio.common;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author piotr
 */
public class FileUtils {

    /**
     * Retrieves all the names of full paths of files with a given extension
     * stored in the given directory
     *
     * @param inputDirName path to the considered directory
     * @param extension extension of the files to consider
     * @return
     */
    public static List<String> getRelevantFiles(String inputDirName, String extension) throws Exception {
        File inputDir = new File(inputDirName);
                
        if (!inputDir.exists()) {
            throw new Exception("The input directory does not exist");
        }

        String[] fileNames = inputDir.list(new FilenameFilterImpl(extension));
        return Arrays.asList(fileNames);
    }

    private static class FilenameFilterImpl implements FilenameFilter {

        private final String extension;

        public FilenameFilterImpl(String fnameExtension) {
            this.extension = fnameExtension;
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(this.extension);
        }
    }
}
