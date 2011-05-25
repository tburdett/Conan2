package uk.ac.ebi.fgpt.conan.ae.restapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An adapter over an REST API process being executed as a native system process.
 * This adapter works by querying for the status of the submitted job polling REST API for updates.
 * Whenever REST API output are recognised as key phrases,
 * events are triggered on any registered listeners.
 * <p/>
 * You can set the monitoring period to adjust the frequency of querying operations as required.
 *
 * @author Natalja Kurbatova
 * @date 23-05-2011
 */
public class RESTAPIProcessAdapter implements RESTAPIProcess {
    private final RestAPIStatusMonitor statusMonitor;
    private final Set<RESTAPIProcessListener> listeners;

    private Thread restApiURLMonitorThread;

    private Logger log = LoggerFactory.getLogger(getClass());

    public RESTAPIProcessAdapter(String statusQuery, int monitoringPeriod) {
        this.statusMonitor = new RestAPIStatusMonitor(statusQuery, monitoringPeriod);
        this.listeners = new HashSet<RESTAPIProcessListener>();
    }


    protected Logger getLog() {
        return log;
    }

    public void addRESTAPIProcessListener(RESTAPIProcessListener listener) {
        // do we have existing listeners?
        boolean startMonitor = listeners.isEmpty();
        listeners.add(listener);
        getLog().debug("Added process listener " + listener);
        // if we had no listeners, we might need to start monitoring
        if (startMonitor) {
            // create new thread if required
            if (restApiURLMonitorThread == null || !restApiURLMonitorThread.isAlive()) {
                getLog().debug("Creating new file monitor thread");
                restApiURLMonitorThread = new Thread(statusMonitor);
            }
            restApiURLMonitorThread.start();
            getLog().debug("Started file monitor thread");
        }
    }

    public void removeRESTAPIProcessListener(RESTAPIProcessListener listener) {
        listeners.remove(listener);
        // if we have removed the last listener, stop the monitor
        statusMonitor.stop();
        getLog().debug("Removed process listener " + listener);
    }

    protected void fireRESTAPIResponseEvent(final String RestApiResponse, final long requestTime) {
      for (RESTAPIProcessListener listener : listeners) {
        listener.restApiResponse(
            new RESTAPIProcessEvent(RestApiResponse, requestTime));
      }
    }

    private class RestAPIStatusMonitor implements Runnable {
        private final String restApiStatusURL;
        private final int interval;

        private boolean running;

        private RestAPIStatusMonitor(String restApiStatusURL, int interval) {
            this.restApiStatusURL = restApiStatusURL;
            this.interval = interval;

            this.running = true;
        }

        public void run() {
            getLog().debug("Polling " + restApiStatusURL + " for status");
            while (running) {
                // make request to restApiStatusURL
                long requestTime = System.currentTimeMillis();
                String jsonResponse = restApiRequest(restApiStatusURL);

                // parse response to determine if complete yet
                fireRESTAPIResponseEvent(jsonResponse, requestTime);

                // sleep for interval seconds
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
            getLog().debug("Stopping polling of " + restApiStatusURL);
        }

        public void stop() {
            running = false;
        }
    }

    private String restApiRequest(String restApiUrl){
      String result = "";
      try {
          // Create a URLConnection object for a URL
          URL url = new URL(restApiUrl);
          URLConnection conn = url.openConnection();
          // Get the response
          BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          StringBuffer sb = new StringBuffer();
          String line;
          while ((line = rd.readLine()) != null)
            sb.append(line);
          rd.close();
          result = sb.toString();
      } catch (Exception e) {
        getLog().debug("Can't get response from " + restApiUrl);
      }
      return result;
    }
}
