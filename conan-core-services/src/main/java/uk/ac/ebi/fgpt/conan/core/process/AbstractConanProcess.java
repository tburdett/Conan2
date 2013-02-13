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
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.model.param.ProcessParams;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: maplesod
 * Date: 12/02/13
 * Time: 18:48
 */
public abstract class AbstractConanProcess implements ConanProcess {

    @Autowired
    protected ConanProcessService conanProcessService;

    private ProcessArgs processArgs;
    private ProcessParams processParams;

    private List<String> preCommands;
    private List<String> postCommands;
    private String executable;

    protected AbstractConanProcess() {
        this("", null, null);
    }

    protected AbstractConanProcess(String executable, ProcessArgs args, ProcessParams params) {
        this.processArgs = args;
        this.processParams = params;
        this.executable = executable;
        this.preCommands = new ArrayList<String>();
        this.postCommands = new ArrayList<String>();
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
            this.preCommands = new ArrayList<String>();
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
    public String getFullCommand() {

        List<String> commands = new ArrayList<String>();

        if (this.preCommands != null && !this.preCommands.isEmpty()) {
            commands.add(this.getPreCommand());
        }

        commands.add(this.getCommand());

        if (this.postCommands != null && !this.postCommands.isEmpty()) {
            commands.add(this.getPostCommand());
        }

        String command = StringUtils.join(commands, "; ");

        return command;
    }


    protected String getCommand(ProcessArgs args) {
        return getCommand(args, false);
    }

    protected String getCommand(ProcessArgs args, boolean build) {
        return getCommand(args, build, "--", " ");
    }

    /**
     * Creates a command line to execute from the supplied proc args.  The are various means of constructing the command
     * line to suit various use cases.  See the param listings for more details.  The command line will be wrapped by
     * any pre and post commands already stored in this object.
     *
     * @param args        The proc arguments from which to build a command line to execute.
     * @param build       If true, the paramPrefix and keyValSep args are used to construct each arg in the following way:
     *                    [paramPrefix][paramName][keyValSep][argValue]. If false, it is assumed the the arg values contains all
     *                    the complete argument string.
     * @param paramPrefix The prefix to apply before each argument.  e.g. "--" is used for long posix style arguments.
     * @param keyValSep   The string that separates the param name from the arg value. e.g. " " is used for posix style arguments.
     * @return A complete command to execute, including any pre or post commands.
     */
    protected String getCommand(ProcessArgs args, boolean build, String paramPrefix, String keyValSep) {

        List<String> commands = new ArrayList<String>();

        StringBuilder sb = new StringBuilder();
        sb.append(this.executable);
        sb.append(" ");
        for (Map.Entry<ConanParameter, String> param : args.getArgMap().entrySet()) {
            if (build) {
                sb.append(paramPrefix);
                sb.append(param.getKey());
            }
            if (!param.getKey().isBoolean()) {
                sb.append(keyValSep);
                sb.append(param.getValue());
            }
            sb.append(" ");
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
    public boolean execute(ExecutionContext executionContext) throws ProcessExecutionException, InterruptedException {

        int exitCode = this.conanProcessService.execute(this, executionContext);

        return exitCode == 0;
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException, InterruptedException {

        return this.execute(parameters, new DefaultExecutionContext());
    }

    @Override
    public boolean execute(Map<ConanParameter, String> parameters, ExecutionContext executionContext)
            throws ProcessExecutionException, InterruptedException {

        this.processArgs.setFromArgMap(parameters);

        return this.execute(executionContext);
    }

}
