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
package uk.ac.ebi.fgpt.conan.core.context;

import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.Locality;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;

public class DefaultExecutionContext implements ExecutionContext {

    private Locality locality;
    private Scheduler scheduler;
    private boolean foregroundJob;

    public DefaultExecutionContext() {
        this(true);
    }

    public DefaultExecutionContext(boolean foregroundJob) {
        this(null, foregroundJob);
    }

    public DefaultExecutionContext(Scheduler scheduler, boolean foregroundJob) {

        this(new Local(), scheduler, foregroundJob);
    }

    public DefaultExecutionContext(Locality locality, Scheduler scheduler, boolean foregroundJob) {

        this.locality = locality;
        this.scheduler = scheduler;
        this.foregroundJob = foregroundJob;
    }

    public DefaultExecutionContext(DefaultExecutionContext copy) {

        this.locality = copy.getLocality().copy();
        this.scheduler = copy.usingScheduler() ? copy.getScheduler().copy() : null;
        this.foregroundJob = copy.isForegroundJob();
    }

    @Override
    public Locality getLocality() {
        return locality;
    }

    @Override
    public boolean usingScheduler() {
        return this.scheduler != null;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public boolean isForegroundJob() {
        return foregroundJob;
    }

    @Override
    public void setForegroundJob(boolean foregroundJob) {
        this.foregroundJob = foregroundJob;
    }

    @Override
    public ExecutionContext copy() {
        return new DefaultExecutionContext(this);
    }

    public void setLocality(Locality locality) {
        this.locality = locality;
    }

}
