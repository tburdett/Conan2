package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;

import java.util.Collection;

/**
 * A service that can be used to explore the available processes in the Conan framework.  Within Conan, there are
 * potentially many available processes that may be run, and are usually grouped into pipelines.
 * <p/>
 * This interface is used to define the ways known processes can be retrieved
 *
 * @author Tony Burdett
 * @date 11-Oct-2010
 */
public interface ConanProcessService {
    /**
     * Get every process known to Conan.  Users can then potentially use this information to create new pipelines.
     *
     * @return all available processes, irrespective of whether they are used in any pipelines
     */
    Collection<ConanProcess> getAllAvailableProcesses();

    /**
     * Gets the process with the given name.  Processes in Conan must be uniquely named, so this is guaranteed to return
     * a single result, or null if there is no process with this name.
     *
     * @param processName the name of the process to acquire
     * @return the process with this name
     */
    ConanProcess getProcess(String processName);
}
