package uk.ac.ebi.fgpt.conan.ae.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

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
    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SubmitterDetails> getSubmitterDetailsByAccession(String accession, SubmitterDetails.ObjectType type) {
        return getJdbcTemplate().query(ConanProperties.getProperty("ae1.submitter.query"),
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
