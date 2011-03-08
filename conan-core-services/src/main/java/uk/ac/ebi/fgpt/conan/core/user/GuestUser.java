package uk.ac.ebi.fgpt.conan.core.user;

import uk.ac.ebi.fgpt.conan.model.ConanUser;

/**
 * A user of the Conan system. This implementation is designed to be used when users cannot be retrieved from the user
 * resource backing Conan (usually a database or LDAP server).  <code>GuestUser</code>s always get granted {@link
 * uk.ac.ebi.fgpt.conan.model.ConanUser.Permissions#GUEST} permissions - they will not be allowed to submit new jobs,
 * but can view any running or completed jobs.
 * <p/>
 * As this is a guest user, with unknown details, some of these fields may be empty.
 *
 * @author Tony Burdett
 * @date 12-Oct-2010
 */
public class GuestUser implements ConanUser {
    private final String email;
    private String ID;

    public GuestUser(String email) {
        this.email = email;
    }

    public String getUserName() {
        return email.substring(0, email.indexOf("@"));
    }

    public String getFirstName() {
        return "";
    }

    public String getSurname() {
        return "guest";
    }

    public String getEmail() {
        return email;
    }

    public String getRestApiKey() {
        return "";
    }

    public String getId() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }


    public Permissions getPermissions() {
        return Permissions.GUEST;
    }

    public void upgradePermissions(Permissions permissions) {
        if (permissions != Permissions.GUEST) {
            // this isn't allowed
            throw new IllegalArgumentException("Cannot upgrade permissions of guest users - this requires a new key");
        }
    }

    public int compareTo(Object o) {
        if (o instanceof ConanUser) {
            return getEmail().compareTo(((ConanUser) o).getEmail());
        }
        else {
            throw new ClassCastException(o.getClass().getSimpleName() + " cannot be compared to " +
                    getClass().getSimpleName());
        }
    }
}
