
package uk.ac.ebi.fgpt.conan.model.param;

import org.apache.commons.cli.Options;

import java.util.List;

/**
 * An Interface for ConanParameter classes, which describes a ConanProcesses parameters.
 */
public interface ProcessParams {

    /**
     * Retrieves a list of all ConanParameters for this ConanProcess
     * @return The conan parameters managed by this object
     */
    List<ConanParameter> getConanParameters();

    /**
     * Creates a list of apache command line options from the defined parameters.
     * @return The conan parameters represented as apache command line options.
     */
    Options createCommandLineOptions();
}