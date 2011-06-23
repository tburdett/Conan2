package uk.ac.ebi.fgpt.conan.process.atlas;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.ae.restapi.AbstractRESTAPIProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import java.util.*;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 15/02/11
 */
@ServiceProvider
public class ExperimentLoadingProcess extends AbstractRESTAPIProcess {

    private final Collection<ConanParameter> parameters;
    private final AccessionParameter accessionParameter;

    private CommonAtlasProcesses atlas = new CommonAtlasProcesses();


    public ExperimentLoadingProcess() {
        parameters = new ArrayList<ConanParameter>();
        accessionParameter = new AccessionParameter();
        parameters.add(accessionParameter);
    }



  @Override protected String getComponentName() {
    return "ATLASLOADER";
  }


  @Override protected boolean isComplete(HashMap<String, Object> response) {
    return atlas.isComplete(response);
  }

  @Override protected String getMessage(HashMap<String, Object> response) {
    return atlas.getMessage(response);
  }

  @Override protected int getExitCode(HashMap<String, Object> response) {
    return atlas.getExitCode(response);
  }

  @Override
  protected String getResultValue(HashMap<String, Object> response,
                                  Map<ConanParameter, String> parameters) {
    String jobID = RESTAPIEvents.WITHOUT_MONITORING.toString();
    // deal with parameters
    AccessionParameter accession = new AccessionParameter();
    accession.setAccession(parameters.get(accessionParameter));
    try {
      jobID =
          response.get(accession.getFile().getParentFile().getAbsolutePath())
              .toString();
      System.out.println(jobID);
    }
    catch (Exception e) {

    }

    return jobID;
  }

  @Override
  protected String getResultValue(HashMap<String, Object> response,
                                  String parameters) {
    return atlas.getResultValue(response, parameters);
  }

  @Override protected String getMonitoringRequest(String id) {
    return atlas.Monitoring + id;
  }

  /**
   * Creates Atlas Rest API request for experiment loading
   *
   * @param parameters the parameters supplied to this ConanProcess
   * @return restApiRequest string
   * @throws IllegalArgumentException
   */
  @Override protected String getRestApiRequest(
      Map<ConanParameter, String> parameters) throws IllegalArgumentException {

    // deal with parameters
    AccessionParameter accession = new AccessionParameter();
    accession.setAccession(parameters.get(accessionParameter));

    if (accession.getAccession() == null) {
      throw new IllegalArgumentException("Accession cannot be null");
    }
    else {
      //execution
      if (accession.isExperiment()) {
        String restApiRequest = atlas.ExperimentUnload +
            accession.getFile().getParentFile().getAbsolutePath();
        return restApiRequest;
      }
      else {
        throw new IllegalArgumentException(
            "Experiment is needed, not array");
      }
    }
  }

  @Override protected String getRestApiRequest(String parameters) {

    String restApiRequest = atlas.ExperimentUnload + parameters;
    System.out.print(restApiRequest);
    return restApiRequest;

  }

  @Override protected String getLoginRequest() {
    return atlas.LogIn;
  }

  public String getName() {
    return "atlas experiment loading";
  }

  public Collection<ConanParameter> getParameters() {
    return parameters;
  }


}
