package uk.ac.ebi.fgpt.conan.dao;

import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Collection;

/**
 * A data access object for retrieving {@link uk.ac.ebi.fgpt.conan.model.ConanUser}s and from a datasource used to
 * persist this information.
 *
 * @author Tony Burdett
 * @date 22-Oct-2010
 */
public interface ConanUserDAO {
    /**
     * Gets a {@link ConanUser} given the user ID.
     *
     * @param userID the user ID of the user to retrieve
     * @return the conan user with this ID
     */
    ConanUser getUser(String userID);

    /**
     * Gets all {@link uk.ac.ebi.fgpt.conan.model.ConanUser}s known to the system.
     *
     * @return all users Conan knows about
     */
    Collection<ConanUser> getUsers();

    /**
     * Gets a {@link uk.ac.ebi.fgpt.conan.model.ConanUser} given a <code>String</code> rest api key used to access this
     * service.  This returns a single user as rest api keys are unique per user
     *
     * @param restApiKey the restAPIKey used to access this a service
     * @return the object that describes this user
     */
    ConanUser getUserByRestApiKey(String restApiKey);

    /**
     * Gets a {@link uk.ac.ebi.fgpt.conan.model.ConanUser} given their username.  Often, the username will be the same
     * as the local part of the users email address.  This returns a collection, as many users can have the same
     * username.
     *
     * @param userName the user name for a known user
     * @return the ConanUser object that describes this user
     */
    Collection<ConanUser> getUserByUserName(String userName);

    /**
     * Gets a {@link uk.ac.ebi.fgpt.conan.model.ConanUser} given their email address.  This returns a collection as
     * potentially, many users may have the same email address
     *
     * @param userEmailAddress the email address of the user to recover
     * @return the ConanUser object that describes this user
     */
    Collection<ConanUser> getUserByEmail(String userEmailAddress);

    /**
     * Persists new users to the backing datasource.  Generally, after creating new {@link
     * uk.ac.ebi.fgpt.conan.model.ConanUser} you should save it with this method and then use the returned reference
     * instead of the original object: this allows some implementations of this interface to rereference equal users to
     * the existing objects.
     *
     * @param user the user to save
     * @return a reference to the (potentially modified) version of the user being saved
     * @throws IllegalArgumentException if the user supplied cannot be saved because it is an illegal type
     */
    ConanUser saveUser(ConanUser user) throws IllegalArgumentException;
}
