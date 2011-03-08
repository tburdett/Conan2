package uk.ac.ebi.fgpt.conan.service.exception;

/**
 * A type of {@link RuntimeException} that is thrown in response to a lookup request for a {@link
 * uk.ac.ebi.fgpt.conan.model.ConanPipeline} that does not exist for the current service.  For example, this type of
 * exception might be thrown when you called {@link uk.ac.ebi.fgpt.conan.service.ConanPipelineService#getPipeline(uk.ac.ebi.fgpt.conan.model.ConanUser
 * , String)} and supply a string for which no pipeline exists.
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public class ConanPipelineLookupException extends ConanLookupException {
    public ConanPipelineLookupException() {
        super();
    }

    public ConanPipelineLookupException(String message) {
        super(message);
    }

    public ConanPipelineLookupException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConanPipelineLookupException(Throwable cause) {
        super(cause);
    }
}
