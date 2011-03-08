package uk.ac.ebi.fgpt.conan.service;

import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import uk.ac.ebi.fgpt.conan.core.user.LdapIdentifiedConanUser;
import uk.ac.ebi.fgpt.conan.dao.LdapConanUserDAO;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.Collection;
import java.util.List;

/**
 * Tests a ConanUserService with some problematic emails
 *
 * @author Tony Burdett
 * @date 28-Oct-2010
 */
public class TestConanUserService extends TestCase {
    private LdapConanUserDAO dao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("ldap-application-context.xml");
        dao = (LdapConanUserDAO) ctx.getBean("ldapUserDAO", LdapConanUserDAO.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        dao = null;
    }

    public void testGetEmail() {
        Collection<ConanUser> users = dao.getUserByEmail("tburdett@ebi.ac.uk");
        assertFalse("Empty collection of users found", users.isEmpty());
    }

    public void testQueryForEmail() {
        LdapTemplate ldapTemplate = dao.getLdapTemplate();
        String userEmailAddress = "tburdett@ebi.ac.uk";

        EqualsFilter filter = new EqualsFilter("mail", userEmailAddress);
        List results = ldapTemplate.search("", filter.encode(), new ConanUserMapper());
        if (results.size() == 1) {
            System.out.println("Got exactly one result");
            System.out.println("User is: " + results.get(0));
        }
        else {
            System.out.println("Got more than one result - " + results.size());
            for (Object result : results) {
                ConanUser user = (ConanUser)result;
                System.out.println("Next user: " + user);
            }
        }

    }

    private class ConanUserMapper implements AttributesMapper {
        public Object mapFromAttributes(Attributes attributes) throws NamingException {
            String username = attributes.get("uid").get().toString();
            String firstName = attributes.get("givenname").get().toString();
            String surname = attributes.get("sn").get().toString();
            String email = attributes.get("mail").get().toString();
            return new LdapIdentifiedConanUser(username, firstName, surname, email);
        }
    }
}
