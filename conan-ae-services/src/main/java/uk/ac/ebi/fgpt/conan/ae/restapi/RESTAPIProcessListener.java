package uk.ac.ebi.fgpt.conan.ae.restapi;

import java.util.EventListener;

/**
 * An event listener that monitors a process submitted through REST API.
 * Callback functions are provided for whenever
 *
 * @author Natalja Kurbatova
 * @date 23-05-2011
 */
public interface RESTAPIProcessListener extends EventListener {
  /**
   * Called whenever a monitored job obtains a JSON object as a response from a call to a REST API URL.
   *
   * @param evt an event reporting a response from a REST API
   */
    void restApiResponse(RESTAPIProcessEvent evt);
}
