package uk.ac.ebi.fgpt.conan.core.process;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.util.Collection;
import java.util.Map;

/**
 * An abstract implementation of the decorator pattern for decorating {@link ConanProcess}es with additional
 * functionality.  This abstract class should be subclassed anywhere where there is a requirement to enhance standard
 * processes discovered by SPI with additional features.
 *
 * @author Tony Burdett
 * @date 09/09/11
 */
public abstract class AbstractProcessDecorator implements ConanProcess {
    private ConanProcess process;

    public AbstractProcessDecorator(ConanProcess process) {
        this.process = process;
    }

    public boolean execute(Map<ConanParameter, String> parameters)
            throws ProcessExecutionException, IllegalArgumentException, InterruptedException {
        return process.execute(parameters);
    }

    public String getName() {
        return process.getName();
    }

    public Collection<ConanParameter> getParameters() {
        return process.getParameters();
    }
}
