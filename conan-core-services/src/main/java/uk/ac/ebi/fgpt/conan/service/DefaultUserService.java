package uk.ac.ebi.fgpt.conan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.util.Assert;
import uk.ac.ebi.fgpt.conan.core.user.ConanUserWithPermissions;
import uk.ac.ebi.fgpt.conan.core.user.GuestUser;
import uk.ac.ebi.fgpt.conan.dao.ConanUserDAO;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;

/**
 * The default implementation of {@link ConanUserService} for Conan, that relies on a two-tier storage system of user
 * information.
 * <p/>
 * Conan by default is backed by a database, and known users are stored in this database.  Any user that has logged into
 * Conan before is assigned a unique key (their REST API key) and their details held in this database.  As such, when we
 * are attempting to locate a user, we first try to find them in this database - this is our "trusted" DAO.
 * <p/>
 * If a query for details about a user that has never before logged in is received, their details are instead fetched
 * from an LDAP server containing all users who are allowed to log in to Conan.  At the point, a new REST API key is
 * generated for them.  This forms our "verification" DAO.
 * <p/>
 * If the user attempting to log in is not found in either resource, if they supply their email address they are
 * attributed guest permissions.  Users cannot log in with bogues usernames.
 * <p/>
 * This implementation delegates most calls to two {@link uk.ac.ebi.fgpt.conan.dao.ConanUserDAO} classes by default,
 * {@link uk.ac.ebi.fgpt.conan.dao.DatabaseConanUserDAO} (the trusted DAO) followed by {@link
 * uk.ac.ebi.fgpt.conan.dao.LdapConanUserDAO} (the verification DAO).
 *
 * @author Tony Burdett
 * @date 27-Oct-2010
 */
public class DefaultUserService implements ConanUserService {
    private final String HEXES = "0123456789ABCDEF";

    private ConanUserDAO trustedDAO;
    private ConanUserDAO verificationDAO;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ConanUserDAO getTrustedDAO() {
        return trustedDAO;
    }

    public void setTrustedDAO(ConanUserDAO trustedDAO) {
        Assert.notNull(trustedDAO, "A ConanUserDAO must be supplied as a trusted source of users");
        this.trustedDAO = trustedDAO;
    }

    public ConanUserDAO getVerificationDAO() {
        return verificationDAO;
    }

    public void setVerificationDAO(ConanUserDAO verificationDAO) {
        this.verificationDAO = verificationDAO;
    }

    public ConanUser createNewUser(String username,
                                   String firstName,
                                   String surname,
                                   String emailAddress,
                                   ConanUser.Permissions permissions) {
        getLog().debug("Creating new user for " + username);
        if (permissions == ConanUser.Permissions.GUEST) {
            return new GuestUser(emailAddress);
        } else {
            // first, create a rest api key for our user
            String restApiKey = generateRestApiKey(emailAddress);

            // now, we need to make a new user with permissions
            ConanUserWithPermissions permissibleUser =
                    new ConanUserWithPermissions(username,
                            firstName,
                            surname,
                            emailAddress,
                            restApiKey);

            getLog().debug("Generated new user! Details are:\n" +
                    "username: " + permissibleUser.getUserName() + "\n" +
                    "name: " + permissibleUser.getFirstName() + " " + permissibleUser.getSurname() + "\n" +
                    "email: " + permissibleUser.getEmail() + "\n" +
                    "restApiKey: " + permissibleUser.getRestApiKey());

            return getTrustedDAO().saveUser(permissibleUser);
        }
    }

    /**
     * Gets the user with this ID, that is known to Conan.  This service treats the user ID from the trusted DAO as the
     * canonical ID - users IDs from the verification DAO are not used.  As such, if you request a user from the
     * verification DAO, that has never logged into Conan, you should expect to get null here.
     *
     * @param userID the users unique ID
     * @return the known user, that has previously logged into conan
     */
    public ConanUser getUser(String userID) {
        return getTrustedDAO().getUser(userID);
    }

    /**
     * Gets all known users in Conan.  This default implementation only returns users that have, at some point, logged
     * into Conan, and are therefore stored in the trusted DAO.  Many users present in the verification DAO will not
     * show up here.
     *
     * @return all users that have previously logged into Conan
     */
    public Collection<ConanUser> getUsers() {
        return getTrustedDAO().getUsers();
    }

