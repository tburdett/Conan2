package uk.ac.ebi.fgpt.conan.factory;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;

import java.util.Map;

/**
 * A factory class that generates {@link uk.ac.ebi.fgpt.conan.model.ConanTask}s that can be submitted to Conan.  Each
 * task executes a Pipeline with a given set of inputs, and includes some provenance data about the user that submitted
 * the task, when it was created, and so on.
 *
 * @author Tony Burdett
 * @date 30-Jul-2010
 * @see uk.ac.ebi.fgpt.conan.model.ConanTask
 */
public interface ConanTaskFactory {
    /**
     * Creates a task from the pipeline supplied and the set of required parameters.
     *
     * @param <P>                  a generic type capturing the type of pipeline supplied
     * @param pipeline             the pipeline to be executed by the created task
     * @param startingProcessIndex the index of the process to start from
     * @param parameters           a map that links all input parameters required to execute this pipeline to their
     *                             values
     * @param priority             the priority this task should run with
     * @param conanUser            the user submitting this task
     * @return the task
     * @throws IllegalArgumentException if either the pipeline referenced by name could not be found, or if the set of
     *                                  supplied parameters was not matched to the given pipeline.
     */
    <P extends ConanPipeline> ConanTask<P> createTask(P pipeline,
                                                      int startingProcessIndex,
                                                      Map<ConanParameter, String> parameters,
                                                      ConanTask.Priority priority,
                                                      ConanUser conanUser)
            throws IllegalArgumentException;
}
