package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A service that can be used to generate or retrieve tasks from user supplied input parameters.  It is the
 * responsibility of this task service to ensure that tasks are retrieved from memory or from a datasource as
 * appropriate to ensure that it is not possible to obtain multiple duplicate copies of the same task, each with
 * different state.  This typically means that pending or completed tasks can be acquired from a DAO, but that running
 * tasks (with states that might be highly changeable) should be recovered directly to avoid changing the state of the
 * task in the database and leaving a "dirty" task still executing.
 * <p/>
 * Generally, given a pipeline name and a set of parameters, a task can be generated using a TaskFactory.  This service
 * basically acts as a facade over the task creation functionality to provide a simple interface to UI controllers.
 *
 * @author Tony Burdett
 * @date 30-Jul-2010
 */
public interface ConanTaskService {
    /**
     * Creates a task given the name of the pipeline, the name of starting process, the input parameters to use, the
     * priority of this task, and the user who created this submission.  The inputParameters argument maps the parameter
     * name (the key) to the argument being supplied (the value, formatted as a string).  The priority should be
     * supplied: higher priority tasks "jump the queue" ahead of lower priority ones. This method validates that the
     * pipeline with the supplied name exists, and that it the arguments supplied are also correct, before generate the
     * task (probably delegating to a {@link uk.ac.ebi.fgpt.conan.factory.ConanTaskFactory}.
     *
     * @param pipelineName         the name of the pipeline to generate a task for
     * @param startingProcessIndex the index of the first process to start from
     * @param inputParameters      the parameters that this task should be created with
     * @param priority             the priority this task has
     * @param conanUser            the user who created this task    @return the resulting task
     * @return the resulting task
     * @throws IllegalArgumentException if either the pipeline referenced by name could not be found, or if the set of
     *                                  supplied parameters was not matched to the given pipeline.
     */
    ConanTask<? extends ConanPipeline> createNewTask(String pipelineName,
                                                     int startingProcessIndex,
                                                     Map<String, String> inputParameters,
                                                     ConanTask.Priority priority,
                                                     ConanUser conanUser)
            throws IllegalArgumentException;

    /**
     * Retrieve a task given the task ID.  This should fetch all the details about the given task, irrespective of
     * whether this task is queued, running or complete.  If there is no task with this ID, this will return null
     *
     * @param taskID the unique ID of the task to recover
     * @return the task, if found, or null otherwise
     */
    ConanTask<? extends ConanPipeline> getTask(String taskID);

    /**
     * Gets a list of all tasks. This includes all pending, running and completed tasks - basically a history of
     * everything task that has ever been created.
     *
     * @return the list of all submitted tasks
     */
    List<ConanTask<? extends ConanPipeline>> getTasks();

    /**
     * Gets a list of all tasks, summarised so as not to include all process information. This includes all pending,
     * running and completed tasks - basically a history of everything task that has ever been created.
     *
     * @return the list of all submitted tasks
     */
    List<ConanTask<? extends ConanPipeline>> getTasksSummary();

    /**
     * Gets a list of all tasks. This includes all pending, running and completed tasks - basically a history of
     * everything task that has ever been created.  The records are ordered by creation date by default: this is
     * equivalent to calling {@link #getTasks(int, int, String)} with a value of "completionDate".
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getTasks(int maxRecords, int startingFrom);

    /**
     * Gets a list of all tasks. This includes all pending, running and completed tasks - basically a history of
     * everything task that has ever been created.  The "order by" field should be the value of the property name of
     * ConanTask by which to sort the results.
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @param orderBy      the ConanTask property to order the results by
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getTasks(int maxRecords, int startingFrom, String orderBy);

    /**
     * Returns a list of all tasks that have been submitted but are pending execution.  Tasks in this list may have been
     * executed but failed: tasks that fail should highlight their failure to the submitter, and flag the task as
     * pending.
     *
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getPendingTasks();

    /**
     * Returns a list of all tasks that have been submitted but are pending execution, summarised so as to exclude the
     * query for process information.  Tasks in this list may have been executed but failed: tasks that fail should
     * highlight their failure to the submitter, and flag the task as pending.
     *
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getPendingTasksSummary();

    /**
     * Gets a list of the specified number of tasks that have a "pending" status.  Tasks in this list may have been
     * executed but failed: tasks that fail should highlight their failure to the submitter, and flag the task as
     * pending. The records are ordered by submission date by default: this is equivalent to calling {@link
     * #getPendingTasks(int, int, String)} with a value of "submissionDate".
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getPendingTasks(int maxRecords, int startingFrom);

    /**
     * Gets a list of the specified number of tasks that have a "pending" status.  Tasks in this list may have been
     * executed but failed: tasks that fail should highlight their failure to the submitter, and flag the task as
     * pending.
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @param orderBy      the ConanTask property to order the results by
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getPendingTasks(int maxRecords, int startingFrom, String orderBy);

    /**
     * Returns a list of all tasks that are currently being executed.
     *
     * @return the currently executing tasks
     */
    List<ConanTask<? extends ConanPipeline>> getRunningTasks();

    /**
     * Returns a list of all tasks that are currently being executed, summarised so as to exclude the query for process
     * information
     *
     * @return the currently executing tasks
     */
    List<ConanTask<? extends ConanPipeline>> getRunningTasksSummary();

    /**
     * Gets a list of the specified number of tasks that have a "running" status. The records are ordered by start date
     * by default: this is equivalent to calling {@link #getRunningTasks(int, int, String)} with a value of
     * "startDate".
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getRunningTasks(int maxRecords, int startingFrom);

    /**
     * Gets a list of the specified number of tasks that have a "running" status.
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @param orderBy      the ConanTask property to order the results by
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getRunningTasks(int maxRecords, int startingFrom, String orderBy);

    /**
     * Returns a list of all tasks that have been executed and completed.  This includes tasks that completed
     * successfully, and those that completed because a process failed and was subsequently marked as complete by the
     * submitter.
     *
     * @return the tasks that have completed
     */
    List<ConanTask<? extends ConanPipeline>> getCompletedTasks();

    /**
     * Returns a list of all tasks that have been executed and completed, summarised so as to exclude the query for
     * process information.  This includes tasks that completed successfully, and those that completed because a process
     * failed and was subsequently marked as complete by the submitter.
     *
     * @return the tasks that have completed
     */
    List<ConanTask<? extends ConanPipeline>> getCompletedTasksSummary();

    /**
     * Gets a list of the specified number of tasks have a "completed" status.  This includes tasks that completed
     * successfully, and those that completed because a process failed and was subsequently marked as complete by the
     * submitter.  The records are ordered by completion date by default.
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getCompletedTasks(int maxRecords, int startingFrom);

    /**
     * Search through the completed tasks for any tasks that match the given name, submitting user, and fall between the
     * range of dates.  For unspecified fields, you can supply nulls. Underlying implementations are free to specify
     * wildcard search patterns or default behaviours.
     *
     * @param name      the task name to search for
     * @param conanUser the user that submitted the task to search for
     * @param fromDate  searches by tasks finished after this date
     * @param toDate    searches by tasks finished before this date
     * @return the list of tasks that fulfil the given search criteria
     */
    List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name,
                                                                  ConanUser conanUser,
                                                                  Date fromDate,
                                                                  Date toDate);
}
