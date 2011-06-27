package uk.ac.ebi.fgpt.conan.process.ae1;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.ae.MAGEMLAccessionParameter;
import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Process for ADF loading
 *
 * @author Natalja Kurbatova
 * @date 16-Nov-2010
 */
@ServiceProvider
public class AE2AfterLoadingProcessCombinedWithAE1 extends AbstractLSFProcess {
    private final Collection<ConanParameter> parameters;
    private final MAGEMLAccessionParameter accessionParameter;

    private Logger log = LoggerFactory.getLogger(getClass());

    public AE2AfterLoadingProcessCombinedWithAE1() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new MAGEMLAccessionParameter();
        parameters.add(accessionParameter);
    }

    protected Logger getLog() {
        return log;
    }

    public String getName() {
        return "afterloading for AE2";
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
            //if (accession.isExperiment()) {
            String environmentPath = ConanProperties.getProperty("environment.path");
            String publicFTPDirectory = ConanProperties.getProperty("ftp.directory") + accession.getSubDir();
            String publicFTPPath = ConanProperties.getProperty("ftp.path");
            return environmentPath + "software/afterload/afterLoad.sh "
                    + accession.getFile().getParentFile().getAbsolutePath() + " "
                    + accession.getAccession() + " "
                    + publicFTPDirectory + " " + publicFTPPath;
            //}
            // else {
            //    throw new IllegalArgumentException("Afterloading works for experiment , not array designs");
            //}
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
            return new File(outputDir, "afterloader.lsfoutput.txt").getAbsolutePath();
        }
    }
}
