package uk.ac.ebi.fgpt.conan.process.ae2;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.exception.ComponentNames;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Process for experiment unloading
 *
 * @author Natalja Kurbatova
 * @date 08-Nov-2010
 */
@ServiceProvider
public class ExperimentUnloadingProcess extends AbstractAE2LSFProcess {
    private final Collection<ConanParameter> parameters;
    private final AccessionParameter accessionParameter;

    private Logger log = LoggerFactory.getLogger(getClass());

    public ExperimentUnloadingProcess() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new AccessionParameter();
        parameters.add(accessionParameter);
    }

    protected Logger getLog() {
        return log;
    }

    public String getName() {
        return "experiment unloading";
    }

    public Collection<ConanParameter> getParameters() {
        return parameters;
    }

    protected int getMemoryRequirement(Map<ConanParameter, String> parameterStringMap) {
        return 512;
    }

    protected String getComponentName() {
        return ComponentNames.EXPLOADER;
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
            if (accession.isExperiment()) {
                String environmentPath = ConanProperties.getProperty("environment.path");
                return environmentPath + "software/framework/MAGETABUnLoader.sh -a " + accession.getAccession();
            }
            else {
                throw new IllegalArgumentException("Experiment Unloader unloads experiments, not arrays");
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
            return new File(outputDir, "experimentunloader.lsfoutput.txt").getAbsolutePath();
        }
    }
}
