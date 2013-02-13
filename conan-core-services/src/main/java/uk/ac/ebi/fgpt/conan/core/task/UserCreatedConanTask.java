package uk.ac.ebi.fgpt.conan.core.task;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Map;

/**
 * An implementation of a {@link uk.ac.ebi.fgpt.conan.model.ConanTask} for tasks created in response to a user request.
 * Normally, these tasks will not be instantiated directly and will instead be created by a {@link
 * uk.ac.ebi.fgpt.conan.factory.ConanTaskFactory}.  The factory will usually intervene to ensure appropriate listeners
 * are registered and that the task is stored in any backing datasource.
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public class UserCreatedConanTask<P extends ConanPipeline> extends AbstractConanTask<P> {
    // must be supplied to constructor
    private final Priority priority;
    private final P conanPipeline;
    private final Map<ConanParameter, String> parameterValues;
    private final ConanUser submitter;

    public UserCreatedConanTask(Priority priority,
                                P conanPipeline,
                                int firstProcessIndex,
                                Map<ConanParameter, String> parameterValues,
                                ConanUser submitter) {
        // super
        super(firstProcessIndex);

        // set user supplied task variables
        this.priority = priority;
        this.conanPipeline = conanPipeline;
        this.parameterValues = parameterValues;
        this.submitter = submitter;
    }

    public String getName() {
        // is once of our parameter values an accession?
        for (ConanParameter parameter : getParameterValues().keySet()) {
            if (parameter.getName().contains("Accession")) {
                return getParameterValues().get(parameter);
            }
        }
        // if we got to here, no accession number, so return task id
        return getId();
    }

    public Priority getPriority() {
        return priority;
    }

    public P getPipeline() {
        return conanPipeline;
    }

    public Map<ConanParameter, String> getParameterValues() {
        return parameterValues;
    }

    public ConanUser getSubmitter() {
        return submitter;
    }
}
