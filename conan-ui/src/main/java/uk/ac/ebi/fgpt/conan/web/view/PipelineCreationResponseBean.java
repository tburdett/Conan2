package uk.ac.ebi.fgpt.conan.web.view;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;

import java.util.Collection;

/**
 * A simple bean that is returned in response to a pipeline creation request.  It returns all information about whether
 * the operation to add a new pipeline succeeded, the new pipeline that resulted, and the collection of pipelines now
 * created by the user submitting the request.
 *
 * @author Tony Burdett
 * @date 15-Oct-2010
 */
public class PipelineCreationResponseBean {
    private final boolean operationSuccessful;
    private final String statusMessage;
    private final ConanPipeline createdPipeline;
    private final Collection<ConanPipeline> userPipelines;

    public PipelineCreationResponseBean(boolean operationSuccessful,
                                        String statusMessage,
                                        ConanPipeline createdPipeline,
                                        Collection<ConanPipeline> userPipelines) {
        this.operationSuccessful = operationSuccessful;
        this.statusMessage = statusMessage;
        this.createdPipeline = createdPipeline;
        this.userPipelines = userPipelines;
    }

    public boolean isOperationSuccessful() {
        return operationSuccessful;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public ConanPipeline getCreatedPipeline() {
        return createdPipeline;
    }

    public Collection<ConanPipeline> getUserPipelines() {
        return userPipelines;
    }
}
