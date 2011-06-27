package uk.ac.ebi.fgpt.conan.web.view;

import java.util.List;

/**
 * A simple bean that encapsulates the information required to request that pipelines present in the Conan UI be
 * reordered.
 *
 * @author Tony Burdett
 * @date 10/06/11
 */
public class PipelineReorderRequestBean {
    private List<String> requestedPipelineOrder;

    private String restApiKey;

    /**
     * Default constructor to allow deserialization of JSON into a request bean: present to allow Jackson/spring to
     * construct a request bean from POST requests properly.
     */
    private PipelineReorderRequestBean() {
    }

    public PipelineReorderRequestBean(List<String> requestedPipelineOrder, String restApiKey) {
        this.requestedPipelineOrder = requestedPipelineOrder;
        this.restApiKey = restApiKey;
    }

    public List<String> getRequestedPipelineOrder() {
        return requestedPipelineOrder;
    }

    public void setRequestedPipelineOrder(List<String> requestedPipelineOrder) {
        this.requestedPipelineOrder = requestedPipelineOrder;
    }

    public String getRestApiKey() {
        return restApiKey;
    }

    public void setRestApiKey(String restApiKey) {
        this.restApiKey = restApiKey;
    }

    @Override public String toString() {
        return "PipelineReorderRequestBean: " +
                "requestedPipelineOrder=" + requestedPipelineOrder + ", " +
                "restApiKey='" + restApiKey + "'";
    }
}
