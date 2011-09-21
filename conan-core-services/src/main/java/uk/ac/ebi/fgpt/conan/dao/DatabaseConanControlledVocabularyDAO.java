package uk.ac.ebi.fgpt.conan.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.util.List;

/**
 * Retrieves controlled vocabularies from a backing database.
 *
 * @author Natalja Kurbatova
 * @date 12-Sep-2011
 */
public class DatabaseConanControlledVocabularyDAO {

    public static final String CONTROLLED_VOCABULARY_SELECT_ATLAS_EXPT_TYPES = "select VALUE from CONTROLLED_VOCABULARY " +
        "where ATLAS_ELIGIBLE=1 " +
        "and TYPE='Experiment type'";

    public static final String CONTROLLED_VOCABULARY_SELECT_AE_EXPT_TYPES = "select VALUE from CONTROLLED_VOCABULARY " +
        "where AE_ELIGIBLE=1 " +
        "and TYPE='Experiment type'";

    public static final String CONTROLLED_VOCABULARY_SELECT_RESTRICTED_PROTOCOL_NAMES = "select VALUE from CONTROLLED_VOCABULARY " +
        "where AE_ELIGIBLE=0 " +
        "and TYPE='Protocol Accession'";

    public static final String CONTROLLED_VOCABULARY_SELECT_ATLAS_FACTOR_TYPES = "select VALUE from CONTROLLED_VOCABULARY " +
        "where ATLAS_ELIGIBLE=1 " +
        "and TYPE='Factor type'";

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

    public List<String> getAtlasExperimentTypes() {
        return getJdbcTemplate().queryForList(
            CONTROLLED_VOCABULARY_SELECT_ATLAS_EXPT_TYPES, String.class);
    }

    public List<String> getAtlasFactorTypes() {
        return getJdbcTemplate().queryForList(
            CONTROLLED_VOCABULARY_SELECT_ATLAS_FACTOR_TYPES, String.class);
    }

    public List<String> getRestrictedProtocolNames() {
        return getJdbcTemplate().queryForList(
            CONTROLLED_VOCABULARY_SELECT_RESTRICTED_PROTOCOL_NAMES, String.class);
    }

    public List<String> getAEExperimentTypes() {
        return getJdbcTemplate().queryForList(
            CONTROLLED_VOCABULARY_SELECT_AE_EXPT_TYPES, String.class);
    }

}
