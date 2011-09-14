package uk.ac.ebi.fgpt.conan.dao;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcessRun;
import uk.ac.ebi.fgpt.conan.model.ConanTask;

import java.util.Date;
import java.util.List;

/**
 * A data access object for retrieving {@link ConanTask}s and associated details from some datasource used to persist
 * this information.
 *
 * @author Tony Burdett
 * @date 18-Oct-2010
 */
public interface ConanTaskDAO {
    /**
     * Indicates whether this DAO will automatically set task and process run IDs for any newly saved {@link ConanTask}s
     * and {@link uk.ac.ebi.fgpt.conan.model.ConanProcessRun}s.
     * <p/>
     * Some implementations - often those that use backing databases - will set ids automatically by using the sequence
     * value for the relevant table.
     * <p/>
     * Other implementations may require that IDs have been manually set before the task can be saved.  Even when this
     * is the case, good implementations will check that each task and process run has a valid and unique ID before
     * allowing it to be saved.
     *
     * @return true if this DAO will automatically set IDs, false otherwise
     */
    boolean supportsAutomaticIDAssignment();

    /**
     * Gets the task with the specified ID.
     *
     * @param taskID the ID of the task to retrieve
     * @return the task that matches this id.
     */
    ConanTask<? extends ConanPipeline> getTask(String taskID);

    /**
     * Returns a list of all known tasks. This includes all pending, running and completed tasks - basically a history
     * of everything that has ever been submitted.  Tasks should be returned ordered by creation date.
     *
     * @return the list of all  tasks
     */
    List<ConanTask<? extends ConanPipeline>> getAllTasks();

    /**
     * Returns a list of all known tasks, excluding process information. This includes all pending, running and
     * completed tasks - basically a history of everything that has ever been submitted.  Tasks should be returned
     * ordered by creation date.
     *
     * @return the list of all  tasks
     */
    List<ConanTask<? extends ConanPipeline>> getAllTasksSummary();

    /**
     * Gets a list of the specified number of tasks. This includes all pending, running and completed tasks - basically
     * a history of everything that has ever been submitted.  Tasks should be returned ordered by creation date.
     *
     * @param maxRecords   the maximum number of tasks to return
     * @param startingFrom the first task index to return
     * @return the list of all  tasks
     */
    List<ConanTask<? extends ConanPipeline>> getAllTasks(int maxRecords, int startingFrom);

    /**
     * Gets a list of the specified number of tasks. This includes all pending, running and completed tasks - basically
     * a history of everything that has ever been submitted.  The tasks returned are ordered by creation date by
     * default.
     *
     * @param maxRecords   the maximum number of tasks to return
     * @param startingFrom the first task index to return
     * @param orderBy      the ConanTask property to order the results by
     * @return the list of all  tasks
     */
    List<ConanTask<? extends ConanPipeline>> getAllTasks(int maxRecords, int startingFrom, String orderBy);

    /**
     * Gets a list of all tasks that have a "pending" status.  Tasks in this list may have been executed but failed:
     * tasks that fail should highlight their failure to the submitter, and flag the task as pending.
     *
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getPendingTasks();

    /**
     * Gets a list of all tasks that have a "pending" status, excluding process info.  Tasks in this list may have been
     * executed but failed: tasks that fail should highlight their failure to the submitter, and flag the task as
     * pending.
     *
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getPendingTasksSummary();

    /**
     * Gets a list of all tasks that have a "running" status.
     *
     * @return the currently executing tasks
     */
    List<ConanTask<? extends ConanPipeline>> getRunningTasks();

    /**
     * Gets a list of all tasks that have a "running" status, excluding process info.
     *
     * @return the currently executing tasks
     */
    List<ConanTask<? extends ConanPipeline>> getRunningTasksSummary();

    /**
     * Gets a list of all tasks have a "completed" status.  This includes tasks that completed successfully, and those
     * that completed because a process failed and was subsequently marked as complete by the submitter.
     *
     * @return the tasks that have completed
     */
    List<ConanTask<? extends ConanPipeline>> getCompletedTasks();

    /**
     * Gets a list of all tasks have a "completed" status, excluding process info.  This includes tasks that completed
     * successfully, and those that completed because a process failed and was subsequently marked as complete by the
     * submitter.
     *
     * @return the tasks that have completed
     */
    List<ConanTask<? extends ConanPipeline>> getCompletedTasksSummary();

    /**
     * Gets a list of the specified number of tasks have a "completed" status.  This includes tasks that completed
     * successfully, and those that completed because a process failed and was subsequently marked as complete by the
     * submitter.  The records are ordered by completion date by default: this is equivalent to calling {@link
     * #getCompletedTasks(int, int, String)} with a value of "completionDate".
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getCompletedTasks(int maxRecords, int startingFrom);

    /**
     * Gets a list of the specified number of tasks have a "completed" status, returning a summary view that excludes
     * process info.  This includes tasks that completed successfully, and those that completed because a process failed
     * and was subsequently marked as complete by the submitter.  The records are ordered by completion date by default:
     * this is equivalent to calling {@link #getCompletedTasks(int, int, String)} with a value of "completionDate".
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getCompletedTasksSummary(int maxRecords, int startingFrom);

    /**
     * Gets a list of the specified number of tasks have a "completed" status.  This includes tasks that completed
     * successfully, and those that completed because a process failed and was subsequently marked as complete by the
     * submitter.  The "order by" field should be the value of the property name of ConanTask by which to sort the
     * results.
     *
     * @param maxRecords   the maximum number of records to return
     * @param startingFrom the position in the list of tasks to start from
     * @param orderBy      the ConanTask property to order the results by
     * @return a list of all tasks pending execution
     */
    List<ConanTask<? extends ConanPipeline>> getCompletedTasks(int maxRecords, int startingFrom, String orderBy);

