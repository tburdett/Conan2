package uk.ac.ebi.fgpt.conan.web.controller.exception;

/**
 * A type of {@link RuntimeException} that is thrown whenever a controller class receives a request that it fails to
 * validate, or could not be understood for some reason.
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException() {
        super();
    }

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRequestException(Throwable cause) {
        super(cause);
    }
}
