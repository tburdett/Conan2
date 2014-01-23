package uk.ac.ebi.fgpt.conan.model.param;

import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 14/01/14
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public interface ParamMap extends Map<ConanParameter, String> {

    /**
     * Retrieves a set of all the entries in this map.
     *
     * @return The entries in this map.
     */
    public Set<ParamMapEntry> paramEntrySet();

    /**
     * Returns a list of the options in this map in no particular order
     *
     * @return A list of the options in this map
     */
    public List<ParamMapEntry> getOptionList();

    /**
     * Returns a list of the arguments (i.e. options with no name that are order dependent) in this map
     *
     * @return A list of arguments returned in the appropriate order.
     */
    public List<ParamMapEntry> getArgList();

    /**
     * Builds a string representing the options in this map
     *
     * @param format The format with which the options should be built
     *
     * @return A string representing the options
     *
     * @throws ConanParameterException Thrown if there was a problem building the option string
     */
    public String buildOptionString(CommandLineFormat format) throws ConanParameterException;

    /**
     * Builds a string representing the options in this map.  Options found in the exception list are excluded from
     * the string.
     *
     * @param format The format with which the options should be built
     *
     * @param exceptions Any parameters that shouldn't be put into the option list
     *
     * @return A string representing the options
     *
     * @throws ConanParameterException Thrown if there was a problem building the option string
     */
    public String buildOptionString(CommandLineFormat format, List<ConanParameter> exceptions) throws ConanParameterException;

    /**
     * Builds a string representing the arguments in this map.  Order dependent.
     *
     * @return The arguments found in this map, represented in string form.
     */
    public String buildArgString();

    /**
     * Validates that all the values in this map pass the corresponding parameters validation routine.
     *
     * @param allParams All the possible parameters for this process.
     *
     * @throws ConanParameterException Thrown if the map is invalid.
     */
    public void validate(ProcessParams allParams) throws ConanParameterException;
}
