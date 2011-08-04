package uk.ac.ebi.fgpt.conan.ae.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.fgpt.conan.ae.MAGEMLAccessionParameter;
import uk.ac.ebi.fgpt.conan.dao.ConanDaemonInputsDAO;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * DAO class that can retrieve accession numbers from Submission Tracking Database for daemon mode to load into AE1
 *
 * @author Natalja Kurbatova
 * @date 19-Nov-2010
 */
public class DaemonInputsForAE1DAO implements ConanDaemonInputsDAO {
    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public Class<? extends ConanParameter> getParameterType() {
        return MAGEMLAccessionParameter.class;
    }

    public List<String> getParameterValues() {
        return getJdbcTemplate().queryForList(ConanProperties.getProperty("ae1.daemon.inputs.query"), String.class);
    }
}
