package uk.ac.ebi.fgpt.conan.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.user.ConanUserWithPermissions;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A dummy user DAO that contains an empty list of users that is populated in memory afresh each time.
 *
 * @author Tony Burdett
 * @date 27-Oct-2010
 */
public class InMemoryConanUserDAO implements ConanUserDAO {
    private final Collection<ConanUser> users;

    private int userCounter;

    private Logger log = LoggerFactory.getLogger(getClass());

    public InMemoryConanUserDAO() {
        this.users = new HashSet<ConanUser>();
        this.userCounter = 1;
    }

    protected Logger getLog() {
        return log;
    }

    public synchronized ConanUser getUser(String userID) {
        Set<ConanUser> results = new HashSet<ConanUser>();
        for (ConanUser user : users) {
            if (userID.equals(user.getId())) {
                getLog().debug("Retrieved user with ID = " + userID);
                return user;
            }
        }
        return null;
    }

    public synchronized Collection<ConanUser> getUserByUserName(String userName) {
        getLog().debug("Looking for user '" + userName + "', users now: " + users.size());
        Set<ConanUser> results = new HashSet<ConanUser>();
        for (ConanUser user : users) {
            if (userName.equals(user.getUserName())) {
                getLog().debug("Retrieved user with username = " + userName);
                results.add(user);
            }
        }
        return results;
    }

    public synchronized Collection<ConanUser> getUserByEmail(String userEmailAddress) {
        getLog().debug("Trying to fetch user with email = " + userEmailAddress);
        Set<ConanUser> results = new HashSet<ConanUser>();
        for (ConanUser user : users) {
            if (userEmailAddress.equals(user.getEmail())) {
                getLog().debug("Retrieved user with email = " + userEmailAddress);
                results.add(user);
            }
        }
        return results;
    }

    public synchronized ConanUser getUserByRestApiKey(String restApiKey) {
        for (ConanUser user : users) {
            if (restApiKey.equals(user.getRestApiKey())) {
                getLog().debug("Retrieved user with rest api key = " + restApiKey);
                return user;
            }
        }
        return null;
    }

    public synchronized ConanUser saveUser(ConanUser user) {
        getLog().debug("Saving user '" + user.getUserName() + "'");
        if (user instanceof ConanUserWithPermissions) {
            if (user.getId() != null) {
                getLog().debug("User '" + user.getUserName() + "' has an ID already assigned, updating old user");
                Iterator<ConanUser> userIt = users.iterator();
                while (userIt.hasNext()) {
                    ConanUser nextUser = userIt.next();
                    if (nextUser.getId().equals(user.getId())) {
                        getLog().debug("Removing user " + nextUser.getId() + " '" + nextUser.getUserName() +
                                "', replaced with a new version");
                        userIt.remove();
                        break;
                    }
                }
                // and add the new user
                users.add(user);
                getLog().debug("Saved new version of user " + user.getId() + " '" + user.getUserName() + "', " +
                        "users now: " + users.size());
            } else {
                ((ConanUserWithPermissions) user).setId(Integer.toString(userCounter++));
                getLog().debug("Assigned ID '" + (userCounter - 1) + "' to new user, saving");
                users.add(user);
            }
            return user;
        } else {
            // not saveable user
            throw new IllegalArgumentException(
                    "Only users of type " + ConanUserWithPermissions.class.getSimpleName() + " can be saved");
        }
    }

    public synchronized Collection<ConanUser> getUsers() {
        return users;
    }
}
