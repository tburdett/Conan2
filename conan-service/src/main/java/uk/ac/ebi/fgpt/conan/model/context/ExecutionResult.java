package uk.ac.ebi.fgpt.conan.model.context;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 17/09/13
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */
public interface ExecutionResult {

    int getExitCode();

    String[] getOutput();

    int getJobId();

    String getFirstOutputLine();
}
