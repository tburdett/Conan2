
package uk.ac.ebi.fgpt.conan.model.param;

import java.util.List;

/**
 * An Interface for ConanParameter classes, which describes a ConanProcesses parameters.
 */
public interface ProcessParams {

    /**
     * Retrieves a list of all ConanParameters for this ConanProcess
     * @return
     */
    List<ConanParameter> getConanParameters();
}