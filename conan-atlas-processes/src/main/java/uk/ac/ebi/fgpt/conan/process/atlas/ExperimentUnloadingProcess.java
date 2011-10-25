package uk.ac.ebi.fgpt.conan.process.atlas;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.rest.AbstractRESTAPIProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Process to unload experiment from Atlas
 *
 * @author Natalja Kurbatova
 * @date 15/02/11
 */
@ServiceProvider
public class ExperimentUnloadingProcess extends AbstractRESTAPIProcess {

  private final Collection<ConanParameter> parameters;
  private final AccessionParameter accessionParameter;
  private CommonAtlasProcesses atlas = new CommonAtlasProcesses();


  /**
   * Constructor for process. Initializes conan2 parameters for the process.
   */
  public ExperimentUnloadingProcess() {
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
          response.get(accession.getFile().getAbsolutePath())
              .toString();
      System.out.println("Atlas job ID: " + jobID);
    }
    catch (Exception e) {
      e.printStackTrace();
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
      System.out.println("Accession cannot be null");
      throw new IllegalArgumentException("Accession cannot be null");
    }
    else {
      //execution
      if (accession.isExperiment()) {
        String restApiRequest = atlas.ExperimentUnload +
            accession.getAccession();
        return restApiRequest;
      }
      else {
        System.out.println("Experiment is needed, not array");
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

    String restApiRequest = atlas.ExperimentUnload + parameters;
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
   * Returns the name of this process.
   *
   * @return the name of this process
   */
  public String getName() {
    return "atlas unloading";
  }

  /**
   * Returns a collection of strings representing the names of the parameters.
   *
   * @return the parameter names required to generate a task
   */
  public Collection<ConanParameter> getParameters() {
    return parameters;
  }

  @Override
  protected String[] logName(Map<ConanParameter, String> parameters) {
    String[] log_parameters = new String[3];
    // deal with parameters
    AccessionParameter accession = new AccessionParameter();
    accession.setAccession(parameters.get(accessionParameter));

    //create parameters for logging
    //1. reports directory
    String reportsDir = accession.getFile().getParentFile().getAbsolutePath() + File.separator +
            "reports";
    log_parameters[0] = reportsDir;
    //2. log file name
    log_parameters[1] = reportsDir + File.separator + accession.getAccession() +
        "_AtlasRestApiUnload";
    //3, Process name
    log_parameters[2] = "Unloading process";
    return log_parameters;
  }

  @Override
  protected String[] logNameMockup(String parameter) {
    String[] log_parameters = new String[3];

    File file = new File(parameter);

    //create parameters for logging
    //1. reports directory
    String reportsDir = file.getParentFile().getAbsolutePath() + File.separator +
            "reports";
    log_parameters[0] = reportsDir;
    //2. log file name
    log_parameters[1] = reportsDir + File.separator + "mockup" +
        "_AtlasRestApiUnload";
    //3, Process name
    log_parameters[2] = "Unloading process";
    return log_parameters;
  }
}
