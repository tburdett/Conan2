package uk.ac.ebi.fgpt.conan.ae.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.dao.ConanDaemonInputsDAO;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;

import java.util.List;

/**
 * DAO class that can retrieve accession numbers from Submission Tracking Database for daemon mode to load into AE2
 *
 * @author Natalja Kurbatova
 * @date 19-Nov-2010
 */
public class DaemonInputsForAE2DAO implements ConanDaemonInputsDAO {

    public static final String GEO_EXPERIMENTS_READY_TO_LOAD =
            "select accession from experiments exp " +
                    "where status='Complete' and is_deleted = 0 and " +
                    "is_released is null and accession is not null and " +
                    "not(accession = '') and date_last_processed is not null " +
                    "and experiment_type='GEO' " +
                    "and (select count(*) from events where experiment_id=exp.id and " +
                    "event_type in ('MAGETABLoader') and " +
                    "end_time > exp.date_last_processed)=0";

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
        return getJdbcTemplate().queryForList(GEO_EXPERIMENTS_READY_TO_LOAD, String.class);
    }
}
