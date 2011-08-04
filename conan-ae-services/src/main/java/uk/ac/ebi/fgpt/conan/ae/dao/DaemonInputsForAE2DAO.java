package uk.ac.ebi.fgpt.conan.ae.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.dao.ConanDaemonInputsDAO;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.util.List;

/**
 * DAO class that can retrieve accession numbers from Submission Tracking Database for daemon mode to load into AE2
 *
 * @author Natalja Kurbatova
 * @date 19-Nov-2010
 */
public class DaemonInputsForAE2DAO implements ConanDaemonInputsDAO {
    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public Class<? extends ConanParameter> getParameterType() {
        return AccessionParameter.class;
    }

    public List<String> getParameterValues() {
        return getJdbcTemplate().queryForList(ConanProperties.getProperty("ae2.daemon.inputs.query"), String.class);
    }
}
