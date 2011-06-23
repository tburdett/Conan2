package uk.ac.ebi.fgpt.conan.process.ae1;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.ae.MAGEMLAccessionParameter;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.microarray.mageloader.MAGELoaderFactory;
import uk.ac.ebi.microarray.mageloader.MAGELoaderMode;
import uk.ac.ebi.microarray.mageloader.MAGEUnloader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Port of AE1 unloading process to Conan 2
 *
 * @author Natalja Kurbatova
 * @date 08-Nov-2010
 */
@ServiceProvider
public class AE1UnloadingProcess implements ConanProcess {
    private final Collection<ConanParameter> parameters;
    private final MAGEMLAccessionParameter accessionParameter;

    private final Semaphore parallelPermit;

    private Logger log = LoggerFactory.getLogger(getClass());

    public AE1UnloadingProcess() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new MAGEMLAccessionParameter();
        parameters.add(accessionParameter);

        parallelPermit = new Semaphore(1, true);
    }

    protected Logger getLog() {
        return log;
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

            // acquire the permit, ensuring we can't do more than one parallel unload
            if (!parallelPermit.tryAcquire()) {
                getLog().info("Parallel AE1 unload attempts detected - unload of " + accession.getAccession() + " " +
                                      "waiting in line...");
                parallelPermit.acquire();
                getLog().info("Unload of " + accession.getAccession() + " able to start at " +
                                      new SimpleDateFormat("kk:mm:ss").format(new Date()));
            }

            try {
                // submit this to an AE1 style mage unloader
                String type = ConanProperties.getProperty("conan.mode");
                MAGEUnloader unloader;
                if (type.equals("TEST")) {
                    unloader = MAGELoaderFactory.createMAGEUnloader(MAGELoaderMode.TEST);
                }
                else {
                    unloader = MAGELoaderFactory.createMAGEUnloader(MAGELoaderMode.PRODUCTION);
                }

                if (!unloader.unload(magemlFile)) {
                    // loader failed
                    ProcessExecutionException pex =
                            new ProcessExecutionException(1, "AE1 Unload failed: " + unloader.getWarnings()[0]);
                    pex.setProcessOutput(unloader.getWarnings());
                    throw pex;
                }
                return true;
            }
            finally {
                parallelPermit.release();
            }
        }
    }

    public String getName() {
        return "AE1 Unload";
    }

    public Collection<ConanParameter> getParameters() {
        return parameters;
    }
}
