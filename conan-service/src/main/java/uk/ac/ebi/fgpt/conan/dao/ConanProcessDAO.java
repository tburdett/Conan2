package uk.ac.ebi.fgpt.conan.dao;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;

import java.util.Collection;

/**
 * A data access object for retrieving {@link ConanProcess}es and associated details from some datasource used to
 * persist this information.  Processes are typically loaded dynamically, so the backing datasource will actually be the
 * jar files loaded at startup, with the relevant annotations.
 *
 * @author Tony Burdett
 * @date 25-Nov-2010
 */
public interface ConanProcessDAO {
    /**
     * Gets a single process with the given unique name, if found.  If no such process exists, null is returned
     *
     * @param processName the name of the process to retrieve
     * @return the process with this unique name
     */
    ConanProcess getProcess(String processName);

    /**
     * Gets a collection of all processes available.  If there are no processes available, this returns an empty list
     *
     * @return all available processes
     */
    Collection<ConanProcess> getProcesses();
}
