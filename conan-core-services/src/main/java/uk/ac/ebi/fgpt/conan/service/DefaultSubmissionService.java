package uk.ac.ebi.fgpt.conan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import uk.ac.ebi.fgpt.conan.dao.ConanTaskDAO;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.service.exception.SubmissionException;

import java.util.*;
import java.util.concurrent.*;

/**
 * A default implementation of a {@link ConanSubmissionService} that queues jobs in an {@link ExecutorService} for
 * parallel execution.  This service should be assigned a number of parallel jobs (the max number of jobs that are
 * allowed to be running at any one time) and a cooling off period (which is the amount of time a job will remain
 * "pending" for before execution actually commences).
 * <p/>
 * This implementation also prevents "duplicate" tasks from being executed together.  "Duplicate" tasks are any tasks
 * with and identical set of parameter/parameter value pairs: they do not have to be submitted to the same pipeline.
 *
 * @author Tony Burdett
 * @date 15-Oct-2010
 */
public class DefaultSubmissionService implements ConanSubmissionService {
    private final ExecutorService taskExecutor;
    private final int coolingOffPeriod;

    private final Map<ConanTask<? extends ConanPipeline>, Future<Boolean>> executingFutures;

    private ConanTaskDAO conanTaskDAO;

    private Logger log = LoggerFactory.getLogger(getClass());

    public DefaultSubmissionService(int numberOfParallelJobs, int coolingOffPeriod) {
        this.taskExecutor = Executors.newFixedThreadPool(numberOfParallelJobs);
        this.coolingOffPeriod = coolingOffPeriod;
        this.executingFutures = new HashMap<ConanTask<? extends ConanPipeline>, Future<Boolean>>();
    }

    protected Logger getLog() {
        return log;
    }

    public ConanTaskDAO getConanTaskDAO() {
        return conanTaskDAO;
    }

    public void setConanTaskDAO(ConanTaskDAO conanTaskDAO) {
        Assert.notNull(conanTaskDAO, "A ConanTaskDAO must be supplied");
        this.conanTaskDAO = conanTaskDAO;
    }

    public void submitTask(ConanTask<? extends ConanPipeline> conanTask) throws SubmissionException {
        // grab task id, executor service always grabs latest version of conanTask from task service
        // rather than retaining a (possibly out of date) reference
        final String taskID = conanTask.getId();
        log.debug("Task submission received! Task ID = " + taskID);

        ConanTask duplicate = checkForDuplication(conanTask);
        if (duplicate == null) {
            // wrap task in a callable and submit
            Future<Boolean> f = taskExecutor.submit(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    ConanTask<? extends ConanPipeline> executingTask = null;
                    try {
                        // all tasks go into a holding pattern for a while before executing
                        Date creationDate = getConanTaskDAO().getTask(taskID).getCreationDate();
                        Date allowedStartDate = new Date(System.currentTimeMillis() - (coolingOffPeriod * 1000));
                        while (creationDate.after(allowedStartDate)) {
                            synchronized (this) {
                                wait(1000);
                            }
                            allowedStartDate = new Date(System.currentTimeMillis() - (coolingOffPeriod * 1000));
                        }

                        // now we've waited for the prescribed cooling off period, execute
                        executingTask = getConanTaskDAO().getTask(taskID);
                        return executingTask.execute();
                    }
                    catch (Exception e) {
                        getLog().error("There was a problem executing task '" + taskID + "'", e);
                        throw e;
                    }
                    finally {
                        if (executingTask != null && executingFutures.containsKey(executingTask)) {
                            executingFutures.remove(executingTask);
                        }
                    }
                }
            });
            executingFutures.put(conanTask, f);

