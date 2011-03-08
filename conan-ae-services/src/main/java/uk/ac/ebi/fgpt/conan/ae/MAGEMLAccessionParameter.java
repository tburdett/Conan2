package uk.ac.ebi.fgpt.conan.ae;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;

/**
 * Port of Conan 1 parameters: this class represents a MAGE-ML accession parameter.  This class is reused across most of
 * the AE2-centric implementations of Conan.
 *
 * @author Tony Burdett
 * @date 05-Nov-2010
 */
public class MAGEMLAccessionParameter extends AbstractConanParameter {
    private String accession;
    private File workFile;

    private Logger log = LoggerFactory.getLogger(getClass());

    public MAGEMLAccessionParameter() {
        super("MAGE-ML Accession");
        this.accession = null;
        this.workFile = null;
    }

    protected Logger getLog() {
        return log;
    }

    @Override public boolean validateParameterValue(String value) {
        return super.validateParameterValue(value) && (value.split("-").length == 3);
    }

    public boolean isExperiment() {
        return this.accession != null && this.accession.substring(0, 1).equals("E");
    }

    public void setAccession(String accessionVal) {
        // make sure the supplied value is valid
        if (!validateParameterValue(accessionVal)) {
            throw new IllegalArgumentException(
                    "Parameter value '" + accessionVal + "' does not look like a real accession, " +
                            "should be of the form E|A-[A-Z]{4}-[0-9]+");
        }
        this.accession = accessionVal;

        String[] accessionParts = this.accession.split("-");
        // override default for MEXP experiments - they go to MTAB instead
        String subdir = accessionParts[1];
        if (isExperiment()) {
            if (accessionParts[1].equals("MEXP")) {
                subdir = "MTAB";
            }
            String repositoryPath = ConanProperties.getProperty("experiment.mageml.directory");
            File expDir = new File(repositoryPath, subdir + File.separator + getAccession());
            this.workFile = new File(expDir, getAccession() + ".xml");
        }
        else {
            String repositoryPath = ConanProperties.getProperty("array.mageml.directory");
            File expDir = new File(repositoryPath, subdir + File.separator + getAccession());
            this.workFile = new File(expDir, getAccession() + ".xml");
        }
    }

    public String getAccession() {
        return accession;
    }

    public File getFile() {
        return workFile;
    }
}
