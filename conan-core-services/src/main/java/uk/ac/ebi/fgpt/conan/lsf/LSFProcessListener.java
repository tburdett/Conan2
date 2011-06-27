package uk.ac.ebi.fgpt.conan.lsf;

import java.util.EventListener;

/**
 * An event listener that monitors a process submitted to an LSF cluster.  Callback functions are provided for whenever
 *
 * @author Tony Burdett
 * @date 01-Nov-2010
 */
public interface LSFProcessListener extends EventListener {
    /**
     * Called whenever a monitored LSF Output File indicates that the underlying process has finished
     *
     * @param evt an event reporting the change to the file
     */
    void processComplete(LSFProcessEvent evt);

    /**
     * Called whenever a monitored LSF output file indicates the the underlying process wrote output.
     *
     * @param evt an event reporting the change to the file
     */
    void processUpdate(LSFProcessEvent evt);

    /**
     * Called whenever an error occurs in monitoring this process (for example, if the output file being monitored is
     * deleted by an external process).
     *
     * @param evt an event reporting the change to the file
     */
    void processError(LSFProcessEvent evt);
}
