package uk.ac.ebi.fgpt.conan.model.context;

import java.io.File;


public interface ExecutionResult {

    int getExitCode();

    String[] getOutput();

    File getOutputFile();

    int getJobId();

    String getFirstOutputLine();
}
