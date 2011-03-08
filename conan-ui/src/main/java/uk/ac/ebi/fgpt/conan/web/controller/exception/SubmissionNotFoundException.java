package uk.ac.ebi.fgpt.conan.web.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A runtime exception that should be thrown by a Spring 3 Controller whenever a submission, requested by a specific ID,
 * was not found.
 *
 * @author Tony Burdett
 * @date 13-Aug-2010
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class SubmissionNotFoundException extends RuntimeException {
    public SubmissionNotFoundException(String taskID) {
        super("No submission with ID '" + taskID + "' could be located");
    }
}
