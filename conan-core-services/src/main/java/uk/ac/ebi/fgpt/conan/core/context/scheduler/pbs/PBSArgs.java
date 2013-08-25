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

public class PBSArgs extends SchedulerArgs {

    public PBSArgs() {
        super();
    }

    public PBSArgs(PBSArgs args) {
        super(args);
    }


    protected String createSimpleOptions() {

        StringJoiner joiner = new StringJoiner(" ");

        joiner.add("-N", this.getJobName());
        joiner.add("-q", this.getQueueName());
        joiner.add(this.getWaitCondition());
        joiner.add(this.getExtraArgs());

        return joiner.toString();
    }

    protected String createUsageString() {

        StringJoiner joiner = new StringJoiner(":");

        joiner.add(this.getThreads() > 0, "select=", Integer.toString(1));
        joiner.add(this.getThreads() > 0, "ncpus=", Integer.toString(this.getThreads()));
        joiner.add(this.getMemoryMB() > 0, "mem=", Integer.toString(this.getMemoryGB()) + "G");

        return this.getThreads() > 0 || this.getMemoryMB() > 0 ? "-l " + joiner.toString() : "";
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

        SchedulerArgs copy = new PBSArgs(this);

        return copy;
    }

}
