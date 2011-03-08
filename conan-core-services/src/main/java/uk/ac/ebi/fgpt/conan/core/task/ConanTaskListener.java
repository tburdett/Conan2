package uk.ac.ebi.fgpt.conan.core.task;

import java.util.EventListener;

/**
 * A listener that can be registered to {@link UserCreatedConanTask}s to provide update events whenever the task
 * changes.
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public interface ConanTaskListener extends EventListener {
    /**
     * Called whenever a {@link uk.ac.ebi.fgpt.conan.model.ConanTask} changes it's state, either because it has started
     * or stopped running for some reason.
     *
     * @param event the event that was fired
     */
    public void stateChanged(ConanTaskEvent event);

    /**
     * Called whenever a {@link uk.ac.ebi.fgpt.conan.model.ConanTask} starts a new process. This does not cause a state
     * change by itself - the state will only change if the task is also complete, and this is not managed by this
     * method.
     *
     * @param event the event that was fired
     */
    public void processStarted(ConanTaskEvent event);

    /**
     * Called whenever a {@link uk.ac.ebi.fgpt.conan.model.ConanTask} finishes a process.  This does not cause a state
     * change by itself - the state will only change if the task is also complete, and this is not managed by this
     * method.
     *
     * @param event the event that was fired
     */
    public void processEnded(ConanTaskEvent event);

    /**
     * Called whenever a {@link uk.ac.ebi.fgpt.conan.model.ConanTask} fails a process.  This should also cause a state
     * change.
     *
     * @param event
     */
    public void processFailed(ConanTaskEvent event);
}
