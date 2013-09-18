package uk.ac.ebi.fgpt.conan.core.context.scheduler.pbs;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 17/09/13
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */
public class PBSSchedulerTest {

    private PBSScheduler pbsScheduler;

    @Before
    public void setup() {
        this.pbsScheduler = new PBSScheduler();
    }

    @Test
    public void createCommandTest() {

        String command = this.pbsScheduler.createCommand("sleep 50 2>&1", true);

        String correct = "echo \"sleep 50 2>&1\" | qsub -V  -W block=true";

        assertTrue(command.equals(correct));
    }


    @Test
    public void createWaitCommandTest() {

        String command = this.pbsScheduler.createWaitCommand(
                this.pbsScheduler.createWaitCondition(PBSExitStatus.AFTER_OK.getExitStatus(), "1001")
        );

        String correct = "echo \"sleep 1 2>&1\" |  qsub -W block=true,depend=afterok:1001";

        assertTrue(command.equals(correct));
    }

    @Test
    public void createKillCommandTest() {
        String command = this.pbsScheduler.createKillCommand("KILL");

        assertTrue(command.equals("qdel KILL"));
    }

    @Test
    public void testJobIdExtraction() {

        String testLine = "4176.UV00000010-P002";

        int jobId = this.pbsScheduler.extractJobIdFromOutput(testLine);

        assertTrue(jobId == 4176);
    }
}
