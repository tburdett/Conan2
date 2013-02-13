package uk.ac.ebi.fgpt.conan.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class that simplifies the process of delegating to external
 * processes and waiting for the output. By instantiating a new ProcessRunner
 * and sending it the command you wish to execute in the current runtime
 * environment, calling classes will acquire a string array that contains the
 * lines output by the native process, as well as a simple String appended to a
 * ProcessListener.
 *
 * @author Tony Burdett
 * @author Rob Davey
 * @date 13-Nov-2009
 */
public class ProcessRunner {
    private boolean stdoutFinished;
    private boolean stderrFinished;

    private String[] stdout;
    private String[] stderr;

    private boolean redirect = false;
    private boolean unrecoverableException = false;

    private final Logger log =
            LoggerFactory.getLogger(getClass());

    private ProcessListener pl;

    public ProcessRunner() {
    }

    public ProcessRunner(ProcessListener pl) {
        this.pl = pl;
    }

    /**
     * Whether or not to redirect the standard error stream of the native process
     * to standard out.  True to redirect.
     *
     * @param redirect true redirects stdout to stderr
     */
    public synchronized void redirectStderr(boolean redirect) {
        this.redirect = redirect;
    }

    public synchronized String[] runCommmand(String command)
            throws CommandExecutionException, IOException {
        final Process process;
        if (System.getProperty("os.name").contains("Windows")) {
            process = new ProcessBuilder("cmd.exe", "/c", command).start();
        } else {
            // native command for linux
            process = new ProcessBuilder("/bin/sh", "-c", command).start();
        }

        // create the process
        log.debug("Executing native runtime process [" + command + "]");

        // create reading threads
        new Thread() {
            public void run() {
                log.debug("Monitoring stdout for native runtime process...");

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String outputLine;
                List<String> output = new ArrayList<String>();

                try {
                    while ((outputLine = reader.readLine()) != null) {
                        output.add(outputLine);
                        if (pl != null) {
                            pl.appendProgressMessageLine(outputLine);
                        }
                    }
                } catch (IOException e) {
                    log.debug("Encountered an error reading from process stdout,");
                    unrecoverableException = true;
                }

                log.debug("Finished monitoring stdout of runtime process, read " +
                        output.size() + " lines");

                updateStdout(output.toArray(new String[output.size()]));
                stdoutStreamFinished();
            }
        }.start();

        new Thread() {
            public void run() {
                log.debug("Monitoring stderr for native runtime process...");

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                List<String> errors = new ArrayList<String>();

                try {
                    while ((errorLine = reader.readLine()) != null) {
                        errors.add(errorLine);
                        if (pl != null) {
                            pl.appendErrorMessageLine(errorLine);
                        }
                    }
                } catch (IOException e) {
                    unrecoverableException = true;
                }
                log.debug("Finished monitoring stderr of runtime process, read " +
                        errors.size() + " lines");

                updateStderr(errors.toArray(new String[errors.size()]));
                stderrStreamFinished();
            }
        }.start();

        // finally, check for completion and return the output stream if successful
        try {
            if (isComplete()) {
                int exitCode = -1;
                while (exitCode < 0) {
                    try {
                        exitCode = process.waitFor();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }

                if (exitCode > 0) {
                    if (redirect) {
                        log.debug(
                                "Return code was " + process.exitValue() + " for " + command +
                                        ", throwing an exception and redirecting stdout");
                        ArrayList<String> result = new ArrayList<String>();
                        result.addAll(Arrays.asList(stdout));
                        result.addAll(Arrays.asList(stderr));
                        throw new CommandExecutionException(exitCode,
                                result.toArray(new String[result.size()]));
                    } else {
                        log.debug(
                                "Return code was " + process.exitValue() + " for " + command +
                                        ", throwing an exception");
                        throw new CommandExecutionException(exitCode, stderr);
                    }
                } else {
                    if (redirect) {
                        log.debug("Return code was " + process.exitValue() + " for " +
                                command + ", redirecting stderr");
                        ArrayList<String> result = new ArrayList<String>();
                        result.addAll(Arrays.asList(stdout));
                        if (stderr.length > 0) {
                            result.add("");
                            result.add(
                                    "***** THIS OPERATION PRODUCED ERRORS/WARNINGS.  OUTPUT FOR DEBUGGING: *****");
                            result.addAll(Arrays.asList(stderr));
                            result.add(
                                    "***************************************************************************");
                        }

                        return result.toArray(new String[result.size()]);
                    } else {
                        log.debug("Return code was " + process.exitValue() + " for " +
                                command + ", returning stdout");
                        ArrayList<String> result = new ArrayList<String>();
                        result.addAll(Arrays.asList(stdout));
                        return result.toArray(new String[result.size()]);
                    }
                }
            } else {
                throw new IOException("Unrecoverable error whilst processing " +
                        "an external process (" + command + ")");
            }
        } finally {
            // make sure the process is properly cleaned up
            process.getInputStream().close();
            process.getOutputStream().close();
            process.getErrorStream().close();
            process.destroy();
        }
    }

    private synchronized void stdoutStreamFinished() {
        this.stdoutFinished = true;
        notifyAll();
    }

    private synchronized void stderrStreamFinished() {
        this.stderrFinished = true;
        notifyAll();
    }

    private synchronized void updateStdout(String[] stdout) {
        this.stdout = stdout;
    }

    private synchronized void updateStderr(String[] stderr) {
        this.stderr = stderr;
    }

    /**
     * Blocks until both stdout and stderr streams have been fully read.
     *
     * @return true, once stdout and stderr have been read.  False ONLY if an
     *         exception occurs that cannot be recovered from, and reading from
     *         the process had to be aborted.
     */
    private synchronized boolean isComplete() {
        log.debug("Checking completion of the process...");
        while (!(stdoutFinished && stderrFinished)) {
            // not finished yet, so wait for notification
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }

        // now finished, return true
        if (unrecoverableException) {
            unrecoverableException = false;
            log.debug("Process completed with unrecoverable exception");
            return false;
        } else {
            log.debug("Process completed normally");
            return true;
        }
    }
}