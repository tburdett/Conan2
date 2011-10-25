package uk.ac.ebi.fgpt.conan.process.ae2;


import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Calculation of the QC using arrayQualityMetric Bioconductor package.
 * Documentation, perl and R scripts are in /ebi/microarray/home/AEQM/
 *
 * @author Natalja Kurbatova
 * @date 25-Oct-2011
 */
@ServiceProvider
public class QMCalculationProcess extends AbstractAE2LSFProcess {
    private final Collection<ConanParameter> parameters;
    private final AccessionParameter accessionParameter;

    private Logger log = LoggerFactory.getLogger(getClass());

    public QMCalculationProcess() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new AccessionParameter();
        parameters.add(accessionParameter);
    }

    protected Logger getLog() {
        return log;
    }


    public String getName() {
        return "QM calculation";
    }

    public Collection<ConanParameter> getParameters() {
        return parameters;
    }

    protected String getComponentName() {
        return LSFProcess.UNSPECIFIED_COMPONENT_NAME;
    }

    protected String getCommand(Map<ConanParameter, String> parameters) throws IllegalArgumentException {
        getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());

        // deal with parameters
        AccessionParameter accession = new AccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }
        else {
            //QM directory
            String QMDir = accession.getFile().getParentFile().getAbsolutePath() + File.separator +
            "QM";
            File QMDirFile = new File(QMDir);
            if (!QMDirFile.exists()) {
              QMDirFile.mkdirs();
            }
            // main command to execute perl script
            String mainCommand = "cd /ebi/microarray/home/AEQM/bin/; " +
                    "perl compute_QC.pl" +
                    " -n " + accession.getAccession() +
                    " -t " + QMDir;

            return mainCommand;
        }
    }

    protected String getLSFOutputFilePath(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException {
        getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());

        // deal with parameters
        AccessionParameter accession = new AccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }
        else {
            // get the mageFile parent directory
            final File parentDir = accession.getFile().getAbsoluteFile().getParentFile();

            // files to write output to
            final File outputDir = new File(parentDir, ".conan");

            // lsf output file
            return new File(outputDir, "qmcalculation.lsfoutput.txt").getAbsolutePath();
        }
    }
}