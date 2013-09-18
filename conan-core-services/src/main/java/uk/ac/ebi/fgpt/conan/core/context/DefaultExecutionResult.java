package uk.ac.ebi.fgpt.conan.core.context;

import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 17/09/13
 * Time: 14:13
 * To change this template use File | Settings | File Templates.
 */
public class DefaultExecutionResult implements ExecutionResult {

    private int exitCode;
    private String[] output;
    private int jobId;

    public DefaultExecutionResult(int exitCode, String[] output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    public DefaultExecutionResult(int exitCode, String[] output, int jobId) {
        this.exitCode = exitCode;
        this.output = output;
        this.jobId = jobId;
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }

    @Override
    public String[] getOutput() {
        return this.output;
    }

    @Override
    public int getJobId() {
        return this.jobId;
    }

    @Override
    public String getFirstOutputLine() {
        return output.length > 0 ? output[0] : null;
    }



}
