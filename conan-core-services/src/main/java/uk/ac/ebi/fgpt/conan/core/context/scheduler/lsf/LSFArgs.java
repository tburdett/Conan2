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

import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;
import uk.ac.ebi.fgpt.conan.util.StringJoiner;


public class LSFArgs extends SchedulerArgs {

    public LSFArgs() {
        super();
    }

    public LSFArgs(LSFArgs args) {
        super(args);
    }

    protected boolean validString(String str) {
        return str != null && !str.isEmpty();
    }


    protected String createSimpleOptions() {

        StringJoiner joiner = new StringJoiner(" ");

        if (this.getJobArrayArgs() == null) {
            joiner.add("-J", this.getJobName());
            joiner.add("-oo", this.getMonitorFile());
        }
        else {
            JobArrayArgs ja = this.getJobArrayArgs();
            StringBuilder sb = new StringBuilder();
            sb.append(this.getJobName());
            sb.append("[");
            sb.append(ja.getMinIndex()).append("-").append(ja.getMaxIndex());

            if (ja.getStepIndex() > 1) {
                sb.append(":").append(ja.getStepIndex());
            }

            sb.append("]");

            if (ja.getMaxSimultaneousJobs() > 1) {
                sb.append("%").append(ja.getMaxSimultaneousJobs());
            }

            joiner.add("-J", sb.toString());

            if (this.getMonitorFile() != null) {
                joiner.add("-oo", this.getMonitorFile().getAbsolutePath() + ".%I");
            }
        }

        joiner.add("-q", this.getQueueName());
        joiner.add("-w ", this.getWaitCondition());
        joiner.add(this.getThreads() > 1, "-n", String.valueOf(this.getThreads()));
        joiner.add("-P", this.getProjectName());
        joiner.add(this.isOpenmpi(), "-a", "openmpi");
        joiner.add(!this.getExtraArgs().startsWith("-R"), "", this.getExtraArgs());

        return joiner.toString();
    }

    protected String createUsageString() {

        final int threads = this.getThreads();
        final int mem = this.getMemoryMB();

        String span = threads > 1 ? "span[ptile=" + threads + "]" : "";
        String rusage = mem > 0 ? "rusage[mem=" + mem + "]" : "";
        String extra = this.getExtraArgs().startsWith("-R") ? this.getExtraArgs().substring(2).trim() : "";

        return !span.isEmpty() || !rusage.isEmpty() || !extra.isEmpty() ? "-R" + rusage + span + extra : "";
    }

    @Override
    public String toString() {

        String simpleOptions = createSimpleOptions();
        String usage = createUsageString();

        StringJoiner joiner = new StringJoiner(" ");

        joiner.add(simpleOptions);
        joiner.add(usage);

        return joiner.toString();
    }

    @Override
    public SchedulerArgs copy() {

        SchedulerArgs copy = new LSFArgs(this);

        return copy;
    }

}
