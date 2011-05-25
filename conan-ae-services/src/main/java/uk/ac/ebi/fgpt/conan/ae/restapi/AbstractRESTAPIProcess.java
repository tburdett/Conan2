package uk.ac.ebi.fgpt.conan.ae.restapi;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.exception.exceptions.AE2Exception;
import uk.ac.ebi.arrayexpress2.exception.manager.ExceptionManager;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract {@link uk.ac.ebi.fgpt.conan.model.ConanProcess} that is designed for to process
 * REST API requests.  You can tailor monitor interval by process.
 *
 * @author Natalja Kurbatova
 * @date 23-05-2011
 */
public abstract class AbstractRESTAPIProcess implements ConanProcess {

    public static final int MONITOR_INTERVAL = 15;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    /**
     *  REST API...
     */
    public boolean execute(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException, ProcessExecutionException, InterruptedException {
        getLog().debug("Executing an REST API process with parameters: " + parameters);
        // process exit value, initialise to -1
        int exitValue = -1;
        HashMap<String, Object> response;
        //have to login to work with REST API
        if (getLoginRequest(parameters)){
          //
          String jobQuery = getRestApiRequest(parameters);
          String idToMonitor = getResultValue(restApiRequest(jobQuery));
          try{
            if (!idToMonitor.equals("WITHOUT_MONITORING")){
              // set up monitoring
              InvocationTrackingRESTAPIProcessListener listener = new InvocationTrackingRESTAPIProcessListener();
              final RESTAPIProcessAdapter adapter = new RESTAPIProcessAdapter(getMonitoringRequest(idToMonitor), MONITOR_INTERVAL);
              adapter.addRESTAPIProcessListener(listener);

              // process monitoring
              getLog().debug("Monitoring process, waiting for completion");
              response = listener.waitFor();
              exitValue = getExitCode(response);
              getLog().debug("REST API Process completed with exit value " + exitValue);
              if (exitValue == 0) {
                return true;
              }
              else {
                AE2Exception cause = ExceptionManager.getException(getComponentName(), exitValue);
                String message = "Failed at " + getName() + ": " + cause.getDefaultMessage();
                getLog().debug("Generating ProcessExecutionException...\n" +
                              "exitValue = " + exitValue + ",\n" +
                              "restApiMessage = " + getMessage(response) + ",\n" +
                              "message = " + message + ",\n" +
                              "cause = " + cause.getClass().getSimpleName());
                ProcessExecutionException pex = new ProcessExecutionException(exitValue, message + ",\n" +
                   getMessage(response), cause);
                throw pex;
              }
            }
            else {
              exitValue = 0;
              getLog().debug("REST API Process completed with exit value " + exitValue);
              return true;
            }
          }
          catch(Exception e){
            getLog().debug("Can't get job id for monitoring process, assume that monitoring is not needed");
            exitValue = 0;
            return true;
          }
        }

      return false;
    }

    /**
     *
     *
     * @param jobQuery the query for REST API (JSON)
     * @return pairs (key,value) of the process
     */
    protected HashMap<String,Object> restApiRequest(String jobQuery) {
        JsonFactory jsonFactory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(jsonFactory);

        TypeReference<HashMap<String, Object>> typeRef
            = new TypeReference<HashMap<String, Object>>() {
        };

        HashMap<String, Object> requestResults = new HashMap<String,Object>();

        getLog().debug("Issuing REST API request: [" + jobQuery + "]");
        try {
          URL Request = new URL(jobQuery);

          requestResults
              = mapper.readValue(Request, typeRef);

          return requestResults;
        }
        catch (Exception e) {

        }
        if (!requestResults.isEmpty()) {
            getLog().debug("Response from REST API request [" + jobQuery + "]: " +
                     requestResults.size() + " pairs: " + requestResults.toString());
        }
        return requestResults;
    }

    /**
     *
     *
     * @param response query result from REST API (JSON)
     * @return pairs (key,value) from the parsed query result
     */
    protected HashMap<String,Object> parseRestApiResponse(String response) {
        JsonFactory jsonFactory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(jsonFactory);

        TypeReference<HashMap<String, Object>> typeRef
            = new TypeReference<HashMap<String, Object>>() {
        };

        HashMap<String, Object> requestResults = new HashMap<String,Object>();

        try {
          requestResults
              = mapper.readValue(response,typeRef);

          return requestResults;
        }
        catch (Exception e) {

        }

        return requestResults;
    }


    /**
     * Returns the name of this component that this process implements, if any.  This is designed to allow registration
     * of error codes to process from ArrayExpress, meaning we can lookp the correct {@link AE2Exception} given the
     * component name that the process implements.  If this method returns any value apart from {@link
     * RESTAPIProcess#UNSPECIFIED_COMPONENT_NAME}, the exit code of the process must be used to extract information about
     * the failure condition and a user-meaningful message displayed indicating the error.
     *
     * @return the name of the AE2 component this process implements, or LSFProcess.UNSPECIFIED_COMPONENT_NAME if
     *         something else.
     */
    protected abstract String getComponentName();

    protected abstract boolean isComplete(HashMap<String, Object> response);

    protected abstract String getMessage(HashMap<String, Object> response);

    protected abstract int getExitCode(HashMap<String, Object> response);

    protected abstract String getResultValue(HashMap<String, Object> response);

    protected abstract String getMonitoringRequest(String id);

    /**
     * The request to complete with REST API.
     *
     * @param parameters the parameters supplied to this ConanProcess
     * @return the query for REST API
     * @throws IllegalArgumentException if the parameters supplied were invalid
     */
    protected abstract String getRestApiRequest(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException;

    protected abstract boolean getLoginRequest(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException;

   /**
     * The query for job status with REST API.
     *
     * @param jobID job accession number
     * @return the status query for REST API
     * @throws IllegalArgumentException if the parameters supplied were invalid
     */
    protected abstract HashMap getStatusRequest(String jobID);

    /**
     * An {@link RESTAPIProcessListener} that encapsulates the state of each invocation of a process and updates flags for
     * completion and success.  Processes using this listener implementation can block on {@link #waitFor()}, which only
     * returns once the LSF process being listened to is complete.
     */
   private class InvocationTrackingRESTAPIProcessListener implements RESTAPIProcessListener {
        private boolean complete;
        private HashMap<String, Object> response;

        InvocationTrackingRESTAPIProcessListener() {
            complete = false;
        }

      public void restApiResponse(RESTAPIProcessEvent evt) {
        response = parseRestApiResponse(evt.getRestApiResponse());
        if (isComplete(response)) {
          complete = true;
        }
        synchronized (this) {
            notifyAll();
        }
      }

        /**
         * Returns the success of the RESTAPIProcess being listened to, only once complete.  This method blocks until
         * completion or an interruption occurs.
         *
         * @return the exit value of the underlying process
         * @throws InterruptedException if the thread was interrupted whilst waiting
         */
        public HashMap<String, Object> waitFor() throws InterruptedException {
            while (!complete) {
                synchronized (this) {
                    wait();
                }
            }
            getLog().debug("Process completed: status message = " + getMessage(response));
            return response;
        }
    }
}
