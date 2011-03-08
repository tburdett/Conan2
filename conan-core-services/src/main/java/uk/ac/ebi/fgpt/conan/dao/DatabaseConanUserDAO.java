package uk.ac.ebi.fgpt.conan.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.fgpt.conan.core.user.ConanUserWithPermissions;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * An implementation of {@link uk.ac.ebi.fgpt.conan.dao.ConanUserDAO} that stores and retrieves user information from a
 * backing database.
 *
 * @author Tony Burdett
 * @date 27-Oct-2010
 */
public class DatabaseConanUserDAO implements ConanUserDAO {
    public static final String SEQUENCE_SELECT =
            "select SEQ_CONAN.NEXTVAL from dual";

    public static final String USER_SELECT =
            "select  ID, USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, RESTAPIKEY, PERMISSIONS " +
                    "from CONAN_USERS";
    public static final String USER_COUNT =
            "select count(*) from CONAN_USERS where ID = ?";
    public static final String USER_SELECT_BY_USERNAME = USER_SELECT + " " +
            "where USER_NAME = ?";
    public static final String USER_SELECT_BY_USER_ID = USER_SELECT + " " +
            "where ID = ?";
    public static final String USER_SELECT_BY_EMAIL = USER_SELECT + " " +
            "where EMAIL = ?";
    public static final String USER_SELECT_BY_REST_API_KEY = USER_SELECT + " " +
            "where RESTAPIKEY = ?";
    public static final String USER_INSERT =
            "insert into CONAN_USERS (ID, USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, RESTAPIKEY, PERMISSIONS) " +
                    "values (?, ?, ?, ?, ?, ?, ?)";
    public static final String USER_UPDATE =
            "update CONAN_USERS " +
                    "set USER_NAME = ?, FIRST_NAME = ?, LAST_NAME = ?, EMAIL = ?, RESTAPIKEY = ?, PERMISSIONS = ? " +
                    "where ID = ?";
    public static final String USER_DELETE =
            "delete from CONAN_USERS where ID = ?";

    private JdbcTemplate jdbcTemplate;

    private Logger log = LoggerFactory.getLogger(getClass());

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected Logger getLog() {
        return log;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<ConanUser> getUserByUserName(String userName) {
        return getJdbcTemplate().query(USER_SELECT_BY_USERNAME,
                                       new Object[]{userName},
                                       new ConanUserMapper());
    }

    public ConanUser getUser(String userID) {
        return getJdbcTemplate().queryForObject(USER_SELECT_BY_USER_ID,
                                                new Object[]{userID},
                                                new ConanUserMapper());
    }

    public Collection<ConanUser> getUserByEmail(String userEmailAddress) {
        return getJdbcTemplate().query(USER_SELECT_BY_EMAIL,
                                       new Object[]{userEmailAddress},
                                       new ConanUserMapper());
    }

    public ConanUser getUserByRestApiKey(String restApiKey) {
        return getJdbcTemplate().queryForObject(USER_SELECT_BY_REST_API_KEY,
                                                new Object[]{restApiKey},
                                                new ConanUserMapper());
    }

    public ConanUser saveUser(ConanUser user) {
        int userCheck = 0;

        if (user.getId() != null) {
            userCheck = getJdbcTemplate().queryForInt(USER_COUNT,
                                                      user.getId());
        }

        //There is no such user in database
        if (userCheck == 0) {
            int userID = getJdbcTemplate().queryForInt(SEQUENCE_SELECT);
            getJdbcTemplate().update(USER_INSERT,
                                     userID,
                                     user.getUserName(),
                                     user.getFirstName(),
                                     user.getSurname(),
                                     user.getEmail(),
                                     user.getRestApiKey(),
                                     user.getPermissions().toString());
            if (user instanceof ConanUserWithPermissions) {
                ((ConanUserWithPermissions) user).setId(Integer.toString(userID));
            }
            else {
                getLog().warn("User acquired from database was of unexpected type " +
                        user.getClass().getSimpleName() + ", cannot set user ID");
            }
        }
        else {
            getJdbcTemplate().update(USER_UPDATE,
                                     user.getUserName(), user.getFirstName(), user.getSurname(),
                                     user.getEmail(),
                                     user.getRestApiKey(), user.getPermissions().toString(), user.getId());
        }

        return user;
    }

    public Collection<ConanUser> getUsers() {
        return getJdbcTemplate().query(USER_SELECT,
                                       new ConanUserMapper());
    }

    public void deleteUser(ConanUser user) {
        getJdbcTemplate().update(USER_DELETE,
                                 user.getId());
    }

    /**
     * Maps database rows to ConanTask objects
     */
    private class ConanUserMapper implements RowMapper<ConanUser> {

        public ConanUser mapRow(ResultSet resultSet, int i) throws
                SQLException {
            ConanUser.Permissions permissions = ConanUser.Permissions.GUEST;
            if (resultSet.getString(7) != null) {
                permissions = ConanUser.Permissions.valueOf(resultSet.getString(7));
            }
            ConanUserWithPermissions user = new ConanUserWithPermissions(resultSet.getString(2),
                                                                         resultSet.getString(3),
                                                                         resultSet.getString(4),
                                                                         resultSet.getString(5),
                                                                         resultSet.getString(6),
                                                                         permissions);
            user.setId(resultSet.getString(1));
            return user;
        }
    }
}
