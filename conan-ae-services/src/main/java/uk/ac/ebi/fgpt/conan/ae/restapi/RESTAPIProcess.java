package uk.ac.ebi.fgpt.conan.ae.restapi;

/**
 * An object that models a process running by using external REST API.  This is not a {@link
 * uk.ac.ebi.fgpt.conan.model.ConanProcess} implementation.
 *
 * @author Natalja Kurbatova
 * @date 23-05-2011
 */
public interface RESTAPIProcess {
    public static final String UNSPECIFIED_COMPONENT_NAME = "unspecified";

    /**
     * Adds a listener that listens to this REST API process and provides callback events on any changes
     *
     * @param listener the listener to add
     */
    void addRESTAPIProcessListener(RESTAPIProcessListener listener);

    /**
     * Removes a listener that is listening to this RESTAPIProcess
     *
     * @param listener the listener to remove
     */
    void removeRESTAPIProcessListener(RESTAPIProcessListener listener);
}
