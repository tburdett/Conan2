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
import java.io.File;
import java.io.FileWriter;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;
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


    private HttpClient httpclient = new DefaultHttpClient();

    private CookieStore cookieStore = new BasicCookieStore();
    // Create local HTTP context
    private HttpContext localContext = new BasicHttpContext();

    public enum RESTAPIEvents {
        WITHOUT_MONITORING, NO_LOGIN;
    }

    /**
     * REST API process
     */
    public boolean execute(Map<ConanParameter, String> parameters)
            throws ProcessExecutionException, IllegalArgumentException,
            InterruptedException {
       // process exit value, initialise to -1
       int exitValue = -1;
       BufferedWriter log = null;
       try{
        String reportsDir = logName(parameters)[0];
        String fileName = logName(parameters)[1] +
        "_" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()) +
        ".report";
        File reportsDirFile = new File(reportsDir);
        if (!reportsDirFile.exists()) {
          reportsDirFile.mkdirs();
        }
        log = new BufferedWriter(new FileWriter(fileName));
        log.write("Atlas REST API: START\n");
        log.write(logName(parameters)[2]+"\n");
        log.write("Executing Atlas REST API process with parameters: " + parameters + "\n");

        HashMap<String, Object> response;
        //have to login to work with REST API
        if (LogIn()) {
            //
            String jobQuery = getRestApiRequest(parameters);
            log.write("Atlas REST API request: " + jobQuery + "\n");
            String idToMonitor = getResultValue(restApiRequest(jobQuery), parameters);
            log.write("Atlas job ID: " + idToMonitor + "\n");
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

                    ProcessExecutionException pex =  new ProcessExecutionException(exitValue,getMessage(response));
                    if (exitValue == 0) {
                        return true;
                    }
                    else {
                        String[] errors = new String[1];
                        errors[0] = getMessage(response);
                        pex.setProcessOutput(errors);
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
                e.printStackTrace();
                exitValue = 1;

                ProcessExecutionException pex =  new ProcessExecutionException(exitValue,e.getMessage());
                String[] errors = new String[1];
                errors[0] = e.getMessage();
                pex.setProcessOutput(errors);
                throw pex;
            }
        }
        else {
         exitValue = 1;

         ProcessExecutionException pex =  new ProcessExecutionException(exitValue,"Can't log in into Atlas");
         String[] errors = new String[1];
         errors[0] = "Can't log in into Atlas";
         pex.setProcessOutput(errors);
         throw pex;
        }
       }
       catch(Exception e){
         e.printStackTrace();
         exitValue = 1;

         ProcessExecutionException pex =  new ProcessExecutionException(exitValue,e.getMessage());
         String[] errors = new String[1];
         errors[0] = e.getMessage();
         pex.setProcessOutput(errors);
         throw pex;
       }
       finally {
         try{
          log.write("Atlas REST API: FINISHED\n");
          log.close();
         }
         catch(Exception e){
           e.printStackTrace();
           exitValue = 1;

           ProcessExecutionException pex =  new ProcessExecutionException(exitValue,e.getMessage());
           String[] errors = new String[1];
           errors[0] = e.getMessage();
           pex.setProcessOutput(errors);
           throw pex;
         }
       }

    }

    public boolean executeMockup(String parameter)
            throws IllegalArgumentException, ProcessExecutionException,
            InterruptedException {
       BufferedWriter log = null;
       try{
        String reportsDir = logNameMockup(parameter)[0];
        String fileName = logNameMockup(parameter)[1] +
         "_" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()) +
         ".report";
        File reportsDirFile = new File(reportsDir);
        if (!reportsDirFile.exists()) {
          reportsDirFile.mkdirs();
        }
        log = new BufferedWriter(new FileWriter(fileName));
        log.write("Atlas REST API: START\n");
        log.write(logNameMockup(parameter)[2]+"\n");
        log.write("Executing Atlas REST API process with parameters: " + parameter + "\n");

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
                    if (exitValue==0)
                      return true;
                    else
                      throw new ProcessExecutionException(1, getMessage(response));
                }
                else {
                    exitValue = 0;
                    log.write("Atlas REST API Process completed with exit value " + exitValue + "\n");
                    return true;
                }
            }
            catch (Exception e) {
                log.write("Some problems in code\n");
                e.printStackTrace();
                throw new ProcessExecutionException(1,"Some problems in code",e);
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
           throw new ProcessExecutionException(1,"Some problems in code",e);
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


    private synchronized boolean LogIn() {
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
                //log.write("Atlas REST API log in request: " + getLoginRequest() + "\n");
                //log.write("Atlas REST API log in response: " + responseString + "\n");
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
            //log.write("Atlas REST API request: " + requestString + "\n");
            //log.write("Atlas REST API response: " + responseString + "\n");
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
            //log.write("Polling " + restApiStatusURL + " for status\n");
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
                            //log.write("Interrupted exception causing thread to die\n");
                            stop();
                        }
                    }
                }
            }
            //log.write("Stopping polling of " + restApiStatusURL + "\n");
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
              //log.write("Process completed: status message = " + getMessage(response) + "\n");
            }
            catch(Exception e){
              e.printStackTrace();
            }
            return response;
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

    protected abstract String[] logName(Map<ConanParameter, String> parameters);

    protected abstract String[] logNameMockup(String parameter);

}
