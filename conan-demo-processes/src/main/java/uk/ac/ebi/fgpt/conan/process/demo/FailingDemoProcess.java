package uk.ac.ebi.fgpt.conan.process.demo;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.util.Map;

/**
 * A demonstration conan process that has a name and a type and always fails.
 *
 * @author Tony Burdett
 * @date 19-Oct-2010
 */
@ServiceProvider
public class FailingDemoProcess extends DemoProcess {
    public boolean execute(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException, ProcessExecutionException, InterruptedException {
        String msg = "Failed at " + getName();

        System.out.print("Waiting");
        for (int i = 0; i < 15; i++) {
            synchronized (this) {
                wait(1000);
                System.out.print(".");
            }
        }
        System.out.println("done!");

        getLog().error(msg);
        throw new ProcessExecutionException(1, msg);
    }

    public String getName() {
        return "demo process (always fails)";
    }
}