    /**
     * Gets the user with this user name.  This default implementation uses two DAOs to obtain users, one trusted source
     * (which is normally a backing database for Conan) and one verification source (which could be an LDAP directory or
     * some other listing of "allowed" users).  If the user is not available from the trusted source, they are checked
     * against the verification source and if found here, automatically added to the trusted source.  A REST API key for
     * that user is also automatically generated.
     *
     * @param userName the user name for this user, which must at least be present in the verification datasource
     * @return a user from the trusted datasource, automatically added if needs be
     */
    public ConanUser getUserByUserName(String userName) {
        // is this user in our database?
        Collection<ConanUser> targetUsers = getTrustedDAO().getUserByUserName(userName);
        ConanUser targetUser;

        if (targetUsers.isEmpty()) {
            getLog().debug("No users with userName '" + userName + "' " +
                    "found in trusted DAO, trying verification DAO");
            // not in DB, is there a verification DAO set?
            if (getVerificationDAO() == null) {
                throw new IllegalArgumentException("No user with the username '" + userName + "' found");
            } else {
                // if so are they in it?
                targetUsers = getVerificationDAO().getUserByUserName(userName);

                if (targetUsers.isEmpty()) {
                    // not in verification dao either, this username is not a legal one
                    throw new IllegalArgumentException("No user with the username '" + userName + "' found");
                } else {
                    // if we got exactly 1 user from verification dao, we must store them in our DB
                    if (targetUsers.size() == 1) {
                        targetUser = storeNewUser(targetUsers.iterator().next());
                    } else {
                        // more than one user from verification dao, complain
                        throw new IllegalArgumentException("Cannot verify user, " +
                                (targetUsers.size() == 0 ? "no " : "too many ") +
                                "users with this username acquired from verification datasource");
                    }
                }
            }
        } else {
            targetUser = targetUsers.iterator().next();
        }

        return targetUser;
    }

    /**
     * Gets the user with this email address.  This default implementation uses two DAOs to obtain users, one trusted
     * source (which is normally a backing database for Conan) and one verification source (which could be an LDAP
     * directory or some other listing of "allowed" users).  If the user is not available from the trusted source, they
     * are checked against the verification source and if found here, automatically added to the trusted source.  A REST
     * API key for that user is also automatically generated.
     *
     * @param userEmailAddress the email address for this user, which must at least be present in the verification
     *                         datasource
     * @return a user from the trusted datasource, automatically added if needs be
     */
    public ConanUser getUserByEmail(String userEmailAddress) {
        // is this user in our database?
        Collection<ConanUser> targetUsers = getTrustedDAO().getUserByEmail(userEmailAddress);
        ConanUser targetUser;

        if (targetUsers.isEmpty()) {
            getLog().debug("No users with email '" + userEmailAddress + "' " +
                    "found in trusted DAO, trying verification DAO");
            // not in DB, is there a verification DAO set?
            if (getVerificationDAO() == null) {
                targetUser = new GuestUser(userEmailAddress);
            } else {
                // if so are they in it?
                targetUsers = getVerificationDAO().getUserByEmail(userEmailAddress);

                if (targetUsers.isEmpty()) {
                    // not in verification dao either, this username is not a legal one
                    getLog().debug("No users with email '" + userEmailAddress + "' found in verificationD");
                    targetUser = new GuestUser(userEmailAddress);
                } else {
                    // if we got exactly 1 user from verification dao, we must store them in our DB
                    if (targetUsers.size() == 1) {
                        targetUser = storeNewUser(targetUsers.iterator().next());
                    } else {
                        // more than one user from verification dao, complain
                        throw new IllegalArgumentException("Cannot verify user, " +
                                (targetUsers.size() == 0 ? "no " : "too many ") +
                                "users with this email address acquired from verification datasource");
                    }
                }
            }
        } else {
            // if there are several users with the same email, iterate over them...
            Iterator<ConanUser> it = targetUsers.iterator();
            targetUser = null;
            while (it.hasNext()) {
                // ...and return the first...
                ConanUser nextUser = it.next();
                if (!nextUser.getUserName().equals("conan-daemon")) {
                    // ...but only if it's not the daemon user
                    targetUser = nextUser;
                    break;
                }
            }
        }

        return targetUser;
    }

