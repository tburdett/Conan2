package uk.ac.ebi.fgpt.conan.process.ae2;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Process for ADF reannotation
 *
 * @author Natalja Kurbatova
 * @date 18-Nov-2010
 */
@ServiceProvider
public class ReannotationProcess extends AbstractAE2LSFProcess {
    private final Collection<ConanParameter> parameters;
    private final AccessionParameter accessionParameter;

    private Logger log = LoggerFactory.getLogger(getClass());

    public ReannotationProcess() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new AccessionParameter();
        parameters.add(accessionParameter);
    }

    protected Logger getLog() {
        return log;
    }

    public String getName() {
        return "adf reannotation";
    }

    public Collection<ConanParameter> getParameters() {
        return parameters;
    }

    protected String getComponentName() {
        return LSFProcess.UNSPECIFIED_COMPONENT_NAME;
    }

    protected String getCommand(Map<ConanParameter, String> parameters) {
        getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());

        // deal with parameters
        AccessionParameter accession = new AccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }
        else {
            //execution
            if (!accession.isExperiment()) {
                String environmentPath = ConanProperties.getProperty("environment.path");
                return environmentPath + "software/bin/arrayReannotator.sh annotate " +
                        accession.getFile().getAbsolutePath();
            }
            else {
                throw new IllegalArgumentException("ADF Reannotator annotate arrays, not experiments");
            }
        }
    }

    protected String getLSFOutputFilePath(Map<ConanParameter, String> parameters) {
        // deal with parameters
        AccessionParameter accession = new AccessionParameter();
        for (ConanParameter conanParameter : parameters.keySet()) {
            if (conanParameter == accessionParameter) {
                accession.setAccession(parameters.get(conanParameter));
            }
        }

        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }
        else {
            // get the mageFile parent directory
            final File parentDir = accession.getFile().getAbsoluteFile().getParentFile();

            // files to write output to
            final File outputDir = new File(parentDir, ".conan");

            // lsf output file
            return new File(outputDir, "adfreannotator.lsfoutput.txt").getAbsolutePath();
        }
    }
}
