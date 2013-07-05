package uk.ac.ebi.fgpt.conan.web.view;

import java.util.List;

/**
 * A simple bean that encapsulates the information in a request for the creation of a new {@link
 * uk.ac.ebi.fgpt.conan.model.ConanPipeline}, supplied by a user.  Pipelines actually include references to {@link
 * uk.ac.ebi.fgpt.conan.model.ConanProcess}es, which are executable and therefore do not easily serialize.  However,
 * each process is necessarily uniquely identifiable by it's name and type.  Therefore, this request bean contains only
 * the name/type pair, not the actual Process.  The server, on receiving this request, should use it to generate a new
 * Pipeline object using references to actual process objects.
 *
 * @author Tony Burdett
 * @date 13-Aug-2010
 */
public class PipelineRequestBean {
    // pipeline details
    private final String name;
    private final List<String> processDescriptions;
    private final boolean isPrivate;

    // the rest api key to access this service
    private final String restApiKey;

    public PipelineRequestBean(String name, List<String> processDescriptions, String restApiKey) {
        this(name, processDescriptions, false, restApiKey);
    }

    public PipelineRequestBean(String name, List<String> processDescriptions, boolean isPrivate, String restApiKey) {
        this.name = name;
        this.restApiKey = restApiKey;
        this.processDescriptions = processDescriptions;

        this.isPrivate = isPrivate;
    }

    /**
     * Gets the name of the new pipeline being requested
     *
     * @return the pipeline name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the rest api key of the user that is requesting the creation of the new pipeline
     *
     * @return the rest api key of the creator of this pipeline request
     */
    public String getRestApiKey() {
        return restApiKey;
    }

    /**
     * Gets a list of string arrays representing process name/type pairs.  Each element in the list should be a string
     * array with a length of 2, where the element at index 0 is the process name and the element at index 1 is the
     * string representing the process type.
     *
     * @return a simple representation of the processes to recover when creating this pipeline
     */
    public List<String> getProcesses() {
        return processDescriptions;
    }

    /**
     * Gets whether or not this pipeline should be made public on creation.
     *
     * @return whether this pipeline is public
     */
    public boolean isPrivate() {
        return isPrivate;
    }

    @Override
    public String toString() {
        return "PipelineRequest: " +
                "name='" + name + "', " +
                "processes={" + processDescriptions + "}, " +
                "isPrivate=" + isPrivate + "}";
    }
}
