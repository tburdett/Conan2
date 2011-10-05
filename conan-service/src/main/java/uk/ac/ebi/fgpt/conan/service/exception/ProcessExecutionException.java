package uk.ac.ebi.fgpt.conan.service.exception;

/**
 * A wrapper exception that should be thrown whenever a {@link uk.ac.ebi.fgpt.conan.model.ConanProcess} that was
 * executed throws an exception.
 *
 * @author Tony Burdett
 * @date 30-Jul-2010
 * @see uk.ac.ebi.fgpt.conan.model.ConanProcess
 */
public class ProcessExecutionException extends Exception {
    private int exitValue = -1;
    private String[] processOutput = new String[]{"No output captured"};
    private String processExecutionHost = "unknown";

    public ProcessExecutionException(int exitValue) {
        super();
        this.exitValue = exitValue;
    }

    public ProcessExecutionException(int exitValue, String message) {
        super(message);
        this.exitValue = exitValue;
    }

    public ProcessExecutionException(int exitValue, String message, Throwable cause) {
        super(message, cause);
        this.exitValue = exitValue;
    }

    public ProcessExecutionException(int exitValue, Throwable cause) {
        super(cause);
        this.exitValue = exitValue;
    }

    public int getExitValue() {
        return exitValue;
    }

    public String[] getProcessOutput() {
        return processOutput;
    }

    public void setProcessOutput(String[] processOutput) {
        this.processOutput = processOutput;
    }

    public String getProcessExecutionHost() {
        return processExecutionHost;
    }

    public void setProcessExecutionHost(String processExecutionHost) {
        this.processExecutionHost = processExecutionHost;
    }
}
