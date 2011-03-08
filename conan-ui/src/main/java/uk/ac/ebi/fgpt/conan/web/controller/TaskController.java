package uk.ac.ebi.fgpt.conan.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.service.ConanTaskService;
import uk.ac.ebi.fgpt.conan.service.ConanUserService;

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
     * tasks - basically a history of everything that has ever been submitted.
     *
     * @return the list of all submitted tasks
     */
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody List<ConanTask<? extends ConanPipeline>> getTasks() {
        return getTaskService().getTasks();
    }

    /**
     * Returns a list of all tasks that have been submitted but are pending execution.  Tasks in this list may have been
     * executed but failed: tasks that fail should highlight their failure to the submitter, and flag the task as
     * pending.
     *
     * @return a list of all tasks pending execution
     */
    @RequestMapping(method = RequestMethod.GET, params = "pending")
    public @ResponseBody List<ConanTask<? extends ConanPipeline>> getPendingTasks() {
        return getTaskService().getPendingTasks();
    }

    /**
     * Returns a list of all tasks that are currently being executed.
     *
     * @return the currently executing tasks
     */
    @RequestMapping(method = RequestMethod.GET, params = "running")
    public @ResponseBody List<ConanTask<? extends ConanPipeline>> getRunningTasks() {
        return getTaskService().getRunningTasks();
    }

    /**
     * Returns a list of all tasks that have been executed and completed.  This includes tasks that completed
     * successfully, and those that completed because a process failed and was subsequently marked as complete by the
     * submitter.
     *
     * @return the tasks that have completed
     */
    @RequestMapping(method = RequestMethod.GET, params = "complete")
    public @ResponseBody List<ConanTask<? extends ConanPipeline>> getCompletedTasks() {
        return getTaskService().getCompletedTasks();
    }
}
