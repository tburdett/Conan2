package uk.ac.ebi.fgpt.conan.service.exception;

/**
 * A type of {@link RuntimeException} that is thrown in response to a lookup request for a {@link
 * uk.ac.ebi.fgpt.conan.model.ConanProcess} that does not exist for the current service.  For example, this type of
 * exception might be thrown when you called {@link uk.ac.ebi.fgpt.conan.service.ConanProcessService#getProcess(String)}
 * and supply a string for which no process exists.
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public class ConanProcessLookupException extends ConanLookupException {
    public ConanProcessLookupException() {
        super();
    }

    public ConanProcessLookupException(String message) {
        super(message);
    }

    public ConanProcessLookupException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConanProcessLookupException(Throwable cause) {
        super(cause);
    }
}
