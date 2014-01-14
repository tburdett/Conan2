package uk.ac.ebi.fgpt.conan.core.process;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
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


    @Override
    public String getName() {
        return process.getName();
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return process.getParameters();
    }

    @Override
    public String getCommand() throws ConanParameterException {
        return process.getCommand();
    }

    @Override
    public String getFullCommand() throws ConanParameterException {
        return process.getFullCommand();
    }

    @Override
    public void addPreCommand(String preCommand) {
        this.process.addPreCommand(preCommand);
    }

    @Override
    public void addPostCommand(String postCommand) {
        this.process.addPostCommand(postCommand);
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters)
            throws ProcessExecutionException, IllegalArgumentException, InterruptedException {
        return process.execute(parameters);
    }

    @Override
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {
        return process.execute(executionContext);
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters, ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {
        return process.execute(parameters, executionContext);
    }

    @Override
    public int getJobId() {
        return process.getJobId();
    }

}
