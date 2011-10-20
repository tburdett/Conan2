package uk.ac.ebi.fgpt.conan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import uk.ac.ebi.fgpt.conan.dao.ConanTaskDAO;
import uk.ac.ebi.fgpt.conan.factory.ConanTaskFactory;
import uk.ac.ebi.fgpt.conan.model.*;
import uk.ac.ebi.fgpt.conan.service.exception.ConanPipelineLookupException;
import uk.ac.ebi.fgpt.conan.service.exception.MissingRequiredParameterException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A default implementation of a {@link ConanTaskService} that delegates creation of tasks to a {@link
 * uk.ac.ebi.fgpt.conan.factory.ConanTaskFactory}.
 * <p/>
 * This service requires wiring with a pipeline and process service in order to do lookups: calls to create new tasks on
 * the service level pass pipeline and process names, but in order to construct an actual task instance the
 * corresponding {@link ConanPipeline} and {@link ConanProcess}.
 * <p/>
 * This service should be configured with a spring jdbc template to enabled storage and retrieval of all created tasks.
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public class DefaultTaskService implements ConanTaskService {
    private ConanPipelineService pipelineService;

    private ConanTaskFactory conanTaskFactory;
    private ConanTaskDAO conanTaskDAO;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ConanPipelineService getPipelineService() {
        return pipelineService;
    }

    public void setPipelineService(ConanPipelineService pipelineService) {
        Assert.notNull(pipelineService, "A PipelineService must be supplied");
        this.pipelineService = pipelineService;
    }

    public ConanTaskFactory getConanTaskFactory() {
        return conanTaskFactory;
    }

    public void setConanTaskFactory(ConanTaskFactory conanTaskFactory) {
        this.conanTaskFactory = conanTaskFactory;
    }

    public ConanTaskDAO getConanTaskDAO() {
        return conanTaskDAO;
    }

    public void setConanTaskDAO(ConanTaskDAO conanTaskDAO) {
        Assert.notNull(conanTaskDAO, "A ConanTaskDAO must be supplied");
        this.conanTaskDAO = conanTaskDAO;
    }

    public ConanTask<? extends ConanPipeline> createNewTask(String pipelineName,
                                                            int startingProcessIndex,
                                                            Map<String, String> inputParameters,
                                                            ConanTask.Priority priority,
                                                            ConanUser conanUser) throws IllegalArgumentException {
        // lookup pipeline
        ConanPipeline pipeline = getPipelineService().getPipeline(conanUser, pipelineName);

        // validate part of the request by checking we found this pipeline
        if (pipeline == null) {
            throw new ConanPipelineLookupException("Unable to locate pipeline '" + pipelineName + "'");
        }

        // create a new map to map parameters to parameter values
        Map<ConanParameter, String> parameters = new HashMap<ConanParameter, String>();

        // then add all parameters for processes after it
        for (ConanProcess process : pipeline.getProcesses()) {
            extractConanParameters(parameters, inputParameters, process);
        }

        // we've now looked up our pipeline, skipped all the processes before starting process,
        // and extract required parameters - so create the task from these inputs
        ConanTask<?> task = getConanTaskFactory().createTask(pipeline,
                                                             startingProcessIndex,
                                                             parameters,
                                                             priority,
                                                             conanUser);

        // save this task, and return the result (this updates the reference if required)
        return getConanTaskDAO().saveTask(task);
    }

    public ConanTask<? extends ConanPipeline> getTask(String taskID) {
        return getConanTaskDAO().getTask(taskID);
    }

    public List<ConanTask<? extends ConanPipeline>> getTasks() {
        long start = System.currentTimeMillis();
        getLog().trace("Retrieving all tasks...");
        List<ConanTask<? extends ConanPipeline>> result = getConanTaskDAO().getAllTasks();
        long end = System.currentTimeMillis();
        double time = ((double) (end - start)) / 1000;
        getLog().trace("Fetched and mapped all tasks in " + time + "s.");
        return result;
    }

    public List<ConanTask<? extends ConanPipeline>> getTasksSummary() {
        long start = System.currentTimeMillis();
        getLog().trace("Retrieving all tasks...");
        List<ConanTask<? extends ConanPipeline>> result = getConanTaskDAO().getAllTasksSummary();
        long end = System.currentTimeMillis();
        double time = ((double) (end - start)) / 1000;
        getLog().trace("Fetched and mapped all tasks in " + time + "s.");
        return result;
    }

    public List<ConanTask<? extends ConanPipeline>> getTasks(int maxRecords, int startingFrom) {
        return getConanTaskDAO().getAllTasks(maxRecords, startingFrom);
    }

    public List<ConanTask<? extends ConanPipeline>> getTasks(int maxRecords, int startingFrom, String orderBy) {
        return getConanTaskDAO().getAllTasks(maxRecords, startingFrom, orderBy);
    }

    public List<ConanTask<? extends ConanPipeline>> getPendingTasks() {
        long start = System.currentTimeMillis();
        getLog().trace("Retrieving pending tasks...");
        List<ConanTask<? extends ConanPipeline>> result = getConanTaskDAO().getPendingTasks();
        long end = System.currentTimeMillis();
        double time = ((double) (end - start)) / 1000;
        getLog().trace("Fetched and mapped all pending tasks in " + time + "s.");
        return result;
    }

    public List<ConanTask<? extends ConanPipeline>> getPendingTasksSummary() {
        long start = System.currentTimeMillis();
        getLog().trace("Retrieving pending tasks...");
        List<ConanTask<? extends ConanPipeline>> result = getConanTaskDAO().getPendingTasksSummary();
        long end = System.currentTimeMillis();
        double time = ((double) (end - start)) / 1000;
        getLog().trace("Fetched and mapped all pending tasks in " + time + "s.");
        return result;
    }

    public List<ConanTask<? extends ConanPipeline>> getPendingTasks(int maxRecords, int startingFrom) {
        // return sublist based on maxRecords, startingFrom
        List<ConanTask<? extends ConanPipeline>> tasks = getConanTaskDAO().getPendingTasks();
        return tasks.subList(startingFrom, startingFrom + maxRecords);
    }

    public List<ConanTask<? extends ConanPipeline>> getPendingTasks(int maxRecords, int startingFrom, String orderBy) {
        List<ConanTask<? extends ConanPipeline>> tasks = getConanTaskDAO().getPendingTasks();
        reorderTasks(tasks, orderBy);
        return tasks.subList(startingFrom, startingFrom + maxRecords);
    }

    public List<ConanTask<? extends ConanPipeline>> getRunningTasks() {
        long start = System.currentTimeMillis();
        getLog().trace("Retrieving running tasks...");
        List<ConanTask<? extends ConanPipeline>> result = getConanTaskDAO().getRunningTasks();
        long end = System.currentTimeMillis();
        double time = ((double) (end - start)) / 1000;
        getLog().trace("Fetched and mapped all running tasks in " + time + "s.");
        return result;
    }

    public List<ConanTask<? extends ConanPipeline>> getRunningTasksSummary() {
        long start = System.currentTimeMillis();
        getLog().trace("Retrieving running tasks...");
        List<ConanTask<? extends ConanPipeline>> result = getConanTaskDAO().getRunningTasksSummary();
        long end = System.currentTimeMillis();
        double time = ((double) (end - start)) / 1000;
        getLog().trace("Fetched and mapped all running tasks in " + time + "s.");
        return result;
    }

    public List<ConanTask<? extends ConanPipeline>> getRunningTasks(int maxRecords, int startingFrom) {
        // return sublist based on maxRecords, startingFrom
        List<ConanTask<? extends ConanPipeline>> tasks = getConanTaskDAO().getRunningTasks();
        return tasks.subList(startingFrom, startingFrom + maxRecords);
    }

    public List<ConanTask<? extends ConanPipeline>> getRunningTasks(int maxRecords, int startingFrom, String orderBy) {
        List<ConanTask<? extends ConanPipeline>> tasks = getConanTaskDAO().getRunningTasks();
        reorderTasks(tasks, orderBy);
        return tasks.subList(startingFrom, startingFrom + maxRecords);
    }

    public List<ConanTask<? extends ConanPipeline>> getCompletedTasks() {
        long start = System.currentTimeMillis();
        getLog().trace("Retrieving completed tasks...");
        List<ConanTask<? extends ConanPipeline>> result = getConanTaskDAO().getCompletedTasks();
        long end = System.currentTimeMillis();
        double time = ((double) (end - start)) / 1000;
        getLog().trace("Fetched and mapped all completed tasks in " + time + "s.");
        return result;
    }

    /**
     * Returns a list of all tasks that have been executed and completed, summarised so as to exclude the query for
     * process information.  This includes tasks that completed successfully, and those that completed because a process
     * failed and was subsequently marked as complete by the submitter.
     * <p/>
     * This implementation enforces a hard limit of 500 tasks maximum to be returned.
     *
     * @return the tasks that have completed
     */
    public List<ConanTask<? extends ConanPipeline>> getCompletedTasksSummary() {
        long start = System.currentTimeMillis();
        getLog().trace("Retrieving completed tasks...");
        List<ConanTask<? extends ConanPipeline>> result = getConanTaskDAO().getCompletedTasksSummary(100, 0);
        long end = System.currentTimeMillis();
        double time = ((double) (end - start)) / 1000;
        getLog().trace("Fetched and mapped all completed tasks in " + time + "s.");
        return result;
    }

    public List<ConanTask<? extends ConanPipeline>> getCompletedTasks(int maxRecords, int startingFrom) {
        return getConanTaskDAO().getCompletedTasks(maxRecords, startingFrom);
    }

    public List<ConanTask<? extends ConanPipeline>> searchCompletedTasks(String name,
                                                                         ConanUser conanUser,
                                                                         Date fromDate,
                                                                         Date toDate) {
        if (name == null) {
            // replace task name with an empty string instead of null
            name = "";
        }

        if (conanUser == null) {
            if (toDate == null) {
                if (fromDate == null) {
                    return getConanTaskDAO().searchCompletedTasks(name);
                }
                else {
                    return getConanTaskDAO().searchCompletedTasks(name, fromDate);
                }
            }
            else {
                return getConanTaskDAO().searchCompletedTasks(name, fromDate, toDate);
            }
        }
        else {
            if (toDate == null) {
                if (fromDate == null) {
                    return getConanTaskDAO().searchCompletedTasks(name, conanUser.getId());
                }
                else {
                    return getConanTaskDAO().searchCompletedTasks(name, conanUser.getId(), fromDate);
                }
            }
            else {
                return getConanTaskDAO().searchCompletedTasks(name, conanUser.getId(), fromDate, toDate);
            }
        }
    }

    public void extractConanParameters(Map<ConanParameter, String> parameters,
                                       Map<String, String> inputValues,
                                       ConanProcess process) {
        for (ConanParameter param : process.getParameters()) {
            // validate our request by checking we have this param value supplied
            if (inputValues.get(param.getName()) == null) {
                throw new MissingRequiredParameterException(
                        "Required parameter '" + param.getName() + "' not supplied, " +
                                "required for process '" + process.getName() + "'");
            }
            else {
                if (!parameters.containsKey(param)) {
                    parameters.put(param, inputValues.get(param.getName()));
                }
            }
        }
    }

    private void reorderTasks(List<ConanTask<? extends ConanPipeline>> tasks, String orderBy) {
        Collections.sort(tasks, getComparatorFor(orderBy));
    }

    private Comparator<ConanTask> getComparatorFor(final String orderBy) {
        return new Comparator<ConanTask>() {
            public int compare(ConanTask task1, ConanTask task2) {
                try {
                    // get orderBy property
                    Method compareMethod = ConanTask.class.getMethod("get" + StringUtils.capitalize(orderBy));
                    try {
                        // invoke orderBy property and compare values
                        Object val1 = compareMethod.invoke(task1);
                        try {
                            Object val2 = compareMethod.invoke(task2);
                            if (val1 instanceof Comparable && val2 instanceof Comparable) {
                                // if values are comparable, return comparison
                                return ((Comparable) val1).compareTo(val2);
                            }
                            else if (val1 instanceof ConanProcess && val2 instanceof ConanProcess) {
                                // if values are processes, compare names
                                return ((ConanProcess) val1).getName().compareTo(((ConanProcess) val2).getName());
                            }
                            else {
                                // otherwise, assume equal
                                return 0;
                            }
                        }
                        catch (InvocationTargetException e) {
                            getLog().error(
                                    "Could not get value for '" + orderBy + "' property on task " + task1.getName());
                        }
                    }
                    catch (InvocationTargetException e) {
                        getLog().error(
                                "Could not get value for '" + orderBy + "' property on task " + task2.getName());
                    }
                }
                catch (NoSuchMethodException e) {
                    // warn, but don't reorder
                    getLog().warn("No such property '" + orderBy + "' to reorder on, using DAO default ordering");
                }
                catch (IllegalAccessException e) {
                    getLog().error("Could not compare tasks: ", e);
                }

                return 0;
            }
        };
    }
}
