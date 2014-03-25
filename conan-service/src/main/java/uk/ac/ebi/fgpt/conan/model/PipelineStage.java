package uk.ac.ebi.fgpt.conan.model;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;

import java.util.List;

/**
 * Created by maplesod on 03/02/14.
 */
public interface PipelineStage {

    int ordinal();

    List<ConanParameter> getParameters();

    boolean checkArgs(ProcessArgs args);

    ProcessArgs getArgs();

    void setArgs(ProcessArgs args);

    ConanProcess createProcess(ConanExecutorService conanExecutorService);
}
