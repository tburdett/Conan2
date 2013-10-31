package uk.ac.ebi.fgpt.conan.core.context;

import org.apache.commons.io.FileUtils;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionResult;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
    private File outputFile;
    private int jobId;

    public DefaultExecutionResult(int exitCode, String[] output, File outputFile) {
        this(exitCode, output, null, -1);
    }

    public DefaultExecutionResult(int exitCode, String[] output, File outputFile, int jobId) {
        this.exitCode = exitCode;
        this.output = output;
        this.outputFile = outputFile;
        this.jobId = jobId;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    public String[] getOutput() {
        return this.output;
    }

    public File getOutputFile() {
        return this.outputFile;
    }

    public int getJobId() {
        return this.jobId;
    }

    public String getFirstOutputLine() {
        return output.length > 0 ? output[0] : null;
    }

    public void writeOutputToFile(File outputFile) throws IOException {
        FileUtils.writeLines(outputFile, Arrays.asList(this.output));
        this.outputFile = outputFile;
    }
}
