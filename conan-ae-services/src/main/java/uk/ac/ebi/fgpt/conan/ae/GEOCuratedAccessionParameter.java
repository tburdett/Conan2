package uk.ac.ebi.fgpt.conan.ae;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 28/03/11
 */
public class GEOCuratedAccessionParameter extends AbstractConanParameter {
    private String accession;
    private File workFile;
    private String subDir;
    private String workDir;

    private Logger log = LoggerFactory.getLogger(getClass());

    public GEOCuratedAccessionParameter() {
        super("GEO Accession Number");
        this.accession = null;
        this.workFile = null;
    }

    protected Logger getLog() {
        return log;
    }

    @Override
    public boolean validateParameterValue(String value) {
        return super.validateParameterValue(value) && (value.split("-").length == 3) && value.startsWith("E-GEOD");
    }

    public boolean isExperiment() {
        return this.accession != null && this.accession.substring(0, 1).equals("E");
    }

    public void setAccession(String accessionVal) {
        // make sure the supplied accession is valid
        if (!validateParameterValue(accessionVal)) {
            throw new IllegalArgumentException(
                    "Parameter value '" + accessionVal + "' does not look like a real GEO accession, " +
                            "should be of the form E-GEOD-[0-9]+");
        }

        this.accession = accessionVal;

        String[] accessionParts = this.accession.split("-");
        String subdir = accessionParts[1];

        if (isExperiment()) {
            String repositoryPath = ConanProperties.getProperty("geo.experiment.load.directory");
            File expDir = new File(repositoryPath, subdir + File.separator + getAccession());
            this.workFile = new File(expDir, getAccession() + ".idf.txt");
            this.workDir = expDir.getAbsolutePath();

        }
        else {
            throw new IllegalArgumentException("Only experiments can be used as GEO parameters");
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
