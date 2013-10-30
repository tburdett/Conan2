
package uk.ac.ebi.fgpt.conan.model.context;

import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

public interface Locality {

    /**
     * Establish a connection to the execution environment, which will be used for executing any supplied commands.
     *
     * @return true if connection was established, otherwise false.
     */
    boolean establishConnection();

    /**
     * Executes the supplied command using the supplied args directly, at the locality indicated by this object.  Will
     * wait until the command has completed before returning with the exitCode produced from executing
     * the command.
     *
     * @param command The command that is to be executed in the foreground
     * @param scheduler      The {@link Scheduler} which may have its own custom way of monitoring the tasks progress
     * @return The exitCode from the proc that was executed
     * @throws InterruptedException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     *
     */
    ExecutionResult execute(String command, Scheduler scheduler)
            throws ProcessExecutionException, InterruptedException;

    /**
     * Intended for executing Scheduled Tasks in the foreground.
     *
     * @param command        The command to execute.
     * @param scheduler      The {@link Scheduler} which may have its own custom way of monitoring the tasks progress
     * @return The exitCode from the proc that was executed
     * @throws InterruptedException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     *
     */
    ExecutionResult monitoredExecute(String command, Scheduler scheduler)
            throws InterruptedException, ProcessExecutionException;

    /**
     * Executes the supplied command using the supplied args, on the requested scheduler at the locality indicated
     * by this object.  Will dispatch the command and leave it running in the background.  This is typically used when
     * the user wants to execute multiple command in parallel.
     *
     * @param command The command that is to be executed in the background
     * @param scheduler      The {@link Scheduler} which may have its own custom way of monitoring the tasks progress
     * @throws InterruptedException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     *
     */
    ExecutionResult dispatch(String command, Scheduler scheduler)
            throws ProcessExecutionException, InterruptedException;

    /**
     * Disconnect from the terminal after use.
     *
     * @return true if disconnected successfully otherwise false
     */
    boolean disconnect();


    /**
     * Returns a deep copy of this locality
     *
     * @return A deep copy of this locality
     */
    Locality copy();

    /**
     * Returns the description for this locality
     * @return
     */
    String getDescription();
}
