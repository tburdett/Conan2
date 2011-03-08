package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.service.exception.SubmissionException;

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
     * Submit a new task to Conan.  This task will be submitted to the tail of the queue whilst this service is running,
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
}
