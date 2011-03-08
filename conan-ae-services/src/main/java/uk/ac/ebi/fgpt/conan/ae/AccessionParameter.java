package uk.ac.ebi.fgpt.conan.ae;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;

/**
 * A specially loaded parameter that conveys MAGE-TAB accession numbers.  This class is reused across most of the
 * AE2-centric implementations of Conan.
 *
 * @author Natalja Kurbatova
 * @date 18-Oct-2010
 */
public class AccessionParameter extends AbstractConanParameter {
    private String accession;
    private File workFile;
    private String subDir;
    private String workDir;

    private Logger log = LoggerFactory.getLogger(getClass());

    public AccessionParameter() {
        super("Accession Number");
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
        // make sure the supplied accession is valid
        if (!validateParameterValue(accessionVal)) {
            throw new IllegalArgumentException(
                    "Parameter value '" + accessionVal + "' does not look like a real accession, " +
                            "should be of the form E|A-[A-Z]{4}-[0-9]+");
        }

        this.accession = accessionVal;

        String[] accessionParts = this.accession.split("-");
        String subdir = accessionParts[1];

        if (isExperiment()) {
            String repositoryPath = ConanProperties.getProperty("experiment.load.directory");
            File expDir = new File(repositoryPath, subdir + File.separator + getAccession());
            this.workFile = new File(expDir, getAccession() + ".idf.txt");
            this.workDir = expDir.getAbsolutePath();

        }
        else {
            String repositoryPath = ConanProperties.getProperty("array.load.directory");
            File arrayDir = new File(repositoryPath, subdir + File.separator + getAccession());
            this.workFile = new File(arrayDir, getAccession() + ".adf.txt");
            this.workDir = arrayDir.getAbsolutePath();

        }
        this.subDir = subdir + File.separator + getAccession();

    }

    public String getAccession() {
        return accession;
    }

    public File getFile() {
        return workFile;
    }

    public String getSubDir() {
        return subDir;
    }

    public String getWorkDir() {
        return workDir;
    }


}
