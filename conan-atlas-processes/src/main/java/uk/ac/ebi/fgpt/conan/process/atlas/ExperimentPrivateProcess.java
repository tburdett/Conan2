package uk.ac.ebi.fgpt.conan.process.atlas;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.rest.AbstractRESTAPIProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Process to make experiment private in Atlas
 *
 * @author Natalja Kurbatova
 * @date 05/05/11
 */
@ServiceProvider
public class ExperimentPrivateProcess extends AbstractRESTAPIProcess {

  private final Collection<ConanParameter> parameters;
  private final AccessionParameter accessionParameter;

  private CommonAtlasProcesses atlas = new CommonAtlasProcesses();

  /**
   * Constructor for process. Initializes conan2 parameters for the process.
   */
  public ExperimentPrivateProcess() {
    parameters = new ArrayList<ConanParameter>();
    accessionParameter = new AccessionParameter();
    parameters.add(accessionParameter);
  }

  /**
   * Returns the name of this component that this process implements, if any.
   *
   * @return null since it not the AE2 component
   */
  @Override protected String getComponentName() {
    return null;
  }

  /**
   * Parses the response from Atlas REST API for the monitored process Returns
   * true if process has been finished.
   *
   * @param response response received from Atlas REST API in HashMap format
   * @return true when process is finished, false otherwise
   */
  @Override protected boolean isComplete(HashMap<String, Object> response) {
    return atlas.isComplete(response);
  }

  /**
   * Parses the response from Atlas REST API, extracts message and returns it.
   *
   * @param response response received from Atlas REST API in HashMap format
   * @return message text extracted from the REST API response
   */
  @Override protected String getMessage(HashMap<String, Object> response) {
    return atlas.getMessage(response);
  }

  /**
   * Parses the response from Atlas REST API, extracts and evaluates the event.
   *
   * @param response response received from Atlas REST API in HashMap format
   * @return exit code for the process
   */
  @Override protected int getExitCode(HashMap<String, Object> response) {
    return atlas.getExitCode(response);
  }

  /**
   * Parses the response from Atlas REST API, by using parameters extracts Atlas
   * job ID.
   *
   * @param response response received from Atlas REST API in HashMap format
   * @return Atlas job ID to monitor the job status
   */
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

  /**
   * Parses the response from Atlas REST API, by using parameters extracts Atlas
   * job ID.
   *
   * @param response response received from Atlas REST API in HashMap format
   * @return Atlas job ID to monitor the job status
   */
  @Override
  protected String getResultValue(HashMap<String, Object> response,
                                  String parameters) {
    return atlas.getResultValue(response, parameters);
  }

  /**
   * Returns Atlas REST API request to monitor the process
   *
   * @param id Atlas job ID
   * @return Atlas REST API request
   */
  @Override protected String getMonitoringRequest(String id) {
    return atlas.Monitoring + id;
  }

  /**
   * Creates Atlas Rest API request to update experiment
   *
   * @param parameters the parameters supplied to this ConanProcess
   * @return restApiRequest string
   * @throws IllegalArgumentException
   */
  @Override
  protected String getRestApiRequest(Map<ConanParameter, String> parameters)
      throws IllegalArgumentException {
    // deal with parameters
    AccessionParameter accession = new AccessionParameter();
    accession.setAccession(parameters.get(accessionParameter));

    if (accession.getAccession() == null) {
      throw new IllegalArgumentException("Accession cannot be null");
    }
    else {
      //execution
      if (accession.isExperiment()) {
        String restApiRequest = atlas.ExperimentUpdatePrivate +
            accession.getFile().getParentFile().getAbsolutePath();
        return restApiRequest;
      }
      else {
        throw new IllegalArgumentException(
            "Experiment is needed, not array");
      }
    }
  }

  /**
   * Creates Atlas Rest API request to update experiment
   *
   * @param parameters path to the mage-tab file
   * @return restApiRequest string
   * @throws IllegalArgumentException
   */
  @Override protected String getRestApiRequest(String parameters) {
    String restApiRequest = atlas.ExperimentUpdatePrivate + parameters;
    System.out.print(restApiRequest);
    return restApiRequest;
  }

  /**
   * Returns Atlas REST API request to log in
   *
   * @return Atlas REST API request
   */
  @Override protected String getLoginRequest() {
    return atlas.LogIn;
  }

  /**
   * Returns process name
   *
   * @return process name
   */
  public String getName() {
    return "atlas private";
  }

  /**
   * Returns a collection of strings representing the names of the parameters.
   *
   * @return the parameter names required to generate a task
   */
  public Collection<ConanParameter> getParameters() {
    return parameters;
  }


}
