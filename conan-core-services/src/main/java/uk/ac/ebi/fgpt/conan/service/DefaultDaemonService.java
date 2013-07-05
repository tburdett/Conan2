package uk.ac.ebi.fgpt.conan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.fgpt.conan.dao.ConanDaemonInputsDAO;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.SubmissionException;

import java.util.*;

/**
 * An implementation of a {@link ConanDaemonService} that maintains a list of {@link
 * uk.ac.ebi.fgpt.conan.model.ConanPipeline}s that are "daemonized" (enabled for daemon mode). Any pipelines added to
 * the daemon service and can subsequently be toggle on and off with a request.
 * <p/>
 * This implementation retrieves inputs by querying the submission tracking database for experiment accession numbers.
 * Therefore, the only input available from this daemon service is "accession".  If you try to daemonize a pipeline that
 * requires more inputs, this service will fail.
 * <p/>
 * This implementation maintains a dedicated daemon user for any submissions that are created by daemon mode.  The email
 * address for this "virtual" user can be changed, thereby ensuring there is always a real user monitoring daemon
 * submissions.
 *
 * @author Tony Burdett
 * @date 15-Oct-2010
 */
public class DefaultDaemonService implements ConanDaemonService {
    public static final int MAXIMUM_SUBMISSION_BATCH_SIZE = 250;
    public static final int POLL_EVERY_N_SECONDS = 3600;

    private final Set<ConanPipeline> daemonizedPipelines;

    private ConanSubmissionService submissionService;
    private ConanTaskService taskService;
    private ConanUserService userService;

    private Collection<ConanDaemonInputsDAO> inputDAOs;

    private boolean enabled;
    private Thread daemonThread;

    private Logger log = LoggerFactory.getLogger(getClass());

    public DefaultDaemonService() {
        this.daemonizedPipelines = new HashSet<ConanPipeline>();
        // initialize inputDAOs to an empty set
        this.inputDAOs = new HashSet<ConanDaemonInputsDAO>();
        // disabled by default
        this.enabled = false;
        this.daemonThread = new Thread(new Daemon());
    }

    public void shutdown() {
        enabled = false;
        daemonThread.interrupt();
    }

    protected Logger getLog() {
        return log;
    }

    public ConanSubmissionService getSubmissionService() {
        return submissionService;
    }

