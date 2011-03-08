package uk.ac.ebi.fgpt.conan.dao;

import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Collection;

/**
 * A test case that tests LDAP dao functionality
 *
 * @author Tony Burdett
 * @date 04-Nov-2010
 */
public class TestLdapConanUserDAO extends TestCase {
    private ConanUserDAO ldapDAO;
    private String userID;
    private String userFirstName;
    private String userSurname;
    private String userEmailAddress;
    private String restApiKey;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("ldap-application-context.xml");
        ldapDAO = ctx.getBean("ldapUserDAO", ConanUserDAO.class);
        userID = "tburdett";
        userFirstName = "Tony";
        userSurname = "Burdett";
        userEmailAddress = "tburdett@ebi.ac.uk";
        restApiKey = "dummy-rest-key";
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ldapDAO = null;
    }

    public void testGetUser() {
        ConanUser user = ldapDAO.getUser(userID);
        checkMatch(user);
    }

    public void testGetUserByUserName() {
        Collection<ConanUser> users = ldapDAO.getUserByUserName(userID);
        assertEquals("Wrong number of results", 1, users.size());
        checkMatch(users.iterator().next());
    }

    public void testGetUserByEmail() {
        Collection<ConanUser> users = ldapDAO.getUserByEmail(userEmailAddress);
        assertEquals("Wrong number of results", 1, users.size());
        checkMatch(users.iterator().next());
    }

    public void testGetUserByRestApiKey() {
        Collection<ConanUser> users = ldapDAO.getUserByEmail(restApiKey);
        assertEquals("Wrong number of results", 0, users.size());
    }

    public void saveUser() {
        try {
            ldapDAO.saveUser(null);
            fail();
        }
        catch (UnsupportedOperationException e) {
            // got exception correctly
        }
    }

    public void testGetUsers() {
        Collection<ConanUser> allUsers = ldapDAO.getUsers();
        assertTrue("Couldn't get users from LDAP", allUsers.size() > 0);
    }

    public void checkMatch(ConanUser user) {
        assertEquals(userID, user.getId());
        assertEquals(userID, user.getUserName());
        assertEquals(userFirstName, user.getFirstName());
        assertEquals(userSurname, user.getSurname());
        assertEquals(userEmailAddress, user.getEmail());

        try {
            user.getRestApiKey();
            fail();
        }
        catch (UnsupportedOperationException e) {
            // got exception correctly
        }
    }
}
