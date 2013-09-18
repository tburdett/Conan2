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
package uk.ac.ebi.fgpt.conan.core.context.scheduler.pbs;

import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;

import java.io.File;

public class PBSArgs extends SchedulerArgs {

    public static final String BLOCK_OPTION = "block=true";


    public PBSArgs() {
        super();
    }

    public PBSArgs(PBSArgs args) {
        super(args);
    }


    protected static String compressJobName(String jobName) {

        if (jobName.length() <= 15) {
            return jobName;
        }

        return Integer.toString(jobName.hashCode());
    }


    protected String createSimpleOptions() {

        StringJoiner joiner = new StringJoiner(" ");

        joiner.add("-N", compressJobName(this.getJobName()));
        joiner.add("-q", this.getQueueName());
        joiner.add("-P", this.getProjectName());
        if (this.getMonitorFile() != null) {
            joiner.add("-o", new File(this.getMonitorFile().getParentFile(), this.getMonitorFile().getName() + ".stdout"));
            joiner.add("-e", new File(this.getMonitorFile().getParentFile(), this.getMonitorFile().getName() + ".stderr"));
        }
        joiner.add(this.getExtraArgs());

        // -V retains the users environment variables
        return "-V " + joiner.toString();
    }

    protected String createAdditionalOptions(boolean isForegroundJob) {

        if (isForegroundJob || (this.getWaitCondition() != null && !this.getWaitCondition().isEmpty())) {

            StringJoiner additionalOptions = new StringJoiner(",");

            additionalOptions.add(isForegroundJob, "", "block=true");
            additionalOptions.add(this.getWaitCondition());

            return "-W " + additionalOptions.toString();
        }

        return "";
    }

    protected String createResourceString() {

        StringJoiner joiner = new StringJoiner(":");

        joiner.add(this.getThreads() > 0, "select=", Integer.toString(1));
        joiner.add(this.getThreads() > 0, "ncpus=", Integer.toString(this.getThreads()));
        joiner.add(this.getMemoryMB() > 0, "mem=", Integer.toString(this.getMemoryGB()) + "G");

        return this.getThreads() > 0 || this.getMemoryMB() > 0 ? "-l " + joiner.toString() : "";
    }

    @Override
    public String toString() {

        return this.toString(true);
    }

    public String toString(boolean isForegroundJob) {

        String simpleOptions = createSimpleOptions();
        String additionalOptions = createAdditionalOptions(isForegroundJob);
        String resources = createResourceString();

        StringJoiner joiner = new StringJoiner(" ");

        joiner.add(simpleOptions);
        joiner.add(additionalOptions);
        joiner.add(resources);

        return joiner.toString().trim();
    }

    @Override
    public SchedulerArgs copy() {

        SchedulerArgs copy = new PBSArgs(this);

        return copy;
    }

}
