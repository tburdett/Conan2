
package uk.ac.ebi.fgpt.conan.model.param;

import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;

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
     *
     * @param args The arguments in string form to parse
     */
    void parse(String args) throws IOException;

    /**
     * This mechanism allows the user to create a conan process without having to define all the options.  By using the
     * unchecked args the user can ask put these additional arguments into the command line of the process.  However,
     * they are completely unchecked so it is the user's responsibility to ensure these are valid arguments / options
     * and won't cause the process to fail.
     * @return
     */
    String getUncheckedArgs();

    /**
     * Converts this arguments class into a Map of ConanParameters to String values, which can then be used for processing in
     * the Conan Engine.
     *
     * @return Map of conan parameters to string values
     */
    ParamMap getArgMap();

    /**
     * Converts a Map of ConanParameters to String values into this object
     *
     * @param pvp The parameter to value map
     *
     * @throws IOException May throw an IOException if there was any trouble reading or writing data to disk.  Most processes
     * are unlikely to attempt to write to disk but if the process tries to load its settings from a configuration file
     * it is possible errors could occur here.
     *
     * @throws ConanParameterException Thrown if there were issues processing any of the parameters.
     */
    void setFromArgMap(ParamMap pvp) throws IOException, ConanParameterException;
}
