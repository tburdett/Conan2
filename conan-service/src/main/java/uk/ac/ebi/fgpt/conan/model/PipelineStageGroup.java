package uk.ac.ebi.fgpt.conan.model;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;

import java.util.List;

/**
 * Created by maplesod on 03/02/14.
 */
public interface PipelineStageGroup {

    PipelineStage[] getAllStages();

    PipelineStage[] parseAll(String[] split);

    String allStagesToString();
}
