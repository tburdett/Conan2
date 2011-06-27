package uk.ac.ebi.fgpt.conan.lsf;

/**
 * An object that models a system process running on the LSF cluster.  This is not a {@link
 * uk.ac.ebi.fgpt.conan.model.ConanProcess} implementation.
 *
 * @author Tony Burdett
 * @date 03-Nov-2010
 */
public interface LSFProcess {
    public static final String UNSPECIFIED_COMPONENT_NAME = "unspecified";

    /**
     * Adds a listener that listens to this LSF process and provides callback events on any changes
     *
     * @param listener the listener to add
     */
    void addLSFProcessListener(LSFProcessListener listener);

    /**
     * Removes a listener that is listening to this LSFProcess
     *
     * @param listener the listener to remove
     */
    void removeLSFProcessListener(LSFProcessListener listener);

    /**
     * Returns true if this process has completed.
     *
     * @return true if the process is complete, false otherwise
     */
    boolean isComplete();

    /**
     * Gets the exit code this system process exited with.  By convention, 0 is sucess and anything else is a fail. This
     * call blocks until the exit code has been obtained, if the process is not yet complete.
     *
     * @return the system exit code
     * @throws InterruptedException if the process was interrupted whilst waiting
     */
    int waitForExitCode() throws InterruptedException;
}
