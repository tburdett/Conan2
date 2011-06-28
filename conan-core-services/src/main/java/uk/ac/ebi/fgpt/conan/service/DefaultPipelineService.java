package uk.ac.ebi.fgpt.conan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.dao.ConanPipelineDAO;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.*;
import java.util.*;

/**
 * A default implementation of a pipeline service that recovers pipelines from a pipeline DAO.  Each pipeline recovered
 * is checked for the possibly of it being daemonized and registered with a daemon service if necessary
 *
 * @author Tony Burdett
 * @date 25-Nov-2010
 */
public class DefaultPipelineService implements ConanPipelineService {
    private ConanPipelineDAO pipelineDAO;

    private ConanDaemonService daemonService;

    private List<ConanPipeline> sortedConanPipelines = new ArrayList<ConanPipeline>();
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
        if (sortedConanPipelines.isEmpty()) {
            // get pipelines from our DAO
            Collection<ConanPipeline> conanPipelines = getPipelineDAO().getPipelines();

            // add any daemonized pipelines to the daemon service
            for (ConanPipeline conanPipeline : conanPipelines) {
                if (conanPipeline.isDaemonized()) {
                    getLog().info("Pipeline '" + conanPipeline.getName() + "' " +
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

            // add all pipelines from the DAO to our sorted collection
            sortedConanPipelines.clear();
            sortedConanPipelines.addAll(conanPipelines);

            // and re-sort based on previously saved sort order, if any
            recoverPipelineSortOrder();
        }
    }


    public void reorderPipelines(ConanUser conanUser, final List<String> requiredPipelineOrder) {
        StringBuilder sb = new StringBuilder();
        sb.append("RequiredPipelineOrder:{");
        for (String s : requiredPipelineOrder) {
            sb.append(s).append(",");
        }
        sb.append("}");
        getLog().debug("Sorting pipelines - " + sb.toString());

        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.ADMINISTRATOR) <= 0) {
            sortPipelines(requiredPipelineOrder);
        }
        else {
            getLog().warn(conanUser.getUserName() + " does not have required admin privileges to resort pipelines");
        }
    }

    public List<ConanPipeline> getPipelines(ConanUser conanUser) {
        getLog().debug("Request to get pipelines for " + conanUser.getUserName());

        // lazy instantiate sorted pipelines cache
        loadPipelines();

        // filter based on user
        List<ConanPipeline> result = new ArrayList<ConanPipeline>();
        for (ConanPipeline conanPipeline : sortedConanPipelines) {
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

        // return the result
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

    private void recoverPipelineSortOrder() {
        // open pipeline ordering config file, if it exists
        File conanDir =
                new File(ConanProperties.getProperty("environment.path"), "software" + File.separatorChar + "conan");
        File configFile = new File(conanDir, "pipeline-order.txt");

        if (configFile.exists()) {
            // read config file
            try {
                final List<String> sortedPipelineNames = new ArrayList<String>();
                BufferedReader reader = new BufferedReader(new FileReader(configFile));
                String pipelineName = "";
                while ((pipelineName = reader.readLine()) != null) {
                    sortedPipelineNames.add(pipelineName);
                }
                reader.close();
                sortPipelines(sortedPipelineNames);
            }
            catch (IOException e) {
                getLog().warn("Unable to load pipeline sort order config from " + configFile.getAbsolutePath() +
                                      ": no ordering of pipelines will be applied");
            }
        }
    }

    private void savePipelineSortOrder() {
        // get pipeline ordering config file
        File conanDir =
                new File(ConanProperties.getProperty("environment.path"), "software" + File.separatorChar + "conan");
        File configFile = new File(conanDir, "pipeline-order.txt");

        // write config file
        getLog().info("Exporting new pipeline sort order to " + configFile.getAbsolutePath());
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(configFile)));
            for (ConanPipeline pipeline : sortedConanPipelines) {
                writer.println(pipeline.getName());
            }
            writer.close();
        }
        catch (IOException e) {
            getLog().warn("Unable to save pipeline sort order config to " + configFile.getAbsolutePath() + ": " +
                                  "pipeline order may be lost on restart");
        }
    }

    private void sortPipelines(final List<String> requiredPipelineOrder) {
        // apply sorting based on names to our pipeline collection - any pipelines not named in the config file will be moved to the end
        Collections.sort(sortedConanPipelines, new Comparator<ConanPipeline>() {
            public int compare(ConanPipeline p1, ConanPipeline p2) {
                if (requiredPipelineOrder.contains(p1.getName())) {
                    if (requiredPipelineOrder.contains(p2.getName())) {
                        // both named, can compare
                        return requiredPipelineOrder.indexOf(p1.getName()) -
                                requiredPipelineOrder.indexOf(p2.getName());
                    }
                    else {
                        // p1 named, p2 not, so move p2 after p1
                        return 1;
                    }
                }
                else {
                    // p1 not named, is p2?
                    if (requiredPipelineOrder.contains(p2.getName())) {
                        // p2 named, p1 not, so move p1 after p2
                        return -1;
                    }
                    else {
                        // neither named, leave where they both are
                        // note: this makes this comparator inconsistent with equals
                        getLog().debug(
                                "Required pipeline order does not contain either '" + p1.getName() + "' or '" +
                                        p2.getName() + "', unable to sort these pipelines");
                        return 0;
                    }
                }
            }
        });

        // now we've applied the sort, make sure we save the prefs to our config file
        savePipelineSortOrder();
    }
}
