package uk.ac.ebi.fgpt.conan.web.view;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;

import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 27/06/11
 */
public class PipelineReorderResponseBean {
    private final boolean operationSuccessful;
    private final String statusMessage;
    private final List<ConanPipeline> resultingPipelineOrder;

    public PipelineReorderResponseBean(boolean operationSuccessful,
                                       String statusMessage,
                                       List<ConanPipeline> resultingPipelineOrder) {
        this.operationSuccessful = operationSuccessful;
        this.statusMessage = statusMessage;
        this.resultingPipelineOrder = resultingPipelineOrder;
    }

    public boolean isOperationSuccessful() {
        return operationSuccessful;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public List<ConanPipeline> getResultingPipelineOrder() {
        return resultingPipelineOrder;
    }
}
