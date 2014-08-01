
package uk.ac.ebi.fgpt.conan.model.context;

import uk.ac.ebi.fgpt.conan.model.monitor.ProcessAdapter;

import java.io.File;
import java.util.List;


public interface Scheduler {


    /**
     * Returns the submit command for this scheduler.  E.g. "bsub" for Platform LSF.
     *
     * @return The submit command for this scheduler.
     */
    String getSubmitCommand();

    /**
     * Returns the scheduler args associated with this scheduler
     *
     * @return Scheduler args
     */
    SchedulerArgs getArgs();

    /**
     * Sets the Scheduler Args for this scheduler object.
     *
     * @param args The scheduler args
     */
    void setArgs(SchedulerArgs args);

    /**
     * Creates a command that wraps the provided internalCommand with this scheduler's specific details.
     *
     * @param command The command describing the proc to execute on this scheduler.
     * @return A command that should be used to execute the provided command on this scheduler.
     */
    String createCommand(String command, boolean isForegroundJob);

    /**
     * Creates a command that can execute the specified wait condition on this architecture.  Typically this is used to
     * generate a command that can be executed as a stand alone proc, which will not complete until the wait condition
     * has been fulfilled.
     *
     * @param waitCondition The wait/dependency condition to convert into an scheduler specific command.
     * @return A scheduler specific wait command.
     */
    String createWaitCommand(String waitCondition);

    /**
     * Generates a job kill command for this scheduler, using the supplied job identifier
     *
     * @param jobId The job identifier
     * @return A command for killing the specified job(s) on this scheduler.
     */
    String createKillCommand(String jobId);

    /**
     * Creates a <code>ProcessAdapter</code> specific to this scheduler.  Automatically, uses the monitor file and interval stored in
     * this object.
     *
     * @return A <code>ProcessAdapter</code> that monitors progress of a scheduled proc.
     */
    ProcessAdapter createProcessAdapter();

    /**
     * Creates a <code>ProcessAdapter</code> specific to this scheduler.  Automatically, uses the monitor file and interval stored in
     * this object.  Keys the monitor file based on the provided job array index.
     *
     * @return A <code>ProcessAdapter</code> that monitors progress of a scheduled proc in a job array.
     */
    ProcessAdapter createProcessAdapter(int jobArrayIndex);

    /**
     * Creates a <code>ProcessAdapter</code> specific to this scheduler.  Uses a custom monitor file and interval.
     *
     * @param monitorFile     The file that this adapter will monitor.
     * @param monitorInterval The frequency at which this adapter will monitor the file.
     * @return A proc adapter specific to this scheduler.
     */
    ProcessAdapter createProcessAdapter(File monitorFile, int monitorInterval);

    /**
     * Creates a wait condition for this architecture, using a job name pattern
     *
     * @param exitStatus The type of exit status to wait for
     * @param condition  The condition to wait for
     * @return A new wait condition object suitable for this architecture
     */
    String createWaitCondition(ExitStatus.Type exitStatus, String condition);

    /**
     * Creates a wait condition for this architecture, using a list of job ids
     *
     * @param exitStatus The type of exit status to wait for
     * @param jobIds  The list of job ids to wait for
     * @return A new wait condition object suitable for this architecture
     */
    String createWaitCondition(ExitStatus.Type exitStatus, List<Integer> jobIds);


    /**
     * Returns a deep copy of this Scheduler
     *
     * @return
     */
    Scheduler copy();

    /**
     * The name of this scheduler
     * @return
     */
    String getName();

    /**
     * Used to mark whether or not this scheduler monitors processes running in the foreground through an external log
     * file.
     * @return
     */
    boolean usesFileMonitor();


    /**
     * Used to mark whether or not this scheduler generates a job id from the standard output
     * @return
     */
    boolean generatesJobIdFromOutput();

    /**
     * Parses an output line to get the job id
     * @param line
     * @return
     */
    int extractJobIdFromOutput(String line);

    /**
     * Returns the string that should be replaced by a job array index in a command line
     * @return The string that represent the job array index for this scheduler.
     */
    String getJobIndexString();
}
