package uk.ac.ebi.fgpt.conan.model.context;

import java.io.File;
import java.io.IOException;


public interface ExecutionResult {

    int getExitCode();

    String[] getOutput();

    File getOutputFile();

    int getJobId();

    String getFirstOutputLine();

    void writeOutputToFile(File outputFile) throws IOException;
}
