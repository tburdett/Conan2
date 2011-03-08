package uk.ac.ebi.fgpt.conan.core.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A default implementation of a Conan pipeline.
 *
 * @author Tony Burdett
 * @date 12-Oct-2010
 */
public class DefaultConanPipeline implements ConanPipeline {
    private String name;
    private ConanUser creator;
    private boolean isPrivate;
    private boolean isDaemonized;
    private List<ConanProcess> conanProcesses;

    private List<ConanParameter> allRequiredParameters;

    private Logger log = LoggerFactory.getLogger(getClass());

    public DefaultConanPipeline(String name, ConanUser creator, boolean isPrivate) {
        this(name, creator, isPrivate, false);
    }

    public DefaultConanPipeline(String name, ConanUser creator, boolean isPrivate, boolean isDaemonized) {
        this.name = name;
        this.creator = creator;
        this.isPrivate = isPrivate;
        this.isDaemonized = isDaemonized;
        this.conanProcesses = new ArrayList<ConanProcess>();
        this.allRequiredParameters = new ArrayList<ConanParameter>();
    }

    protected Logger getLog() {
        return log;
    }

    public void setProcesses(List<ConanProcess> conanProcesses) {
        this.conanProcesses.addAll(conanProcesses);

        // once we've added processes, cache all required parameters to play nice with json serialization
        for (ConanProcess process : getProcesses()) {
            for (ConanParameter parameter : process.getParameters()) {
                getLog().trace("Next parameter for process " + getName() + " = " + parameter.getName());
                if (!allRequiredParameters.contains(parameter)) {
                    getLog().trace("'" + parameter.getName() + "' is a required parameter for pipelines containing " +
                            "'" + getName() + "'");
                    allRequiredParameters.add(parameter);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public ConanUser getCreator() {
        return creator;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isDaemonized() {
        return isDaemonized;
    }

    public List<ConanProcess> getProcesses() {
        return conanProcesses;
    }

    public List<ConanParameter> getAllRequiredParameters() {
        return allRequiredParameters;
    }
}
