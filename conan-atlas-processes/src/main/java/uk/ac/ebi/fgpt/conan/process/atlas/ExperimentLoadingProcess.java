package uk.ac.ebi.fgpt.conan.process.atlas;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 15/02/11
 */
@ServiceProvider
public class ExperimentLoadingProcess implements ConanProcess {

    private final Collection<ConanParameter> parameters;
    private final AccessionParameter accessionParameter;

    private Logger log = LoggerFactory.getLogger(getClass());

    private CommonAtlasProcesses atlas = new CommonAtlasProcesses();

    protected Logger getLog() {
        return log;
    }

    public ExperimentLoadingProcess() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new AccessionParameter();
        parameters.add(accessionParameter);
    }

    public boolean execute(Map<ConanParameter, String> parameters)
            throws ProcessExecutionException, IllegalArgumentException, InterruptedException {


        getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());

        // deal with parameters
        AccessionParameter accession = new AccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));

        JsonFactory jsonFactory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(jsonFactory);

        TypeReference<HashMap<String,Object>> typeRef
              = new TypeReference<HashMap<String,Object>>() {};

        try{


          URL loadRequest = new URL(atlas.ExperimentLoad + accession.getFile().getAbsolutePath());

          if (atlas.LogIn()){
           HashMap<String,Object> loadResults
             = mapper.readValue(loadRequest, typeRef);

           //ToDo: try to load into dev. instance and read out the results
           //to get true/false loading result

          }

        }
        catch (Exception e){

        }


        return false;
    }

    public String getName() {
        return "atlas experiment loading";
    }

    public Collection<ConanParameter> getParameters() {
        return parameters;
    }


}
