package uk.ac.ebi.fgpt.conan.core.param;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMapEntry;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 14/01/14
 * Time: 11:13
 * To change this template use File | Settings | File Templates.
 */
public class DefaultParamMapEntry implements ParamMapEntry {

    private ConanParameter param;
    private String value;

    public DefaultParamMapEntry(ConanParameter param, String value) {
        this.param = param;
        this.value = value;
    }

    @Override
    public ConanParameter getKey() {
        return param;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(String value) {

        this.value = value;
        return value;
    }
}