    /**
     * Persists new tasks to the backing datasource.  Generally, after creating new {@link
     * uk.ac.ebi.fgpt.conan.model.ConanTask} you should save it with this method and then use the returned reference
     * instead of the original object: this allows some implementations of this interface to rereference equal tasks to
     * the existing objects.
     * <p/>
     * Some implementations may set the task ID for you when you save a new task, whereas other implementations may
     * require that you have set it yourself.  See {@link #supportsAutomaticIDAssignment()} for more details.
     *
     * @param conanTask the task to save
     * @return a reference to the (potentially modified) version of the task being saved
     */
    <P extends ConanPipeline> ConanTask<P> saveTask(ConanTask<P> conanTask);

    /**
     * Updates an existing task to the new state given.
     *
     * @param conanTask the task to update
     * @param <P>       the pipeline type associated with the updated task
     * @return a reference to the newly updated conan task, for chaining
     * @throws IllegalArgumentException if there was no task with the given ID
     */
    <P extends ConanPipeline> ConanTask<P> updateTask(ConanTask<P> conanTask)
            throws IllegalArgumentException;

    /**
     * Saves process runs to the backing database.  Generally, whenever a task starts executing a new process, a new
     * process run object should be generated and stored - this will cause a new process run to be inserted into the
     * Conan 2 datasource.  Alternatively, if the process run already exists, the process run will be updated.  Process
     * runs in the datasource are assumed to have unique IDs, so if you create and save a new process run with a
     * conflicting ID to one already in the datasource, it will be overwritten.  Generally, process runs are only ever
     * updated with an end date.
     * <p/>
     * Some implementations may set the ID for new process runs for you whenever you save one, whereas other
     * implementations may require that you have set it yourself.  See {@link #supportsAutomaticIDAssignment()} for more
     * details.
     *
     * @param conanTaskID     the ID of the task to assign the new process run to
     * @param conanProcessRun the process run to save, whether new or updated
     * @param <P>             the pipeline type associated with the updated task
     * @return a reference to the newly updated conan task, for chaining
     * @throws IllegalArgumentException if there was no task with the given ID
     */
    <P extends ConanPipeline> ConanTask<P> saveProcessRun(String conanTaskID, ConanProcessRun conanProcessRun)
            throws IllegalArgumentException;

    /**
     * Retrieves completed tasks from the underlying database which have a task name matching the supplied string.  Task
     * name searches should be case insensitive and should be considered as a fragment for matching - in other words,
     * the actual search should be <code>LIKE '%{name}%'</code> or the equivalent.
     *
     * @param name the task name to search by
     * @return the list of conan tasks matching the search parameter
     */
    List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name);

    /**
     * Retrieves completed tasks from the underlying database which have a task name matching the supplied string and
     * were submitted by the user with the given ID.  Task name searches should be case insensitive and should be
     * considered as a fragment for matching - in other words, the actual search should be <code>LIKE '%{name}%'</code>
     * or the equivalent.
     *
     * @param name   the task name to search by
     * @param userID the user ID of the user submitting the tasks to search for
     * @return the list of conan tasks matching the search parameter
     */
    List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name, String userID);

    /**
     * Retrieves completed tasks from the underlying database which have a task name matching the supplied string and
     * completed after the given date.  Task name searches should be case insensitive and should be considered as a
     * fragment for matching - in other words, the actual search should be <code>LIKE '%{name}%'</code> or the
     * equivalent.
     *
     * @param name     the task name to search by
     * @param fromDate the earliest date of completion to search for tasks from
     * @return the list of conan tasks matching the search parameter
     */
    List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name, Date fromDate);

    /**
     * Retrieves completed tasks from the underlying database which have a task name matching the supplied string and
     * completed between the supplied dates.  The from date parameter may be null: in this case, all tasks before the
     * toDate should be returned. Task name searches should be case insensitive and should be considered as a fragment
     * for matching - in other words, the actual search should be <code>LIKE '%{name}%'</code> or the equivalent.
     *
     * @param name     the task name to search by
     * @param fromDate the earliest date to search for tasks from
     * @param toDate   the latest date to search for tasks from
     * @return the list of conan tasks matching the search parameter
     */
    List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name, Date fromDate, Date toDate);

    /**
     * Retrieves completed tasks from the underlying database which have a task name matching the supplied string, were
     * submitted by the user with the given ID, and were completed after the given date.  Task name searches should be
     * case insensitive and should be considered as a fragment for matching - in other words, the actual search should
     * be <code>LIKE '%{name}%'</code> or the equivalent.
     *
     * @param name     the task name to search by
     * @param userID   the user ID of the user submitting the tasks to search for
     * @param fromDate the earliest date to search for tasks from
     * @return the list of conan tasks matching the search parameter
     */
    List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name,
                                                                  String userID,
                                                                  Date fromDate);

    /**
     * Retrieves completed tasks from the underlying database which have a task name matching the supplied string, were
     * submitted by the user with the given ID, and completed between the supplied dates.  If the from date parameter is
     * null, tasks should only be filtered if they completed after the to date parameter.  Task name searches should be
     * case insensitive and should be considered as a fragment for matching - in other words, the actual search should
     * be <code>LIKE '%{name}%'</code> or the equivalent.
     *
     * @param name     the task name to search by
     * @param userID   the user ID of the user submitting the tasks to search for
     * @param fromDate the earliest date to search for tasks from
     * @param toDate   the latest date to search for tasks from
     * @return the list of conan tasks matching the search parameter
     */
    List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name,
                                                                  String userID,
                                                                  Date fromDate,
                                                                  Date toDate);
}
