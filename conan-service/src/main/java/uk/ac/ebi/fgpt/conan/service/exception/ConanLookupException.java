package uk.ac.ebi.fgpt.conan.service.exception;

/**
 * A abstract type of {@link RuntimeException} that is thrown in response to a lookup request for a Conan object that does not
 * exist for the current service.  Usually, more specific types of exception will be thrown depending on the type of
 * concept being looked up.
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public abstract class ConanLookupException extends RuntimeException {
    public ConanLookupException() {
        super();
    }

    public ConanLookupException(String message) {
        super(message);
    }

    public ConanLookupException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConanLookupException(Throwable cause) {
        super(cause);
    }
}
