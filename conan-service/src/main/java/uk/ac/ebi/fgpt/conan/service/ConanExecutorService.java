package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;
import java.util.List;

/**
 * This interface provides a useful level of abstraction away from the process so that we can test the process wrapping
 * code without actually executing under lying process.  In addition, this provides some useful scheduling abstraction
 * so that we can execute child conan processes in parallel within a single master conan process.  It's possible to wait
 * until the child processes have finished by using the executeScheduledWait method
 */
public interface ConanExecutorService {

    /**
     * Initialises this executor.  Needs a process service and an execution context.
     * @param conanProcessService The conan process service that will used to execute the underlying processes within
     *                            the given execution context.
     * @param executionContext The execution context which defines where and how a ConanProcess is executed
     */
    void initialise(ConanProcessService conanProcessService, ExecutionContext executionContext);

    /**
     * This executes a waiting job for the scheduler defined in the execution context.  It will wait until either the
     * list of other scheduled jobs denoted by their IDs have concluded and in which state.  We supply the jobname and
     * the output directory so the the log for this waitjob is put into a sensible place and is called a sensible name.
     * @param jobIds The job ids that should be completed.
     * @param waitCondition If the scheduler doesn't use job ids we can also try waiting by job name (Note: this can be
     *                      unreliable when running lots of jobs in parallel on the same system!)
     * @param exitStatusType The exit condition that must be satisfied
     * @param jobName The jobname
     * @param outputDir The output directory
     * @throws ProcessExecutionException
     * @throws InterruptedException
     */
    void executeScheduledWait(List<Integer> jobIds, String waitCondition, ExitStatus.Type exitStatusType,
                              String jobName, File outputDir)
            throws ProcessExecutionException, InterruptedException;

    /**
     * Executes a conan process within the defined execution context.  We can supply resource data for use by the
     * scheduler (if requested).  The runParallel options allows us to execute this job in parallel with other jobs.  i.e
     * we don't wait for the job to complete before returning.  In this case the method returns an ExecutionResult object
     * which will contain the allocated jobId which the client can manually track and use with the executeScheduledWait
     * command to control where the program flow should pause until the job's completion.
     * @param process The process to execute
     * @param outputDir Where output from this process should go
     * @param jobName The schedulers job name
     * @param threads The threads to request from the scheduler
     * @param memoryMb The memory to request from the scheduler
     * @param runParallel Whether to run this job in the foreground or the background
     * @return An executionResult object containing the job id (if run on a scheduler) and the jobs standard output.
     * @throws InterruptedException
     * @throws ProcessExecutionException
     * @throws ConanParameterException
     */
    ExecutionResult executeProcess(ConanProcess process, File outputDir, String jobName, int threads,
                                   int memoryMb, boolean runParallel)
            throws InterruptedException, ProcessExecutionException, ConanParameterException;
}
