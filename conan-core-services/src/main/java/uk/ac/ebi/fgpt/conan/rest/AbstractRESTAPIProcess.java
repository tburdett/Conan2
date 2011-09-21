package uk.ac.ebi.fgpt.conan.rest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract {@link uk.ac.ebi.fgpt.conan.model.ConanProcess} that is designed for to process REST API requests.  You
 * can tailor monitor interval by process.
 *
 * @author Natalja Kurbatova
 * @date 23-05-2011
 */
public abstract class AbstractRESTAPIProcess implements ConanProcess {

    public static final int MONITOR_INTERVAL = 15;

    private BufferedWriter log;

    private HttpClient httpclient = new DefaultHttpClient();

    private CookieStore cookieStore = new BasicCookieStore();
    // Create local HTTP context
    private HttpContext localContext = new BasicHttpContext();

    public enum RESTAPIEvents {
        WITHOUT_MONITORING, NO_LOGIN;
    }

    protected BufferedWriter initLog(Map<ConanParameter, String> parameters) {
        return log;
    }

    protected BufferedWriter initLogMockup(String parameter) {
        return log;
    }

    /**
     * REST API...
     */
    public boolean execute(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException, ProcessExecutionException,
            InterruptedException {
       try{
        log = initLog(parameters);
        log.write("Executing Atlas REST API process with parameters: " + parameters + "\n");
        // process exit value, initialise to -1
        int exitValue = -1;
        HashMap<String, Object> response;
        //have to login to work with REST API
        if (LogIn()) {
            //
            String jobQuery = getRestApiRequest(parameters);
            String idToMonitor = getResultValue(restApiRequest(jobQuery), parameters);
            try {
                if (!idToMonitor.equals(RESTAPIEvents.WITHOUT_MONITORING)) {
                    // set up monitoring
                    final RESTAPIStatusMonitor
                            statusMonitor =
                            new RESTAPIStatusMonitor(getMonitoringRequest(idToMonitor),
                                                     MONITOR_INTERVAL);

                    // process monitoring
                    log.write("Monitoring process, waiting for completion\n");
                    new Thread(statusMonitor).start();
                    response = statusMonitor.waitFor();
                    exitValue = getExitCode(response);
                    log.write("Atlas REST API Process completed with exit value " + exitValue + "\n");

                    ProcessExecutionException pex = interpretExitValue(exitValue);
                    if (pex == null) {
                        return true;
                    }
                    else {
                        pex.setProcessOutput(new String[]{getMessage(response)});
                        throw pex;
                    }
                }
                else {
                    exitValue = 0;
                    log.write("Atlas REST API Process completed with exit value " + exitValue + "\n");
                    return true;
                }
            }
            catch (Exception e) {
                log.write(
                        "Can't get job id for monitoring process, assume that monitoring is not needed\n");
                exitValue = 0;
                return true;
            }
        }
       }
       catch(Exception e){
         e.printStackTrace();
         return false;
       }
       finally {
         try{
          log.write("Atlas REST API: FINISHED\n");
          log.close();
         }
         catch(Exception e){
           e.printStackTrace();
         }
       }
       return false;
    }

    public boolean executeMockup(String parameter)
            throws IllegalArgumentException, ProcessExecutionException,
            InterruptedException {
       try{
        log = initLogMockup(parameter);
        log.write("Executing Atlas REST API process with parameters: " + parameter + "\n");
        // process exit value, initialise to -1
        int exitValue = -1;
        HashMap<String, Object> response;
        //have to login to work with REST API
        if (LogIn()) {

            String jobQuery = getRestApiRequest(parameter);
            String idToMonitor = getResultValue(restApiRequest(jobQuery), parameter);
            try {
                if (!idToMonitor.equals(RESTAPIEvents.WITHOUT_MONITORING.toString())) {
                    // set up monitoring
                    final RESTAPIStatusMonitor
                            statusMonitor =
                            new RESTAPIStatusMonitor(getMonitoringRequest(idToMonitor),
                                                     MONITOR_INTERVAL);

                    // process monitoring
                    log.write("Monitoring process, waiting for completion\n");
                    new Thread(statusMonitor).start();
                    response = statusMonitor.waitFor();
                    exitValue = getExitCode(response);
                    log.write("Atlas REST API Process completed with exit value " + exitValue + "\n");
                    System.out.println("Atlas REST API Process completed with exit value " + exitValue );
                    ProcessExecutionException pex = interpretExitValue(exitValue);
                    if (pex == null) {
                        return true;
                    }
                    else {
                        pex.setProcessOutput(new String[]{getMessage(response)});
                        throw pex;
                    }
                }
                else {
                    exitValue = 0;
                    log
                            .write("Atlas REST API Process completed with exit value " + exitValue + "\n");
                    return true;
                }
            }
            catch (Exception e) {
                log.write(
                        "Can't get job id for monitoring process, assume that monitoring is not needed\n");
                exitValue = 0;
                return true;
            }
        }
       }
       catch(Exception e){
         e.printStackTrace();
         return false;
       }
       finally {
         try{
          log.write("Atlas REST API: FINISHED\n");
          log.close();
         }
         catch(Exception e){
           e.printStackTrace();
         }
       }
       return false;

    }
//*****************************************************************************//
//*********************Private help methods************************************//
//*****************************************************************************//

    /**
     * @param response query result from REST API (JSON)
     * @return pairs (key,value) from the parsed query result
     */
    private HashMap<String, Object> parseRestApiResponse(String response) {
        JsonFactory jsonFactory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(jsonFactory);

        TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<HashMap<String, Object>>() {
        };

        HashMap<String, Object> requestResults = new HashMap<String, Object>();

        try {
            requestResults
                    = mapper.readValue(response, typeRef);


            return requestResults;
        }
        catch (Exception e) {
          e.printStackTrace();
        }

        return requestResults;
    }


    private boolean LogIn() {
        //localContext is used as a session identifier
        CookieStore cookieStore = new BasicCookieStore();
        // remove the local context to start new session
        localContext.removeAttribute(ClientContext.COOKIE_STORE);
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        if (!getLoginRequest().equals(RESTAPIEvents.NO_LOGIN.toString())) {
            try {

                HttpGet httpget = new HttpGet(getLoginRequest());
                // Pass local context as a parameter
                HttpResponse response = httpclient.execute(httpget, localContext);
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity);
                log.write("Atlas REST API log in request: " + getLoginRequest() + "\n");
                log.write("Atlas REST API log in response: " + responseString + "\n");
                HashMap<String, Object> logonResults =
                        parseRestApiResponse(responseString);
                return (Boolean) logonResults.get("success");
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private HashMap<String, Object> restApiRequest(String requestString) {
        HashMap<String, Object> requestResults = new HashMap<String, Object>();
        try {

            HttpGet httpget = new HttpGet(requestString);
            // Pass local context as a parameter
            HttpResponse response = httpclient.execute(httpget, localContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            System.out.println("Atlas REST API request: " + requestString);
            System.out.println("Atlas REST API response: " + responseString);
            log.write("Atlas REST API request: " + requestString + "\n");
            log.write("Atlas REST API response: " + responseString + "\n");
            requestResults = parseRestApiResponse(responseString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return requestResults;
    }

    private class RESTAPIStatusMonitor implements Runnable {
        private final String restApiStatusURL;
        private final int interval;

        private HashMap<String, Object> response;
        private boolean running;
        private boolean complete;

        private RESTAPIStatusMonitor(String restApiStatusURL, int interval) {
            this.restApiStatusURL = restApiStatusURL;
            this.interval = interval;

            this.running = true;
            this.complete = false;
        }

        public void run() {
           try{
            log.write("Polling " + restApiStatusURL + " for status\n");
            System.out.println("Polling " + restApiStatusURL + " for status\n");
            while (running) {
                // make request to restApiStatusURL
                // parse response to determine if complete yet
                response = restApiRequest(restApiStatusURL);
                if (isComplete(response)) {
                    complete = true;
                    stop();
                }
                synchronized (this) {
                    notifyAll();
                }

                // sleep for interval seconds
                if (running) {
                    synchronized (this) {
                        try {
                            wait(interval * 1000);
                        }
                        catch (InterruptedException e) {
                            // if interrupted, die
                            log.write("Interrupted exception causing thread to die\n");
                            stop();
                        }
                    }
                }
            }
            log.write("Stopping polling of " + restApiStatusURL + "\n");
           }
           catch(Exception e){
             e.printStackTrace();
             stop();
           }
        }

        public void stop() {
            running = false;
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
            try{
              System.out.println("Process completed: status message = " + getMessage(response));
              log.write("Process completed: status message = " + getMessage(response) + "\n");
            }
            catch(Exception e){
              e.printStackTrace();
            }
            return response;
        }


    }

    /**
     * Translates an exit value returned by the LSF process into a meaningful java exception.  Override this method if
     * you want to do something clever with certain exit values.  The default behaviour is to wrap the supplied exit
     * value inside a ProcessExecutionException and provide a generic error message, if the exit code is non-zero.  If
     * an exit code of zero is passed, this method should return null.
     *
     * @param exitValue the exit value returned from the LSF process upon completion
     * @return a ProcessExecutionException that minimally wraps the exit value of the process, and possibly provides
     *         further informative error messages if the exit value is non-zero, otherwsie null
     */
    protected ProcessExecutionException interpretExitValue(int exitValue) {
        if (exitValue == 0) {
            return null;
        }
        else {
            return new ProcessExecutionException(exitValue);
        }
    }

    //*****************************************************************************//
//*********************Abstract methods to be implemented *********************//
//*****************************************************************************//
    protected abstract String getComponentName();

    protected abstract boolean isComplete(HashMap<String, Object> response);

    protected abstract String getMessage(HashMap<String, Object> response);

    protected abstract int getExitCode(HashMap<String, Object> response);

    protected abstract String getResultValue(HashMap<String, Object> response,
                                             Map<ConanParameter, String> parameters);

    protected abstract String getResultValue(HashMap<String, Object> response,
                                             String parameters);

    protected abstract String getMonitoringRequest(String id);

    protected abstract String getRestApiRequest(
            Map<ConanParameter, String> parameters)
            throws IllegalArgumentException;

    protected abstract String getRestApiRequest(String parameters);

    protected abstract String getLoginRequest();

}
