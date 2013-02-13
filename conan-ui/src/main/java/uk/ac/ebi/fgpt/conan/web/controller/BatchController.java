package uk.ac.ebi.fgpt.conan.web.controller;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.ConanPipelineService;
import uk.ac.ebi.fgpt.conan.service.ConanUserService;
import uk.ac.ebi.fgpt.conan.web.view.BatchResponseBean;
import uk.ac.ebi.fgpt.conan.web.view.SubmissionRequestBean;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A controller that handles file uploads for batch files and can create multiple submissions.  The batch file
 * submission route is only available for single-parameter pipelines: to create multiple submissions for multi-parameter
 * pipelines you should script against the REST API.
 * <p/>
 * Batch files should be formatted as one parameter value per line.  The uploaded file is then parsed and the content
 * sent back to the client as a json object, which can be reviewed and then accepted for submission.
 *
 * @author Tony Burdett
 * @date 09-Nov-2010
 */
@Controller
@RequestMapping("/generate-request")
public class BatchController {
    private ConanPipelineService pipelineService;
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

    public ConanUserService getUserService() {
        return userService;
    }

    @Autowired
    public void setUserService(ConanUserService userService) {
        this.userService = userService;
    }

    /**
     * Generates a {@link uk.ac.ebi.fgpt.conan.web.view.BatchResponseBean} from the supplied string that, if the
     * operation was successful, describes a batch of submissions that should be posted to the server.  If the operation
     * failed, the response bean will describe the problems encountered.  The response bean can be handled client side
     * before forwarding each submission request back to a {@link uk.ac.ebi.fgpt.conan.web.controller.SubmissionController}.
     * <p/>
     * Only pipelines that require a single parameter will accept batch submissions.  In order to correctly generate a
     * batch, the restApiKey of the user performing this request should be passed, along with the pipeline the batch
     * should be submitted to, the index of starting process for the batch of submissions, and a list of values for the
     * single parameter that is required for this pipeline.  This list of values should actually be submitted as one
     * long string, with distinct values separated by newline characters.
     *
     * @param restApiKey           the restApiKey of the user who is generating this batch
     * @param pipeline             the pipeline to submit this batch to
     * @param startingProcessIndex the index of the starting process for this batch of submissions
     * @param multiParams          a newline character separated list of parameter values
     * @return a batch response bean describing the outcome of batch generation
     */
    @RequestMapping(value = "/multi", method = RequestMethod.POST)
    public
    @ResponseBody
    BatchResponseBean generateBatchSubmissionFromString(@RequestParam String restApiKey,
                                                        @RequestParam String pipeline,
                                                        @RequestParam int startingProcessIndex,
                                                        @RequestParam String multiParams) {
        getLog().debug("Received post request for string: " + multiParams + ", pipeline " + pipeline);

        // get the user, identified by their rest api key
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        boolean uploadOK = true;
        boolean pipelineAcceptsBatches = false;
        List<SubmissionRequestBean> submissions = new ArrayList<SubmissionRequestBean>();

        // before we do anything else check out pipeline will accept this batch
        ConanPipeline p = getPipelineService().getPipeline(conanUser, pipeline);
        String pipelineParamName = "";
        if (p.getAllRequiredParameters().size() == 1) {
            pipelineAcceptsBatches = true;
            pipelineParamName = p.getAllRequiredParameters().get(0).getName();
        }

        // now tokenize our string to extract parameters
        String[] paramValues = multiParams.split("\n");
        if (pipelineAcceptsBatches) {
            // create a new submission request for each accession
            for (String paramValue : paramValues) {
                Map<String, String> paramValueMap = new HashMap<String, String>();
                paramValueMap.put(pipelineParamName, paramValue);
                SubmissionRequestBean request = new SubmissionRequestBean(ConanTask.Priority.LOW.toString(),
                        pipeline,
                        paramValueMap);
                request.setStartingProcessIndex(startingProcessIndex);
                request.setRestApiKey(restApiKey);
                submissions.add(request);
            }
        }

        return new BatchResponseBean(uploadOK, pipelineAcceptsBatches, submissions);
    }

