package uk.ac.ebi.fgpt.conan.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * An abstract implementation of a {@link uk.ac.ebi.fgpt.conan.model.ConanParameter} that takes the parameter name as
 * it's constructor.  You can optionally supply a description, although it is not required.
 * <p/>
 * It is recommended that you subclass this class rather than implement the {@link
 * uk.ac.ebi.fgpt.conan.model.ConanParameter} interface directly, because this class handles parameter equality between
 * different processes.  The strategy for doing this is simple - parameters of the same class with the same name are
 * assumed to alway be equal.
 *
 * @author Tony Burdett
 * @date 19-Oct-2010
 * @see uk.ac.ebi.fgpt.conan.model.ConanParameter
 */
@JsonSerialize(as = ConanParameter.class)
public abstract class AbstractConanParameter implements ConanParameter {
    private String name;
    private String description;
    private boolean isBooleanParameter;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected AbstractConanParameter(String name) {
        this(name, false);
    }

    protected AbstractConanParameter(String name, boolean isBoolean) {
        this(name, name, isBoolean);
    }

    protected AbstractConanParameter(String name, String description) {
        this(name, description, false);
    }

    protected AbstractConanParameter(String name, String description, boolean isBoolean) {
        this.name = name;
        this.description = description;
        this.isBooleanParameter = isBoolean;
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
        return !StringUtils.containsWhitespace(value) && !value.contains("/") && !value.contains(File.separator);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBoolean() {
        return isBooleanParameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractConanParameter that = (AbstractConanParameter) o;
        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
