package uk.ac.ebi.fgpt.conan.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.ConanDaemonService;
import uk.ac.ebi.fgpt.conan.service.ConanResponderService;
import uk.ac.ebi.fgpt.conan.service.ConanUserService;
import uk.ac.ebi.fgpt.conan.web.view.DaemonResponseBean;

import java.util.HashSet;
import java.util.Set;

/**
 * Enables and disables daemon mode within Conan.
 *
 * @author Tony Burdett
 * @date 30-Jul-2010
 */
@Controller
@RequestMapping("/daemon")
public class DaemonController {
    private ConanDaemonService daemonService;
    private ConanUserService userService;
    private Set<ConanResponderService> responderServices;

    private Logger log = LoggerFactory.getLogger(getClass());

    public DaemonController() {
        this.responderServices = new HashSet<ConanResponderService>();
    }

    protected Logger getLog() {
        return log;
    }

    public ConanDaemonService getDaemonService() {
        return daemonService;
    }

    @Autowired
    public void setDaemonService(ConanDaemonService daemonService) {
        this.daemonService = daemonService;
    }

    public ConanUserService getUserService() {
        return userService;
    }

    @Autowired
    public void setUserService(ConanUserService userService) {
        this.userService = userService;
    }

    public Set<ConanResponderService> getResponderServices() {
        return responderServices;
    }

    @Autowired(required = false)
    public void setResponderServices(Set<ConanResponderService> responderServices) {
        this.responderServices = responderServices;
    }

    /**
     * Gets a {@link uk.ac.ebi.fgpt.conan.web.view.DaemonResponseBean} that indicates the current state of daemon mode.
     * This bean basically wraps a boolean - true or false, depending on whether daemon mode is enabled or disabled.
     *
     * @return a daemon response bean indicating daemon mode state
     */
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody DaemonResponseBean isRunning() {
        getLog().debug("Querying for current state of daemon mode");
        boolean running = getDaemonService().isRunning();
        String msg = "Daemon mode " + (running ? "is" : "is not") + " running";
        return new DaemonResponseBean(true, msg, running, getDaemonService().getDaemonUser().getEmail());
    }

    /**
     * Toggles daemon mode.  Triggered by a PUT request to '/daemon' with a required boolean parameter "enabled".  This
     * generates a {@link uk.ac.ebi.fgpt.conan.web.view.DaemonResponseBean} reporting on the state of daemon mode after
     * the PUT operation has completed.
     *
     * @param enable     true to start daemon mode, false to stop
     * @param restApiKey the rest api key of the user accessing this service
     * @return true if daemon mode is activated, false otherwise
     */
    @RequestMapping(method = RequestMethod.PUT)
    public @ResponseBody DaemonResponseBean toggle(@RequestParam boolean enable, @RequestParam String restApiKey) {
        getLog().debug("Request to toggle daemon mode: currently " +
                (getDaemonService().isRunning() ? "enabled" : "disabled") + ", request to " +
                (enable ? "enable" : "disable"));
        // get the user identified by this rest api key
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // user has permission to do this?
        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.ADMINISTRATOR) > -1) {
            if (enable) {
                if (!getDaemonService().isRunning()) {
                    getDaemonService().start();
                }
            }
            else {
                if (getDaemonService().isRunning()) {
                    getDaemonService().stop();
                }
            }
            String msg = "Daemon mode " + (enable ? "enabled" : "disabled") + " successfully";
            getLog().debug((enable ? "Started" : "Stopped") + " daemon mode ok!");

            // generate responses before returning a result
            for (ConanResponderService responder : getResponderServices()) {
                responder.generateDaemonModeToggleResponse(getDaemonService().isRunning(),
                                                           conanUser,
                                                           getDaemonService().getDaemonUser());
            }

            // and return the successful result
            return new DaemonResponseBean(true,
                                          msg,
                                          getDaemonService().isRunning(),
                                          getDaemonService().getDaemonUser().getEmail());
        }
        else {
            String msg = "You do not have permission to toggle daemon mode";
            return new DaemonResponseBean(false,
                                          msg,
                                          getDaemonService().isRunning(),
                                          getDaemonService().getDaemonUser().getEmail());
        }
    }

    /**
     * Updates the email address associated with daemon mode.
     *
     * @param emailAddress the email address to send daemon mode notifications to
     * @param restApiKey   the rest api key of the user accessing this service
     * @return a response bean indicating whether this update was successful or not
     */
    @RequestMapping(value = "email", method = RequestMethod.PUT)
    public @ResponseBody DaemonResponseBean updateNotificationEmailAddress(@RequestParam String emailAddress,
                                                                           @RequestParam String restApiKey) {
        getLog().debug(
                "Request to update daemon mode email: " +
                        "currently '" + getDaemonService().getDaemonUser().getEmail() + "', " +
                        "updating to '" + emailAddress + "'");
        // get the user identified by this rest api key
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // user has permission to do this?
        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.ADMINISTRATOR) > -1) {
            String oldEmail = getDaemonService().getDaemonUser().getEmail();
            getDaemonService().setNotificationEmailAddress(emailAddress);
            String msg = "Daemon mode email notification address updated to '" + emailAddress + "'";

            // generate responses before returning a result
            for (ConanResponderService responder : getResponderServices()) {
                responder.generateDaemonOwnerChangeResponse(oldEmail,
                                                            conanUser,
                                                            getDaemonService().getDaemonUser());
            }

            // and return the successful result
            return new DaemonResponseBean(true,
                                          msg,
                                          getDaemonService().isRunning(),
                                          getDaemonService().getDaemonUser().getEmail());
        }
        else {
            String msg = "You do not have permission to toggle daemon mode";
            return new DaemonResponseBean(false,
                                          msg,
                                          getDaemonService().isRunning(),
                                          getDaemonService().getDaemonUser().getEmail());
        }
    }
}
