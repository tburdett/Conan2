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
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.util.HashMap;


/**
 * An abstract that is designed to process REST API requests. You
 * can tailor monitor interval by process.
 *
 * @author Natalja Kurbatova
 * @date 23-05-2011
 */
public abstract class AbstractRESTAPISubprocess {

    private HttpClient httpclient = new DefaultHttpClient();

    private CookieStore cookieStore = new BasicCookieStore();
    // Create local HTTP context
    private HttpContext localContext = new BasicHttpContext();

    public enum RESTAPIEvents {
        WITHOUT_MONITORING, NO_LOGIN;
    }

    public String execute(String parameter)
            throws IllegalArgumentException, ProcessExecutionException,
            InterruptedException {
        String result = "empty";
        System.out.println("Executing REST API process with parameters: " + parameter);
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
        } catch (Exception e) {

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
            } catch (Exception e) {
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
            System.out.println("Atlas REST API request: " + requestString);
            // Pass local context as a parameter
            HttpResponse response = httpclient.execute(httpget, localContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            System.out.println("Atlas REST API response: " + responseString);
            requestResults = parseRestApiResponse(responseString);
        } catch (Exception e) {
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
                        } catch (InterruptedException e) {
                            // if interrupted, die
                            System.out.println("Interrupted exception causing thread to die");
                            stop();
                        }
                    }
                }
            }
            System.out.println("Stopping polling of " + restApiStatusURL);
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
            System.out.println("Process completed: status message = " + getMessage(response));
            return response;
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
