package uk.ac.ebi.fgpt.conan.ae.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.magetab.utils.CommandExecutionException;
import uk.ac.ebi.arrayexpress2.magetab.utils.ProcessRunner;

import java.io.File;
import java.io.IOException;

/**
 * General utils class for common tasks
 *
 * @author tburdett
 * @date 08-Jul-2008
 */
public class ProcessUtils {
    /**
     * Create all the passed files.  Any required directories will be created on demand.
     *
     * @param files the files to create
     * @return true if this succeeds, false otherwise
     */
    public static boolean createFiles(File... files) {
        boolean success = true;

        // get all files
        for (File file : files) {
            // get the parent directory
            boolean pfCreated = true;
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                // create all dirs if they don't exist
                pfCreated = parentFile.mkdirs();
            }

            // now create the child file
            boolean fCreated = true;
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                fCreated = false;
            }

            success = success && pfCreated && fCreated;
        }

        return success;
    }

    /**
     * Delete all these files.  Any directories passed to this method will be ingored.
     *
     * @param files files to delete - ignores directories
     * @return true if successful, false otherwise
     */
    public static boolean deleteFiles(File... files) {
        final Logger log = LoggerFactory.getLogger(ProcessUtils.class);

        boolean success = true;

        // get all files
        for (File file : files) {
            if (!file.isDirectory()) {
                // delete the file
                boolean fDeleted = file.delete();
                if (!fDeleted) {
                    log.debug("Couldn't delete file " + file.getAbsolutePath());
                }

                success = success && fDeleted;
            }
        }

        return success;
    }

    /**
     * Execute a string as a shell command, returning the output as an array of strings, where each element in the array
     * represents one line of the output of the command.
     * <p/>
     * If the native process exits with an error code other than 0, a {@link CommandExecutionException} is thrown.  It
     * is the callers responsibility to repsond to this as appropriate, as an exit code of 2 may only represent warnings
     * rather than a critical failure.  If a CommandExecutionException is thrown, access to the error stream is
     * available by calling
     * <pre><code>
     * catch (CommandExecutionException cme) { String[] errors =
     * cme.getErrorOutput() }
     * </code></pre>
     * If no exception is thrown, any output issued by the output stream is returned in the String[] returned from this
     * method.
     *
     * @param command the command to execute
     * @return the output of this process
     * @throws CommandExecutionException if the command writes to stderr
     * @throws IOException               if the command fails to execute at runtime
     */
    public static String[] runCommand(String command)
            throws CommandExecutionException, IOException {
        final Logger log = LoggerFactory.getLogger(ProcessUtils.class);

        log.debug("Issuing command: [" + command + "]");
        ProcessRunner runner = new ProcessRunner();
        runner.redirectStderr(true);
        String[] output = runner.runCommmand(command);
        if (output.length > 0) {
            log.debug("Response from command [" + command + "]: " +
                    output.length + " lines, first line was " + output[0]);
        }
        return output;
    }
}
