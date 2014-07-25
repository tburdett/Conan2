package uk.ac.ebi.fgpt.conan.model;

//import net.sourceforge.fluxion.spi.Spi;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * A process is a specific job that must be run as part of a {@link ConanPipeline}.  Each pipeline chains together
 * several processes, and a {@link ConanTask} executes each process in turn.
 * <p/>
 * Processes can either be created programmatically or generated automatically from metadata, where available. This
 * interface follows the Java Service Provider Interface specification allowing implementations of Process to be
 * dynamically discovered.  See the <a href="http://download.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html">Java
 * documentation</a> for more details on this.  Implementing classes should be annotated with an {
 * net.sourceforge.fluxion.spi.ServiceProvider} annotation to enable them for discovery.
 * <p/>
 * Note that because <code>Process</code>es are always programmatically instantiated (for example, using a {@link
 * java.util.ServiceLoader}), all processes should have a default constructor.
 *
 * @author Tony Burdett + Dan Mapleson
 * @date 28-Jul-2010
 * @see uk.ac.ebi.fgpt.conan.model.ConanPipeline
 * @see uk.ac.ebi.fgpt.conan.model.param.ConanParameter
 * @see uk.ac.ebi.fgpt.conan.model.ConanTask
 */
@JsonSerialize(typing = JsonSerialize.Typing.STATIC)
//@Spi
public interface ConanProcess extends Serializable {


    /**
     * Get the name of the executable represented by this process.
     *
     * @return The name of the executable, or null or empty string if this process doesn't wrap an external tool.
     */
    String getExecutable();

    /**
     * Get the command to execute for this <code>ConanProcess</code>
     *
     * @return The command to execute
     *
     * @throws ConanParameterException if there was any problems building the command line options
     */
    String getCommand() throws ConanParameterException;

    /**
     * Get the command to execute including and pre and post commands as a single compound command to execute
     *
     * @return The full compound command to execute for this <code>ConanProcess</code>
     */
    String getFullCommand() throws ConanParameterException;

    /**
     * Add an additional command to execute before running the main proc.  This command should be prepended to the list
     * of pre-commands, so that it occurs first.
     *
     * @param preCommand The command to execute before running this proc.
     */
    void prependPreCommand(String preCommand);

    /**
     * Add an additional command to execute before running the main proc.  This pre-command will be appended to the end
     * of the pre-command list
     *
     * @param preCommand The command to execute before running this proc.
     */
    void addPreCommand(String preCommand);

    /**
     * Add an additional command to execute after running the main proc.  This command should be prepended to the list
     * of post-commands, so that it occurs before all other post-commands.
     *
     * @param postCommand The command to execute after running this proc.
     */
    void prependPostCommand(String postCommand);

    /**
     * Add an additional command to execute after running the main proc.  This post-command will be appended to the end
     * of the post-command list
     *
     * @param postCommand The command to execute after running this proc.
     */
    void addPostCommand(String postCommand);

    /**
     * Executes this process with the managed {@link uk.ac.ebi.fgpt.conan.model.context.ExecutionContext}.  Assumes the
     * process arguments have all been set
     *
     * @return Whether this <code>ConanProcess</code> executed successfully or not.
     */
    boolean execute()
            throws ProcessExecutionException, InterruptedException, ConanParameterException;

    /**
     * Executes this process within a defined {@link uk.ac.ebi.fgpt.conan.model.context.ExecutionContext}.  Assumes the
     * process arguments have all been set
     *
     * @param executionContext The execution context within which to execute this <code>ConanProcess</code>
     * @return Whether this <code>ConanProcess</code> executed successfully or not.
     */
    boolean execute(ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException, ConanParameterException;

    /**
     * Executes this process within a defined {@link ExecutionContext}, provides the process with arguments.
     *
     * @param executionContext The execution context within which to execute this <code>ConanProcess</code>
     * @return Whether this <code>ConanProcess</code> executed successfully or not.
     */
    boolean execute(Map<ConanParameter, String> params, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException, ConanParameterException;

    /**
     * Executes this process with the supplied parameters.  The thread in which a <code>ConanProcess</code> is running
     * can legitimately be interrupted with a shutdown request to the Conan system.  <code>ConanProcess</code>es should
     * therefore always respond to interruptions - either by throwing an {@link InterruptedException} or with periodical
     * checks to <code>Thread.interrupted()</code> - allowing an interrupt to kill any running processes.  Failure to do
     * so will prevent Conan shutting down gracefully and may result in incomplete tasks.  Likewise, long running
     * processes should utilise some mechanism to ensure they do not block for extended periods of time, preventing
     * shutdown, either by shelling out to a new background operating system process so the process can complete whilst
     * Conan has shutdown or by periodically checking for interrupts and rolling back any incomplete changes.
     * <p/>
     * All ConanProcess implementations must guard against duplicate submissions themselves, where this should not be
     * allowed.  How this is done is entirely up to the implementation.  Any processes that opt to shell out to OS
     * processes must, if re-executed (for example, after a failure of the Conan system) with the same parameters whilst
     * still running, resume monitoring the existing process instead of creating a new one.
     * <p/>
     * This method throws a ProcessExecutionException for any failure condition that has been checked by the {@link
     * ConanProcess} implementation.  This exception type is used to convey richer information to the user than simple
     * true/false exit status, so it is preferable for implementations to throw this exception wherever possible.
     * However, this method also returns a true/false flag indicating success, so it is legal for implementations to
     * catch and swallow any exceptions before returning false.
     *
     * @param parameters maps parameters to the supplied values required in order to execute a process
     * @return true if the execution completed successfully, false if not
     * @throws ProcessExecutionException if the execution of the process caused an exception
     * @throws IllegalArgumentException  if an incorrect set of parameter values has been supplied, or if required
     *                                   values are null
     * @throws InterruptedException      if the execution of a process is interrupted, which causes it to terminate
     *                                   early
     */
    boolean execute(Map<ConanParameter, String> parameters)
            throws ProcessExecutionException, IllegalArgumentException, InterruptedException, ConanParameterException;

    /**
     * Returns the name of this process.  This should be something human-readable, as this process name is what the
     * graphical interface will present to users.
     *
     * @return the name of this process
     */
    String getName();

    /**
     * Returns a collection of strings representing the names of the parameters that must be supplied in order to
     * execute this process.
     *
     * @return the parameter names required to generate a task
     */
    Collection<ConanParameter> getParameters();


    /**
     * Returns the job id for this process, if allocated by a scheduler, otherwise returns -1
     * @return
     */
    int getJobId();


    /**
     * A method to check whether this process is currently in a state where it should run in the current execution context
     *
     * @param executionContext The execution context within which to execute this <code>ConanProcess</code>
     *
     * @return True if this process should run in this execution context, false otherwise.
     */
    boolean isOperational(ExecutionContext executionContext);
}
