package uk.ac.ebi.fgpt.conan.service.exception;

/**
 * An exception thrown whenever a task submission is rejected for any reason.
 *
 * @author Tony Burdett
 * @date 07-Dec-2010
 */
public class SubmissionException extends Exception {
    public SubmissionException() {
        super();
    }

    public SubmissionException(String message) {
        super(message);
    }

    public SubmissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubmissionException(Throwable cause) {
        super(cause);
    }
}
