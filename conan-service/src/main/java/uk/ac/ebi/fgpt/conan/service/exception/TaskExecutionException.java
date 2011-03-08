package uk.ac.ebi.fgpt.conan.service.exception;

/**
 * A wrapper exception that should be thrown whenever a {@link uk.ac.ebi.fgpt.conan.model.ConanTask} that was executed throws an exception.
 *
 * @author Tony Burdett
 * @date 28-Jul-2010
 * @see uk.ac.ebi.fgpt.conan.model.ConanTask
 */
public class TaskExecutionException extends Exception {
    public TaskExecutionException() {
        super();
    }

    public TaskExecutionException(String message) {
        super(message);
    }

    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskExecutionException(Throwable cause) {
        super(cause);
    }
}
