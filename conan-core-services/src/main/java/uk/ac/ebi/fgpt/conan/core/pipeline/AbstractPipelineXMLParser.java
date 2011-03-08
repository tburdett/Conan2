package uk.ac.ebi.fgpt.conan.core.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.dao.ConanProcessDAO;
import uk.ac.ebi.fgpt.conan.dao.ConanUserDAO;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * A parser for conan pipelines.xml files.  This parser reads, and if possible validates, the XML, and uses it to
 * construct a series of {@link uk.ac.ebi.fgpt.conan.model.ConanPipeline} objects.  Note that the processes referenced
 * in pipelines.xml MUST be present and available from the supplied {@link uk.ac.ebi.fgpt.conan.service.ConanProcessService},
 * else a {@link java.util.ServiceConfigurationError} will be thrown.
 *
 * @author Tony Burdett
 * @date 16-Oct-2010
 */
public abstract class AbstractPipelineXMLParser {
    public static final String PIPELINES_SCHEMA_NAMESPACE = "http://www.ebi.ac.uk/arrayexpress/conan/pipelines";
    public static final String PIPELINES_SCHEMA_LOCATION = "conan/pipelines.xsd";

    public static final String PIPELINE_ELEMENT = "pipeline";
    public static final String PIPELINE_NAME_ATTRIBUTE = "name";
    public static final String PIPELINE_CREATOR_ATTRIBUTE = "creator";
    public static final String PIPELINE_DAEMONIZED_ATTRIBUTE = "daemonized";
    public static final String PIPELINE_PRIVATE_ATTRIBUTE = "public";
    public static final String PROCESSES_ELEMENT = "processes";
    public static final String PROCESS_ELEMENT = "process";
    public static final String PROCESS_NAME_ATTRIBUTE = "name";

    private ConanUserDAO userDAO;
    private ConanProcessDAO processDAO;

    private Logger log = LoggerFactory.getLogger(getClass());

    public AbstractPipelineXMLParser(ConanUserDAO userDAO,
                                     ConanProcessDAO processDAO) {
        // set DAOs to resolve pipeline dependant objects
        this.userDAO = userDAO;
        this.processDAO = processDAO;
    }

    protected Logger getLog() {
        return log;
    }

    public ConanUserDAO getUserDAO() {
        return userDAO;
    }

    public ConanProcessDAO getProcessDAO() {
        return processDAO;
    }

    /**
     * Parses pipeline.xml files and produces the resulting pipeline objects.  This parses the XML only, it does not
     * also validate.
     *
     * @param pipelineXMLResource the URL of the next pipeline declaration file
     * @return a collection of pipelines
     * @throws java.io.IOException if the resource supplied could not be read
     */
    public abstract Collection<ConanPipeline> parsePipelineXML(URL pipelineXMLResource) throws IOException;

    /**
     * Parses and validates pipeline.xml files, and produces the resulting pipeline objects.  This parses the XML only,
     * it does not also validate.
     *
     * @param pipelineXMLResource the URL of the next pipeline declaration file
     * @return a collection of pipelines
     * @throws java.io.IOException if the resource supplied could not be read
     */
    public abstract Collection<ConanPipeline> parseAndValidatePipelineXML(URL pipelineXMLResource) throws IOException;
}
