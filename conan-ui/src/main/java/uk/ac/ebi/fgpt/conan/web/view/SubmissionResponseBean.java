package uk.ac.ebi.fgpt.conan.web.view;

/**
 * A simple bean indicating whether a submission request was successful, and if so returns the created task.
 *
 * @author Tony Burdett
 * @date 15-Oct-2010
 */
public class SubmissionResponseBean {
    private final boolean operationSuccessful;
    private final String statusMessage;
    private final String submittedTaskID;
    private final String submittedTaskName;

    public SubmissionResponseBean(boolean operationSuccessful,
                                  String statusMessage,
                                  String submittedTaskID,
                                  String submittedTaskName) {
        this.operationSuccessful = operationSuccessful;
        this.statusMessage = statusMessage;
        this.submittedTaskID = submittedTaskID;
        this.submittedTaskName = submittedTaskName;
    }

    public boolean isOperationSuccessful() {
        return operationSuccessful;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getSubmittedTaskID() {
        return submittedTaskID;
    }

    public String getSubmittedTaskName() {
        return submittedTaskName;
    }
}
