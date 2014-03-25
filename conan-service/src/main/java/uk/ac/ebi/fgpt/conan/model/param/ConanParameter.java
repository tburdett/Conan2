package uk.ac.ebi.fgpt.conan.model.param;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;

import java.io.Serializable;

/**
 * Am input parameter that must be supplied before a {@link uk.ac.ebi.fgpt.conan.model.ConanProcess} can be executed.  This is a simple interface
 * that encapsulates the name and the description attached to a given parameter.
 * <p/>
 * It is not normally desirable to implement this interface yourself directly - you should generally subclass {@link
 * AbstractConanParameter} instead, as doing so will take care of parameter equality for you.
 * If you <b>do</b> wish to implement this interface directly, you should ensure that you have a strategy for ensuring
 * equality between parameters, or else reusing parameters between {@link uk.ac.ebi.fgpt.conan.model.ConanProcess}
 * instances.  Chaining multiple processes whilst relying on native equality will not normally give desired results.
 *
 * @author Tony Burdett
 * @date 30-Jul-2010
 * @see uk.ac.ebi.fgpt.conan.model.ConanProcess
 * @see uk.ac.ebi.fgpt.conan.model.ConanPipeline
 * @see AbstractConanParameter
 */
@JsonSerialize(typing = JsonSerialize.Typing.STATIC)
public interface ConanParameter extends Serializable {


    /**
     * Returns an identifier that describes this parameter.  This maybe automatically generated or user defined.
     *
     * @return An human readable indentifier for this parameter.
     */
    String getIdentifier() throws ConanParameterException;

    /**
     * Returns the short name of this parameter
     *
     * @return the short parameter name
     */
    String getShortName();

    /**
     * Returns the long name of this parameter
     *
     * @return the long parameter name
     */
    String getLongName();

    /**
     * Returns the description of this parameter
     *
     * @return the parameter description
     */
    String getDescription();

    /**
     * A flag indicating whether this parameter is a boolean or not.  The values supplied to invoke each tasks will
     * still be strings - "true" or "false" - but the Conan interface may chose to render a boolean parameter as a
     * checkbox prior to submission.
     *
     * @return true if this parameter accepts boolean strings "true" or "false", false otherwise
     */
    boolean isBoolean();

    /**
     * A flag indicating whether this parameter should take any arguments or not.  This is essentially, the inverse of
     * "isBoolean".
     *
     * @return True if this parameter takes an argument.  False if not (i.e. this param represents a flag).
     */
    boolean hasArg();

    /**
     * A flag indicating whether this parameter represents an option or not.  This should return the inverse of "isRequired".
     *
     * @return True if this parameter is optional.
     */
    boolean isOptional();

    /**
     * A flag indicating whether or not this parameter is required. This should return the inverse of "isOptional".
     *
     * @return True if this parameter is required.
     */
    boolean isRequired();

    /**
     * A flag indicating whether or not this parameter is an option (i.e. it HAS a name and is NOT order dependent).
     * This should return the opposite of "isArgument".
     *
     * @return True if this parameter represents an argument
     */
    boolean isOption();

    /**
     * A flag indicating whether or not this parameter is an argument (i.e. it has no name and is order dependent).
     * This should return the opposite of "isOption".
     *
     * @return True if this parameter represents an argument
     */
    boolean isArgument();

    /**
     * A flag indicating whether or not this parameter represents an output redirection to a file (i.e. it follows a ">")
     * Note that we currently do not support appending to a file only overwriting.
     *
     * @return True if this parameter represents an redirect to file
     */
    boolean isRedirect();

    /**
     * If this parameter represents a program argument (i.e. it has no name and is order dependent).  Then this method
     * should return the 0-based index of the argument.  For example, 0 would be the first argument, 1 second and so on.
     *
     * @return The 0-based index of where the argument should be.  Or -1 if this parameter represents an option.
     */
    int getArgumentIndex();

    /**
     * Validates a parameter value to ensure that it is legal for submissions.  Typically, this may check if the
     * supplied value contains no whitespace, slashes, or file separators as parameter values containing these
     * characters frequently cause problems.
     * <p/>
     *
     * @param value the parameter value supplied
     * @return true if this is a valid value, false otherwise
     */
    boolean validateParameterValue(String value);
}
