package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.WaitCondition;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.util.Collection;

/**
 * A service that can be used to explore or execute the available processes in the Conan framework.  Within Conan, there are
 * potentially many available processes that may be run, and are usually grouped into pipelines.
 * <p/>
 * This interface is used to define the ways known processes can be retrieved or executed
 *
 * @author Tony Burdett
 * @date 11-Oct-2010
 */
public interface ConanProcessService {
    /**
     * Get every process known to Conan.  Users can then potentially use this information to create new pipelines.
     *
     * @return all available processes, irrespective of whether they are used in any pipelines
     */
    Collection<ConanProcess> getAllAvailableProcesses();

    /**
     * Gets the process with the given name.  Processes in Conan must be uniquely named, so this is guaranteed to return
     * a single result, or null if there is no process with this name.
     *
     * @param processName the name of the process to acquire
     * @return the process with this name
     */
    ConanProcess getProcess(String processName);

    /**
     * Execute a defined {@link ConanProcess}.  The proc may be executed in the foreground or the background depending
     * on how the {@link ExecutionContext} is configured.
     * {@link ExecutionContext} and waits for it to complete before returning.
     *
     * @param conanProcess    The {@link ConanProcess} to execute
     * @param executionContext The {@link ExecutionContext} within which to execute the {@link ConanProcess}
     * @throws InterruptedException      Thrown if the executed proc was interrupted during the job
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException Thrown if there were any problems initialising the job or with the job output
     */
    int execute(ConanProcess conanProcess, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException;

    /**
     * Execute a command in the shell.  The proc may be executed in the foreground or the background depending
     * on how the {@link ExecutionContext} is configured. This is used for simple shell commands that do not require any
     * specific proc management.  E.g. for changing directory, or linking files.  For tasks that are non-trivial, i.e
     * tasks that do not complete within a couple of seconds it is recommended that the user creates a ConanXProcess and
     * executes that using the alternative variant of this method.
     *
     * @param command          The shell command to execute
     * @param executionContext The {@link ExecutionContext} within which to execute the shell command.
     * @throws InterruptedException      Thrown if the executed proc was interrupted during the job
     * @throws ProcessExecutionException Thrown if there were any problems initialising the job or with the job output
     */
    int execute(String command, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException;

    /**
     * If a proc or command (or set of processes or commands) was executed in the background, then this method can be
     * called to wait for it (them) to complete.
     *
     * @param waitCondition    The {@link WaitCondition} describing which job(s) to wait for.
     * @param executionContext The {@link ExecutionContext} within which the job(s) to wait for is(are) running.
     * @return The exit code for the job, returned after the job(s) has(have) completed.
     * @throws InterruptedException      Thrown if the wait condition was interrupted before the jobs completed
     * @throws ProcessExecutionException Thrown if there were any problems initialising the wait condition or with the
     *                                   result of waiting for the job(s) to complete
     */
    int waitFor(WaitCondition waitCondition, ExecutionContext executionContext)
            throws InterruptedException, ProcessExecutionException;
}
