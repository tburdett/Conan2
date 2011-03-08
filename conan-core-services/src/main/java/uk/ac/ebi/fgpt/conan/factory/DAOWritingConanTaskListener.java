package uk.ac.ebi.fgpt.conan.factory;

import uk.ac.ebi.fgpt.conan.core.task.ConanTaskEvent;
import uk.ac.ebi.fgpt.conan.core.task.ConanTaskListener;
import uk.ac.ebi.fgpt.conan.dao.ConanTaskDAO;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.service.ConanResponderService;

import java.util.HashSet;
import java.util.Set;

/**
 * A dedicated listener that writes status updates from any {@link uk.ac.ebi.fgpt.conan.model.ConanTask} to a database.
 *
 * @author Tony Burdett
 * @date 15-Oct-2010
 */
public class DAOWritingConanTaskListener implements ConanTaskListener {
    private Set<ConanResponderService> responderServices;
    private ConanTaskDAO conanTaskDAO;

    public DAOWritingConanTaskListener() {
        this.responderServices = new HashSet<ConanResponderService>();
    }

    public ConanTaskDAO getConanTaskDAO() {
        return conanTaskDAO;
    }

    public void setConanTaskDAO(ConanTaskDAO conanTaskDAO) {
        this.conanTaskDAO = conanTaskDAO;
    }

    public Set<ConanResponderService> getResponderServices() {
        return responderServices;
    }

    public void setResponderServices(Set<ConanResponderService> responderServices) {
        this.responderServices = responderServices;
    }

    public void stateChanged(ConanTaskEvent event) {
        getConanTaskDAO().updateTask(event.getTask());

        // if the state has changed to complete, we might need to issue a response
        if (event.getCurrentState() == ConanTask.State.COMPLETED) {
            for (ConanResponderService responder : getResponderServices()) {
                if (responder.respondsTo(event.getTask())) {
                    responder.generateResponse(event.getTask());
                }
            }
        }
    }

    public void processStarted(ConanTaskEvent event) {
        // update task, as current execution index needs to be updated
        getConanTaskDAO().updateTask(event.getTask());
        // save process run
        if (event.getCurrentProcessRun() != null) {
            getConanTaskDAO().saveProcessRun(event.getTask().getId(), event.getCurrentProcessRun());
        }
    }

    public void processEnded(ConanTaskEvent event) {
        // update task, as current execution index needs to be updated
        getConanTaskDAO().updateTask(event.getTask());

        if (event.getCurrentProcessRun() != null) {
            getConanTaskDAO().saveProcessRun(event.getTask().getId(), event.getCurrentProcessRun());
        }

        // and because this process ended, we might need to issue a response
        for (ConanResponderService responder : getResponderServices()) {
            if (responder.respondsTo(event.getTask())) {
                responder.generateResponse(event.getTask());
            }
        }
    }

    public void processFailed(ConanTaskEvent event) {
        // state should be updated to FAILED
        stateChanged(event);

        if (event.getCurrentProcessRun() != null) {
            getConanTaskDAO().saveProcessRun(event.getTask().getId(), event.getCurrentProcessRun());
        }

        // because this process failed, we might need to issue a response
        for (ConanResponderService responder : getResponderServices()) {
            if (responder.respondsTo(event.getTask())) {
                if (event.getCurrentProcessException() == null) {
                    responder.generateResponse(event.getTask());
                }
                else {
                    responder.generateResponse(event.getTask(), event.getCurrentProcessException());
                }
            }
        }
    }
}
