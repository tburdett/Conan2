package uk.ac.ebi.fgpt.conan.process.atlas;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.util.Collection;
import java.util.Map;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 15/02/11
 */
@ServiceProvider
public class IndexBuilderProcess implements ConanProcess {
    public boolean execute(Map<ConanParameter, String> parameters)
            throws ProcessExecutionException, IllegalArgumentException, InterruptedException {
        return false;
    }

    public String getName() {
        return null;
    }

    public Collection<ConanParameter> getParameters() {
        return null;
    }
}