    @Required
    public void setSubmissionService(ConanSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    public ConanTaskService getTaskService() {
        return taskService;
    }

    @Required
    public void setTaskService(ConanTaskService taskService) {
        this.taskService = taskService;
    }

    public ConanUserService getUserService() {
        return userService;
    }

    @Required
    public void setUserService(ConanUserService userService) {
        this.userService = userService;
    }

    public Collection<ConanDaemonInputsDAO> getInputDAOs() {
        return inputDAOs;
    }

    public void setInputDAOs(Collection<ConanDaemonInputsDAO> inputDAOs) {
        this.inputDAOs = inputDAOs;
    }

    public Collection<ConanPipeline> getPipelines() {
        return daemonizedPipelines;
    }

    public boolean addPipeline(ConanPipeline conanPipeline) {
        if (conanPipeline.getAllRequiredParameters().size() != 1) {
            getLog().error("Pipeline '" + conanPipeline.getName() + "' requires " +
                    conanPipeline.getAllRequiredParameters().size() + " parameters.  " +
                    "Only single-parameter pipelines can be daemonized - this pipeline will not be added");
            return false;
        } else {
            return daemonizedPipelines.add(conanPipeline);
        }
    }

    public boolean removePipeline(ConanPipeline conanPipeline) {
        return daemonizedPipelines.remove(conanPipeline);
    }

    public boolean isRunning() {
        return enabled;
    }

    public boolean start() {
        getLog().debug("Starting daemon mode");
        enabled = true;
        daemonThread.start();
        return true;
    }

    public boolean stop() {
        getLog().debug("Stopping daemon mode");
        try {
            shutdown();
            return true;
        } finally {
            // always replace the existing daemon with a new one
            daemonThread = new Thread(new Daemon());
        }
    }

    public ConanUser getDaemonUser() {
        try {
            return getUserService().getUserByUserName("conan-daemon");
        } catch (IllegalArgumentException e) {
            getLog().warn("No 'daemon' user found - a new user will be created for daemon mode");
            // daemon user has no first name or surname, and conan-daemon@ebi.ac.uk is not a real email
            // we add the email to ensure that a rest api key can be generated,
            // but it will be set to an empty value straight away
            ConanUser daemon = getUserService().createNewUser("conan-daemon",
                    "",
                    "Daemon",
                    "conan-daemon@ebi.ac.uk",
                    ConanUser.Permissions.SUBMITTER);
            return getUserService().updateUserEmail(daemon, "");
        }
    }

    public void setNotificationEmailAddress(String emailAddress) {
        getUserService().updateUserEmail(getDaemonUser(), emailAddress);
    }

    protected Collection<ConanTask> createNewSubmittableTasks(ConanPipeline pipeline) {
        Collection<ConanTask> tasks = new ArrayList<ConanTask>();

        // check we have a daemon-authorised user, and that this user has permission to submit
        ConanUser daemonUser = getDaemonUser();
        if (daemonUser != null && daemonUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
            List<ConanParameter> parameters = pipeline.getAllRequiredParameters();
            // parameters.get(0) should always work, as we've screened our pipelines for number of params when adding
            ConanParameter requiredInputType = parameters.get(0);
            // check DAOs for implementations that return this type
            getLog().debug("Polling DAOs for new daemon mode inputs to pipeline '" + pipeline.getName() + "', " +
                    "requires parameters of type '" + requiredInputType.getClass().getSimpleName() +
                    "'");
            int i = 0;
            for (ConanDaemonInputsDAO dao : inputDAOs) {
                getLog().debug("Next DAO is '" + dao.getClass().getSimpleName() + "', " +
                        "returns parameters of type '" + dao.getParameterType().getSimpleName() + "'");
                if (dao.getParameterType().equals(requiredInputType.getClass())) {
                    getLog().debug("Matched " + dao.getParameterType().getSimpleName() + " " +
                            "to " + requiredInputType.getClass().getSimpleName() + ", " +
                            "using DAO to obtain daemon mode inputs");
                    // got a dao that returns the right type of param, so get available input values
                    List<String> inputValues = dao.getParameterValues();
                    getLog().debug("There are " + inputValues.size() + " " + requiredInputType.getName() + "s " +
                            "available for submission to daemon mode.");
                    for (String inputValue : inputValues) {
                        // create a new task for each
                        if (i < MAXIMUM_SUBMISSION_BATCH_SIZE) {
                            Map<String, String> inputMap = new HashMap<String, String>();
                            inputMap.put(requiredInputType.getName(), inputValue);
                            try {
                                tasks.add(getTaskService().createNewTask(pipeline.getName(),
                                        0,
                                        inputMap,
                                        ConanTask.Priority.LOW,
                                        daemonUser));
                                i++;
                            } catch (IllegalArgumentException e) {
                                getLog().warn(
                                        "Could not create task: an illegal pipeline or set of inputs was supplied", e);
                            }
                        } else {
                            getLog().warn("Batch size for daemon mode exceeded.  " +
                                    "No more than " + MAXIMUM_SUBMISSION_BATCH_SIZE +
                                    " can be submitted in one go.  " +
                                    "Remaining jobs will be added at the next daemon mode iteration");
                            break;
                        }
                    }
                } else {
                    getLog().debug("Cannot match " + dao.getParameterType().getSimpleName() + " " +
                            "to " + requiredInputType.getClass().getSimpleName());
                }
            }
        } else {
            if (daemonUser == null) {
                getLog().warn("Daemon User not found, no submissions can be created");
            } else {
                getLog().warn("Daemon user does not have permissions to create new tasks " +
                        "(" + daemonUser.getPermissions() + ")");
            }
        }

        return tasks;
    }

    protected void submitNewTasks(Collection<ConanTask> submittableTasks) {
        for (ConanTask task : submittableTasks) {
            try {
                getSubmissionService().submitTask(task);
            } catch (SubmissionException e) {
                getLog().warn("Task ID '" + task.getId() + "' could not be submitted by daemon mode, " +
                        "and will be skipped [" + e.getMessage() + "]");
            }
        }
    }

    private class Daemon implements Runnable {
        public void run() {
            while (enabled) {
                getLog().debug("Daemon mode is enabled and will look for inputs");
                for (ConanPipeline pipeline : daemonizedPipelines) {
                    getLog().debug("Looking for inputs that can be submitted to " + pipeline.getName());
                    try {
                        Collection<ConanTask> submittableTasks = createNewSubmittableTasks(pipeline);
                        submitNewTasks(submittableTasks);
                    } catch (RuntimeException e) {
                        getLog().error("A runtime exception occurred in the Daemon thread, daemon mode will exit", e);
                        stop();
                    }
                }

                // if still enabled, wait for the designated time
                if (enabled) {
                    synchronized (this) {
                        try {
                            wait(POLL_EVERY_N_SECONDS * 1000);
                        } catch (InterruptedException e) {
                            // if wait is interrupted, disable
                            enabled = false;
                        }
                    }
                }
            }
        }
    }
}
