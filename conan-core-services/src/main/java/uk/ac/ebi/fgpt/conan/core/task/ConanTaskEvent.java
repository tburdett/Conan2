package uk.ac.ebi.fgpt.conan.core.task;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanProcessRun;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

/**
 * An object encapsulating an event that occurs on a {@link uk.ac.ebi.fgpt.conan.model.ConanTask}.  Events are triggered
 * by changes in state - pending, running or complete - or the commencement or completion of a new process in the task's
 * pipeline. The current process run that is occurring being executed, and the current state, can be extracted from this
 * event.  For any events generated in response to a process fail (reported with a {@link
 * uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException}) you should also set the exception, in order for
 * any reporters to extract additional information about the process failure.
 *
 * @author Tony Burdett
 * @date 13-Oct-2010
 */
public class ConanTaskEvent {
    private ConanTask<?> task;
    private ConanTask.State currentState;
    private ConanProcess currentProcess;
    private ConanProcessRun currentProcessRun;
    private ProcessExecutionException currentProcessException;

    public ConanTaskEvent(ConanTask<?> conanTask,
                          ConanTask.State currentState,
                          ConanProcess currentProcess,
                          ConanProcessRun currentProcessRun) {
        this.task = conanTask;
        this.currentState = currentState;
        this.currentProcess = currentProcess;
        this.currentProcessRun = currentProcessRun;
    }

    public ConanTaskEvent(ConanTask<?> conanTask,
                          ConanTask.State currentState,
                          ConanProcess currentProcess,
                          ConanProcessRun currentProcessRun,
                          ProcessExecutionException currentProcessException) {
        this.task = conanTask;
        this.currentState = currentState;
        this.currentProcess = currentProcess;
        this.currentProcessRun = currentProcessRun;
        this.currentProcessException = currentProcessException;
    }

    public ConanTask<?> getTask() {
        return task;
    }

    public ConanTask.State getCurrentState() {
        return currentState;
    }

    public ConanProcess getCurrentProcess() {
        return currentProcess;
    }

    public ConanProcessRun getCurrentProcessRun() {
        return currentProcessRun;
    }

    public ProcessExecutionException getCurrentProcessException() {
        return currentProcessException;
    }
}
