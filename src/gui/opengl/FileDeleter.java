package gui.opengl;

import java.io.File;

/**
 * Contains a {@link #main(String[])} method used to delete a given file.
 */
public class FileDeleter {

    /**
     * Tries to delete the file (not directory) given as the second command line argument within the timeout specified
     * as the first argument. If the timeout is negative a single attempt to delete the file will be made.
     *
     * @param args
     *         first the timeout in ms, second the path to the file to delete
     */
    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Invalid number of arguments. Expecting 2 arguments - first the timeout in ms, second "
                    + "the path to the file to delete.");
            return;
        }

        int timeOut;
        String timeoutString = args[0];
        try {
            timeOut = Integer.parseInt(timeoutString);
        } catch (NumberFormatException e) {
            System.err.println("Invalid timeout " + timeoutString);
            return;
        }

        String pathname = args[1];
        File toDelete = new File(pathname);

        if (!toDelete.exists()) {
            System.err.println(pathname + " does not exist.");
            return;
        }

        if (toDelete.isDirectory()) {
            System.err.println(pathname + " is a directory.");
            return;
        }

        long startTime = System.currentTimeMillis();
        do {
            if (toDelete.delete()) {
                return;
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while trying to delete " + pathname);
                    return;
                }
            }
        } while ((System.currentTimeMillis() - startTime) < timeOut);

        System.err.println("Timeout while trying to delete " + pathname);
    }
}
