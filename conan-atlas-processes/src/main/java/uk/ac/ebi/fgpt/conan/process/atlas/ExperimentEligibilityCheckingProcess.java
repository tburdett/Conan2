package uk.ac.ebi.fgpt.conan.process.atlas;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.mged.magetab.error.ErrorCode;
import org.mged.magetab.error.ErrorItem;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.HybridizationNode;
import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.rest.AbstractRESTAPIProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.*;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.*;

import javax.sql.DataSource;
import java.io.File;
import java.util.*;

/**
 * Process to load experiment into Atlas
 *
 * @author Natalja Kurbatova
 * @date 15/02/11
 */
@ServiceProvider
public class ExperimentEligibilityCheckingProcess extends AbstractRESTAPIProcess {

 private JdbcTemplate jdbcTemplate;

  private final Collection<ConanParameter> parameters;
  private final AccessionParameter accessionParameter;
  private List<String> ArrayDesignAccessions;

  private CommonAtlasProcesses atlas = new CommonAtlasProcesses();

  /**
   * Constructor for process. Initializes conan2 parameters for the process.
   */
  public ExperimentEligibilityCheckingProcess() {
    parameters = new ArrayList<ConanParameter>();
    accessionParameter = new AccessionParameter();
    parameters.add(accessionParameter);
  }

  public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
  }

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
  }

  public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcTemplate.setLazyInit(true);
  }

  private List<String> ParseMAGETAB(File MageTabFile){
      // make a new parser
      MAGETABParser parser = new MAGETABParser();

      // add an error item listener to the parser, this one just reports parsing errors as stdout
      parser.addErrorItemListener(new ErrorItemListener() {

      public void errorOccurred(ErrorItem item) {
        // locate the error code from the enum, to check the generic message
        ErrorCode code = null;
        for (ErrorCode ec : ErrorCode.values()) {
          if (item.getErrorCode() == ec.getIntegerValue()) {
            code = ec;
            break;
          }
        }

        if (code != null) {
          // this just dumps out some info about the type of error
          System.out.println("Listener reported error...");
          System.out.println("\tError Code: " + item.getErrorCode() + " [" +
              code.getErrorMessage() + "]");
          System.out.println("\tError message: " + item.getMesg());
          System.out.println("\tCaller: " + item.getCaller());
        }
      }
    });

    // now, parse from a file
    File idfFile = MageTabFile;

    // print some stdout info
    System.out.println("Parsing " + idfFile.getAbsolutePath() + "...");

    // Get list of array designs used in experiment
    MAGETABInvestigation investigation = parser.parse(idfFile);
    // Get experiment type
    for (String expType : investigation.IDF.getComments().get("AEExperimentType"){
      //Todo check if is in Controlled Vocabulary
    }

    Collection<HybridizationNode> hybridizationNodes =  investigation.SDRF.getNodes(HybridizationNode.class);
    for (HybridizationNode hybNode : hybridizationNodes){
            for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns)
              if (!ArrayDesignAccessions.contains(arrayDesign.getAttributeValue()))
                    ArrayDesignAccessions.add(arrayDesign.getAttributeValue());
    }

    return ArrayDesignAccessions;
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
        String restApiRequest = atlas.ArrayDesignSearch +
            ArrayDesignAccession;
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

    String restApiRequest = atlas.ExperimentLoad + parameters;
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
   * Returns the name of this process.
   *
   * @return the name of this process
   */
  public String getName() {
    return "atlas loading";
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
