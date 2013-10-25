
package uk.ac.ebi.fgpt.conan.model.param;

import java.io.IOException;
import java.util.Map;


/**
 * Interface for Conan Process Arguments.  Enables conversion of a generic "Argument" class (essentially a class that
 * contains class variables for a ConanProcess) into different forms which are useful for Conan and the underlying
 * process.
 *
 * @author Dan Mapleson
 */
public interface ProcessArgs {

    /**
     * Parses a string containing the process arguments into this argument object.
     * @param args The arguments in string form to parse
     */
    void parse(String args) throws IOException;

    /**
     * Converts this arguments class into a Map of ConanParameters to String values, which can then be used for processing in
     * the Conan Engine.
     * @return Map of conan parameters to string values
     */
    Map<ConanParameter, String> getArgMap();

    /**
     * Converts a Map of ConanParameters to String values into this object
     * @param pvp
     */
    void setFromArgMap(Map<ConanParameter, String> pvp);
}
