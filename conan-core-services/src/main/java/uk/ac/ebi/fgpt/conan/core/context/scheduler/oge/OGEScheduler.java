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
package uk.ac.ebi.fgpt.conan.core.context.scheduler.oge;

import uk.ac.ebi.fgpt.conan.core.context.scheduler.AbstractScheduler;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.model.monitor.ProcessAdapter;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;

import java.io.File;
import java.util.List;

public class OGEScheduler extends AbstractScheduler {

    public static final String QSUB = "qsub";

    public OGEScheduler() {
        this(new OGEArgs());
    }

    public OGEScheduler(OGEArgs args) {
        super(QSUB, args);
    }

    @Override
    public ProcessAdapter createProcessAdapter(File monitorFile, int monitorInterval) {
        return null;
    }

    @Override
    public String createCommand(String internalCommand, boolean isForegroundJob) {

        // Create command to execute
        String commandPart = "echo \"" + internalCommand + "\"";

        // create PBS part
        StringJoiner pbsPartJoiner = new StringJoiner(" ");
        pbsPartJoiner.add(this.getSubmitCommand());
        pbsPartJoiner.add(this.getArgs() != null, "", ((OGEArgs)this.getArgs()).toString(isForegroundJob));

        String pbsPart = pbsPartJoiner.toString();

        return commandPart + " | " + pbsPart;
    }

    @Override
    public String createWaitCommand(String waitCondition) {

        // Create command to execute
        /*String commandPart = "echo \"sleep 1 2>&1\"";

        StringJoiner sj = new StringJoiner(" ");
        sj.add("echo \"sleep 1 2>&1\" | ");
        sj.add(this.getSubmitCommand());
        sj.add("-W block=true," + waitCondition);
        sj.add("-q", this.getArgs().getQueueName());
        sj.add("-eo", this.getArgs().getMonitorFile());

        return sj.toString();*/

        throw new UnsupportedOperationException("Can't create wait/hold commands for OGE yet");
    }

    @Override
    public String createKillCommand(String jobId) {
        return "qdel " + jobId;
    }

    @Override
    public String createWaitCondition(ExitStatus.Type exitStatus, String condition) {
        throw new UnsupportedOperationException("Haven't implemented wait conditions for OGE yet");
    }

    @Override
    public String createWaitCondition(ExitStatus.Type exitStatus, List<Integer> jobIds) {

        throw new UnsupportedOperationException("Haven't implemented wait conditions for OGE yet");
    }

    @Override
    public Scheduler copy() {
        //TODO Not too nice... shouldn't really use casting here but it will always give the right result.  To tidy up late.
        return new OGEScheduler(new OGEArgs((OGEArgs) this.getArgs()));
    }

    @Override
    public String getName() {
        return "OGE";
    }

    @Override
    public boolean usesFileMonitor() {
        return false;
    }

    @Override
    public boolean generatesJobIdFromOutput() {

        //TODO maybe change this later
        return false;
    }

    @Override
    public int extractJobIdFromOutput(String line) {

        /*String[] parts = line.split("\\.");

        if (parts.length >= 2) {
            return Integer.parseInt(parts[0]);
        }

        throw new IllegalArgumentException("Could not extract OGE job id from: " + line);*/

        throw new UnsupportedOperationException("Don't know if we can extract job ids from submission output yet");
    }

}
