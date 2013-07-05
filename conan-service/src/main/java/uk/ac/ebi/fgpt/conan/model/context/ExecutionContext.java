
package uk.ac.ebi.fgpt.conan.model.context;

/**
 * Interface for an Execution Context, which defines where and how a ConanTask or ConanProcess is executed.
 *
 * @author Dan Mapleson
 */
public interface ExecutionContext {

    /**
     * Defines where to execute
     * @return
     */
    Locality getLocality();

    /**
     * Whether or not this execution context defines a scheduler
     * @return
     */
    boolean usingScheduler();

    /**
     * Retrieves the scheduler to use if present
     * @return
     */
    Scheduler getScheduler();

    /**
     * Retrieves the ExternalProcessConfiguration object if defined. If present this can be used to automatically add
     * "pre-commands" to the ConanProcess
     * @return
     */
    ExternalProcessConfiguration getExternalProcessConfiguration();

    /**
     * Should return true if this ConanProcess should be executed in the foreground.  i.e. we should wait until the job is
     * complete before continueing
     * @return
     */
    boolean isForegroundJob();

    /**
     * Defines whether or not this job should be executed in the foreground or not.
     * @param isForegroundJob
     */
    void setForegroundJob(boolean isForegroundJob);

    /**
     * Makes a deep-copy of this ExecutionContext
     * @return
     */
    ExecutionContext copy();
}
