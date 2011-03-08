package uk.ac.ebi.fgpt.conan.mock.dao;

import uk.ac.ebi.fgpt.conan.core.user.ConanUserWithPermissions;
import uk.ac.ebi.fgpt.conan.dao.ConanUserDAO;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Collection;
import java.util.Collections;

/**
 * A dummy verification user DAO for testing
 *
 * @author Tony Burdett
 * @date 11-Nov-2010
 */
public class DummyVerificationUserDAO implements ConanUserDAO {
    ConanUser tony = new ConanUserWithPermissions("tburdett", "Tony", "Burdett", "tburdett@ebi.ac.uk", "12345",
                                                  ConanUser.Permissions.ADMINISTRATOR);

    public ConanUser getUser(String userID) {
        return tony;
    }

    public Collection<ConanUser> getUsers() {
        return Collections.singleton(tony);
    }

    public ConanUser getUserByRestApiKey(String restApiKey) {
        return tony;
    }

    public Collection<ConanUser> getUserByUserName(String userName) {
        if (!userName.equals("daemon")) {
            return Collections.singleton(tony);
        }
        else {
            return Collections.emptyList();
        }
    }

    public Collection<ConanUser> getUserByEmail(String userEmailAddress) {
        return Collections.singleton(tony);
    }

    public ConanUser saveUser(ConanUser user) throws IllegalArgumentException {
        return tony;
    }
}
