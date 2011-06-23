package uk.ac.ebi.fgpt.conan.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.Request;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.ConanPipelineService;
import uk.ac.ebi.fgpt.conan.service.ConanProcessService;
import uk.ac.ebi.fgpt.conan.service.ConanUserService;
import uk.ac.ebi.fgpt.conan.web.view.PipelineCreationResponseBean;
import uk.ac.ebi.fgpt.conan.web.view.PipelineReorderRequestBean;
import uk.ac.ebi.fgpt.conan.web.view.PipelineRequestBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Allows querying for pipelines and creation of new pipelines.
 *
 * @author Tony Burdett
 * @date 30-Jul-2010
 */
@Controller
@RequestMapping("/pipelines")
public class PipelineController {
    private ConanPipelineService pipelineService;
    private ConanProcessService processService;
    private ConanUserService userService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ConanPipelineService getPipelineService() {
        return pipelineService;
    }

    @Autowired
    public void setPipelineService(ConanPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    public ConanProcessService getProcessService() {
        return processService;
    }

    @Autowired
    public void setProcessService(ConanProcessService processService) {
        this.processService = processService;
    }

    public ConanUserService getUserService() {
        return userService;
    }

    @Autowired
    public void setUserService(ConanUserService userService) {
        this.userService = userService;
    }

    /**
     * Gets a collection of pipelines that can be used when submitting tasks.  This operation requires knowledge of the
     * user making this request, so takes a restApiKey parameter.
     *
     * @param restApiKey the rest api key used to access this service, unique to each user
     * @return the list of pipelines
     */
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Collection<ConanPipeline> getPipelines(@RequestParam String restApiKey) {
        // retrieve the user
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // return this user, or log an error and return an empty collection if this isn't a valid user
        if (conanUser != null) {
            return getPipelineService().getPipelines(conanUser);
        }
        else {
            getLog().warn("Cannot recover any details about the logged in user.  " +
                    "No pipelines will be available to them.");
            return Collections.emptyList();
        }
    }

    // todo - post request for reordering pipelines
//    @RequestMapping(method = RequestMethod.POST)
//    public @ResponseBody Collection<ConanPipeline> reorderPipelines(@RequestBody PipelineReorderRequestBean reorderRequest) {
//        // recover the rest api key from the request
//        String restApiKey = reorderRequest.getRestApiKey();
//
//        // get the user identified by this rest api key
//        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);
//
//        // user has permission to do this?
//        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
//            // recover the processes for the given process descriptions
//            List<ConanProcess> conanProcesses = new ArrayList<ConanProcess>();
//            for (String processName : reorderRequest.getProcesses()) {
//                // lookup process
//                ConanProcess conanProcess = getProcessService().getProcess(processName);
//                conanProcesses.add(conanProcess);
//            }
//
//            // now we've created all the processes we need, generate the pipeline
//            ConanPipeline newPipeline = getPipelineService().createPipeline(pipelineRequest.getName(),
//                                                                            conanProcesses,
//                                                                            conanUser,
//                                                                            pipelineRequest.isPrivate());
//
//            // and return the list of pipelines now
//            String msg = "Your pipeline '" + newPipeline.getName() + "' was successfully created";
//            return new PipelineCreationResponseBean(true, msg, newPipeline,
//                                                    getPipelineService().getPipelines(conanUser));
//        }
//        else {
//            String msg = "You do not have permission to create new pipelines";
//            return new PipelineCreationResponseBean(false, msg, null, Collections.<ConanPipeline>emptySet());
//        }
//    }

    /**
     * Submits a request to create a new pipeline, by POST request.  This request requires a single parameter that is a
     * JSON encoded version of a {@link uk.ac.ebi.fgpt.conan.web.view.PipelineRequestBean}.  This request is validated
     * within the server and used to add a new pipeline.  The response is the list of pipelines available to Conan after
     * the new pipeline request is added.
     *
     * @param pipelineRequest the request describing the pipeline to add
     * @return the pipelines available to use after the new pipelines have been added
     */
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody PipelineCreationResponseBean addPipeline(@RequestBody PipelineRequestBean pipelineRequest) {
        // recover the rest api key from the request
        String restApiKey = pipelineRequest.getRestApiKey();

        // get the user identified by this rest api key
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // user has permission to do this?
        if (conanUser.getPermissions().compareTo(ConanUser.Permissions.SUBMITTER) > -1) {
            // recover the processes for the given process descriptions
            List<ConanProcess> conanProcesses = new ArrayList<ConanProcess>();
            for (String processName : pipelineRequest.getProcesses()) {
                // lookup process
                ConanProcess conanProcess = getProcessService().getProcess(processName);
                conanProcesses.add(conanProcess);
            }

            // now we've created all the processes we need, generate the pipeline
            ConanPipeline newPipeline = getPipelineService().createPipeline(pipelineRequest.getName(),
                                                                            conanProcesses,
                                                                            conanUser,
                                                                            pipelineRequest.isPrivate());

            // and return the list of pipelines now
            String msg = "Your pipeline '" + newPipeline.getName() + "' was successfully created";
            return new PipelineCreationResponseBean(true, msg, newPipeline,
                                                    getPipelineService().getPipelines(conanUser));
        }
        else {
            String msg = "You do not have permission to create new pipelines";
            return new PipelineCreationResponseBean(false, msg, null, Collections.<ConanPipeline>emptySet());
        }
    }
}
