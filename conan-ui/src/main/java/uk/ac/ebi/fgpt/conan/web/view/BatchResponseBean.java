package uk.ac.ebi.fgpt.conan.web.view;

import java.util.List;

/**
 * A simple bean that represents the outcome of a file upload request of a submission batch file.  This contains fields
 * indicating whether the upload was successful, whether the pipeline this upload is a batch submission for allows
 * submission of batches, and if both of those fields are true, this bean also contains the list of submissions request
 * beans that can be passed immediately back to the server.
 *
 * @author Tony Burdett
 * @date 09-Nov-2010
 */
public class BatchResponseBean {
    private boolean uploadSuccessful;
    private boolean pipelineAcceptsBatches;
    private List<SubmissionRequestBean> requests;

    public BatchResponseBean(boolean uploadSuccessful,
                             boolean pipelineAcceptsBatches,
                             List<SubmissionRequestBean> requests) {
        this.uploadSuccessful = uploadSuccessful;
        this.pipelineAcceptsBatches = pipelineAcceptsBatches;
        this.requests = requests;
    }

    public boolean isUploadSuccessful() {
        return uploadSuccessful;
    }

    public boolean isPipelineAcceptsBatches() {
        return pipelineAcceptsBatches;
    }

    public List<SubmissionRequestBean> getRequests() {
        return requests;
    }
}
