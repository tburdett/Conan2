package uk.ac.ebi.fgpt.conan.core.param;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 13/01/14
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */
public class ParameterBuilder {

    private DefaultConanParameter parameter;

    public ParameterBuilder() {
        this.parameter = new DefaultConanParameter();
    }

    public DefaultConanParameter create() {
        return this.parameter;
    }

    public ParameterBuilder shortName(String shortName) {
        this.parameter.name = shortName;
        return this;
    }

    public ParameterBuilder longName(String longName) {
        this.parameter.longName = longName;
        return this;
    }

    public ParameterBuilder description(String description) {
        this.parameter.description = description;
        return this;
    }

    public ParameterBuilder isFlag(boolean isFlag) {
        this.parameter.isFlag = isFlag;
        return this;
    }

    public ParameterBuilder isOption(boolean isOption) {
        this.parameter.paramType = isOption ? DefaultConanParameter.ParamType.OPTION : DefaultConanParameter.ParamType.ARGUMENT;
        return this;
    }

    public ParameterBuilder isOptional(boolean isOptional) {
        this.parameter.isOptional = isOptional;
        return this;
    }

    public ParameterBuilder argIndex(int argIndex) {
        this.parameter.argIndex = argIndex;
        return this;
    }

    public ParameterBuilder argValidator(ArgValidator argValidator) {
        this.parameter.argValidator = argValidator;
        return this;
    }

    public ParameterBuilder type(DefaultConanParameter.ParamType type) {
        this.parameter.paramType = type;
        return this;
    }
}
