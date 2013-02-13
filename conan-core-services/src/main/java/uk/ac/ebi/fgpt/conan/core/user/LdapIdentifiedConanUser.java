package uk.ac.ebi.fgpt.conan.core.user;

import uk.ac.ebi.fgpt.conan.model.ConanUser;

/**
 * A {@link uk.ac.ebi.fgpt.conan.model.ConanUser} implementation that represents a user that is known but not yet sotred
 * in Conan's database of users.  Users will normally be in this state after being retrieved from an LDAP server.  At
 * this point, they will not have an assigned REST API key but they will have a full complement of details (first name,
 * surname, username, email).  Usually, users will be immediately assigned a REST API key and assigned permissions
 * before anything else happens - known users without permissions aren't much use.
 *
 * @author Tony Burdett
 * @date 27-Oct-2010
 */
public class LdapIdentifiedConanUser implements ConanUser {
    private final String uid;
    private final String firstName;
    private final String surname;
    private final String email;

    public LdapIdentifiedConanUser(String uid, String firstName, String surname, String email) {
        this.uid = uid;
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
    }

    public String getId() {
        return uid;
    }

    public String getUserName() {
        return uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getRestApiKey() {
        throw new UnsupportedOperationException("LDAP-obtained users don't have REST API keys - " +
                "this is an anonymously verified user only.  Create a new ConanUserWithPermissions instead");
    }

    public Permissions getPermissions() {
        return Permissions.SUBMITTER;
    }

    public void upgradePermissions(Permissions permissions) {
        throw new UnsupportedOperationException("LDAP-obtained users don't have permissions - " +
                "this is an anonymously verified user only.  Create a new ConanUserWithPermissions instead");
    }

    public int compareTo(Object o) {
        if (o instanceof ConanUser) {
            return getSurname().compareTo(((ConanUser) o).getSurname());
        } else {
            throw new ClassCastException(o.getClass().getSimpleName() + " cannot be compared to " +
                    getClass().getSimpleName());
        }
    }

    public String toString() {
        return getFirstName() + " " + getSurname() + " (" + getUserName() + ") [" + getEmail() + "]";
    }
}
