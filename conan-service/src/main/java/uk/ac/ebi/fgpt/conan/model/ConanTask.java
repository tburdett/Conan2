package uk.ac.ebi.fgpt.conan.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A task that can be submitted to Conan.  A task is a specific execution of a pipeline, running the constituent {@link
 * ConanProcess}es incrementally. Tasks also include provenance data about the submitter, the data and time it was
 * created, and the priority with which it should be run
 *
 * @author Tony Burdett
 * @date 28-Jul-2010
 */
@JsonSerialize(typing = JsonSerialize.Typing.STATIC)
public interface ConanTask<P extends ConanPipeline> extends Serializable {
    /**
     * Gets the ID of this task.  IDs will normally be assigned to a task upon submission, so do not need to be manually
     * created.
     *
     * @return the task ID, or null if an ID has not been assigned
     */
    String getId();

    /**
     * Sets the ID of this task.
     *
     * @param id the task unique ID
     */
    void setId(String id);

    /**
     * Gets the user-friendly name of this task.  Names exist so that tasks can be given a name that is more meaningful
     * to a user, rather than a random alphanumeric string.  Optionally this may be the same as the task ID, but should
     * only ever be null if the task ID is also null and you have also not chosen to assign a name.
     *
     * @return the user friendly name of this task
     */
    String getName();

    /**
     * Executes this task.  To execute a task means that each {@link ConanProcess} in that task's {@link ConanPipeline}
     * is executed in turn.  The {@link ConanParameter}s supplied to this task must be the union of all parameters
     * required by each process in the pipeline - there is a convenience method to acquire these process on {@link
     * ConanPipeline}, {@link uk.ac.ebi.fgpt.conan.model.ConanPipeline#getAllRequiredParameters()}.
     * <p/>
     * Whilst executing, this task should keep tracking information up to date, such as {@link #getCurrentProcess()},
     * {@link #getCurrentState()} and so on.  This enables a monitoring client to determine the current progress of this
     * task.
     * <p/>
     * Good implementations of this method will check that the calling thread is not interrupted between processes, so
     * as to allow graceful termination of tasks on a shutdown request.  It is very desirable for tasks to cleanly
     * shutdown on such a request, updating tracking information as they do so, so that tasks are easily recoverable on
     * restart.
     * <p/>
     * This method returns true if the task succeeds (indicating all constituent Processes, by definition, succeeded) or
     * false otherwise.
     *
     * @return true if the execution completed successfully, false if not
     * @throws TaskExecutionException if the execution of the task caused an exception
     * @throws InterruptedException   if the execution of the task was interrupted
     */
    boolean execute() throws TaskExecutionException, InterruptedException;

    /**
     * Gets the priority of this task.  High priority tasks should always be executed before medium priority tasks, and
     * medium priority tasks always executed before low priority ones.
     *
     * @return the priority of this task
     */
    Priority getPriority();

    /**
     * Gets the pipeline that this task runs.
     *
     * @return the pipeline this task is part of
     */
    @JsonSerialize(as = ConanPipeline.class) P getPipeline();

    /**
     * Returns a map of parameters to the values set for this Task.
     *
     * @return all supplied parameter values
     */
    Map<ConanParameter, String> getParameterValues();

    /**
     * Gets the {@link ConanUser} that created this task.
     *
     * @return the user who submitted this task
     */
    ConanUser getSubmitter();

    /**
     * Gets the date and time that this task was created.  This should never be null.
     *
     * @return the creation date of this task
     */
    Date getCreationDate();

    /**
     * Gets the date and time that this task was submitted for execution.  If this task was never submitted, this is
     * null.
     *
     * @return the submission date of this task
     */
    Date getSubmissionDate();

    /**
     * Gets the date and time that this task was first executed.  If this task has not yet started, this is null.
     *
     * @return the date this task was executed
     */
    Date getStartDate();

    /**
     * Gets the data and time that this task completed all execution.  If this task has not yet finished, this is null.
     *
     * @return the date the task completed
     */
    Date getCompletionDate();

    /**
     * Gets the list of {@link ConanProcessRun}s for every process that this task executed, in the order in which they
     * were executed.  If the task has not yet been run, this collection should be empty.
     *
     * @return the set of execution tracking objects for every process run
     */
    List<ConanProcessRun> getConanProcessRuns();

    /**
     * Gets a list of the {@link ConanProcessRun}s that have executed for the given process, in the order in which they
     * were executed.  If this process has not yet been run, this should be empty.  If a process fails, it may have been
     * rerun many times.  If the supplied process is not part of the pipeline for this task, an {@link
     * IllegalArgumentException} is raised.
     *
     * @param process the process we want the runs for
     * @return the collection of runs that were executed for this process
     * @throws IllegalArgumentException if the process is not part of the pipeline for this task
     */
    List<ConanProcessRun> getConanProcessRunsForProcess(ConanProcess process) throws IllegalArgumentException;

