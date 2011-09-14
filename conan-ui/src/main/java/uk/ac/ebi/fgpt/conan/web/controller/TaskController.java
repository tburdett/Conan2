package uk.ac.ebi.fgpt.conan.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.ConanTaskService;
import uk.ac.ebi.fgpt.conan.service.ConanUserService;

import java.util.Date;
import java.util.List;

/**
 * Controls user interaction with {@link uk.ac.ebi.fgpt.conan.model.ConanTask}s.  Tasks can be paused (manually halted
 * during execution), resumed (after a possible error was flagged but determined by a user to be non-critical), retried
 * (starting from the last failed task), restarted (retry everything from the first process) or deleted altogether.
 *
 * @author Tony Burdett
 * @date 21-Oct-2010
 */
@Controller
@RequestMapping("/tasks")
public class TaskController {
    private ConanTaskService taskService;
    private ConanUserService userService;

    public ConanTaskService getTaskService() {
        return taskService;
    }

    @Autowired
    public void setTaskService(ConanTaskService taskService) {
        this.taskService = taskService;
    }

    public ConanUserService getUserService() {
        return userService;
    }

    @Autowired
    public void setUserService(ConanUserService userService) {
        this.userService = userService;
    }

    /**
     * Gets the {@link uk.ac.ebi.fgpt.conan.model.ConanTask} with the given ID.
     *
     * @param taskID the ID of the task to retrieve
     * @return the task assigned this ID
     */
    @RequestMapping(value = "/{taskID}", method = RequestMethod.GET)
    public @ResponseBody ConanTask getTask(@PathVariable String taskID) {
        return getTaskService().getTask(taskID);
    }

    /**
     * Returns a list of all submitted tasks, in submission order. This includes all pending, running and completed
     * tasks - basically a history of everything that has ever been submitted.  The summaryView parameter controls
     * whether or not to do a deep fetch of these tasks, including all process run information.  The default is false
     * (fetch all info), true just obtains task metadata
     *
     * @param summaryView whether or not to display a summary of this task
     * @return the list of all submitted tasks
     */
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody List<ConanTask<? extends ConanPipeline>> getTasks(
            @RequestParam(defaultValue = "false") boolean summaryView) {
        return (summaryView ? getTaskService().getTasksSummary() : getTaskService().getTasks());
    }

    /**
     * Returns a list of all tasks that have been submitted but are pending execution.  Tasks in this list may have been
     * executed but failed: tasks that fail should highlight their failure to the submitter, and flag the task as
     * pending.The summaryView parameter controls whether or not to do a deep fetch of these tasks, including all
     * process run information.  The default is false (fetch all info), true just obtains task metadata
     *
     * @param summaryView whether or not to display a summary of this task
     * @return a list of all tasks pending execution
     */
    @RequestMapping(method = RequestMethod.GET, params = "pending")
    public @ResponseBody List<ConanTask<? extends ConanPipeline>> getPendingTasks(
            @RequestParam(defaultValue = "false") boolean summaryView) {
        return (summaryView ? getTaskService().getPendingTasksSummary() : getTaskService().getPendingTasks());
    }

    /**
     * Returns a list of all tasks that are currently being executed.  The summaryView parameter controls whether or not
     * to do a deep fetch of these tasks, including all process run information.  The default is false (fetch all info),
     * true just obtains task metadata
     *
     * @param summaryView whether or not to display a summary of this task
     * @return the currently executing tasks
     */
    @RequestMapping(method = RequestMethod.GET, params = "running")
    public @ResponseBody List<ConanTask<? extends ConanPipeline>> getRunningTasks(
            @RequestParam(defaultValue = "false") boolean summaryView) {
        return (summaryView ? getTaskService().getRunningTasksSummary() : getTaskService().getRunningTasks());
    }

    /**
     * Returns a list of all tasks that have been executed and completed.  This includes tasks that completed
     * successfully, and those that completed because a process failed and was subsequently marked as complete by the
     * submitter.  The summaryView parameter controls whether or not to do a deep fetch of these tasks, including all
     * process run information.  The default is false (fetch all info), true just obtains task metadata
     *
     * @param summaryView whether or not to display a summary of this task
     * @return the tasks that have completed
     */
    @RequestMapping(method = RequestMethod.GET, params = "complete")
    public @ResponseBody List<ConanTask<? extends ConanPipeline>> getCompletedTasks(
            @RequestParam(defaultValue = "false") boolean summaryView) {
        return (summaryView ? getTaskService().getCompletedTasksSummary() : getTaskService().getCompletedTasks());
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public @ResponseBody List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String userID,
            @RequestParam(defaultValue = "") String from,
            @RequestParam(defaultValue = "") String to) {
        String taskName = name.equals("") ? null : name;
        ConanUser user = userID.equals("") ? null : getUserService().getUser(userID);
        Date fromDate = from.equals("") ? null : new Date(Long.parseLong(from));
        Date toDate = to.equals("") ? null : new Date(Long.parseLong(to));
        return getTaskService().searchCompletedTasks(taskName, user, fromDate, toDate);
    }
}
