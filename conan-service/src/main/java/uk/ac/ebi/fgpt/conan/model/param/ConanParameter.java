package uk.ac.ebi.fgpt.conan.model.param;

import org.codehaus.jackson.map.annotate.JsonSerialize;

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
     * Returns the name of this parameter
     *
     * @return the parameter name
     */
    String getName();

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
