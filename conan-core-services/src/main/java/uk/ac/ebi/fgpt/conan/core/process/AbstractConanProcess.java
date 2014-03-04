/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.ebi.fgpt.conan.core.process;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.param.*;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.DefaultExecutorService;
import uk.ac.ebi.fgpt.conan.service.exception.ConanParameterException;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 18:48
 */
@Component
@Scope("prototype")
public abstract class AbstractConanProcess implements ConanProcess {

    @Autowired
    protected ConanExecutorService conanExecutorService;


    private ProcessArgs processArgs;
    private ProcessParams processParams;

    private List<String> preCommands;
    private List<String> postCommands;
    private String executable;
    private String mode;

    private int jobId;

    protected AbstractConanProcess() {
        this("", null, null);
    }

    public AbstractConanProcess(String executable, ProcessArgs args, ProcessParams params) {
        this(executable, args, params, new DefaultExecutorService());
    }

    public AbstractConanProcess(String executable, ProcessArgs args, ProcessParams params, ConanExecutorService conanExecutorService) {
        this.processArgs = args;
        this.processParams = params;
        this.executable = executable;
        this.mode = "";
        this.preCommands = new ArrayList<>();
        this.postCommands = new ArrayList<>();
        this.jobId = -1;
        this.conanExecutorService = conanExecutorService;
    }

    public ConanProcessService getConanProcessService() {
        return this.conanExecutorService.getConanProcessService();
    }

    public void setConanProcessService(ConanProcessService conanProcessService) {
        this.conanExecutorService.setConanProcessService(conanProcessService);
    }

    @Override
    public String getExecutable() {
        return this.executable;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public ProcessArgs getProcessArgs() {
        return processArgs;
    }

    public void setProcessArgs(ProcessArgs processArgs) {
        this.processArgs = processArgs;
    }

    public ProcessParams getProcessParams() {
        return processParams;
    }

    public void setProcessParams(ProcessParams processParams) {
        this.processParams = processParams;
    }

    public String getPreCommand() {
        return StringUtils.join(preCommands, "; ");
    }

    public List<String> getPreCommands() {
        return preCommands;
    }

    public void setPreCommands(List<String> preCommands) {
        this.preCommands = preCommands;
    }

    @Override
    public void addPreCommand(String preCommand) {
        if (this.preCommands == null) {
            this.preCommands = new ArrayList<>();
        }
        this.preCommands.add(preCommand);
    }

    public String getPostCommand() {
        return StringUtils.join(postCommands, "; ");
    }

    public List<String> getPostCommands() {
        return postCommands;
    }

    @Override
    public void addPostCommand(String postCommand) {
        if (this.postCommands == null) {
            this.postCommands = new ArrayList<String>();
        }
        this.postCommands.add(postCommand);
    }

    @Override
    public String getFullCommand() throws ConanParameterException {

        List<String> commands = new ArrayList<String>();

        if (this.preCommands != null && !this.preCommands.isEmpty()) {
            commands.add(this.getPreCommand());
        }

        commands.add(this.getCommand() + " 2>&1");

        if (this.postCommands != null && !this.postCommands.isEmpty()) {
            commands.add(this.getPostCommand());
        }

        String command = StringUtils.join(commands, "; ");

        return command;
    }


    @Override
    public String getCommand() throws ConanParameterException {
        return getCommand(CommandLineFormat.POSIX);
    }

    /**
     * Creates a command line to execute from the supplied proc args.  The are various means of constructing the command
     * line to suit various use cases.  See the param listings for more details.
     *
     * @param format      The format to apply to the command line options.
     * @return The command to execute, excluding any pre or post commands.
     */
    protected String getCommand(CommandLineFormat format) throws ConanParameterException {

        // Ensure all parameters are valid before we try to make a command
        this.processArgs.getArgMap().validate(this.processParams);

        List<String> commands = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append(this.executable);

        // Add the mode, if this process has one.
        if (!this.mode.isEmpty()) {
            sb.append(" ").append(this.mode);
        }

        // Add the options
        String options = this.processArgs.getArgMap().buildOptionString(format).trim();
        if (!options.isEmpty()) {
            sb.append(" ").append(options);
        }

        // Add the arguments
        String args = this.processArgs.getArgMap().buildArgString().trim();
        if (!args.isEmpty()) {
            sb.append(" ").append(args);
        }

        // Add redirection string
        String redirection = this.processArgs.getArgMap().buildRedirectionString().trim();
        if (!redirection.isEmpty()) {
            sb.append(" > ").append(redirection);
        }

        commands.add(sb.toString().trim());

        String command = StringUtils.join(commands, "; ");

        return command;
    }

    @Override
    public Collection<ConanParameter> getParameters() {
        return this.processParams.getConanParameters();
    }

    @Override
    public boolean execute()
            throws ProcessExecutionException, InterruptedException {

        return this.execute(this.conanExecutorService.getExecutionContext());
    }

    @Override
    public boolean execute(ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException {

        ExecutionResult result = this.conanExecutorService.getConanProcessService().execute(this, executionContext);;

        this.jobId = result.getJobId();

        return result.getExitCode() == 0;
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters)
            throws ProcessExecutionException, InterruptedException {

        return this.execute(parameters, new DefaultExecutionContext());
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException {

        // Ignore for the time being... think the original way of setting arguments doesn't make sense.
        //this.processArgs.setFromArgMap(parameters);

        return this.execute(executionContext);
    }

    @Override
    public int getJobId() {
        return this.jobId;
    }

    @Override
    public boolean isOperational(ExecutionContext executionContext) {
        return this.getConanProcessService().isLocalProcessOperational(this, executionContext);
    }

}
