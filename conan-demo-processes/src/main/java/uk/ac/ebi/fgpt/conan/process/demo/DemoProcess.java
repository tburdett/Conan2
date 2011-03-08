package uk.ac.ebi.fgpt.conan.process.demo;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * A demonstration conan process that has a name and a type and requires a single parameter.  This process doesn't
 * actually do anything except wait for 15 seconds (and prints some dots to stdout)
 *
 * @author Tony Burdett
 * @date 13-Aug-2010
 */
@ServiceProvider
public class DemoProcess implements ConanProcess {
    private final Collection<ConanParameter> params;

    private Logger log = LoggerFactory.getLogger(getClass());

    public DemoProcess() {
        params = new ArrayList<ConanParameter>();
        params.add(new DemoProcessParameter("demo parameter 1"));
    }

    protected Logger getLog() {
        return log;
    }

    public boolean execute(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException, ProcessExecutionException, InterruptedException {
        getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());
        System.out.print("Waiting");
        for (int i = 0; i < 15; i++) {
            synchronized (this) {
                wait(1000);
                System.out.print(".");
            }
        }
        System.out.println("done!");
        return true;
    }

    public String getName() {
        return "demo process";
    }

    public Collection<ConanParameter> getParameters() {
        return params;
    }
}
