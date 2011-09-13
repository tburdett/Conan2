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
import uk.ac.ebi.fgpt.conan.rest.AbstractRESTAPISubprocess;

import javax.sql.DataSource;
import java.io.File;
import java.util.*;

/**
 * Process to check array design existence in Atlas
 *
 * @author Natalja Kurbatova
 * @date 15/02/11
 */

public class ArrayDesignExistenceChecking extends AbstractRESTAPISubprocess {

  private CommonAtlasProcesses atlas = new CommonAtlasProcesses();

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
   * Parses the response from Atlas REST API, by using parameters extracts Atlas
   * job ID.
   *
   * @param response response received from Atlas REST API in HashMap format
   * @return Atlas job ID to monitor the job status
   */
  @Override
  protected String getResultValue(HashMap<String, Object> response,
                                  String parameters) {
    String result = "empty";
    try{
      System.out.println(response.get("arraydesigns").toString().toLowerCase());
      if (Integer.parseInt(response.get("numTotal").toString())>0) {
        result = "is";
        if (response.get("arraydesigns").toString().toLowerCase().contains("affy"))
          result="affy";
      }
      else
        result = "no";
      return result;
    }
    catch (Exception e){
      return result;
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

    String restApiRequest = atlas.ArrayDesignSearch + parameters;
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


}
