package uk.ac.ebi.fgpt.conan.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.ConanUserService;
import uk.ac.ebi.fgpt.conan.web.view.RestApiKeyResponseBean;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;

/**
 * Used to obtain user details for registered users, or request new REST API keys for new users.
 *
 * @author Tony Burdett
 * @date 27-Oct-2010
 */
@Controller
@RequestMapping("/users")
public class UserController {
    private ConanUserService userService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ConanUserService getUserService() {
        return userService;
    }

    @Autowired
    public void setUserService(ConanUserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/{userID}", method = RequestMethod.GET)
    public
    @ResponseBody
    ConanUser getUser(@PathVariable String userID) {
        return getUserService().getUser(userID);
    }

    @RequestMapping(method = RequestMethod.GET)
    public
    @ResponseBody
    Collection<ConanUser> getUsers() {
        return getUserService().getUsers();
    }


    /**
     * Gets the {@link ConanUser} with this email address by delegating to a {@link
     * uk.ac.ebi.fgpt.conan.service.ConanUserService}. The normal strategy is to create new users if the email has not
     * been seen before, subject to a verification operation.  If verification fails, this will normally return an
     * anonymous guest user.
     *
     * @param email the email address the user is logging in with
     * @return a simple bean wrapping the user's rest api key
     */
    @RequestMapping(value = "/email-query", method = RequestMethod.GET)
    public
    @ResponseBody
    ConanUser getUserByEmail(@RequestParam(value = "email") String email) {
        getLog().debug("Attempting to acquire user with email " + email);
        try {
            String decodedEmail = URLDecoder.decode(email, "UTF-8");
            return getUserService().getUserByEmail(decodedEmail);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding should be supported, but wasn't.  JVM configuration error?", e);
        }
    }

    /**
     * Gets the {@link ConanUser} that has been assigned this restApiKey by delegating to a {@link
     * uk.ac.ebi.fgpt.conan.service.ConanUserService}.  This is generally a verification method, to confirm that you can
     * retrieve the correct user given a rest api key.  If the rest api key is wrong or out of date, this method will
     * return null.
     *
     * @param restApiKey the rest api key that identifies the user we want
     * @return the user with this key, or null if there is none
     */
    @RequestMapping(value = "/restApiKey-query", method = RequestMethod.GET)
    public
    @ResponseBody
    ConanUser getUserByRestApiKey(@RequestParam(value = "restApiKey") String restApiKey) {
        getLog().debug("Attempting to acquire user with rest api key " + restApiKey);
        try {
            return getUserService().getUserByRestApiKey(restApiKey);
        } catch (IllegalArgumentException e) {
            getLog().debug("No such user for REST API key " + restApiKey);
            return null;
        }
    }

    @RequestMapping(value = "/{userID}/restApiKey", method = RequestMethod.GET)
    public
    @ResponseBody
    RestApiKeyResponseBean getRestApiKeyForUser(@PathVariable String userID) {
        getLog().debug("Requesting REST API key for user " + userID);
        ConanUser user = getUserService().getUser(userID);
        getLog().debug("Got user " + user.getEmail());
        return new RestApiKeyResponseBean(user);
    }

}
