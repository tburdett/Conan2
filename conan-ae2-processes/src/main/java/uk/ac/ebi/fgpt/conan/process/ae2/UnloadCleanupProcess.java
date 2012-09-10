package uk.ac.ebi.fgpt.conan.process.ae2;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.ebi.fgpt.conan.utils.ProcessRunner;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Process that should be run after each unload to perform required cleanup of load directories and FTP directories,
 * ready for "clean" reload.
 * <p/>
 * Must be run locally - ony works on banana.  If Conan is ever migrated to a different machine (not banana) this
 * process will need modifying to take account for this (or else script permssions must be updated).
 *
 * @author Tony Burdett
 * @date 19/04/11
 */
@ServiceProvider
public class UnloadCleanupProcess implements ConanProcess {
    private final Collection<ConanParameter> parameters;
    private final AccessionParameter accessionParameter;

    private Logger log = LoggerFactory.getLogger(getClass());

    public UnloadCleanupProcess() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new AccessionParameter();
        parameters.add(accessionParameter);
    }

    protected Logger getLog() {
        return log;
    }

    public String getName() {
        return "unload cleanup";
    }

    public Collection<ConanParameter> getParameters() {
        return parameters;
    }

    public boolean execute(Map<ConanParameter, String> parameters)
            throws ProcessExecutionException, IllegalArgumentException, InterruptedException {
        try {
            String command = getCommand(parameters);
            getLog().debug("Issuing command: [" + command + "]");
            ProcessRunner runner = new ProcessRunner();
            runner.redirectStderr(true);
            String[] output = runner.runCommmand(command);

            // debugging output
            if (output.length > 0) {
                getLog().debug("Response from command [" + command + "]: " +
                                       output.length + " lines, first line was " + output[0]);

                // write response to report file
                File f = new File(getOutputFilePath(parameters));
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(f)));
                for (String line : output) {
                    writer.println(line);
                }
                writer.close();
            }

            return true;
        }
        catch (CommandExecutionException e) {
            // could not dispatch to LSF
            getLog().error("Failed to dispatch job (exited with exit code " + e.getExitCode() + ")", e);
            ProcessExecutionException pex = new ProcessExecutionException(
                    e.getExitCode(),
                    "Failed to dispatch job (exited with exit code " + e.getExitCode() + ")",
                    e);
            pex.setProcessOutput(e.getErrorOutput());
            try {
                pex.setProcessExecutionHost(InetAddress.getLocalHost().getHostName());
            }
            catch (UnknownHostException e1) {
                getLog().debug("Unknown host", e1);
            }
            throw pex;
        }
        catch (IOException e) {
            getLog().error("Failed to read output stream of native system process");
            getLog().debug("IOException follows", e);
            throw new ProcessExecutionException(1, "Failed to read output stream of native system process", e);
        }
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
            return generateCleanupCommand(accession);
        }
    }

    private String getOutputFilePath(Map<ConanParameter, String> parameters) {
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
            return new File(outputDir, "unloadcleanup.lsfoutput.txt").getAbsolutePath();
        }
    }

    private String generateCleanupCommand(AccessionParameter accession) {
        // execute unloadCleanup script given environment and accession
        String environmentPath = ConanProperties.getProperty("environment.path");
        String ftpPath = ConanProperties.getProperty("full.ftp.location");
        return environmentPath + "software/bin/unloadCleanup.sh -a " + accession.getAccession() + " -f " + ftpPath;
    }
}
