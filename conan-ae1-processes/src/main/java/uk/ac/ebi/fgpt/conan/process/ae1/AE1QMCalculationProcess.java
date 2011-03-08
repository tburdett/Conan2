package uk.ac.ebi.fgpt.conan.process.ae1;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.ae.MAGEMLAccessionParameter;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.microarray.mageloader.MAGELoaderFactory;
import uk.ac.ebi.microarray.mageloader.MAGERProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


/**
 * Port of AE1 QM calculation process to Conan 2
 *
 * @author Natalja Kurbatova
 * @date 08-Nov-2010
 */
@ServiceProvider
public class AE1QMCalculationProcess implements ConanProcess {

    private final Collection<ConanParameter> parameters;
    private final MAGEMLAccessionParameter accessionParameter;

    public AE1QMCalculationProcess() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new MAGEMLAccessionParameter();
        parameters.add(accessionParameter);
    }

    public boolean execute(Map<ConanParameter, String> parameters)
            throws ProcessExecutionException, IllegalArgumentException, InterruptedException {
        // deal with parameters
        MAGEMLAccessionParameter accession = new MAGEMLAccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }
        else {
            // get the mageml file
            File magemlFile = accession.getFile();

            // submit this to an AE1 style mage R process
            MAGERProcess rprocess = MAGELoaderFactory.createMAGERProcess();

            if (!rprocess.qmcalculation(magemlFile)) {
                // loader failed
                ProcessExecutionException pex =
                        new ProcessExecutionException(1, "AE1 QM calculation failed: " + rprocess.getWarnings()[0]);
                pex.setProcessOutput(rprocess.getWarnings());
                throw pex;
            }
            return true;
        }
    }

    public String getName() {
        return "AE1 QM Calculation";
    }

    public Collection<ConanParameter> getParameters() {
        return parameters;
    }
}
