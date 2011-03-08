package uk.ac.ebi.fgpt.conan.process.demo;

import net.sourceforge.fluxion.spi.ServiceProvider;
import uk.ac.ebi.fgpt.conan.ae.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.ae.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * A demo process that runs on the LSF cluster
 *
 * @author Tony Burdett
 * @date 02-Nov-2010
 */
@ServiceProvider
public class LSFDemoProcess extends AbstractLSFProcess {
    private final Collection<ConanParameter> params;

    public LSFDemoProcess() {
        params = new ArrayList<ConanParameter>();
        params.add(new DemoProcessParameter("demo parameter 1"));
    }

    public String getName() {
        return "demo process (executes on LSF)";
    }

    public Collection<ConanParameter> getParameters() {
        return params;
    }

    @Override
    protected String getComponentName() {
        return LSFProcess.UNSPECIFIED_COMPONENT_NAME;
    }

    @Override
    protected String getCommand(Map<ConanParameter, String> parameterStringMap) {
        return "sleep 300; echo \\\"Hello world\\!\\\"";
    }

    @Override
    protected String getLSFOutputFilePath(Map<ConanParameter, String> parameterStringMap) {
        return "/homes/tburdett/lsf-test/lsfDemoProcess.lsfOutput.txt";
    }
}
