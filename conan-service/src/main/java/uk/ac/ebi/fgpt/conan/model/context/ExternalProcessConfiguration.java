
package uk.ac.ebi.fgpt.conan.model.context;

import java.io.IOException;

/**
 * Defines an object containing a Map of ConanProcess IDs to additional pre-commands.  This enables platform specific
 * commands to be executed before each specific ConanProcess.  This is useful to bring tools onto the PATH for example.
 *
 * @author Dan Mapleson
 */
public interface ExternalProcessConfiguration {

    /**
     * Retrive the filepath to an external configuration file that defines the map of ConanProcess ids to commands.
     * @return
     */
    String getProcessConfigFilePath();

    /**
     * Set the filepath to the external configuration file that defines the map of ConanProcess ids to commands
     * @param postProcessFilePath
     */
    void setProcessConfigFilePath(String postProcessFilePath);

    /**
     * Loads the configuration file into this object
     * @throws IOException
     */
    void load() throws IOException;

    /**
     * Retrieves the Commands associated with the ConanProcessId.
     * @param key The ConanProcess name that keys with the external command
     * @return The command keyed by the given ConanProcess ID.
     */
    String getCommand(String key);
}
