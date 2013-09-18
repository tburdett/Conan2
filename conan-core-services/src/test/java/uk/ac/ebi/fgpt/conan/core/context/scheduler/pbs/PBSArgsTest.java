package uk.ac.ebi.fgpt.conan.core.context.scheduler.pbs;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 17/09/13
 * Time: 09:34
 * To change this template use File | Settings | File Templates.
 */
public class PBSArgsTest {

    @Test
    public void testDefaultArgs() {
        PBSArgs args = new PBSArgs();

        String test = args.toString();

        assertTrue(test.equals("-V  -W block=true"));
    }

    @Test
    public void testSimpleArgs() {
        PBSArgs args = new PBSArgs();
        args.setJobName("Job1");
        args.setProjectName("ProjectRampart");
        args.setQueueName("production");

        String test = args.toString();

        String correct = "-V -NJob1 -qproduction -PProjectRampart -W block=true";

        assertTrue(test.equals(correct));
    }


    @Test
    public void testFullArgs() {

        PBSArgs args = new PBSArgs();
        args.setJobName("Job1");
        args.setProjectName("ProjectRampart");
        args.setQueueName("production");
        args.setThreads(8);
        args.setOpenmpi(true);
        args.setWaitCondition("depend=afterany:Job0");
        args.setMemoryMB(60000);

        String test = args.toString();

        String correct = "-V -NJob1 -qproduction -PProjectRampart -W block=true,depend=afterany:Job0 -l select=1:ncpus=8:mem=60G";

        assertTrue(test.equals(correct));
    }
}
