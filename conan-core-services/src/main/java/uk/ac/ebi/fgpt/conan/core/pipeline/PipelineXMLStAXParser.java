package uk.ac.ebi.fgpt.conan.core.pipeline;

import uk.ac.ebi.fgpt.conan.dao.ConanProcessDAO;
import uk.ac.ebi.fgpt.conan.dao.ConanUserDAO;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import javax.xml.XMLConstants;
import javax.xml.stream.*;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * A parser for conan pipelines.xml files that uses a StAX XML parser to read the pipeline XML. *
 *
 * @author Tony Burdett
 * @date 12-Oct-2010
 */
public class PipelineXMLStAXParser extends AbstractPipelineXMLParser {
    private final XMLInputFactory inputFactory;

    public PipelineXMLStAXParser(ConanUserDAO userDAO, ConanProcessDAO processDAO) {
        super(userDAO, processDAO);

        // create a new input factory for reading xml docs
        this.inputFactory = XMLInputFactory.newInstance();
    }

    /**
     * Parses pipeline.xml files and produces the resulting pipeline objects.
     *
     * @param pipelineXMLResource the URL of the next pipeline declaration file
     * @return a collection of pipelines
     * @throws IOException if the resource supplied could not be read
     */
    @Override
    public Collection<ConanPipeline> parsePipelineXML(URL pipelineXMLResource) throws IOException {
        getLog().debug("Parsing pipeline XML from " + pipelineXMLResource);
        Collection<ConanPipeline> conanPipelines = new HashSet<ConanPipeline>();

        try {
            getLog().debug("Creating XMLStreamReader from " + pipelineXMLResource);

            XMLStreamReader reader;
            synchronized (inputFactory) {
                // switch schema validation off
                inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
                reader = inputFactory.createXMLStreamReader(pipelineXMLResource.openStream());
            }

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(PIPELINE_ELEMENT)) {
                    conanPipelines.add(readPipeline(reader));
                }
                else {
                    // skip
                }
            }
        }
        catch (XMLStreamException e) {
            throw new IOException("Unable to read from " + pipelineXMLResource, e);
        }

        return conanPipelines;
    }

    @Override
    public Collection<ConanPipeline> parseAndValidatePipelineXML(URL pipelineXMLResource) throws IOException {
        getLog().debug("Parsing and validating pipeline XML from " + pipelineXMLResource);
        Collection<ConanPipeline> conanPipelines = new HashSet<ConanPipeline>();

        try {
            getLog().debug("Creating XMLStreamReader from " + pipelineXMLResource);

            XMLStreamReader reader;
            synchronized (inputFactory) {
                // switch schema validation on
                inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.TRUE);
                inputFactory.setXMLReporter(new PipelineXMLReporter());
                reader = inputFactory.createXMLStreamReader(pipelineXMLResource.openStream());
            }

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(PIPELINE_ELEMENT)) {
                    conanPipelines.add(readPipeline(reader));
                }
                else {
                    // skip
                }
            }
        }
        catch (XMLStreamException e) {
            throw new IOException("Unable to read from " + pipelineXMLResource, e);
        }

        return conanPipelines;
    }

    private ConanPipeline readPipeline(XMLStreamReader reader) throws XMLStreamException {
        // read out pipeline metadata
        String name = reader.getAttributeValue(null, PIPELINE_NAME_ATTRIBUTE);
        String usernameStr = reader.getAttributeValue(null, PIPELINE_CREATOR_ATTRIBUTE);
        String isPrivateStr = reader.getAttributeValue(null, PIPELINE_PRIVATE_ATTRIBUTE);
        String isDaemonizedStr = reader.getAttributeValue(null, PIPELINE_DAEMONIZED_ATTRIBUTE);

        DefaultConanPipeline conanPipeline;
        // lookup user by username
        Collection<ConanUser> conanUsers = getUserDAO().getUserByUserName(usernameStr);
        if (conanUsers.isEmpty()) {
            getLog().error("An unknown user '" + usernameStr + "' was named as the creator of " +
                    "the pipeline '" + name + "'.  This pipeline will not be loaded.");
            return null;
        }
        else {
            if (conanUsers.size() > 1) {
                getLog().error(
                        "The username '" + usernameStr + "' is ambiguous, there are " + conanUsers.size() + " " +
                                "users with this name.  The first user from the database will be marked as the creator of " +
                                "this pipeline, but database consistency should be checked");
            }

            ConanUser conanUser = conanUsers.iterator().next();
            boolean isPrivate = Boolean.parseBoolean(isPrivateStr);
            boolean isDaemonized = Boolean.parseBoolean(isDaemonizedStr);

            // create pipeline
            conanPipeline = new DefaultConanPipeline(name, conanUser, isPrivate, isDaemonized);
        }

        // read forward to each process
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(PROCESSES_ELEMENT)) {
                conanPipeline.setProcesses(readProcesses(reader));
            }
            else {
                // skip
            }
        }

        getLog().debug("Parsed pipeline '" + conanPipeline.getName() + "'");
        return conanPipeline;
    }

    private List<ConanProcess> readProcesses(XMLStreamReader reader) throws XMLStreamException {
        List<ConanProcess> conanProcesses = new ArrayList<ConanProcess>();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(PROCESS_ELEMENT)) {
                conanProcesses.add(readProcess(reader));
            }
        }

        getLog().debug("Parsed " + conanProcesses.size() + " processes");
        return conanProcesses;
    }

    private ConanProcess readProcess(XMLStreamReader reader) {
        getLog().debug("Process attributes:");
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            getLog().debug("\t" + reader.getAttributeNamespace(i) + ":" + reader.getAttributeLocalName(i) + " (" +
                    reader.getAttributeName(i) + ") = " + reader.getAttributeValue(i));
        }

        String processName = reader.getAttributeValue(null, PROCESS_NAME_ATTRIBUTE);
        ConanProcess p = getProcessDAO().getProcess(processName);
        if (p != null) {
            return p;
        }
        else {
            String msg = "pipelines.xml references a process (" + processName + ") that was not loaded";
            getLog().error(msg);
            throw new ServiceConfigurationError(msg);
        }
    }

    private class PipelineXMLReporter implements XMLReporter {
        public void report(String message,
                           String errorType,
                           Object relatedInformation,
                           Location location) throws XMLStreamException {
            String error = "XML did not validate -  " + errorType + ": " + message + " at line " +
                    location.getLineNumber() + ", column " + location.getColumnNumber();
            getLog().error(error);
            throw new XMLStreamException(error);
        }
    }
}
