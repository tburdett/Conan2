package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 23/01/14
 * Time: 16:32
 * To change this template use File | Settings | File Templates.
 */
public class DefaultExecutorService implements ConanExecutorService {

    protected ConanProcessService conanProcessService;
    protected ExecutionContext executionContext;

    public DefaultExecutorService() {
        this(null, null);
    }

    public DefaultExecutorService(ConanProcessService conanProcessService, ExecutionContext executionContext) {
        this.initialise(conanProcessService, executionContext);
    }

    @Override
    public void initialise(ConanProcessService conanProcessService, ExecutionContext executionContext) {
        this.conanProcessService = conanProcessService;
        this.executionContext = executionContext;
    }

    @Override
    public void executeScheduledWait(List<Integer> jobIds, String waitCondition, ExitStatus.Type exitStatusType, String jobName, File outputDir)
            throws ProcessExecutionException, InterruptedException {

        // Duplicate the execution context so we don't modify the original accidentally.
        ExecutionContext executionContextCopy = executionContext.copy();
        executionContextCopy.setContext(jobName, true, new File(outputDir, jobName + ".log"));

        this.conanProcessService.executeScheduledWait(jobIds, waitCondition, exitStatusType, executionContextCopy);
    }

    @Override
    public ExecutionResult executeProcess(ConanProcess process, File outputDir, String jobName, int threads,
                                          int memoryMb, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException {

        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(jobName, !runParallel,
                new File(outputDir, jobName + ".log"));

        if (executionContextCopy.usingScheduler()) {
            SchedulerArgs sArgs = executionContext.getScheduler().getArgs();
            sArgs.setThreads(threads);
            sArgs.setMemoryMB(memoryMb);
        }

        return this.conanProcessService.execute(process, executionContextCopy);
    }

    @Override
    public ExecutionResult executeProcess(String command, File outputDir, String jobName, int threads, int memoryMb, boolean runParallel) throws InterruptedException, ProcessExecutionException, ConanParameterException {

        ExecutionContext executionContextCopy = this.executionContext.copy();
        executionContextCopy.setContext(jobName, !runParallel,
                new File(outputDir, jobName + ".log"));

        if (executionContextCopy.usingScheduler()) {
            SchedulerArgs sArgs = executionContext.getScheduler().getArgs();
            sArgs.setThreads(threads);
            sArgs.setMemoryMB(memoryMb);
        }

        return this.conanProcessService.execute(command, executionContextCopy);
    }

    @Override
    public boolean usingScheduler() {
        return this.executionContext.usingScheduler();
    }

    @Override
    public ExecutionContext getExecutionContext() {
        return this.executionContext;
    }

    @Override
    public ConanProcessService getConanProcessService() {
        return this.conanProcessService;
    }
}
