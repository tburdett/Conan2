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
package uk.ac.ebi.fgpt.conan.core.context.scheduler.lsf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.context.scheduler.AbstractScheduler;
import uk.ac.ebi.fgpt.conan.model.context.ExitStatus;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.model.context.WaitCondition;
import uk.ac.ebi.fgpt.conan.model.monitor.ProcessAdapter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;

import java.io.File;

public class LSFScheduler extends AbstractScheduler {

    private static Logger log = LoggerFactory.getLogger(LSFScheduler.class);

    public static final String BSUB = "bsub";

    public LSFScheduler() {
        this(new LSFArgs());
    }

    public LSFScheduler(LSFArgs lsfArgs) {
        super(BSUB, lsfArgs);
    }

    @Override
    public ProcessAdapter createProcessAdapter(File monitorFile, int monitorInterval) {
        return new LSFFileProcessAdapter(monitorFile.getAbsolutePath(), monitorInterval);
    }

    @Override
    public String createCommand(String internalCommand) {

        // get email address to use as backup in case proc fails
        String backupEmail = ConanProperties.getProperty("scheduler.backup.email");

        // Create command to execute
        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.getSubmitCommand());
        sj.add(this.getArgs() != null, "", this.getArgs().toString());
        if (backupEmail != null && !backupEmail.isEmpty()) {
            sj.add("-u " + backupEmail);
        }
        sj.add("\"" + internalCommand + "\"");

        String cmd = sj.toString();

        return cmd;
    }

    @Override
    public String createWaitCommand(WaitCondition waitCondition) {

        // get email address to use as backup in case proc fails
        String backupEmail = ConanProperties.getProperty("scheduler.backup.email");

        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.getSubmitCommand());
        if (backupEmail != null && !backupEmail.isEmpty()) {
            sj.add("-u " + backupEmail);
        }
        sj.add("-oo", this.getArgs().getMonitorFile());
        sj.add(waitCondition.toString());
        sj.add("-q", this.getArgs().getQueueName());
        sj.add("\"sleep 1 2>&1\"");

        return sj.toString();
    }

    @Override
    public String createKillCommand(String jobId) {
        return "bkill " + jobId;
    }

    @Override
    public WaitCondition createWaitCondition(ExitStatus.Type exitStatus, String condition) {
        return new LSFWaitCondition(LSFExitStatusType.select(exitStatus), condition);
    }

    @Override
    public Scheduler copy() {
        //TODO Not too nice... shouldn't really use casting here but it will always give the right result.  To tidy up late.
        return new LSFScheduler(new LSFArgs((LSFArgs) this.getArgs()));
    }

    @Override
    public String getName() {
        return "LSF";
    }

}
