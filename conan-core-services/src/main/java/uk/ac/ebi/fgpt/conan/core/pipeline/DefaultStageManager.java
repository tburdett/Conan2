package uk.ac.ebi.fgpt.conan.core.pipeline;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.PipelineStage;
import uk.ac.ebi.fgpt.conan.model.PipelineStageGroup;
import uk.ac.ebi.fgpt.conan.model.param.ProcessArgs;
import uk.ac.ebi.fgpt.conan.service.ConanExecutorService;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;

import java.util.*;

/**
 * Created by maplesod on 03/02/14.
 */
public class DefaultStageManager extends ArrayList<PipelineStage> {

    private Set<PipelineStage> distinctStages;

    public DefaultStageManager() {
        super();
        this.distinctStages = new HashSet<>();
    }

    public DefaultStageManager(String stages, PipelineStageGroup type) {
        super();
        this.distinctStages = new HashSet<>();

        PipelineStage[] stageArray = stages.trim().equalsIgnoreCase("ALL") ?
            type.getAllStages() :
            type.parseAll(stages.split(","));

        if (stageArray != null && stageArray.length != 0) {
            for(PipelineStage stage : stageArray) {
                this.add(stage);
            }
        }
    }



    @Override
    public void add(int index, PipelineStage stage) {
        throw new UnsupportedOperationException("Cannot insert stages");
    }


    @Override
    public boolean add(PipelineStage stage) {

        if (distinctStages.contains(stage)) {
            throw new IllegalArgumentException("Cannot add duplicated stage to RAMPART stage list");
        }

        this.distinctStages.add(stage);
        boolean result = super.add(stage);

        this.sort();

        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends PipelineStage> stages) {
        throw new UnsupportedOperationException("Cannot insert collection of stages");
    }

    @Override
    public boolean contains(Object stage) {
        return this.distinctStages.contains(stage);
    }

    public List<ConanProcess> createProcesses(ConanExecutorService conanExecutorService) {

        List<ConanProcess> processes = new ArrayList<>();

        for(PipelineStage stage : this) {

            if(stage.getArgs() != null) {
                processes.add(stage.createProcess(conanExecutorService));
            }
        }

        return processes;
    }

    public PipelineStage get(PipelineStage stageToFind) {

        for(PipelineStage stage : this) {
            if (stage == stageToFind) {
                return stage;
            }
        }

        return null;
    }

    /*public List<ConanProcess> getExternalTools() {

        List<ConanProcess> processes = new ArrayList<>();

        for(PipelineStage stage : this) {
            processes.addAll(stage.getArgs().getExternalProcesses());
        }

        return processes;
    }*/

    public String toString() {

        return StringUtils.join(this, ",");
    }

    public void setArgsIfPresent(PipelineStage stage, ProcessArgs args) {

        if (args != null) {
            for(PipelineStage rs : this) {
                if (rs == stage) {
                    rs.setArgs(args);
                }
            }
        }
    }

    public void sort() {
        Collections.sort(this, new Comparator<PipelineStage>() {
            @Override
            public int compare(PipelineStage o1, PipelineStage o2) {
                return o1.ordinal() - o2.ordinal();
            }
        });
    }
}
