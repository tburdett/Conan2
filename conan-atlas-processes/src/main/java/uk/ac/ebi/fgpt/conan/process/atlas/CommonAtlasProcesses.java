package uk.ac.ebi.fgpt.conan.process.atlas;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.net.URL;
import java.util.HashMap;

/**
 * Javadocs go here!
 *
 * @author Natalja Kurbatova
 * @date 05/05/11
 */
public class CommonAtlasProcesses {

  public String ExperimentUpdatePrivate = ConanProperties.getProperty("atlas.path") +
        "admin?op=schedule&runMode=RESTART&type=" +
        "updateexperiment&autoDepends=false&useRawData=true&private=true&accession=";

  public String ExperimentUpdatePublic = ConanProperties.getProperty("atlas.path") +
        "admin?op=schedule&runMode=RESTART&type=" +
        "updateexperiment&autoDepends=false&useRawData=true&private=false&accession=";

  public String ExperimentLoad = ConanProperties.getProperty("atlas.path") +
        "admin?op=schedule&runMode=RESTART&type=" +
        "loadexperiment&autoDepends=false&useRawData=true&private=true&accession=";

  public String ExperimentUnload = ConanProperties.getProperty("atlas.path") +
        "admin?op=schedule&runMode=RESTART&type=" +
        "unloadexperiment&autoDepends=false&useRawData=true&accession=";

  public boolean LogIn() {

/*    String AtlasLogin = ConanProperties.getProperty("atlas.path") +
        "admin?op=login&userName=" +
        ConanProperties.getProperty("atlas.username") +
        "&password=" +
        ConanProperties.getProperty("atlas.password");*/

    String AtlasLogin = "http://lime.ebi.ac.uk:14032/gxa-load/" +
        "admin?op=login&userName=" +
        "autosubs" +
        "&password=" +
        "password";

    JsonFactory jsonFactory = new JsonFactory();

    ObjectMapper mapper = new ObjectMapper(jsonFactory);

    TypeReference<HashMap<String, Object>> typeRef
        = new TypeReference<HashMap<String, Object>>() {
    };
    try {
      URL loginRequest = new URL(AtlasLogin);

      HashMap<String, Object> logonResults
          = mapper.readValue(loginRequest, typeRef);

      return (Boolean) logonResults.get("success");
    }
    catch (Exception e) {
      return false;
    }

  }
}
