package uk.ac.ebi.fgpt.conan.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.pipeline.AbstractPipelineXMLParser;
import uk.ac.ebi.fgpt.conan.core.pipeline.PipelineXMLSAXParser;
import uk.ac.ebi.fgpt.conan.core.pipeline.PipelineXMLStAXParser;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * A pipeline DAO that loads pipelines from XML.  Conan pipeline XML should be classpath resources located at
 * conan/pipelines.xml.  This is extensible, so jars containing new processes may also declare new pipelines in XML and
 * these will be created.
 * <p/>
 * <p/>
 * Currently, runtime creation of new pipelines is not supported.  Attempting to create new pipelines will result in an
 * {@link UnsupportedOperationException} being thrown.
 *
 * @author Tony Burdett
 * @date 12-Oct-2010
 */
public class XMLLoadingPipelineDAO implements ConanPipelineDAO {
    public static final XMLParseMethod XML_PARSE_METHOD = XMLParseMethod.SAX;
    public static final boolean XML_VALIDATION = true;

    private ConanUserDAO userDAO;
    private ConanProcessDAO processDAO;

    private Set<ConanPipeline> conanPipelines;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ConanUserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(ConanUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public ConanProcessDAO getProcessDAO() {
        return processDAO;
    }

    public void setProcessDAO(ConanProcessDAO processDAO) {
        this.processDAO = processDAO;
    }

    public void reset() {
        conanPipelines.clear();
        conanPipelines = null;
    }

    /**
     * Gets the collection of pipelines known to Conan that are accessible to the current user.
     * <p/>
     * Note that pipelines may be loaded lazily from XML the first time this method is called.  Subsequent calls will
     * reuse the loaded collection of pipelines.
     *
     * @return the collection of all pipelines this user can access
     */
    public Collection<ConanPipeline> getPipelines() {
        // return all pipeline
        if (conanPipelines == null) {
            loadXML();
        }
        return conanPipelines;
    }

    public ConanPipeline getPipeline(String pipelineName) {
        for (ConanPipeline pipeline : getPipelines()) {
            if (pipeline.getName().equals(pipelineName)) {
                return pipeline;
            }
        }

        // if we got to here, we've no suitable pipeline with this name, return null
        return null;
    }

    public ConanPipeline createPipeline(String name, List<ConanProcess> conanProcesses, ConanUser creator) {
        throw new UnsupportedOperationException("Creating new pipelines is not yet supported");
    }

    public ConanPipeline createPipeline(String name,
                                        List<ConanProcess> conanProcesses,
                                        ConanUser creator,
                                        boolean isPrivate) {
        throw new UnsupportedOperationException("Creating new pipelines is not yet supported");
    }

    /**
     * Loads the pipelines XML file from resources at 'conan/pipelines.xml'.  The XML is parser using an {@link
     * uk.ac.ebi.fgpt.conan.core.pipeline.AbstractPipelineXMLParser} acquired using the factory method {@link
     * #createXMLParser()}.  You can configure the XML parsing strategy to use either a StAX or SAX parser by changing
     * the flag {@link #XML_PARSE_METHOD}.  To use other strategies you should subclass this class and override the
     * factory method.
     */
    protected synchronized void loadXML() {
        AbstractPipelineXMLParser parser = createXMLParser();

        // instantiate collections
        conanPipelines = new HashSet<ConanPipeline>();

        // try to read XML...
        try {
            Enumeration<URL> pipelineResources = getClass().getClassLoader().getResources("conan/pipelines.xml");
            getLog().debug("Lazy loading conan pipelines from classpath:conan/pipelines.xml");
            while (pipelineResources.hasMoreElements()) {
                URL pipelineXML = pipelineResources.nextElement();

                try {
                    // parse XML to get all known pipelines
                    if (XML_VALIDATION) {
                        conanPipelines.addAll(parser.parseAndValidatePipelineXML(pipelineXML));
                    }
                    else {
                        conanPipelines.addAll(parser.parsePipelineXML(pipelineXML));
                    }
                }
                catch (IOException e) {
                    // this is a configuration error, so throw a runtime exception
                    String msg = "Failed to parse " + pipelineResources;
                    getLog().error(msg);
                    getLog().debug("IOException follows", e);
                    throw new ServiceConfigurationError(msg, e);
                }
            }
        }
        catch (IOException e) {
            String msg = "Could not access conan/pipelines.xml resources";
            getLog().error(msg);
            getLog().debug("IOException follows", e);
            throw new ServiceConfigurationError(msg, e);
        }

        // now we've loaded all pipelines, validate contents to ensure we don't have pipelines with duplicated names
        Set<String> pipelineNames = new HashSet<String>();
        for (ConanPipeline p : conanPipelines) {
            if (!pipelineNames.contains(p.getName())) {
                pipelineNames.add(p.getName());
            }
            else {
                String msg = "Multiple Conan pipelines with the same pipeline name ('" + p.getName() + "') " +
                        "are declared in conan/pipelines.xml.  Pipeline names must be unique.";
                getLog().error(msg);
                throw new ServiceConfigurationError(msg);
            }
        }
    }

    /**
     * Generates an {@link AbstractPipelineXMLParser} that will be used to parse pipeline XML files.  Override this
     * method if you wish to use a strategy other than StAX or SAX.
     *
     * @return the pipeline xml parser that will be used to parse the pipelines.xml resource
     */
    protected AbstractPipelineXMLParser createXMLParser() {
        // make sure we use the correct type of parser
        switch (XML_PARSE_METHOD) {
            case SAX:
                return new PipelineXMLSAXParser(getUserDAO(), getProcessDAO());
            case STAX:
            default:
                return new PipelineXMLStAXParser(getUserDAO(), getProcessDAO());
        }
    }

    private enum XMLParseMethod {
        STAX,
        SAX
    }
}
