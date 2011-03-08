package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Collection;

/**
 * A service that is used to automatically detect and submit new {@link uk.ac.ebi.fgpt.conan.model.ConanTask}s to Conan.
 * This is designed to run as a service with minimal user intervention.  Instead of requiring a user to manually submit
 * tasks, this daemon service automatically looks for new inputs that can be supplied to the pipelines configured.
 *
 * @author Tony Burdett
 * @date 30-Jul-2010
 */
public interface ConanDaemonService {
    /**
     * Returns the {@link uk.ac.ebi.fgpt.conan.model.ConanPipeline}s that this daemon service has been set up to submit
     * tasks to.
     *
     * @return the pipelines tasks will be submitted to
     */
    Collection<ConanPipeline> getPipelines();

    /**
     * Adds a new {@link uk.ac.ebi.fgpt.conan.model.ConanPipeline} to this daemon service that in future will be used to
     * submit jobs to
     *
     * @param conanPipeline the new pipeline to use in this daemon service
     * @return true if this pipeline is successfully added
     */
    boolean addPipeline(ConanPipeline conanPipeline);

    /**
     * Removes the given {@link uk.ac.ebi.fgpt.conan.model.ConanPipeline} from this daemon service, so that any newly
     * submitted {@link uk.ac.ebi.fgpt.conan.model.ConanTask}s will no longer go to this pipeline
     *
     * @param conanPipeline the pipeline to remove from the daemon service
     * @return true if this pipeline is successfully removed
     */
    boolean removePipeline(ConanPipeline conanPipeline);

    /**
     * Returns whether this daemon service is currently running.
     *
     * @return true if the daemon is running, false otherwise
     */
    boolean isRunning();

    /**
     * Starts this daemon service.  This enables dynamic discovery and submission of new inputs to available pipelines.
     *
     * @return true if startup completed successfully
     */
    boolean start();

    /**
     * Stops this daemon service.  Dynamic discovery and submission of new inputs to available pipelines will no longer
     * happen.
     *
     * @return true if daemon mode halted successfully
     */
    boolean stop();

    /**
     * The user account for daemon mode.  All jobs submitted via daemon mode will be owned by this user.
     *
     * @return the daemon user
     */
    ConanUser getDaemonUser();

    /**
     * Updates the email address that daemon mode notifications are sent to.
     *
     * @param emailAddress the email address associated with the daemon mode user
     */
    void setNotificationEmailAddress(String emailAddress);
}
