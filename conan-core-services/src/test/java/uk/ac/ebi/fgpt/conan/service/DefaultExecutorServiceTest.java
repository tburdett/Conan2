package uk.ac.ebi.fgpt.conan.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.core.context.scheduler.lsf.LSFScheduler;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.model.context.SchedulerArgs;

import static org.junit.Assert.*;

public class DefaultExecutorServiceTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private ConanExecutorService lsfExecutorService;

    @Before
    public void setup() {

        Scheduler lsfScheduler = new LSFScheduler();

        this.lsfExecutorService = new DefaultExecutorService(new DefaultProcessService(), new DefaultExecutionContext(new Local(), lsfScheduler, null));
    }

    //@Test
    public void testExecuteJobArray() throws Exception {

        ExecutionResult res = this.lsfExecutorService.executeJobArray("sleep 10", temp.newFolder("sleep1"), "sleepy", new SchedulerArgs.JobArrayArgs(1, 5, 1, -1), 1, 0);
    }
}