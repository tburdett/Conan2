package uk.ac.ebi.fgpt.conan.web.view;

import java.util.Map;

/**
 * A simple bean that contains all the information a user must provide in order to generate a new {@link
 * uk.ac.ebi.fgpt.conan.model.ConanTask} in Conan. The server should use this request bean to recover the pipeline,
 * referenced by name here, and each process that requires execution.  This bean includes a map of parameter values that
 * should cover all values requred for every process in the supplied pipeline.
 *
 * @author Tony Burdett
 * @date 13-Aug-2010
 */
public class SubmissionRequestBean {
    // submission request details
    private String priority;
    private String pipelineName;
    private int startingProcessIndex;
    private Map<String, String> inputParameters;

    // rest api key for this request
    private String restApiKey;

    /**
     * Default constructor to allow deserialization of JSON into a request bean: present to allow Jackson/spring to
     * construct a request bean from POST requests properly.
     */
    private SubmissionRequestBean() {
    }

    public SubmissionRequestBean(String priority, String pipelineName, Map<String, String> inputParameters) {
        this.priority = priority;
        this.pipelineName = pipelineName;
        this.inputParameters = inputParameters;
    }

    /**
     * Gets the priority of the task this request should create.  High priority tasks should always be executed before
     * medium priority tasks, and medium priority tasks always executed before low priority ones.
     *
     * @return the priority of this task
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Sets the priority of this task.  Takes a string value, which should match the value of {@link
     * uk.ac.ebi.fgpt.conan.model.ConanTask.Priority#toString()} for the priority you wish to use.
     *
     * @param priority the string representation of this task's priority
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Gets the pipeline that this task runs.
     *
     * @return the pipeline this request should create a task for
     */
    public String getPipelineName() {
        return pipelineName;
    }

    /**
     * Set the name of the pipeline the task created by this request should run
     *
     * @param pipelineName the pipeline this request should create a task for
     */
    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    /**
     * Gets the index of the first process to execute in this pipeline.  This is present so users can resume create a
     * new task that skips processes that are known to have completed previously - effectively skipping earlier
     * processes.
     *
     * @return the name of the first process to execute
     */
    public int getStartingProcessIndex() {
        return startingProcessIndex;
    }

    /**
     * Sets the index of the first process from the pipeline to execute.  If this isn't the same as the name of the
     * first process in the pipeline, processes will be skipped.
     *
     * @param startingProcessIndex the name of the process to start the task at
     */
    public void setStartingProcessIndex(int startingProcessIndex) {
        this.startingProcessIndex = startingProcessIndex;
    }

    /**
     * Returns a map of parameters to their values that this request should set for on the resulting Task.
     *
     * @return all supplied parameter values
     */
    public Map<String, String> getInputParameters() {
        return inputParameters;
    }

    /**
     * Sets the map of input parameters to the supplied values for this task.
     *
     * @param inputParameters the set of parameters supplied, mapped from parameter name to value
     */
    public void setInputParameters(Map<String, String> inputParameters) {
        this.inputParameters = inputParameters;
    }

    /**
     * Gets the rest api key of the user supplying this request.
     *
     * @return the rest api key
     */
    public String getRestApiKey() {
        return restApiKey;
    }

    /**
     * Sets the rest api key of the user supplying this request.
     *
     * @param restApiKey the users rest api key
     */
    public void setRestApiKey(String restApiKey) {
        this.restApiKey = restApiKey;
    }

    @Override public String toString() {
        return "SubmissionRequest: " +
                "pipelineName='" + pipelineName + "', " +
                "startingProcessIndex='" + startingProcessIndex + "', " +
                "inputParameters=" + inputParameters + "', " +
                "priority='" + priority + "'";
    }
}
