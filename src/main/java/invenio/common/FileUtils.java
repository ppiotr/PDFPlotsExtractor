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

    /**
     * Returns the name of the file without
     *
     * @param fname
     * @return
     */
    public static String stripFileExt(String fname) {
        int endIndex = fname.length() - 1;
        while (endIndex >= 0 && fname.charAt(endIndex) != '.') {
            endIndex--;
        }
        if (endIndex <= 0) {
            return "";
        }
        return fname.substring(0, endIndex);
    }
}
