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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract that is designed to process REST API requests. You
 * can tailor monitor interval by process.
 *
 * @author Natalja Kurbatova
 * @date 23-05-2011
 */
public abstract class AbstractRESTAPISubprocess {

    public static final int MONITOR_INTERVAL = 5;

    private Logger log = LoggerFactory.getLogger(getClass());

    private HttpClient httpclient = new DefaultHttpClient();

    private CookieStore cookieStore = new BasicCookieStore();
    // Create local HTTP context
    private HttpContext localContext = new BasicHttpContext();

    public enum RESTAPIEvents {
        WITHOUT_MONITORING, NO_LOGIN;
    }

    protected Logger getLog() {
        return log;
    }

    public String execute(String parameter)
            throws IllegalArgumentException, ProcessExecutionException,
            InterruptedException {
        String result = "empty";
        getLog()
                .debug("Executing an REST API process with parameters: " + parameter);
        // process exit value, initialise to -1
        int exitValue = -1;
        HashMap<String, Object> response;
        //have to login to work with REST API
        if (LogIn()) {

            String jobQuery = getRestApiRequest(parameter);
            result = getResultValue(restApiRequest(jobQuery), parameter);

        }
        return result;

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
            System.out.println(responseString);
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
            getLog().debug("Polling " + restApiStatusURL + " for status");
            System.out.println("Polling " + restApiStatusURL + " for status");
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
                            getLog().debug("Interrupted exception causing thread to die");
                            stop();
                        }
                    }
                }
            }
            getLog().debug("Stopping polling of " + restApiStatusURL);
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
            getLog()
                    .debug("Process completed: status message = " + getMessage(response));
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

    protected abstract boolean isComplete(HashMap<String, Object> response);

    protected abstract String getMessage(HashMap<String, Object> response);

    protected abstract String getResultValue(HashMap<String, Object> response,
                                             String parameters);

    protected abstract String getRestApiRequest(String parameters);

    protected abstract String getLoginRequest();

}
