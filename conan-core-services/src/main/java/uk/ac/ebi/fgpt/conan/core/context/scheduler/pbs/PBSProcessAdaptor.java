package uk.ac.ebi.fgpt.conan.core.context.scheduler.pbs;

import uk.ac.ebi.fgpt.conan.core.process.monitor.AbstractTaskProcessAdapter;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.File;
import java.util.List;

/**
 * Not sure how this is going to look at the moment, but will probably involve "tracejob"
 * User: maplesod
 * Date: 25/08/13
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
public class PBSProcessAdaptor extends AbstractTaskProcessAdapter {

    protected PBSProcessAdaptor(int jobId, int monitoringPeriod) {
        super(jobId, monitoringPeriod);
    }

    protected PBSProcessAdaptor(String jobName, int monitoringPeriod) {
        super(jobName, monitoringPeriod);
    }

    @Override
    protected void parseLine(String line, List<String> lines) {

    }

    @Override
    public File getFile() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean inRecoveryMode() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createMonitor() throws ProcessExecutionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeMonitor() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
