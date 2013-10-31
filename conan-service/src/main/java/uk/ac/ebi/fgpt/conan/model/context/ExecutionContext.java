
package uk.ac.ebi.fgpt.conan.model.context;

import java.io.File;

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
     * It's common that you will which to vary the jobname, foreground job and monitor file throughout the pipeline for
     * different processes.  This method enables you to set the variable context, while retaining the common context such
     * as the locality, scheduling system and external process configuration.
     * @param jobName        The job name for the current process to execute
     * @param foregroundJob  Whether to run the process in the foreground or background
     * @param monitorFile    A location that output from the process can be stored.  Can be used by some schedulers (such
     *                       as LSF) to track the job's progress.
     */
    void setContext(String jobName, boolean foregroundJob, File monitorFile);


    /**
     * Should return true if this ConanProcess should be executed in the foreground.  i.e. we should wait until the job is
     * complete before continueing
     * @return
     */
    boolean isForegroundJob();

    /**
     * Retreives the monitor file for this execution context
     * @return
     */
    File getMonitorFile();

    /**
     * Retreives the job name for this execution context
     * @return
     */
    String getJobName();

    /**
     * Makes a deep-copy of this ExecutionContext
     * @return
     */
    ExecutionContext copy();
}
