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
import uk.ac.ebi.fgpt.conan.model.context.ExternalProcessConfiguration;
import uk.ac.ebi.fgpt.conan.model.context.Locality;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;

import java.io.File;

public class DefaultExecutionContext implements ExecutionContext {

    private Locality locality;
    private Scheduler scheduler;
    private ExternalProcessConfiguration externalProcessConfiguration;
    private boolean foregroundJob;
    private File monitorFile;
    private String jobName;

    public DefaultExecutionContext() {
        this(new Local(), null, null);
    }

    public DefaultExecutionContext(Locality locality, Scheduler scheduler, ExternalProcessConfiguration externalProcessConfiguration) {

        this.locality = locality;
        this.scheduler = scheduler;
        this.externalProcessConfiguration = externalProcessConfiguration;

        this.jobName = "";
        this.monitorFile = null;
        this.foregroundJob = true;
    }

    public DefaultExecutionContext(DefaultExecutionContext copy) {

        this(
                copy.locality != null ? copy.locality.copy() : null,
                copy.scheduler != null ? copy.scheduler.copy() : null,
                copy.getExternalProcessConfiguration() //TODO deep copy this.
        );

        this.setContext(
                copy.jobName,
                copy.foregroundJob,
                copy.monitorFile != null ? new File(copy.monitorFile.getAbsolutePath()) : null
        );
    }

    @Override
    public void setContext(String jobName, boolean foregroundJob, File monitorFile) {

        this.setJobName(jobName);
        this.foregroundJob = foregroundJob;
        this.setMonitorFile(monitorFile);
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

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;

        if (scheduler != null) {
            this.scheduler.getArgs().setJobName(jobName);
        }
    }

    public void setForegroundJob(boolean foregroundJob) {
        this.foregroundJob = foregroundJob;
    }

    @Override
    public ExternalProcessConfiguration getExternalProcessConfiguration() {
        return externalProcessConfiguration;
    }

    @Override
    public ExecutionContext copy() {
        return new DefaultExecutionContext(this);
    }

    public void setLocality(Locality locality) {
        this.locality = locality;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setExternalProcessConfiguration(ExternalProcessConfiguration externalProcessConfiguration) {
        this.externalProcessConfiguration = externalProcessConfiguration;
    }

    @Override
    public File getMonitorFile() {
        return monitorFile;
    }

    public void setMonitorFile(File monitorFile) {
        this.monitorFile = monitorFile;

        if (scheduler != null) {
            this.scheduler.getArgs().setMonitorFile(monitorFile);
        }
    }
}
