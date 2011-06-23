package uk.ac.ebi.fgpt.conan.process.atlas;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.ae.restapi.AbstractRESTAPIProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Javadocs go here!
 *
 * @author Natalja Kurbatova
 * @date 05/05/11
 */
public class CommonAtlasProcesses {

  public enum AtlasEvents {
    SCHEDULED, STARTED, FAILED;
  }

  private String atlasPath = "http://banana.ebi.ac.uk:14072/gxa/";
  //ConanProperties.getProperty("atlas.path")
  private String atlasUsername = "autosubs";
  //ConanProperties.getProperty("atlas.username")
  private String atlasPassword = "password";
  //ConanProperties.getProperty("atlas.password")

  public String ExperimentUpdatePrivate = atlasPath +
      "admin?op=schedule&runMode=RESTART&type=" +
      "updateexperiment&autoDepends=false&useRawData=true&private=true&accession=";

  public String ExperimentUpdatePublic = atlasPath +
      "admin?op=schedule&runMode=RESTART&type=" +
      "updateexperiment&autoDepends=false&useRawData=true&private=false&accession=";

  public String ExperimentLoad = atlasPath +
      "admin?op=schedule&runMode=RESTART&type=" +
      "loadexperiment&autoDepends=false&useRawData=true&private=true&accession=";

  public String ExperimentUnload = atlasPath +
      "admin?op=schedule&runMode=RESTART&type=" +
      "unloadexperiment&autoDepends=false&useRawData=true&accession=";

  public String Monitoring = atlasPath +
      "admin?op=tasklog&event=";

  public String LogIn = atlasPath +
      "admin?op=login&userName=" +
      atlasUsername +
      "&password=" +
      atlasPassword;

  public int getExitCode(HashMap<String, Object> response) {
    HashMap<String, Object> items = items(response.get("items").toString());
    if (!items.get("event").equals(
        AtlasEvents.FAILED.toString())) {
      return -1;
    }
    else {
      return 0;
    }
  }

  public String getMessage(HashMap<String, Object> response) {
    String result = "";
    HashMap<String, Object> items = items(response.get("items").toString());
    result = items.get("message").toString();

    return result;
  }

  public boolean isComplete(HashMap<String, Object> response) {
    boolean result = false;

    HashMap<String, Object> items = items(response.get("items").toString());
    System.out.println("EVENT:" + items.get("event"));

    if (!items.get("event").equals(
        CommonAtlasProcesses.AtlasEvents.SCHEDULED.toString()) &&
        !items.get("event").equals(
            CommonAtlasProcesses.AtlasEvents.STARTED.toString())) {
      result = true;
    }
    return result;
  }

  public String getResultValue(HashMap<String, Object> response,
                               String parameters) {
    String jobID = "WITHOUT_MONITORING";
    try {
      jobID = response.get(parameters).toString();
      System.out.println("ID:" + jobID);
    }
    catch (Exception e) {
      jobID =
          AbstractRESTAPIProcess.RESTAPIEvents.WITHOUT_MONITORING.toString();
      System.out.println("Can't get ID");
      e.printStackTrace();
    }

    return jobID;
  }

  public HashMap<String, Object> items(String response) {
    //output parsing and HashMap object creation
    String delimiter = ",";
    String[] temp = response.split(delimiter);
    HashMap<String, Object> items = new HashMap<String, Object>();
    for (int i = 0; i < temp.length; i++) {
      delimiter = "=";
      String val = temp[i].replaceAll("\\[", "").replaceAll("\\{", "")
          .replaceAll("\\]", "").replaceAll("\\}", "");
      items.put(val.split(delimiter)[0].trim(),
                (val.split(delimiter).length > 1 ? val.split(delimiter)[1]
                    : null));
    }

    return items;
  }
}
