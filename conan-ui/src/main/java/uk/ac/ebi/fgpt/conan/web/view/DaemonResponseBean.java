package uk.ac.ebi.fgpt.conan.web.view;

/**
 * A simple bean that encapsulates a boolean indicating whether daemon mode is enabled or disabled.  This becomes the
 * response from the {@link uk.ac.ebi.fgpt.conan.web.controller.DaemonController}, and is represented thus to allow
 * automatic JSON serialization out of the box.
 *
 * @author Tony Burdett
 * @date 13-Aug-2010
 */
public class DaemonResponseBean {
    private final boolean operationSuccessful;
    private final String statusMessage;
    private final boolean enabled;
    private final String ownerEmail;

    public DaemonResponseBean(boolean operationSuccessful, String statusMessage, boolean enabled, String ownerEmail) {
        this.operationSuccessful = operationSuccessful;
        this.statusMessage = statusMessage;
        this.enabled = enabled;
        this.ownerEmail = ownerEmail;
    }

    public boolean isOperationSuccessful() {
        return operationSuccessful;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }
}
