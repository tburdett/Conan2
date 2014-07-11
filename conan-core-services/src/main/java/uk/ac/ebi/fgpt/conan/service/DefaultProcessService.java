package uk.ac.ebi.fgpt.conan.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.dao.ConanProcessDAO;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.*;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

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
                log.debug("Added precommand \"" + extPreCommand + "\" from external process configuration file to process \"" + process.getName() + "\"");
            }
        }

        String command;
        try {
            command = process.getFullCommand();
        }
        catch(ConanParameterException cpe) {
            throw new ProcessExecutionException(3, "Could not build command from supplied parameters", cpe);
        }

        return this.execute(command, executionContext);
    }

    @Override
    public ExecutionResult execute(String command, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException {

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

                log.debug("Finished executing command [" + command + "].  " + details);
            }
            else {
                log.info("Running scheduled command in background [" + commandToExecute + "].");
                result = locality.dispatch(commandToExecute, scheduler);
                log.debug("Successfully dispatched command [" + command + "].  Output:\n" +
                        StringUtils.join(result.getOutput(), "\n") + "\n");
            }
        }
        else {

            if (executionContext.isForegroundJob()) {
                log.info("Running command in foreground [" + command + "].");
                result = locality.execute(command, null);

                if (executionContext.getMonitorFile() != null) {
                    try {
                        result.writeOutputToFile(executionContext.getMonitorFile());
                    }
                    catch(IOException e) {
                        throw new ProcessExecutionException(-1, e);
                    }
                }

                String details = result.getOutputFile() != null && result.getOutputFile().exists() ?
                        "Output from this command can be found at: \"" + result.getOutputFile().getAbsolutePath() + "\"" :
                        "Output: \n" + StringUtils.join(result.getOutput(), "\n") + "\n";

                log.debug("Finished executing command [" + command + "].  " + details);
            }
            else {
                throw new UnsupportedOperationException("Can't dispatch unscheduled commands yet");
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

    @Override
    public String makeLinkCommand(File inputFile, File outputFile) {

        return "ln -s -f " + inputFile.getAbsolutePath() + " " + outputFile.getAbsolutePath();
    }

    @Override
    public void createLocalSymbolicLink(File inputFile, File outputFile)
            throws ProcessExecutionException, InterruptedException {

        this.execute(makeLinkCommand(inputFile, outputFile), new DefaultExecutionContext(new Local(), null, null));
    }


    @Override
    public void executeScheduledWait(List<Integer> jobIds, String waitCondition, ExitStatus.Type exitStatusType,
                                     ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException {

        if (!executionContext.usingScheduler())
            throw new UnsupportedOperationException("Cannot dispatch a scheduled wait job without using a scheduler");

        Scheduler scheduler = executionContext.getScheduler();

        String condition = scheduler.generatesJobIdFromOutput() ?
                scheduler.createWaitCondition(exitStatusType, jobIds) :
                scheduler.createWaitCondition(exitStatusType, waitCondition);

        this.waitFor(condition, executionContext);
    }

    /**
     * There maybe better ways to do this, but what this method does is add any pre-commands that are found in the
     * execution context, and then uses the unix 'which' command to see if anything sensible comes back.
     * @param conanProcess The process to check the status of.
     * @param executionContext The execution context this process will run in. This is important because if additional
     *                         pre-commands are normally added to this process, they may be required here to determine
     *                         if the process is operational
     * @return
     */
    @Override
    public boolean isLocalProcessOperational(ConanProcess conanProcess, ExecutionContext executionContext) {

        String preCommand = "";

        if (executionContext.getExternalProcessConfiguration() != null) {
            String extPreCommand = executionContext.getExternalProcessConfiguration().getCommand(conanProcess.getName());

            if (extPreCommand != null && !extPreCommand.isEmpty()) {
                preCommand += extPreCommand + "; ";
                log.debug("Added precommand \"" + extPreCommand + "\" from external process configuration file to process " +
                            "\"" + conanProcess.getName() + "\", which has an executable called: \"" + conanProcess.getExecutable() + "\"");
            }
        }

        return this.executableOnPath(conanProcess.getExecutable(), preCommand, executionContext);
    }

    @Override
    public boolean executableOnPath(String executable, ExecutionContext executionContext) {

        return this.executableOnPath(executable, "", executionContext);
    }

    @Override
    public boolean executableOnPath(String executable, String preCommand, ExecutionContext executionContext) {

        ExecutionResult result = null;
        try {
            result = this.execute(preCommand + "which " + executable, executionContext);
        } catch (Exception e) {
            log.error("Error occurred trying to determine if process was operational: " + e.getMessage());
            return false;
        }

        if (result.getOutput().length == 0)
            return false;

        return !(result.getOutput()[0].contains(" no ") && result.getOutput()[0].contains(" in "));
    }
}
