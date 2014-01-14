package uk.ac.ebi.fgpt.conan.core.param;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;

/**
 * An abstract implementation of a {@link uk.ac.ebi.fgpt.conan.model.param.ConanParameter} that takes the parameter name as
 * it's constructor.  You can optionally supply a description, although it is not required.
 * <p/>
 * It is recommended that you subclass this class rather than implement the {@link
 * uk.ac.ebi.fgpt.conan.model.param.ConanParameter} interface directly, because this class handles parameter equality between
 * different processes.  The strategy for doing this is simple - parameters of the same class with the same name are
 * assumed to alway be equal.
 *
 * @author Tony Burdett
 * @date 19-Oct-2010
 * @see uk.ac.ebi.fgpt.conan.model.param.ConanParameter
 */
@JsonSerialize(as = ConanParameter.class)
public class DefaultConanParameter implements ConanParameter {

    protected String name;
    protected String longName;
    protected String description;
    protected boolean isFlag;
    protected boolean isOption;
    protected boolean isOptional;
    protected int argIndex;
    protected ArgValidator argValidator;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected DefaultConanParameter() {

        this.name = "";
        this.longName = "";
        this.description = "";
        this.isFlag = false;
        this.isOption = true;
        this.isOptional = true;
        this.argIndex = -1;
        this.argValidator = ArgValidator.DEFAULT;
    }

    protected DefaultConanParameter(String name) {
        this(name, false);
    }

    protected DefaultConanParameter(String name, boolean isBoolean) {
        this(name, name, isBoolean);
    }

    protected DefaultConanParameter(String name, String description) {
        this(name, description, false);
    }

    protected DefaultConanParameter(String name, String description, boolean isFlag) {
        this.name = name;
        this.description = description;
        this.isFlag = isFlag;
    }



    protected Logger getLog() {
        return log;
    }

    /**
     * Abstract implementation of this method, setting protected scope.  This implementation checks for whitespace,
     * slashes and file separators and returns false if any of these are present. This method is designed to be
     * overridden by individual parameter types that can define their own rules about what is legal.
     *
     * @param value the parameter value supplied
     * @return
     */
    public boolean validateParameterValue(String value) {
        return this.argValidator.validate(value);
    }


    @Override
    public String getIdentifier() throws ConanParameterException {

        if (this.isOption) {
            if (this.longName != null && !this.longName.isEmpty()) {
                return "Option name: " + longName;
            }
            else if (this.name != null && !this.name.isEmpty()) {
                return "Option name: " + name;
            }
            else {
                throw new ConanParameterException("Option parameter has been created without a name.");
            }
        }
        else {
            return"Arg index: " + Integer.toString(this.argIndex);
        }
    }

    @Override
    public String getShortName() {
        return name;
    }

    @Override
    public String getLongName() {
        return this.longName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean isBoolean() {
        return isFlag;
    }

    @Override
    public boolean hasArg() {
        return !isFlag;
    }

    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    @Override
    public boolean isRequired() {
        return !this.isOptional;
    }

    @Override
    public boolean isOption() {
        return this.isOption;
    }

    @Override
    public boolean isArgument() {
        return !this.isOption;
    }

    @Override
    public int getArgumentIndex() {
        return this.argIndex;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null)
            return false;

        if (this == o)
            return true;

        if (!(o instanceof DefaultConanParameter))
            return false;


        DefaultConanParameter other = (DefaultConanParameter) o;
        return new EqualsBuilder()
                .append(name, other.name)
                .append(longName, other.longName)
                .append(description, other.description)
                .append(isFlag, other.isFlag)
                .append(isOption, other.isOption)
                .append(isOptional, other.isOptional)
                .append(argIndex, other.argIndex)
                .append(argValidator, other.argValidator)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(name)
                .append(longName)
                .append(description)
                .append(isFlag)
                .append(isOption)
                .append(isOptional)
                .append(argIndex)
                .append(argValidator)
                .toHashCode();
    }


    @Override
    public String toString() {

        return this.isOption ?
                "Short name: " + name + "; Long name: " + longName + "; Description: " + this.description +
                        "; Is required: " + Boolean.toString(this.isRequired()) + "; Arg required: " + Boolean.toString(this.hasArg()) :
                "Arg index: " + argIndex + "; Description: " + description + "; Is required: " + Boolean.toString(this.isRequired());
    }
}
