package uk.ac.ebi.fgpt.conan.service.exception;

/**
 * A type of {@link RuntimeException} that is thrown whenever you attempt to generate a {@link
 * uk.ac.ebi.fgpt.conan.model.ConanTask} but fail to supply the correct parameter values.
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public class MissingRequiredParameterException extends RuntimeException {
    public MissingRequiredParameterException() {
        super();
    }

    public MissingRequiredParameterException(String message) {
        super(message);
    }

    public MissingRequiredParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingRequiredParameterException(Throwable cause) {
        super(cause);
    }
}
