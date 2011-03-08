package uk.ac.ebi.fgpt.conan.web.view;

import java.util.List;

/**
 * A simple wrapper to appease jackson that wraps a list of {@link uk.ac.ebi.fgpt.conan.web.view.SubmissionRequestBean}s
 * into one typed object
 *
 * @author Tony Burdett
 * @date 15-Nov-2010
 */
public class BatchRequestBean {
    private List<SubmissionRequestBean> submissionRequests;

    /**
     * Default constructor to allow deserialization of JSON into a request bean: present to allow Jackson/spring to
     * construct a request bean from POST requests properly.
     */
    private BatchRequestBean() {
    }

    public BatchRequestBean(List<SubmissionRequestBean> submissionRequests) {
        this.submissionRequests = submissionRequests;
    }

    public List<SubmissionRequestBean> getSubmissionRequests() {
        return submissionRequests;
    }

    public void setSubmissionRequests(List<SubmissionRequestBean> submissionRequests) {
        this.submissionRequests = submissionRequests;
    }
}
