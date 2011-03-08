package uk.ac.ebi.fgpt.conan.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.ConanSubmissionService;
import uk.ac.ebi.fgpt.conan.service.ConanTaskService;
import uk.ac.ebi.fgpt.conan.service.ConanUserService;
import uk.ac.ebi.fgpt.conan.service.exception.SubmissionException;
import uk.ac.ebi.fgpt.conan.web.view.BatchRequestBean;
import uk.ac.ebi.fgpt.conan.web.view.SubmissionRequestBean;
import uk.ac.ebi.fgpt.conan.web.view.SubmissionResponseBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Governs the creation of new submissions and the retrieval of details about old submissions.
 *
 * @author Tony Burdett
 * @date 30-Jul-2010
 */
@Controller
@RequestMapping("/submissions")
public class SubmissionController {
    private ConanSubmissionService submissionService;
    private ConanTaskService taskService;
    private ConanUserService userService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ConanSubmissionService getSubmissionService() {
        return submissionService;
    }

    @Autowired
    public void setSubmissionService(ConanSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

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
     * Submits a new task to a submission service.  This method takes all the required arguments to be able to generate
     * a new task from the known pipelines configured in Conan.  The response contains the ID of the submitted task.
     *
     * @param submissionRequest the details of the task to be created
     * @return the view name for the newly created task
     */
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody SubmissionResponseBean submitTask(@RequestBody SubmissionRequestBean submissionRequest) {
        getLog().debug("Submission request received: " + submissionRequest.toString());

        // get the user, identified by their rest api key
        ConanUser conanUser = getUserService().getUserByRestApiKey(submissionRequest.getRestApiKey());

        // user has permission to do this?
        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
            // priority is MEDIUM unless request contains a different (and valid) priority
            ConanTask.Priority priority = ConanTask.Priority.MEDIUM;
            for (ConanTask.Priority nextPriority : ConanTask.Priority.values()) {
                if (nextPriority.toString().equalsIgnoreCase(submissionRequest.getPriority())) {
                    priority = nextPriority;
                    break;
                }
            }

            try {
                // generate task
                ConanTask<? extends ConanPipeline> conanTask =
                        getTaskService().createNewTask(submissionRequest.getPipelineName(),
                                                       submissionRequest.getStartingProcessIndex(),
                                                       submissionRequest.getInputParameters(),
                                                       priority,
                                                       conanUser);

                // and submit the newly generated task
                getSubmissionService().submitTask(conanTask);

                String msg = "Your submission was accepted - " +
                        "task '" + conanTask.getName() + "' has been added to the queue";
                return new SubmissionResponseBean(true, msg, conanTask.getId(), conanTask.getName());
            }
            catch (SubmissionException e) {
                String msg = "Your submission was rejected - " + e.getMessage();
                return new SubmissionResponseBean(false, msg, null, null);
            }
            catch (IllegalArgumentException e) {
                String msg = "Your submission was rejected - " + e.getMessage();
                return new SubmissionResponseBean(false, msg, null, null);
            }
        }
        else {
            String msg = "You do not have permission to submit new tasks";
            return new SubmissionResponseBean(false, msg, null, null);
        }
    }

