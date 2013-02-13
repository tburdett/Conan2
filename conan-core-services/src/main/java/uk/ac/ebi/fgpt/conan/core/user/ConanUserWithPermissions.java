package uk.ac.ebi.fgpt.conan.core.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

/**
 * A user of the Conan system.  This is a default implementation that is designed to be used in conjunction with some
 * external resource containing the user information - for example, a database table or LDAP server.  This class assumes
 * the user was located in the resource: you should not instantiate members of this class yourself, as doing so will
 * assign higher permissions to users that are not found in the resource.
 * <p/>
 * This implementation assumes that the datasource containing user details does NOT contain Conan permissions, however -
 * these are retrieved from a configuration file.  This file should be located on the classpath, and the resource will
 * be loaded from 'conan/user.properties'
 * <p/>
 * The user.properties file should be a standard java properties file, where the key is the username (not the email
 * address) and the grants additional privileges.  Any user found in the user details datasource, but without an entry
 * in this properties file, is granted {@link uk.ac.ebi.fgpt.conan.model.ConanUser.Permissions#SUBMITTER} permissions -
 * this is the default, as all members of this class should be located in the datasource.  Any users named in this
 * properties file can be granted {@link uk.ac.ebi.fgpt.conan.model.ConanUser.Permissions#ADMINISTRATOR} or {@link
 * uk.ac.ebi.fgpt.conan.model.ConanUser.Permissions#DEVELOPER}.
 *
 * @author Tony Burdett
 * @date 12-Oct-2010
 */
public class ConanUserWithPermissions implements ConanUser {
    private final String userName;
    private final String firstName;
    private final String surname;
    private final String restApiKey;
    private String email;
    private String id;

    private Permissions permissions;

    private Logger log = LoggerFactory.getLogger(getClass());

    public ConanUserWithPermissions(String userName,
                                    String firstName,
                                    String surname,
                                    String email,
                                    String restApiKey) {
        this(userName, firstName, surname, email, restApiKey, Permissions.SUBMITTER);
    }

    public ConanUserWithPermissions(String userName,
                                    String firstName,
                                    String surname,
                                    String email,
                                    String restApiKey,
                                    Permissions permissions) {
        this.userName = userName;
        this.firstName = firstName;
        this.surname = surname;
        this.email = email;
        this.restApiKey = restApiKey;
        this.permissions = permissions;
    }

    protected Logger getLog() {
        return log;
    }

    public String getUserName() {
        return userName;
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
        return restApiKey;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public void upgradePermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    public int compareTo(Object o) {
        if (o instanceof ConanUser) {
            return getSurname().compareTo(((ConanUser) o).getSurname());
        } else {
            throw new ClassCastException(o.getClass().getSimpleName() + " cannot be compared to " +
                    getClass().getSimpleName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConanUserWithPermissions that = (ConanUserWithPermissions) o;

        if (email != null ? !email.equals(that.email) : that.email != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (restApiKey != null ? !restApiKey.equals(that.restApiKey) : that.restApiKey != null) {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (restApiKey != null ? restApiKey.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
