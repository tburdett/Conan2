package uk.ac.ebi.fgpt.conan.ae.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
public class SubmitterDetailsFromAE2DAO implements SubmitterDetailsDAO {
    private JdbcTemplate jdbcTemplate;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SubmitterDetails> getSubmitterDetailsByAccession(
            String accession, SubmitterDetails.ObjectType type) {
        if (type == SubmitterDetails.ObjectType.EXPERIMENT) {
            getLog().debug("Querying for experiment submitter details for " + accession);
            List<SubmitterDetails> result = getJdbcTemplate().query(
                    ConanProperties.getProperty("ae2.experiment.submitter.query"),
                    new Object[]{accession},
                    new SubmitterDetailsMapper());
            getLog().debug("Submitter details returned " + result.size() + " results");
            return result;
        }
        else if (type == SubmitterDetails.ObjectType.ARRAY_DESIGN) {
            getLog().debug("Querying for array design submitter details for " + accession);
            List<SubmitterDetails> result = getJdbcTemplate().query(
                    ConanProperties.getProperty("ae2.arraydesign.submitter.query"),
                    new Object[]{accession},
                    new SubmitterDetailsMapper());
            getLog().debug("Submitter details returned " + result.size() + " results");
            return result;
        }
        else {
            throw new UnsupportedOperationException("Only EXPERIMENT or ARRAY_DESIGN details can be obtained from AE2");
        }
    }

    /**
     * Maps database rows to ConanTask objects
     */
    private class SubmitterDetailsMapper implements RowMapper<SubmitterDetails> {

        public SubmitterDetails mapRow(ResultSet resultSet, int i) throws SQLException {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, 1);
                Date tomorrow = calendar.getTime();
                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                String activationDateString = df2.format(tomorrow);

                getLog().debug("Submitter details row\n\t " +
                                       "(accession, activation date, name, release data, owner name, owner password, owner email):\n\t " +
                                       resultSet.getString(1) + ", " + tomorrow.toString() + ", " +
                                       resultSet.getString(2) + ", " +
                                       resultSet.getString(3) + ", " + resultSet.getString(4) + ", " +
                                       resultSet.getString(5) + ", " +
                                       resultSet.getString(6));

                SubmitterDetails submitterDetails = new SubmitterDetails();
                submitterDetails.setAccession(resultSet.getString(1));
                submitterDetails.setActivationDate(activationDateString);
                submitterDetails.setName(resultSet.getString(2));
                submitterDetails.setReleaseDate(resultSet.getString(3));

                submitterDetails.setUsername(resultSet.getString(4));
                submitterDetails.setPassword(resultSet.getString(5));
                submitterDetails.setEmail(resultSet.getString(6));

                return submitterDetails;
            }
            catch (SQLException e) {
                getLog().debug("Caught SQLException", e);
                throw e;
            }
            catch (RuntimeException e) {
                getLog().debug("Caught RuntimeException", e);
                throw e;
            }
        }
    }
}
