package uk.ac.ebi.fgpt.conan.ae.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * DAO class that can retrieve important details and permissions for submitters from AE2.  These details are
 * encapsulated in a Submitt
 *
 * @author Tony Burdett
 * @date 10-Nov-2010
 */
public class SubmitterDetailsFromAE1DAO implements SubmitterDetailsDAO {


    public static final String SUBMITTER_DETAILS_SELECT =
            "select lbl.MAINOBJ_NAME,iden.name," +
                    "nvt.VALUE, usr.NAME, usr.PASSWORD, usr.EMAIL " +
                    "from PL_USER usr, PL_LABEL lbl, PL_VISIBILITY vis,TT_IDENTIFIABLE iden, TT_NAMEVALUETYPE nvt " +
                    "where lbl.MAINOBJ_NAME = ?" +
                    "and usr.id = vis.USER_ID " +
                    "and vis.LABEL_ID = lbl.ID " +
                    "and iden.identifier =  lbl.MAINOBJ_NAME " +
                    "and nvt.T_EXTENDABLE_ID(+) = lbl.MAINOBJ_ID " +
                    "and nvt.NAME(+) = 'ArrayExpressReleaseDate'";

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SubmitterDetails> getSubmitterDetailsByAccession(String accession, SubmitterDetails.ObjectType type) {
        return getJdbcTemplate().query(SUBMITTER_DETAILS_SELECT,
                                       new Object[]{accession},
                                       new SubmitterDetailsMapper());
    }

    /**
     * Maps database rows to ConanTask objects
     */
    private class SubmitterDetailsMapper implements RowMapper<SubmitterDetails> {
        public SubmitterDetails mapRow(ResultSet resultSet, int i) throws SQLException {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            Date tomorrow = calendar.getTime();
            SubmitterDetails submitterDetails = new SubmitterDetails();
            submitterDetails.setAccession(resultSet.getString(1));
            submitterDetails.setActivationDate(tomorrow.toString());
            submitterDetails.setName(resultSet.getString(2));
            submitterDetails.setReleaseDate(resultSet.getString(3));

            submitterDetails.setUsername(resultSet.getString(4));
            submitterDetails.setPassword(resultSet.getString(5));
            submitterDetails.setEmail(resultSet.getString(6));

            return submitterDetails;
        }
    }
}
