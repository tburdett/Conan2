package uk.ac.ebi.fgpt.conan.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import uk.ac.ebi.fgpt.conan.core.user.LdapIdentifiedConanUser;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link uk.ac.ebi.fgpt.conan.dao.ConanTaskDAO} that stores and retrieves user information from an
 * LDAP server.
 *
 * @author Tony Burdett
 * @date 27-Oct-2010
 */
public class LdapConanUserDAO implements ConanUserDAO {
    private LdapTemplate ldapTemplate;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public ConanUser getUser(String userID) {
        EqualsFilter filter = new EqualsFilter("uid", userID);
        List results = ldapTemplate.search("", filter.encode(), new ConanUserMapper());
        if (results.size() == 1) {
            return (ConanUser) results.get(0);
        } else {
            getLog().warn("User ID '" + userID + "' does not match exactly one entry " +
                    "(actually " + results.size() + "), returning no details");
            return null;
        }
    }

    public Collection<ConanUser> getUserByUserName(String userName) {
        EqualsFilter filter = new EqualsFilter("uid", userName);
        return ldapTemplate.search("", filter.encode(), new ConanUserMapper());
    }

    public Collection<ConanUser> getUserByEmail(String userEmailAddress) {
        EqualsFilter filter = new EqualsFilter("mail", userEmailAddress);
        return ldapTemplate.search("", filter.encode(), new ConanUserMapper());
    }

    public ConanUser getUserByRestApiKey(String restApiKey) {
        return null;
    }

    public ConanUser saveUser(ConanUser user) {
        throw new UnsupportedOperationException(
                "Cannot save users to LDAP server, this is for anonymous verification only");
    }

    public Collection<ConanUser> getUsers() {
        return ldapTemplate.search("", "(objectclass=person)", new ConanUserMapper());
    }

    private class ConanUserMapper implements AttributesMapper {
        public Object mapFromAttributes(Attributes attributes) throws NamingException {
            String uid =
                    attributes.get("uid") != null ? attributes.get("uid").get().toString() : null;
            String firstName =
                    attributes.get("givenname") != null ? attributes.get("givenname").get().toString() : null;
            String surname =
                    attributes.get("sn") != null ? attributes.get("sn").get().toString() : null;
            String email =
                    attributes.get("mail") != null ? attributes.get("mail").get().toString() : null;
            return new LdapIdentifiedConanUser(uid, firstName, surname, email);
        }
    }
}
