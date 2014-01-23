package uk.ac.ebi.fgpt.conan.model.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 13/01/14
 * Time: 13:52
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractProcessParams implements ProcessParams {

    private boolean posixFormat;

    public AbstractProcessParams() {
        this.posixFormat = true;
    }

    public abstract ConanParameter[] getConanParametersAsArray();

    @Override
    public List<ConanParameter> getConanParameters() {
        return new ArrayList<>(Arrays.asList(this.getConanParametersAsArray()));
    }
}
