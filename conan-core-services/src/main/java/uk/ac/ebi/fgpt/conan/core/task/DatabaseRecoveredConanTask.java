package uk.ac.ebi.fgpt.conan.core.task;

import org.springframework.util.Assert;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcessRun;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.*;

/**
 * An implementation of a {@link uk.ac.ebi.fgpt.conan.model.ConanTask} that is constructed by recovering all the
 * important provenance information from a database.  To this end, there are getters and setters for all fields.
 * <p/>
 * All tracking information is still looked after by the abstract class {@link uk.ac.ebi.fgpt.conan.core.task.AbstractConanTask}.
 * You should take care to update the important fields if this task is due to resume executing (namely, the current
 * state, status message, and currentExecutionIndex).
 *
 * @author Tony Burdett
 * @date 28-Oct-2010
 */
public class DatabaseRecoveredConanTask<P extends ConanPipeline> extends AbstractConanTask<P> {
    private String name;
    private Priority priority;
    private P conanPipeline;
    private Map<ConanParameter, String> parameterValues;
    private ConanUser submitter;

    public DatabaseRecoveredConanTask() {
        super(0);
    }

    public String getName() {
        return name;
    }

    public void setName(String taskName) {
        this.name = taskName;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public P getPipeline() {
        Assert.notNull(conanPipeline, "This task was not fully created.  Use a TaskFactory to acquire a " +
                "fully resolved ConanTask from the database");
        return conanPipeline;
    }

    public void setPipeline(P pipeline) {
        this.conanPipeline = pipeline;
    }

    public Map<ConanParameter, String> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(Map<ConanParameter, String> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public void addParameterValue(ConanParameter parameter, String value) {
        // lazy init
        if (getParameterValues() == null) {
            setParameterValues(new HashMap<ConanParameter, String>());
        }
        this.parameterValues.put(parameter, value);
    }

    public ConanUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(ConanUser submitter) {
        this.submitter = submitter;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public void setCurrentStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setFirstTaskIndex(int firstTaskIndex) {
        this.firstTaskIndex = firstTaskIndex;
    }

    public void setCurrentExecutionIndex(int currentExecutionIndex) {
        this.currentExecutionIndex = currentExecutionIndex;
    }

    public void setConanProcessRuns(List<ConanProcessRun> processes) {
        this.processRuns = processes;
    }

    public void addConanProcessRun(ConanProcessRun process) {
        if (getConanProcessRuns() == null) {
            setConanProcessRuns(new ArrayList<ConanProcessRun>());
        }
        this.processRuns.add(process);
    }
}
