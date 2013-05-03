package uk.ac.ebi.fgpt.conan.process.ae2;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 02/05/2013
 * Time: 09:18
 * To change this template use File | Settings | File Templates.
 */
@ServiceProvider
public class BamGenerationTriggerProcess extends AbstractAE2LSFProcess {
    private final Collection<ConanParameter> parameters;
    private final AccessionParameter accessionParameter;

    private static Logger log = LoggerFactory.getLogger(BamGenerationTriggerProcess.class); // getClass()

    public BamGenerationTriggerProcess() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new AccessionParameter();
        parameters.add(accessionParameter);
//        setQueueName("production");
    }

    protected Logger getLog() {
        return log;
    }


    public String getName() {
        return "ae2.bam.gen.trigger";
    }

    public Collection<ConanParameter> getParameters() {
        return parameters;
    }

    protected String getComponentName() {
        return LSFProcess.UNSPECIFIED_COMPONENT_NAME;
    }

    protected String getCommand(Map<ConanParameter, String> parameters) throws IllegalArgumentException {
        getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());

        // parameters
        AccessionParameter accession = new AccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));

        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }

        else {
            // check the accession is an experiments
            //
            if (accession.isExperiment()) {
                // main command
                String triggerToolPath = "/ebi/microarray/home/biocep/service/bam.gen/tools/bam.ae2.trigger ";
                //String triggerToolPath = ConanProperties.getProperty(getName() + ".path");

                return triggerToolPath + " " + accession.getAccession();
            } else {
                throw new IllegalArgumentException(".bam generates from experiments, not arrays");
            }

        }
    }

    protected String getLSFOutputFilePath(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException {
        // deal with parameters
        AccessionParameter accession = new AccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        } else {
            // get the mageFile parent directory
            final File parentDir = accession.getFile().getAbsoluteFile().getParentFile();

            // files to write output to
            final File outputDir = new File(parentDir, ".conan");

            // lsf output file
            return new File(outputDir, getName() + ".lsfoutput.txt").getAbsolutePath();
        }
    }

    public static void main(String [] args) {

        try {
            Field privateInternalProps = ConanProperties.class.getDeclaredField("conanProperties");
            privateInternalProps.setAccessible(true);
            ConanProperties props = (ConanProperties) privateInternalProps.get(ConanProperties.class);


            Field privatePropFile = ConanProperties.class.getDeclaredField("conanPropertiesFile");
            privatePropFile.setAccessible(true);
            privatePropFile.set(props, new File("/Users/andrew/project/Conan2/conan2/conan-ui/src/main/webresources-perftest/conan.properties"));

            BamGenerationTriggerProcess p = new BamGenerationTriggerProcess();
            Map<ConanParameter, String> map = new HashMap<ConanParameter, String>();
            AccessionParameter accession = new AccessionParameter();
            accession.setAccession("E-GEOD-1234");
            map.put(accession, accession.getAccession());

            p.execute(map);
        } catch (Exception ex) {
            log.error("Error!", ex);
        }
    }
}
