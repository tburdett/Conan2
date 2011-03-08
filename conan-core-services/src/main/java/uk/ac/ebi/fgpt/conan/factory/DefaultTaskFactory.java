package uk.ac.ebi.fgpt.conan.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.task.AbstractConanTask;
import uk.ac.ebi.fgpt.conan.core.task.ConanTaskListener;
import uk.ac.ebi.fgpt.conan.core.task.UserCreatedConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A abstract implentation of a {@link ConanTaskFactory} that generates {@link UserCreatedConanTask}s.  This task
 * implementation can have listeners registered to them that update Conan's backing database.  Each task, on creation,
 * has the set of listeners that have been set on this class registered to it.
 * <p/>
 * Extending classes can also use the {@link #registerListeners(uk.ac.ebi.fgpt.conan.model.ConanTask)} method to
 * register listeners to tasks retrieved from some other source.  As long as these tasks are implementations of the
 * class {@link uk.ac.ebi.fgpt.conan.core.task.AbstractConanTask} then the set listeners will be registered.  Note that
 * listeners must be registered before creating a task if they are to be used; listeners are not retrospectively applied
 * to previously constructed tasks
 * <p/>
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public class DefaultTaskFactory implements ConanTaskFactory {
    private Set<ConanTaskListener> conanTaskListeners;

    private Logger log = LoggerFactory.getLogger(getClass());

    public DefaultTaskFactory() {
        this.conanTaskListeners = new HashSet<ConanTaskListener>();
    }

    protected Logger getLog() {
        return log;
    }

    public Set<ConanTaskListener> getConanTaskListeners() {
        return conanTaskListeners;
    }

    public void setConanTaskListeners(Set<ConanTaskListener> conanTaskListeners) {
        this.conanTaskListeners = conanTaskListeners;
    }

    public <P extends ConanPipeline> ConanTask<P> createTask(P pipeline,
                                                             int startingProcessIndex,
                                                             Map<ConanParameter, String> parameters,
                                                             ConanTask.Priority priority,
                                                             ConanUser conanUser) {
        // validate supplied parameter values up front
        for (ConanParameter param : parameters.keySet()) {
            if (!param.validateParameterValue(parameters.get(param))) {
                throw new IllegalArgumentException(
                        "Parameter value '" + parameters.get(param) + "' is not valid");
            }
        }

        // create new trackable task
        UserCreatedConanTask<P> task = new UserCreatedConanTask<P>(priority,
                                                                   pipeline,
                                                                   startingProcessIndex,
                                                                   parameters,
                                                                   conanUser);
        // register listeners to this task
        registerListeners(task);

        // return task
        return task;
    }

    protected void registerListeners(ConanTask<? extends ConanPipeline> conanTask) {
        // add listeners, if possible
        if (conanTask instanceof AbstractConanTask) {
            for (ConanTaskListener listener : getConanTaskListeners()) {
                ((AbstractConanTask) conanTask).addConanTaskListener(listener);
            }
        }
    }
}

