package uk.ac.ebi.fgpt.conan.core.process;

import uk.ac.ebi.fgpt.conan.model.param.*;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 14/01/14
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractProcessArgs implements ProcessArgs {

    protected ProcessParams params;

    public AbstractProcessArgs(ProcessParams params) {
        this.params = params;
    }

    protected abstract void setOptionFromMapEntry(ConanParameter param, String value);

    protected abstract void setArgFromMapEntry(ConanParameter param, String value);

    @Override
    public void setFromArgMap(ParamMap pvp) throws IOException, ConanParameterException {

        for(ParamMapEntry entry : pvp.getOptionList()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new ConanParameterException("Parameter invalid: " + entry.getKey().getIdentifier() + " : " + entry.getValue());
            }

            this.setOptionFromMapEntry(entry.getKey(), entry.getValue().trim());
        }

        for(ParamMapEntry entry : pvp.getArgList()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new ConanParameterException("Parameter invalid: " + entry.getKey().getIdentifier() + " : " + entry.getValue());
            }

            this.setArgFromMapEntry(entry.getKey(), entry.getValue().trim());
        }
    }
}
