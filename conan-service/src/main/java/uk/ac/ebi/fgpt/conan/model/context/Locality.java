
package uk.ac.ebi.fgpt.conan.model.context;

import uk.ac.ebi.fgpt.conan.model.monitor.ProcessAdapter;
import uk.ac.ebi.fgpt.conan.model.monitor.ProcessListener;
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
     * @return The exitCode from the proc that was executed
     * @throws InterruptedException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     *
     */
    int execute(String command)
            throws ProcessExecutionException, InterruptedException;

    /**
     * Intended for executing Scheduled Tasks in the foreground.  A {@link ProcessAdapter} is used to monitor progress of
     * the proc.
     *
     * @param command        The command to execute.
     * @param processAdapter The {@link ProcessAdapter} which should monitor Task progress
     * @param processListener The {@link ProcessListener} which should monitor Task completion
     * @return The exitCode from the proc that was executed
     * @throws InterruptedException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     *
     */
    int monitoredExecute(String command, ProcessAdapter processAdapter, ProcessListener processListener)
            throws InterruptedException, ProcessExecutionException;

    /**
     * Executes the supplied command using the supplied args, on the requested scheduler at the locality indicated
     * by this object.  Will dispatch the command and leave it running in the background.  This is typically used when
     * the user wants to execute multiple command in parallel.
     *
     * @param command The command that is to be executed in the background
     * @throws InterruptedException
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     *
     */
    void dispatch(String command)
            throws ProcessExecutionException, InterruptedException;

    /**
     * If a proc was dispatched and is executing in the background, then the user may want to wait for that proc,
     * or processes, to complete before continueing.
     *
     * @param waitCondition The waitCondition that needs to be satisfied before continueing.
     * @return The exit code produced after the wait condition has been satisfied.
     * @throws uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException
     *
     * @throws InterruptedException
     * @throws java.io.IOException
     */
    int waitFor(WaitCondition waitCondition)
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
}
