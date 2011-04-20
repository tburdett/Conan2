package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

/**
 * A service that provides a response as feedback on completion of certain processes.  Whenever a task completes a
 * process, it should attempt to generate a response by passing the task being executed and the process run that
 * completed.  This response may take the form of an email to the submitting user, a log statement to a particular
 * server, or something else.  Processes may opt to generate no response at all. Normally, implementations of this class
 * will have awareness of the types of processes that can be run, thereby filtering their responses appropriately.
 *
 * @author Tony Burdett
 * @date 10-Nov-2010
 */
public interface ConanResponderService {
    /**
     * Returns a flag that indicates if this task, given it's current state, should be used to generate a response.
     *
     * @param task the task that is being executed
     * @return true if this needs a response, false otherwise
     */
    boolean respondsTo(ConanTask task);

    /**
     * Generates a response to the "owner" of daemon mode, informing them that daemon mode was enabled or disabled.
     *
     * @param daemonModeActive     whether daemon mode has been activated or deactivated
     * @param userRequestingChange the user that requested the change to daemon mode
     * @param daemonOwner          the daemon mode user
     */
    void generateDaemonModeToggleResponse(boolean daemonModeActive,
                                          ConanUser userRequestingChange,
                                          ConanUser daemonOwner);

    /**
     * Generates a response to the previous and the new owners of daemon mode, informing them that the email address
     * that any notifications generated by daemon mode will now be directed to the new owner.
     *
     * @param previousOwnerEmail   the email address that was previously associated with daemon mode notifications
     * @param userRequestingChange the user that has requested the change in daemon mode
     * @param daemonOwner          the daemon mode user, with the newly associated email address
     */
    void generateDaemonOwnerChangeResponse(String previousOwnerEmail,
                                           ConanUser userRequestingChange,
                                           ConanUser daemonOwner);

    /**
     * Generates a user response, outside of the normal Conan interface feedback system.  You should use this if, for
     * example, you want to email users on completion of a particular type of process.
     *
     * @param task the task that is being executed
     */
    void generateResponse(ConanTask task);

    /**
     * Generates a user response, outside of the normal Conan interface feedback system.  This form of the method should
     * be used whenever you want to generate a response tailored to a particular failure condition.
     *
     * @param task the task that is being executed
     * @param pex  the ProcessExecutionException that occurred
     */
    void generateResponse(ConanTask task, ProcessExecutionException pex);
}