package uk.ac.ebi.fgpt.conan.process.biosd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Map;

import net.sourceforge.fluxion.spi.ServiceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.conan.utils.CommandExecutionException;
import uk.ac.ebi.fgpt.conan.utils.ProcessUtils;

@ServiceProvider
public class MakePublicProcess extends AbstractBioSDProcess {

    private Logger log = LoggerFactory.getLogger(getClass());


    public String getName() {
        return "makepublic";
    }
    
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException,
            IllegalArgumentException, InterruptedException {

        log.info(
                "Executing " + getName() + " with the following parameters: "
                        + parameters.toString());

        // deal with parameters
        SampleTabAccessionParameter accession = new SampleTabAccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }

        String scriptpath = ConanProperties.getProperty("biosamples.script.path");
        File script = new File(scriptpath, "TagControl.sh");
        
        String command = script.getAbsolutePath() 
            + " -u "+ConanProperties.getProperty("biosamples.biosd.username")
            + " -p "+ConanProperties.getProperty("biosamples.biosd.password")
            + " -h \""+ConanProperties.getProperty("biosamples.biosd.url")+"\""
            + " -r Security:Private"
            + " -i "+accession.getAccession();
        
        int exitCode = 0;
        try {
            ProcessUtils.runCommand(command);
        } catch (CommandExecutionException e) {
            exitCode = e.getExitCode();
        } catch (IOException e) {
            return false;
        }
        
        if (exitCode == 0){
            return true;
        } else {
            //some error
            return false;
        }
        
    }

}