    /**
     * Gets the user with this REST API key.  Unlike the other methods on this service implementation, users are never
     * automatically added by REST API key - this MUST be correct, checked against the trusted DAO.  If there is no user
     * in the trusted DAO with this key, an {@link IllegalArgumentException} will be thrown.
     *
     * @param restApiKey the rest api key used to access this a service
     * @return the user with this REST API key, if found
     */
    public ConanUser getUserByRestApiKey(String restApiKey) {
        // only query the database - ldap doesn't store rest api keys, so if it's not in the DB it's bogus
        try {
            ConanUser result = getTrustedDAO().getUserByRestApiKey(restApiKey);
            if (result != null) {
                return result;
            } else {
                throw new IllegalArgumentException(
                        "No user with this REST API key (" + restApiKey + ") could be found");
            }
        } catch (EmptyResultDataAccessException e) {
            // no user, special case
            getLog().warn("Invalid REST API key - no user found");
            throw new IllegalArgumentException("Invalid REST API key", e);
        } catch (IncorrectResultSizeDataAccessException e) {
            // already caught no user, so this must be >1 users with the same REST API key
            getLog().error(
                    "UserDAO returned an invalid result: REST API keys must be unique, " +
                            "but " + e.getActualSize() + " users share this key");
            throw new IllegalArgumentException(
                    "REST API keys must be unique, but " + e.getActualSize() + " users share this key", e);
        }
    }

    public ConanUser updateUserEmail(ConanUser existingUser, String newEmail) {
        getLog().debug("Updating user email from '" + existingUser.getEmail() + "' to '" + newEmail + "'");
        ConanUser fetchedUser = getTrustedDAO().getUser(existingUser.getId());
        if (fetchedUser == null) {
            throw new IllegalArgumentException("The supplied user does not exist: " +
                    "no user with matching ID (" + existingUser.getId() + ") found");
        } else {
            // create a new user that is basically a copy of the old one
            ConanUserWithPermissions newUser = new ConanUserWithPermissions(fetchedUser.getUserName(),
                    fetchedUser.getFirstName(),
                    fetchedUser.getSurname(),
                    newEmail,
                    fetchedUser.getRestApiKey(),
                    fetchedUser.getPermissions());
            // set new user id - as long as this equals the old ID, user will be updated
            newUser.setId(fetchedUser.getId());
            // now save
            return getTrustedDAO().saveUser(newUser);
        }
    }

    protected ConanUser storeNewUser(ConanUser user) throws UnsupportedOperationException {
        getLog().debug("Storing new user " + user.getUserName());
        if (!(user instanceof GuestUser)) {
            // first, create a rest api key for our user
            String restApiKey = generateRestApiKey(user.getEmail());

            // now, we need to make a new user with permissions
            ConanUserWithPermissions permissibleUser;
            if (user.getEmail().equals("tburdett@ebi.ac.uk")) {
                // todo - backdoor for testing, to grant admin privileges to me
                permissibleUser =
                        new ConanUserWithPermissions(user.getUserName(),
                                user.getFirstName(),
                                user.getSurname(),
                                user.getEmail(),
                                restApiKey,
                                ConanUser.Permissions.ADMINISTRATOR);
            } else {
                permissibleUser =
                        new ConanUserWithPermissions(user.getUserName(),
                                user.getFirstName(),
                                user.getSurname(),
                                user.getEmail(),
                                restApiKey,
                                user.getPermissions());
            }

            getLog().debug("Generated new user! Details are:\n" +
                    "username: " + permissibleUser.getUserName() + "\n" +
                    "name: " + permissibleUser.getFirstName() + " " + permissibleUser.getSurname() + "\n" +
                    "email: " + permissibleUser.getEmail() + "\n" +
                    "restApiKey: " + permissibleUser.getRestApiKey() + "\n" +
                    "permissions: " + permissibleUser.getPermissions());

            return getTrustedDAO().saveUser(permissibleUser);
        } else {
            getLog().warn("Attempting to store a guest user.  This operation is not supported");
            throw new UnsupportedOperationException("Cannot store Guest users");
        }
    }

    protected String generateRestApiKey(String email) {
        String timestamp = Long.toString(System.currentTimeMillis());
        getLog().debug("Generating new REST API key for " + email);
        String keyContent = email + timestamp;
        try {
            // encode the email using SHA-1
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(keyContent.getBytes("UTF-8"));

            // now translate the resulting byte array to hex
            String restKey = getHexRepresentation(digest);
            getLog().debug("REST API key was generated: " + restKey);
            return restKey;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported!");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not available, required to generate REST api key");
        }
    }

    protected String getHexRepresentation(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