    /**
     * Submits a list of new tasks to a submission service.  This method takes a list of {@link
     * uk.ac.ebi.fgpt.conan.web.view.SubmissionRequestBean}s, which can be deserialized from a json array, and submits
     * each task in turn.  The response is the list of response beans, with the same ordering as the request.
     *
     * @param batchRequest the details of the task to be created
     * @return the view name for the newly created task
     */
    @RequestMapping(value = "/batch", method = RequestMethod.POST)
    public @ResponseBody List<SubmissionResponseBean> submitTasks(
            @RequestBody BatchRequestBean batchRequest) {
        List<SubmissionResponseBean> submissionResponses = new ArrayList<SubmissionResponseBean>();
        for (SubmissionRequestBean submissionRequest : batchRequest.getSubmissionRequests()) {
            getLog().debug("Submission request received: " + submissionRequest.toString());

            // get the user, identified by their rest api key
            ConanUser conanUser = getUserService().getUserByRestApiKey(submissionRequest.getRestApiKey());

            // user has permission to do this?
            if (conanUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
                // priority is MEDIUM unless request contains a different (and valid) priority
                ConanTask.Priority priority = ConanTask.Priority.MEDIUM;
                for (ConanTask.Priority nextPriority : ConanTask.Priority.values()) {
                    if (nextPriority.toString().equalsIgnoreCase(submissionRequest.getPriority())) {
                        priority = nextPriority;
                        break;
                    }
                }

                // generate task
                try {
                    ConanTask<? extends ConanPipeline> conanTask =
                            getTaskService().createNewTask(submissionRequest.getPipelineName(),
                                                           submissionRequest.getStartingProcessIndex(),
                                                           submissionRequest.getInputParameters(),
                                                           priority,
                                                           conanUser);

                    // and submit the newly generated task
                    getSubmissionService().submitTask(conanTask);

                    String msg =
                            "Your submission was accepted - task '" + conanTask.getName() +
                                    "' has been added to the queue";
                    submissionResponses.add(new SubmissionResponseBean(true,
                                                                       msg,
                                                                       conanTask.getId(),
                                                                       conanTask.getName()));
                }
                catch (SubmissionException e) {
                    String msg = "Your submission was rejected - " + e.getMessage();
                    submissionResponses.add(new SubmissionResponseBean(false, msg, null, null));
                }
                catch (IllegalArgumentException e) {
                    String msg = "Your submission was rejected - " + e.getMessage();
                    submissionResponses.add(new SubmissionResponseBean(false, msg, null, null));
                }
            }
            else {
                String msg = "You do not have permission to submit new tasks";
                submissionResponses.add(new SubmissionResponseBean(false, msg, null, null));
            }
        }
        return submissionResponses;
    }

