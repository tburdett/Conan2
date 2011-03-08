package uk.ac.ebi.fgpt.conan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.dao.ConanPipelineDAO;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A default implementation of a pipeline service that recovers pipelines from a pipeline DAO.  Each pipeline recovered
 * is checked for the possibly of it being daemonized and registered with a daemon service if necessary
 *
 * @author Tony Burdett
 * @date 25-Nov-2010
 */
public class DefaultPipelineService implements ConanPipelineService {
    private ConanPipelineDAO pipelineDAO;

    private ConanUserService userService;
    private ConanProcessService processService;
    private ConanDaemonService daemonService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ConanPipelineDAO getPipelineDAO() {
        return pipelineDAO;
    }

    public void setPipelineDAO(ConanPipelineDAO pipelineDAO) {
        this.pipelineDAO = pipelineDAO;
    }

    public ConanDaemonService getDaemonService() {
        return daemonService;
    }

    public void setDaemonService(ConanDaemonService daemonService) {
        this.daemonService = daemonService;
    }

    public void loadPipelines() {
        // get pipelines from our DAO
        Collection<ConanPipeline> conanPipelines = getPipelineDAO().getPipelines();

        // add any daemonized pipelines to the daemon service
        for (ConanPipeline conanPipeline : conanPipelines) {
            if (conanPipeline.isDaemonized()) {
                getLog().debug("Pipeline '" + conanPipeline.getName() + "' " +
                        "is daemonized and will be added to daemon service");
                if (getDaemonService() != null) {
                    getDaemonService().addPipeline(conanPipeline);
                }
                else {
                    getLog().warn("No DaemonService was configured - pipeline '" + conanPipeline.getName() + "' " +
                            "was flagged as daemonized but will not be started");
                }
            }
        }
    }

    public Collection<ConanPipeline> getPipelines(ConanUser conanUser) {
        getLog().debug("Request to get pipelines for " + conanUser.getUserName());
        Collection<ConanPipeline> result = new ArrayList<ConanPipeline>();
        for (ConanPipeline conanPipeline : getPipelineDAO().getPipelines()) {
            if (conanPipeline.isPrivate()) {
                // if the pipeline is private, check the user
                if (conanPipeline.getCreator().equals(conanUser)) {
                    // matching user, ok to add
                    result.add(conanPipeline);
                }
            }
            else {
                // ok to return
                result.add(conanPipeline);
            }
        }
        return result;
    }

    public ConanPipeline getPipeline(ConanUser conanUser, String pipelineName) {
        // get the pipeline by name
        getLog().debug("Request to get pipeline '" + pipelineName + "' for " + conanUser.getUserName());
        ConanPipeline conanPipeline = getPipelineDAO().getPipeline(pipelineName);
        if (conanPipeline.isPrivate()) {
            // if the pipeline is private, check the user
            if (conanPipeline.getCreator().equals(conanUser)) {
                // matching user, ok to return
                return conanPipeline;
            }
            else {
                return null;
            }
        }
        else {
            // ok to return
            return conanPipeline;
        }
    }

    public ConanPipeline createPipeline(String name, List<ConanProcess> conanProcesses, ConanUser creator) {
        throw new UnsupportedOperationException("Creating new pipelines is not yet supported");
    }

    public ConanPipeline createPipeline(String name,
                                        List<ConanProcess> conanProcesses,
                                        ConanUser creator,
                                        boolean isPrivate) {
        throw new UnsupportedOperationException("Creating new pipelines is not yet supported");
    }

}
