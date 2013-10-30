package uk.ac.ebi.fgpt.conan.core.pipeline;

import uk.ac.ebi.fgpt.conan.core.process.AbstractConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.DefaultProcessService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 29/10/13
 * Time: 11:18
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractConanPipeline implements ConanPipeline {

    private String name;
    private ConanUser creator;
    private boolean isPrivate;
    private boolean isDaemonized;
    private ConanProcessService conanProcessService;
    private List<AbstractConanProcess> processList;

    public AbstractConanPipeline(String name, ConanUser creator, boolean isPrivate) {
        this(name, creator, isPrivate, false, new DefaultProcessService());
    }

    public AbstractConanPipeline(String name, ConanUser creator, boolean isPrivate, boolean isDaemonized,
                                 ConanProcessService conanProcessService) {
        this.name = name;
        this.creator = creator;
        this.isPrivate = isPrivate;
        this.isDaemonized = isDaemonized;
        this.conanProcessService = conanProcessService;
        this.processList = new ArrayList<>();
    }

    public ConanProcessService getConanProcessService() {
        return conanProcessService;
    }

    public void setConanProcessService(ConanProcessService conanProcessService) throws IOException {

        this.conanProcessService = conanProcessService;

        for(AbstractConanProcess process : this.processList) {
            process.setConanProcessService(this.conanProcessService);
        }
    }

    public void clearProcessList() {
        this.processList.clear();
    }

    public void addProcess(AbstractConanProcess process) {

        process.setConanProcessService(this.conanProcessService);
        this.processList.add(process);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ConanUser getCreator() {
        return this.creator;
    }

    @Override
    public boolean isPrivate() {
        return this.isPrivate;
    }

    @Override
    public boolean isDaemonized() {
        return this.isDaemonized;
    }

    @Override
    public List<ConanProcess> getProcesses() {

        List<ConanProcess> conanProcessList = new ArrayList<>();

        for(AbstractConanProcess process : this.processList) {
            conanProcessList.add(process);
        }

        return conanProcessList;
    }

    @Override
    public List<ConanParameter> getAllRequiredParameters() {

        List<ConanParameter> params = new ArrayList<>();

        for(AbstractConanProcess process : this.processList) {
            params.addAll(process.getParameters());
        }

        return params;
    }
}