    /**
     * Pauses a currently executing task, defined by the given task ID.  Pause operations take effect at the end of the
     * current process - processes are never killed, as this is considered to be potentially unsafe.  Instead, the given
     * task is identified as paused and will stop execution after it's current process has completed.  This task will
     * then be flagged for user attention.
     *
     * @param taskID     the ID of the task to pause
     * @param restApiKey the key of the user requesting this action
     * @return true if this operation succeeds, false if the task could not be paused (for example, because it has
     *         completed)
     * @throws IllegalArgumentException if there was no task with the given ID
     */
    @RequestMapping(value = "/{taskID}", method = RequestMethod.PUT, params = "pause")
    public @ResponseBody SubmissionResponseBean pauseTask(@PathVariable String taskID, @RequestParam String restApiKey)
            throws IllegalArgumentException {
        // retrieve the user
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // retrieve the task
        ConanTask task = getTaskService().getTask(taskID);

        // check user permissions, and update task if sufficient
        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
            if (task != null) {
                if (task.getCurrentState() == ConanTask.State.RUNNING) {
                    // valid request, update task
                    String msg = "Task '" + task.getName() + "' will be paused once the current process completes";
                    task.pause();
                    return new SubmissionResponseBean(true, msg, taskID, task.getName());
                }
                else if (task.getCurrentState().compareTo(ConanTask.State.RUNNING) < 0) {
                    return new SubmissionResponseBean(false,
                                                      "Task '" + task.getName() + "' is already paused",
                                                      taskID,
                                                      task.getName());
                }
                else {
                    return new SubmissionResponseBean(false,
                                                      "Task '" + task.getName() +
                                                              "' cannot be paused because it has finished",
                                                      taskID,
                                                      task.getName());
                }
            }
            else {
                throw new IllegalArgumentException("There is no task with ID '" + taskID + "'");
            }
        }
        else {
            return new SubmissionResponseBean(false,
                                              "You cannot pause task '" + task.getName() + "', " +
                                                      "because you do not have permission",
                                              taskID,
                                              task.getName());
        }
    }

    /**
     * Resumes a paused task.  The halted task will continue execution from the next process.  If the pause occurred
     * because of an error executing a prior process, this error will be ignored.
     *
     * @param taskID     the ID of the task to resume
     * @param restApiKey the key of the user requesting this action
     * @return true if this operation succeeds, false if the task could not be resumed (for example, because it is not
     *         paused)
     * @throws IllegalArgumentException if there was no task with the given ID
     */
    @RequestMapping(value = "/{taskID}", method = RequestMethod.PUT, params = "resume")
    public @ResponseBody SubmissionResponseBean resumeTask(@PathVariable String taskID, @RequestParam String restApiKey)
            throws IllegalArgumentException {
        // retrieve the user
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // retrieve the task
        ConanTask<? extends ConanPipeline> task = getTaskService().getTask(taskID);

        // check user permissions, and update task if sufficient
        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
            if (task != null) {
                if (task.getCurrentState() == ConanTask.State.PAUSED ||
                        task.getCurrentState() == ConanTask.State.FAILED) {
                    // valid request, update task and resubmit
                    String msg = task.getNextProcess() != null
                            ? "Task '" + task.getName() + "' will be resumed from " + task.getNextProcess().getName()
                            : "Task '" + task.getName() + "' will be resumed, and will complete";
                    task.resume();

                    try {
                        getSubmissionService().submitTask(task);
                        return new SubmissionResponseBean(true, msg, task.getId(), task.getName());
                    }
                    catch (SubmissionException e) {
                        msg = "Your submission was rejected - " + e.getMessage();
                        return new SubmissionResponseBean(false, msg, null, null);
                    }
                }
                else {
                    return new SubmissionResponseBean(false,
                                                      "Task '" + task.getName() + "' cannot be resumed because " +
                                                              "it is not paused",
                                                      taskID,
                                                      task.getName());
                }
            }
            else {
                throw new IllegalArgumentException("There is no task with ID '" + taskID + "'");
            }
        }
        else {
            return new SubmissionResponseBean(false,
                                              "You cannot resume task '" + task.getName() + "', " +
                                                      "because you do not have permission",
                                              taskID,
                                              task.getName());
        }
    }

    /**
     * Retries a paused task, starting with the last failed process.
     *
     * @param taskID     the ID of the task to retry
     * @param restApiKey the key of the user requesting this action
     * @return true if this operation succeeds, false if the task could not be retried (for example, because it is not
     *         paused)
     * @throws IllegalArgumentException if there was no task with the given ID
     */
    @RequestMapping(value = "/{taskID}", method = RequestMethod.PUT, params = "retry")
    public @ResponseBody SubmissionResponseBean retryTask(@PathVariable String taskID, @RequestParam String restApiKey)
            throws IllegalArgumentException {
        // retrieve the user
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // retrieve the task
        ConanTask<? extends ConanPipeline> task = getTaskService().getTask(taskID);

        // check user permissions, and update task if sufficient
        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
            if (task != null) {
                if (task.getCurrentState() == ConanTask.State.PAUSED ||
                        task.getCurrentState() == ConanTask.State.FAILED) {
                    // valid request, update task and resubmit
                    String msg = task.getLastProcess() != null
                            ? "Task '" + task.getName() + "' will retry " + task.getLastProcess().getName()
                            : "Task '" + task.getName() + "' will retry the last process";

                    task.retryLastProcess();
                    try {
                        getSubmissionService().submitTask(task);
                        return new SubmissionResponseBean(true, msg, task.getId(), task.getName());
                    }
                    catch (SubmissionException e) {
                        msg = "Your submission was rejected - " + e.getMessage();
                        return new SubmissionResponseBean(false, msg, null, null);
                    }
                }
                else {
                    return new SubmissionResponseBean(false,
                                                      "Task '" + task.getName() + "' cannot be retried because " +
                                                              "it is not paused",
                                                      taskID,
                                                      task.getName());
                }
            }
            else {
                throw new IllegalArgumentException("There is no task with ID '" + taskID + "'");
            }
        }
        else {
            return new SubmissionResponseBean(false,
                                              "You cannot retry this task, because you do not have permission",
                                              taskID,
                                              task.getName());
        }
    }

    /**
     * Restarts a paused task from it's first process.  This operation should be used with extreme care, as it
     * potentially duplicates operations that have already completed successfully.
     *
     * @param taskID     the ID of the task to restart
     * @param restApiKey the key of the user requesting this action
     * @return true if this operation succeeds, false if the task could not be restarted (for example, because it is not
     *         paused)
     * @throws IllegalArgumentException if there was no task with the given ID
     */
    @RequestMapping(value = "/{taskID}", method = RequestMethod.PUT, params = "restart")
    public @ResponseBody SubmissionResponseBean restartTask(@PathVariable String taskID,
                                                            @RequestParam String restApiKey)
            throws IllegalArgumentException {
        // retrieve the user
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // retrieve the task
        ConanTask<? extends ConanPipeline> task = getTaskService().getTask(taskID);

        // check user permissions, and update task if sufficient
        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
            if (task != null) {
                if (task.getCurrentState() == ConanTask.State.PAUSED ||
                        task.getCurrentState() == ConanTask.State.FAILED) {
                    // valid request, update task and resubmit
                    String msg = task.getFirstProcess() != null
                            ? "Task '" + task.getName() + "' will restart completely, from " +
                            task.getFirstProcess().getName()
                            : "Task '" + task.getName() + "' will restart completely, from the first process";

                    task.restart();
                    try {
                        getSubmissionService().submitTask(task);
                        return new SubmissionResponseBean(true, msg, task.getId(), task.getName());
                    }
                    catch (SubmissionException e) {
                        msg = "Your submission was rejected - " + e.getMessage();
                        return new SubmissionResponseBean(false, msg, null, null);
                    }
                }
                else {
                    return new SubmissionResponseBean(false,
                                                      "Task '" + task.getName() + "' cannot be restarted " +
                                                              "because it is not paused",
                                                      taskID,
                                                      task.getName());
                }
            }
            else {
                throw new IllegalArgumentException("There is no task with ID '" + taskID + "'");
            }
        }
        else {
            return new SubmissionResponseBean(false,
                                              "You cannot restart task '" + task.getName() + "', " +
                                                      "because you do not have permission",
                                              taskID,
                                              task.getName());
        }
    }

    /**
     * Stops a task entirely, rather than attempt to retry more options.  To be stopped, tasks must first be paused.
     * This operation manually flags a task as having a {@link uk.ac.ebi.fgpt.conan.model.ConanTask.State#ABORTED}
     * state, and cannot then be resumed.
     *
     * @param taskID     the ID of the task to delete
     * @param restApiKey the key of the user requesting this action
     * @return true if this operation succeeds, false if the task could not be deleted (for example, because it is not
     *         paused)
     * @throws IllegalArgumentException if there was no task with the given ID
     */
    @RequestMapping(value = "/{taskID}", method = RequestMethod.PUT, params = "stop")
    public @ResponseBody SubmissionResponseBean stopTask(@PathVariable String taskID, @RequestParam String restApiKey)
            throws IllegalArgumentException {
        // retrieve the user
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // retrieve the task
        ConanTask task = getTaskService().getTask(taskID);

        // check user permissions, and update task if sufficient
        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
            if (task != null) {
                // stop tasks if the current state is paused, failed, or never started
                if (task.getCurrentState() == ConanTask.State.PAUSED ||
                        task.getCurrentState() == ConanTask.State.FAILED ||
                        task.getCurrentState().compareTo(ConanTask.State.RUNNING) < 0) {
                    // valid request, update task
                    String msg = "Task '" + task.getName() + "' will be stopped";
                    task.abort();
                    return new SubmissionResponseBean(true, msg, taskID, task.getName());
                }
                else {
                    return new SubmissionResponseBean(false,
                                                      "Task '" + task.getName() + "' cannot be stopped because " +
                                                              "it is not paused",
                                                      taskID,
                                                      task.getName());
                }
            }
            else {
                throw new IllegalArgumentException("There is no task with ID '" + taskID + "'");
            }
        }
        else {
            return new SubmissionResponseBean(false,
                                              "You cannot stop this task '" + task.getName() + "', " +
                                                      "because you do not have permission",
                                              taskID,
                                              task.getName());
        }
    }
}