    /**
     * Generates a {@link uk.ac.ebi.fgpt.conan.web.view.BatchResponseBean} from the supplied file that, if the operation
     * was successful, describes a batch of submissions that should be posted to the server.  If the operation failed,
     * the response bean will describe the problems encountered.  The response bean can be handled client side before
     * forwarding each submission request back to a {@link uk.ac.ebi.fgpt.conan.web.controller.SubmissionController}.
     * <p/>
     * Only pipelines that require a single parameter will accept batch submissions.  In order to correctly generate a
     * batch, the restApiKey of the user performing this request should be passed, along with the pipeline the batch
     * should be submitted to, the index of starting process for the batch of submissions, and a list of values for the
     * single parameter that is required for this pipeline.  This list of values should actually be submitted as one
     * long string, with distinct values separated by newline characters.
     *
     * @param restApiKey           the restApiKey of the user who is generating this batch
     * @param pipeline             the pipeline to submit this batch to
     * @param startingProcessIndex the index of the starting process for this batch of submissions
     * @param batchFile            a multipart file post element that is a collection of single parameter values, one
     *                             per line
     * @return a batch response bean describing the outcome of batch generation
     * @throws java.io.IOException if the batchFile posted could not be read
     */
    @RequestMapping(value = "/batch", method = RequestMethod.POST)
    public
    @ResponseBody
    String generateBatchSubmissionFromFile(
            @RequestParam String restApiKey,
            @RequestParam String pipeline,
            @RequestParam int startingProcessIndex,
            @RequestParam MultipartFile batchFile)
            throws IOException {
        getLog().debug(
                "Received post request including file: " + batchFile.getOriginalFilename() + ", pipeline " + pipeline);

        // get the user, identified by their rest api key
        ConanUser conanUser = getUserService().getUserByRestApiKey(restApiKey);

        // get temp directory
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File uploadedFile = null;
        File uploadedDir = null;

        boolean uploadOK = false;
        boolean pipelineAcceptsBatches = false;
        List<SubmissionRequestBean> submissions = new ArrayList<SubmissionRequestBean>();

        try {
            // transfer uploaded file to temp directory
            String origName = batchFile.getOriginalFilename();
            String batchFileName = origName.substring(0, origName.indexOf("."));
            uploadedDir = new File(tempDir, batchFileName);
            if (!uploadedDir.exists()) {
                uploadedDir.mkdirs();
            }
            uploadedFile = new File(uploadedDir, batchFile.getOriginalFilename());
            batchFile.transferTo(uploadedFile);

            uploadOK = true;

            // now, before we do anything else check out pipeline will accept this batch
            ConanPipeline p = getPipelineService().getPipeline(conanUser, pipeline);
            String pipelineParamName = "";
            if (p.getAllRequiredParameters().size() == 1) {
                pipelineAcceptsBatches = true;
                pipelineParamName = p.getAllRequiredParameters().get(0).getName();
            }

            // now, read the contents of our file to extract accessions
            if (uploadOK && pipelineAcceptsBatches) {
                List<String> paramValues = new ArrayList<String>();
                BufferedReader reader = new BufferedReader(new FileReader(uploadedFile));
                String nextParamValue;
                while ((nextParamValue = reader.readLine()) != null) {
                    nextParamValue = nextParamValue.trim();
                    if (!(nextParamValue == null || nextParamValue.isEmpty())) {
                        paramValues.add(nextParamValue);
                    }
                }

                // once all accessions are read, close the reader and delete the file
                reader.close();
                uploadedFile.delete();

                // now create a new submission request for each accession
                for (String paramValue : paramValues) {
                    Map<String, String> paramValueMap = new HashMap<String, String>();
                    paramValueMap.put(pipelineParamName, paramValue);
                    SubmissionRequestBean request = new SubmissionRequestBean(ConanTask.Priority.LOW.toString(),
                            pipeline,
                            paramValueMap);
                    request.setStartingProcessIndex(startingProcessIndex);
                    request.setRestApiKey(restApiKey);
                    submissions.add(request);
                }
            }
        } finally {
            if (uploadedDir != null) {
                if (recursivelyDelete(uploadedDir)) {
                    log.debug("Deleted " + uploadedDir + " ok.");
                } else {
                    log.warn("Failed to delete " + uploadedDir);
                }
            }
        }

        BatchResponseBean response = new BatchResponseBean(uploadOK, pipelineAcceptsBatches, submissions);
        StringWriter out = new StringWriter();
        JsonFactory f = new MappingJsonFactory();
        JsonGenerator g = f.createJsonGenerator(out);
        g.writeObject(response);
        return out.toString();
    }

    /**
     * Recursively deletes this file, and any files it contains
     *
     * @param file the file or directory to delete
     * @return true if successful, false otherwise
     */
    private boolean recursivelyDelete(File file) {
        boolean success = true;

        if (file.isDirectory()) {
            // recursive delete all children
            for (File childFile : file.listFiles()) {
                success = success && recursivelyDelete(childFile);
            }

            // delete the directory, which should now be empty
            success = success && file.delete();
        } else {
            file.delete();
        }

        if (!success) {
            log.debug("Couldn't delete " + file.getAbsolutePath());
        }

        return success;
    }
}
