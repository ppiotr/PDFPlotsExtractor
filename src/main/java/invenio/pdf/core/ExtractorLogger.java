package invenio.pdf.core;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author piotr
 */
public class ExtractorLogger {
    private static int level = 1; // the logger level

    /**
     * A function logging a single message
     * @param l level of the message to be logged. Starting from 0. Smaller number
     *          signifies a more important message
     * @param msg message to be logged
     */

    public static void logMessage(int l, String msg){
        if (l <= level){
            System.out.println(msg);
        }
    }

    /**
     * Sets the level of logging (all the messages with the level smaller
     * or equal to the declared level will appear
     */
    public static void setLevel(int l){
        level = l;
    }
}