            // flag the fact that this task was submitted, if it hasn't been restarted
            if (!conanTask.isSubmitted()) {
                conanTask.submit();
            }
        }
        else {
            // abort task, otherwise it will be forever stuck with "created" status
            conanTask.abort();
            throw new SubmissionException(
                    "Task '" + conanTask.getId() + "' [" + conanTask.getName() + "] would duplicate " +
                            "task '" + duplicate.getId() + "[" + duplicate.getName() + "]");
        }
    }

    /**
     * Resubmits any recovered tasks to Conan.  Recovered tasks are those that were interrupted whilst running (for
     * example, due to a Conan shutdown or failure) and have been retrieved from e.g. a {@link
     * uk.ac.ebi.fgpt.conan.dao.ConanTaskDAO}.  These tasks should immediately be resubmitted.
     * <p/>
     * This implementation does nothing special with recovered tasks, so this method simply delegates to {@link
     * #submitTask(uk.ac.ebi.fgpt.conan.model.ConanTask)}.
     *
     * @param conanTask the recovered tasks to submit
     */
    public void resubmitTask(final ConanTask<? extends ConanPipeline> conanTask) throws SubmissionException {
        getLog().debug("Task resubmission request received! Task ID = " + conanTask.getId());
        submitTask(conanTask);
    }

    public void interruptTask(final ConanTask<? extends ConanPipeline> conanTask) {
        Future<Boolean> f = executingFutures.get(conanTask);
        if (f != null) {
            getLog().debug("Forcing interruption of Task ID = " + conanTask.getId());
            f.cancel(true);
            getLog().debug("Cancelled Task ID = " + conanTask.getId() + " successfully");
        }
        else {
            getLog().debug("Failed to interrupt Task ID = " + conanTask.getId() + ": not currently executing");
        }
    }

    public Set<ConanTask<? extends ConanPipeline>> getExecutingTasks() {
        return Collections.unmodifiableSet(executingFutures.keySet());
    }

    /**
     * On startup, this submission service recovers any pre-existing and running tasks and immediately resubmits them
     * with {@link #resubmitTask(uk.ac.ebi.fgpt.conan.model.ConanTask)}.  This allows any tasks that were running at the
     * previous shutdown (or failure) to be recovered wherever possible.
     */
    public void init() {
        Assert.notNull(getConanTaskDAO(), "A ConanTaskDAO must be provided");

        long start = System.currentTimeMillis();
        getLog().debug("Startup of " + getClass().getSimpleName() + " triggered, recovering running tasks");
        List<ConanTask<? extends ConanPipeline>> recoveredTasks = new ArrayList<ConanTask<? extends ConanPipeline>>();
        // add any pending tasks that were submitted and never started (i.e. not those that are paused or failed)
        for (ConanTask pendingTask : getConanTaskDAO().getPendingTasks()) {
            if (pendingTask.getCurrentState() == ConanTask.State.SUBMITTED) {
                recoveredTasks.add(pendingTask);
            }
        }
        recoveredTasks.addAll(getConanTaskDAO().getRunningTasks());
        for (ConanTask<? extends ConanPipeline> recoveredTask : recoveredTasks) {
            // todo - AE1 hack to prevent resubmission, remove this before full release!
            if (recoveredTask.getCurrentProcess() != null &&
                    recoveredTask.getCurrentProcess().getName().startsWith("AE1")) {
                getLog().warn("Not recovering '" + recoveredTask.getId() + "', " +
                                      "this is doing an AE1 process so can't be restarted. " +
                                      "This task will be aborted following shutdown/restart");
                recoveredTask.abort();
            }
            else {
                try {
                    resubmitTask(recoveredTask);
                }
                catch (SubmissionException e) {
                    getLog().warn("Automatic resubmission of task '" + recoveredTask.getId() + "' failed", e);
                }
            }
        }

        long end = System.currentTimeMillis();
        double time = ((double) (end - start)) / 1000;
        getLog().info("Submission service startup in " + time + " s.  " + recoveredTasks.size() +
                              " tasks were recovered and resubmitted.");
    }

    /**
     * On shutdown, this submission service attempts a {@link java.util.concurrent.ExecutorService#shutdownNow()} on the
     * executor service to which tasks are submitted, killing all running tasks once either: the running process
     * completes; or the executing process responds to the interrupt.  Any processes that respod to the interrupt should
     * be re-executed on startup.
     */
    public void destroy() {
        getLog().debug("Shutdown of " + getClass().getSimpleName() + " triggered, " +
                               "will attempt shutdownNow() on " + taskExecutor.getClass().getSimpleName());

        // shutdown the taskExecutor
        taskExecutor.shutdownNow();

        // and block until termination succeeds
        long start = System.currentTimeMillis();
        try {
            boolean terminated = taskExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
            if (terminated) {
                long end = System.currentTimeMillis();
                double time = ((double) (end - start)) / 1000;
                getLog().info("Submission service shutdown in " + time + " s.");
            }
            else {
                getLog().error("Failed to cleanly shutdown submission service.  " +
                                       "You may need to manually kill the process");
            }
        }
        catch (InterruptedException e1) {
            getLog().error("Shutdown request failed due to an interruption.  You may need to kill this process");
        }
    }

    /**
     * Checks whether this task would duplicate another.  If so, returns the duplicated task, else returns null
     *
     * @param task the task to check for duplication
     * @return the duplicated task, or null if the task passed doesn't duplicate anything
     */
    private ConanTask checkForDuplication(ConanTask<? extends ConanPipeline> task) {
        // comparator that checks for tasks that are equal or have all the same params
        TaskParametersComparator comparator = new TaskParametersComparator();

        for (ConanTask executingTask : getConanTaskDAO().getRunningTasks()) {
            // don't compare tasks if they have the same ID
            if (!task.getId().equals(executingTask.getId())) {
                // compare all parameters of executing task to our new task
                if (comparator.compare(task, executingTask) == 0) {
                    // tasks are equal, so this IS a duplicate
                    getLog().debug("Found task with duplicated parameters: " +
                                           "task '" + task.getId() + "' [" + task.getName() + "] would duplicate " +
                                           "task '" + executingTask.getId() + "' [" + executingTask.getName() + "]");
                    return executingTask;
                }
            }
        }

        // found no duplicated executing task, this is ok
        getLog().debug("Task ID '" + task.getId() + "' [" + task.getName() + "] does not duplicate another task");
        return null;
    }

    private class TaskParametersComparator implements Comparator<ConanTask> {
        public int compare(ConanTask task1, ConanTask task2) {
            getLog().debug("Comparing task ID '" + task1.getId() + "' [" + task1.getName() + "] with " +
                                   "task '" + task2.getId() + "' [" + task2.getName() + "]");

            if (task1.equals(task2)) {
                getLog().debug("Task '" + task1.getId() + "' [" + task1.getName() + "] is equal to " +
                                       "task '" + task2.getId() + "' [" + task2.getName() + "]");
                return 0;
            }
            else {
                boolean allEqual = true;
                int diffCount = 0;

                // do pairwise check of parameters
                getLog().debug("Comparing parameter values of task '" + task1.getId() + "' [" + task1.getName() + "] " +
                                       "and task '" + task2.getId() + "' [" + task2.getName() + "]");
                Map<ConanParameter, String> paramValues1 = task1.getParameterValues();
                Map<ConanParameter, String> paramValues2 = task2.getParameterValues();
                for (ConanParameter param : paramValues1.keySet()) {
                    if (paramValues2.containsKey(param)) {
                        // get the values for this key and check
                        getLog().debug("Compared tasks have parameter type " + param.getName() + " in common, " +
                                               "checking values...");
                        String val1 = paramValues1.get(param);
                        String val2 = paramValues2.get(param);

                        // if values are equal, all vals are still equal
                        getLog().debug("Comparing values '" + val1 + "' and '" + val2 + "'");
                        if (!val1.equals(val2)) {
                            diffCount++;
                        }
                        allEqual = allEqual && val1.equals(val2);
                    }
                    else {
                        allEqual = false;
                        diffCount++;
                    }
                }

                if (allEqual) {
                    // if all our parameters are equal, we must return 0
                    getLog().debug("All parameter values are equal, so these tasks duplicate each other");
                    return 0;
                }
                else {
                    // otherwise they're different, so to obey the contract we must return some sorted integer value
                    // return the number of different parameters -
                    // positive if task1 has more params than task2, or if they have the same number of params,
                    // and negative if negative if task2 has more params
                    int result;
                    if (diffCount != 0) {
                        result = task1.getParameterValues().size() < task2.getParameterValues().size()
                                ? 0 - diffCount
                                : diffCount;
                    }
                    else {
                        // don't let result be zero
                        result = 1;
                    }

                    getLog().debug("Comparison of tasks gives result: " + result);
                    return result;
                }
            }
        }
    }
}