    /**
     * Returns the first {@link ConanProcess} this tasks executed (or will execute, if still pending).  This may or may
     * not be the first {@link ConanProcess} in the {@link ConanPipeline} for this task.
     *
     * @return the first process that will be executed
     */
    ConanProcess getFirstProcess();

    /**
     * Returns the display name assigned to the first {@link ConanProcess} in the {@link ConanPipeline} that this task
     * executes.
     *
     * @return the display name assigned to the first process for this task
     */
    String getFirstProcessDisplayName();

    /**
     * Returns the {@link uk.ac.ebi.fgpt.conan.model.ConanProcess} that was executed last in this task, provided at
     * least one process has completed.  If no processes have yet completed, this returns null.  If this process is
     * paused, this will return the process that completed before the pause operation occurred.
     *
     * @return the process that was last executed
     */
    ConanProcess getLastProcess();

    /**
     * Returns the display name for the last {@link ConanProcess} in the {@link ConanPipeline} that this task executed.
     * If no processes have yet been executed, this will return null.
     *
     * @return the display name for the last process
     */
    String getLastProcessDisplayName();

    /**
     * Returns the current {@link uk.ac.ebi.fgpt.conan.model.ConanProcess} this task is currently executing.  If the
     * task is not yet running, is paused, or has already completed, this will be null
     *
     * @return the process that is currently being executed, if this task is running
     */
    ConanProcess getCurrentProcess();

    /**
     * Returns the display name for the {@link ConanProcess} that this task is currently executing, if any.  If the task
     * is not yet running, is paused, or has already completed, this will be null.
     *
     * @return the display name of the current process
     */
    String getCurrentProcessDisplayName();

    /**
     * Returns the next {@link ConanProcess} this task will execute, or null if this task is currently executing the
     * final {@link ConanProcess} in the {@link ConanPipeline}.  If the task is paused, this will be the task that will
     * start on resumption.  If the last failed it's last task, this will be the same process that is returned from
     * (@link #getLastProcess()}.
     *
     * @return the process that will be executed next by this task
     */
    ConanProcess getNextProcess();

    /**
     * Returns the display name assigned to the {@link ConanProcess} that this task will execute next, or null if this
     * is the last task.
     *
     * @return the display name assigned to the next process
     */
    String getNextProcessDisplayName();

    /**
     * Gets the current state of this task.  Tasks may have one of several states depending on whether they have started
     * execution or encountered failures.  Each change in state should also be associated with a status message to
     * obtain feedback on, for example, the reason for a task being halted.
     *
     * @return the current state of this task
     */
    State getCurrentState();

    /**
     * Gets a message that is associated with any change in state of this task. This should be provided to indicate, for
     * example, why a task was halted if something went wrong, whether a task was executed from the queue, whether a
     * user flagged a failure, and so on.  This should be a meaningful message to provide feedback to users.
     *
     * @return the message associated with any change in state of this task.
     */
    String getStatusMessage();

    /**
     * Submits a task, which essentially sets a flag indicating the task is known to Conan.  Once submitted, this flag
     * will always be true (so cannot be unset).
     */
    void submit();

    /**
     * Temporarily halts this this task.  After pausing, it will be flagged for intervention.  This effectively mimicks
     * a fail.
     */
    void pause();

    /**
     * Resumes a halted task by commencing the next process in the pipeline.  Tasks may be halted if one of it's
     * processes indicates that it failed, or if it is manually paused with the {@link #pause()} method.  Calling this
     * method allows a task to continue executing as if nothing went wrong, so it assumes that a user has examined the
     * result of the last process, concluded that the failure condition it highlighted was not critical, and allowed the
     * task to resume with the next process it it's pipeline.  For genuine failure cases, the {@link
     * #retryLastProcess()} or {@link #abort()} methods should be used instead.
     */
    void resume();

    /**
     * Resumes a halted task by rerunning the last process that was executed.  Tasks may be halted if one of it's
     * processes indicates that it failed, or if it is manually paused with the {@link #pause()} method.
     */
    void retryLastProcess();

    /**
     * Restarts a halted task from it's first original process.
     */
    void restart();

    /**
     * Indicates that this task conclusively failed and has been reviewed by a user, who confirmed as such.  This should
     * not be called automatically - tasks should be paused pending review - but rather in response to feedback from a
     * user.  Once thios method has been called, this task should be flagged as aborted and any subsequent execution
     * requests should immediately fail with an exception.
     */
    void abort();

    /**
     * Indicates whether this task has ever been submitted.  Tasks that have been previously submitted and currently
     * have a failed state should still return true.
     *
     * @return true if this task has been submitted
     */
    boolean isSubmitted();

    /**
     * Indicates whether this task is currently paused.
     *
     * @return true if the task is paused, false if it is running or has never been submitted
     */
    boolean isPaused();

    public enum Priority {
        LOWEST,
        LOW,
        MEDIUM,
        HIGH,
        HIGHEST
    }

    public enum State {
        CREATED,
        SUBMITTED,
        RECOVERED,
        PAUSED,
        FAILED,
        RUNNING,
        COMPLETED,
        ABORTED
    }
}
