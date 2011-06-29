package uk.ac.ebi.fgpt.conan.mock.dao;

import uk.ac.ebi.fgpt.conan.core.user.ConanUserWithPermissions;
import uk.ac.ebi.fgpt.conan.dao.ConanUserDAO;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Collection;
import java.util.Collections;

/**
 * A dummy verification user DAO for testing  Creates a single user with admin permissions, "anonymous".  Log in with
 * email anon@conan.com.
 *
 * @author Tony Burdett
 * @date 11-Nov-2010
 */
public class DummyUserDAO implements ConanUserDAO {
    private ConanUser user;

    public DummyUserDAO() {
        ConanUserWithPermissions cuwp = new ConanUserWithPermissions(
                "anonymous", "Jeff", "Nonymous", "anon@conan.com", "12345", ConanUser.Permissions.ADMINISTRATOR);
        cuwp.setId("1");
        this.user = cuwp;
    }

    public ConanUser getUser(String userID) {
        return user;
    }

    public Collection<ConanUser> getUsers() {
        return Collections.singleton(user);
    }

    public ConanUser getUserByRestApiKey(String restApiKey) {
        return user;
    }

    public Collection<ConanUser> getUserByUserName(String userName) {
        if (!userName.equals("daemon")) {
            return Collections.singleton(user);
        }
        else {
            return Collections.emptyList();
        }
    }

    public Collection<ConanUser> getUserByEmail(String userEmailAddress) {
        return Collections.singleton(user);
    }

    public ConanUser saveUser(ConanUser user) throws IllegalArgumentException {
        return this.user;
    }
}
