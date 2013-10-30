package uk.ac.ebi.fgpt.conan.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.dao.ConanProcessDAO;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.Locality;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.util.Collection;

/**
 * Simple implementation of a process service that delegates lookup calls to a process DAO.
 *
 * @author Tony Burdett
 * @date 25-Nov-2010
 */
@Service(value="conanProcessService")
public class DefaultProcessService implements ConanProcessService {

    private static Logger log = LoggerFactory.getLogger(DefaultProcessService.class);


    private ConanProcessDAO conanProcessDAO;

    public ConanProcessDAO getConanProcessDAO() {
        return conanProcessDAO;
    }

    public void setConanProcessDAO(ConanProcessDAO conanProcessDAO) {
        this.conanProcessDAO = conanProcessDAO;
    }

    public Collection<ConanProcess> getAllAvailableProcesses() {
        return getConanProcessDAO().getProcesses();
    }

    public ConanProcess getProcess(String processName) {
        return getConanProcessDAO().getProcess(processName);
    }

    @Override
    public ExecutionResult execute(ConanProcess process, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

        if (executionContext.getExternalProcessConfiguration() != null) {
            String extPreCommand = executionContext.getExternalProcessConfiguration().getCommand(process.getName());

            if (extPreCommand != null && !extPreCommand.isEmpty()) {
                process.addPreCommand(extPreCommand);
            }
        }

        return this.execute(process.getFullCommand(), executionContext);
    }

    @Override
    public ExecutionResult execute(String command, ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException {

        Locality locality = executionContext.getLocality();

        if (locality == null) {
            log.warn("No locality specified in execution context.  Will not execute command: " + command);
            return null;
        }

        if (!locality.establishConnection()) {
            throw new ProcessExecutionException(-1, "Could not establish connection to the terminal.  Command " +
                    command + " will not be submitted.");
        }

        ExecutionResult result = null;

        if (executionContext.usingScheduler()) {

            Scheduler scheduler = executionContext.getScheduler();

            String commandToExecute = scheduler.createCommand(command, executionContext.isForegroundJob());

            if (executionContext.isForegroundJob()) {
                log.info("Running scheduled command in foreground [" + commandToExecute + "].");
                result = locality.monitoredExecute(commandToExecute, scheduler);
                String details = result.getOutputFile() != null && result.getOutputFile().exists() ?
                        "Output from this command can be found at: \"" + result.getOutputFile().getAbsolutePath() + "\"" :
                        "Output: \n" + StringUtils.join(result.getOutput(), "\n") + "\n";

                log.info("Finished executing command [" + command + "].   " + details);
            }
            else {
                log.info("Running scheduled command in background [" + commandToExecute + "].");
                result = locality.dispatch(commandToExecute, scheduler);
                log.info("Successfully dispatched command [" + command + "].  Output:\n" +
                        StringUtils.join(result.getOutput(), "\n") + "\n");
            }
        }
        else {

            if (executionContext.isForegroundJob()) {
                log.info("Running command in foreground [" + command + "].");
                result = locality.execute(command, null);
                log.info("Finished executing command [" + command + "].   Output: \n" +
                        StringUtils.join(result.getOutput(), "\n"));
            } else {
                throw new UnsupportedOperationException("Can't dispatch simple commands yet");
            }
        }

        if (!locality.disconnect()) {
            throw new ProcessExecutionException(-1, "Command was submitted but could not disconnect the terminal session.  Future jobs may not work.");
        }

        return result;
    }


    @Override
    public ExecutionResult waitFor(String waitCondition, ExecutionContext executionContext) throws InterruptedException, ProcessExecutionException {

        if (!executionContext.usingScheduler()) {
            throw new UnsupportedOperationException("Can't wait for non-scheduled tasks yet");
        }

        Scheduler scheduler = executionContext.getScheduler();

        String waitCommand = scheduler.createWaitCommand(waitCondition);

        return executionContext.getLocality().monitoredExecute(waitCommand, scheduler);
    }
}
