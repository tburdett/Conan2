package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.service.exception.SubmissionException;

import java.util.Set;

/**
 * A service that is used to submit new {@link uk.ac.ebi.fgpt.conan.model.ConanTask}s to Conan.  A submission service
 * can only run tasks, but it has awareness of calibrated {@link uk.ac.ebi.fgpt.conan.model.ConanPipeline}s and their
 * constituent {@link uk.ac.ebi.fgpt.conan.model.ConanProcess}es and validates each tasks against these.
 *
 * @author Tony Burdett
 * @date 26-Jul-2010
 */
public interface ConanSubmissionService {
    /**
     * Submit a new task to Conan.  This task will be submitted to the tail of the queue whilst this service is
     * running,
     * unless this task duplicates another.  If the service is shutdown, or is the task is a duplicate, a
     * SubmissionException will be thrown.
     *
     * @param conanTask the task being submitted
     * @throws uk.ac.ebi.fgpt.conan.service.exception.SubmissionException
     *          if the submission is rejected because it duplicates another, or because this service is no longer
     *          accepting new submissions
     */
    void submitTask(ConanTask<? extends ConanPipeline> conanTask) throws SubmissionException;

    /**
     * Resubmits any recovered tasks to Conan.  Recovered tasks are those that were interrupted whilst running (for
     * example, due to a Conan shutdown or failure) and have been retrieved from e.g. a {@link
     * uk.ac.ebi.fgpt.conan.dao.ConanTaskDAO}.  These tasks should immediately be resubmitted, unless the service is
     * shutdown or the resubmitted task duplicates another, in which case it will be rejected.
     *
     * @param conanTask the recovered tasks to submit
     * @throws uk.ac.ebi.fgpt.conan.service.exception.SubmissionException
     *          if the task duplicates another or if this service is no longer accepting submissions
     */
    void resubmitTask(ConanTask<? extends ConanPipeline> conanTask) throws SubmissionException;

    /**
     * Forcibly attempts to interrupt this task, halting execution of any operations currently running (if possible).
     * This should only be used as a measure of last resort, as it can potentially leave processes in an inconsistent
     * state.  Concrete classes should implement this method by taking measures to halt or interrupt running tasks and
     * their processes as smoothly as possible, but with the proviso that tasks should always be removed from the
     * interface as soon as possible even if execution of underlying processes could not be terminated.
     *
     * @param conanTask the task to interrupt
     */
    void interruptTask(ConanTask<? extends ConanPipeline> conanTask);

    /**
     * Returns the set of tasks that have been submitted to Conan and that are currently executing.  Pending tasks, or
     * tasks that are still in the holding queue prior to execution, should not be returned here, but any task that has
     * had it's {@link uk.ac.ebi.fgpt.conan.model.ConanTask#execute()} method called and has not completed execution
     * should be returned by this method.
     *
     * @return the set of currently executing Conan tasks.
     */
    Set<ConanTask<? extends ConanPipeline>> getExecutingTasks();
}
