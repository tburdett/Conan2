package uk.ac.ebi.fgpt.conan.process.lsf;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.ae.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.ae.lsf.LSFProcessAdapter;
import uk.ac.ebi.fgpt.conan.ae.lsf.LSFProcessEvent;
import uk.ac.ebi.fgpt.conan.ae.lsf.LSFProcessListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Tests functionality of {@link LSFProcessAdapter} by creating mock LSF files and updating/deleting them whilst
 * listening for events.
 * <p/>
 * IMPORTANT: this test case isn't a real test case, as junit doesn't handle multithreaded test cases.
 *
 * @author Tony Burdett
 * @date 03-Nov-2010
 */
public class LSFProcessAdapterTestMockup {
    public static void main(String[] args) {
        LSFProcessAdapterTestMockup test = new LSFProcessAdapterTestMockup();
        try {
            test.runTests();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runTests() throws Exception {
        try {
            setUp();
            testProcessSuccess();
        }
        finally {
            tearDown();
        }

        try {
            setUp();
            testProcessFail();
        }
        finally {
            tearDown();
        }

        try {
            setUp();
            testProcessUpdate();
        }
        finally {
            tearDown();
        }

        try {
            setUp();
            testProcessError();
        }
        finally {
            tearDown();
        }
    }

    private String lsfOutputTest = "lsf-output-test.txt";
    private LSFProcess lsfProcess;

    private PrintWriter out;

    private String[] newOutput;
    private boolean correctEvent;
    private int exitValue;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    protected void setUp() throws Exception {
        // create the file
        new File(lsfOutputTest).createNewFile();

        // wait for a moment to prevent our file create/write events getting merged
        synchronized (this) {
            wait(10);
        }

        this.out = new PrintWriter(new BufferedWriter(new FileWriter(lsfOutputTest)));
        this.lsfProcess = new LSFProcessAdapter(lsfOutputTest, 1);
    }

    protected void tearDown() throws Exception {
        this.out.close();
        this.out = null;
        new File(lsfOutputTest).delete();
        this.lsfProcess = null;

        this.newOutput = null;
        this.correctEvent = false;
        this.exitValue = -1;

        // wait for a second to give resources a chance to cleanup
        synchronized (this) {
            wait(1500);
        }
    }

    public void testProcessSuccess() {
        // create a process listener
        LSFProcessListener listener = new LSFProcessListener() {
            public void processComplete(LSFProcessEvent evt) {
                getLog().info("Got process complete event");
                for (String s : evt.getNewOutput()) {
                    getLog().debug(s);
                }
                newOutput = evt.getNewOutput();
                correctEvent = true;
                exitValue = evt.getExitValue();
                synchronized (this) {
                    notifyAll();
                }
            }

            public void processUpdate(LSFProcessEvent evt) {
            }

            public void processError(LSFProcessEvent evt) {
            }
        };

        // add the listener to our lsfProcess
        lsfProcess.addLSFProcessListener(listener);

        // write "Successfully completed." to output file
        String msg = "Successfully completed.";
        out.println(msg);
        out.flush();

        // wait max 5 seconds for complete event
        if (!correctEvent) {
            synchronized (this) {
                try {
                    wait(5000);
                }
                catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        // if not complete now, fail
        Assert.assertTrue("Process complete was not fired promptly", correctEvent);
        Assert.assertEquals("Process completed with wrong exit code", 0, exitValue);
        Assert.assertEquals("Wrong amount of new content", 1, newOutput.length);
        Assert.assertEquals("New content was wrong", msg, newOutput[0]);

        lsfProcess.removeLSFProcessListener(listener);
    }

    public void testProcessFail() {
        // create a process listener
        LSFProcessListener listener = new LSFProcessListener() {
            public void processComplete(LSFProcessEvent evt) {
                getLog().info("Got process complete event");
                for (String s : evt.getNewOutput()) {
                    getLog().debug(s);
                }
                newOutput = evt.getNewOutput();
                correctEvent = true;
                exitValue = evt.getExitValue();
                getLog().debug("Event exit value is " + evt.getExitValue());
                synchronized (this) {
                    notifyAll();
                }
            }

            public void processUpdate(LSFProcessEvent evt) {
            }

            public void processError(LSFProcessEvent evt) {
            }
        };

        // add the listener to our lsfProcess
        lsfProcess.addLSFProcessListener(listener);

        // write "Successfully completed." to output file
        getLog().debug("Printing content to file...");
        String msg = "Exited with exit code 27.";
        out.println(msg);
        out.flush();

        // wait max 5 seconds for complete event
        if (!correctEvent) {
            synchronized (this) {
                try {
                    wait(5000);
                }
                catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        // if not complete now, fail
        Assert.assertTrue("Process fail was not fired promptly", correctEvent);
        Assert.assertEquals("Process completed with wrong exit code", 27, exitValue);
        Assert.assertEquals("Wrong amount of new content", 1, newOutput.length);
        Assert.assertEquals("New content was wrong", msg, newOutput[0]);

        lsfProcess.removeLSFProcessListener(listener);
    }

    public void testProcessUpdate() {
        // create a process listener
        LSFProcessListener listener = new LSFProcessListener() {
            public void processComplete(LSFProcessEvent evt) {
            }

            public void processUpdate(LSFProcessEvent evt) {
                getLog().info("Got process update event");
                for (String s : evt.getNewOutput()) {
                    getLog().debug(s);
                }
                newOutput = evt.getNewOutput();
                if (newOutput.length > 0) {
                    correctEvent = true;
                }
                synchronized (this) {
                    notifyAll();
                }
            }

            public void processError(LSFProcessEvent evt) {
            }
        };

        // add the listener to our lsfProcess
        lsfProcess.addLSFProcessListener(listener);

        // write "Successfully completed." to output file
        String msg = "Some random content";
        out.println(msg);
        out.flush();

        // wait max 5 seconds for complete event
        if (!correctEvent) {
            synchronized (this) {
                try {
                    wait(5000);
                }
                catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        // if not complete now, fail
        Assert.assertTrue("Process complete was not fired promptly", correctEvent);
        Assert.assertEquals("Wrong amount of new content", 1, newOutput.length);
        Assert.assertEquals("New content was wrong", msg, newOutput[0]);

        Assert.assertFalse("LSFProcess should not be complete", lsfProcess.isComplete());

        // write "Successfully completed." to output file
        String msg2 = "Successfully completed.";
        out.println(msg2);
        out.flush();

        // wait max 5 seconds for complete event
        synchronized (this) {
            try {
                wait(5000);
            }
            catch (InterruptedException e) {
                // ignore
            }
        }
        Assert.assertTrue("LSFProcess should be complete now", lsfProcess.isComplete());

        lsfProcess.removeLSFProcessListener(listener);
    }

    public void testProcessError() {
        // create a process listener
        LSFProcessListener listener = new LSFProcessListener() {
            public void processComplete(LSFProcessEvent evt) {
            }

            public void processUpdate(LSFProcessEvent evt) {
            }

            public void processError(LSFProcessEvent evt) {
                getLog().error("Got process error event, should only report complete");
                for (String s : evt.getNewOutput()) {
                    getLog().debug(s);
                }
                newOutput = evt.getNewOutput();
                correctEvent = true;
                exitValue = evt.getExitValue();
                synchronized (this) {
                    notifyAll();
                }
            }
        };

        // add the listener to our lsfProcess
        lsfProcess.addLSFProcessListener(listener);

        // wait for file to be detected before deleting
        synchronized (this) {
            try {
                wait(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // delete the file
        File f = new File(lsfOutputTest);
        getLog().debug("Deleting file " + f.getAbsolutePath() + " - exists currently? " + f.exists());
        Assert.assertTrue("Couldn't delete file", f.delete());

        // wait max 5 seconds for complete event
        if (!correctEvent) {
            synchronized (this) {
                try {
                    wait(5000);
                }
                catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        // if not complete now, fail
        Assert.assertTrue("Process complete was not fired promptly", correctEvent);
        Assert.assertEquals("Process completed with wrong exit code", -1, exitValue);
        Assert.assertEquals("Wrong amount of new content", 0, newOutput.length);

        lsfProcess.removeLSFProcessListener(listener);
    }
}
