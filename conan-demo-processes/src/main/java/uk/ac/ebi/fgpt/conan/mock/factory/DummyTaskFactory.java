package uk.ac.ebi.fgpt.conan.mock.factory;

import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;

import java.util.Map;

/**
 * A dummy implementation of a task factory that adds a task ID to each task, incrementing based on an internal counter
 *
 * @author Tony Burdett
 * @date 29/06/11
 */
public class DummyTaskFactory extends DefaultTaskFactory {
    private int counter = 1;

    @Override
    public <P extends ConanPipeline> ConanTask<P> createTask(P pipeline,
                                                             int startingProcessIndex,
                                                             Map<ConanParameter, String> parameters,
                                                             ConanTask.Priority priority,
                                                             ConanUser conanUser) {
        ConanTask<P> t = super.createTask(pipeline, startingProcessIndex, parameters, priority, conanUser);
        t.setId(Integer.toString(counter++));
        return t;
    }
}
