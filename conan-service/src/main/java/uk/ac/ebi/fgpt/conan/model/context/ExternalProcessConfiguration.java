/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
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
