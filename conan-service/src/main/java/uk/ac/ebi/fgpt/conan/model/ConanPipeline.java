package uk.ac.ebi.fgpt.conan.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;

import java.io.Serializable;
import java.util.List;

/**
 * Basically, a series of processes to run incrementally.  Pipeline objects also include some additional metadata, such
 * as an assigned name, the user that created it, and so on.
 *
 * @author Tony Burdett
 * @date 28-Jul-2010
 * @see uk.ac.ebi.fgpt.conan.model.ConanProcess
 * @see uk.ac.ebi.fgpt.conan.model.ConanTask
 */
@JsonSerialize(typing = JsonSerialize.Typing.STATIC)
public interface ConanPipeline extends Serializable {
    /**
     * The name of this pipeline.  This name should be unique throughout Conan.
     *
     * @return the pipeline name
     */
    String getName();

    /**
     * Gets the {@link ConanUser} who created this pipeline
     *
     * @return the pipeline creator
     */
    @JsonIgnore
    ConanUser getCreator();

    /**
     * Gets whether this pipeline is private or not.  Public pipelines can be used by all submitters, whereas private
     * pipelines will only be visible to the user who created it and administrators.  All pipelines are public by
     * default.
     *
     * @return true if this pipeline is private, false otherwise
     */
    @JsonIgnore
    boolean isPrivate();

    /**
     * Gets a flag that indicates whether this pipeline should be used for daemon mode submissions.
     *
     * @return true if daemon mode inputs should be submitted to this pipeline, false otherwise
     */
    @JsonIgnore
    boolean isDaemonized();

    /**
     * A list of processes that should be invoked (in order) during the execution of this pipeline.
     *
     * @return the processes this pipeline chains together
     */
    List<ConanProcess> getProcesses();

    /**
     * Returns the set of input parameters that are required by this pipeline.  Pipeline parameters are the sum total of
     * all input parameters required by the {@link uk.ac.ebi.fgpt.conan.model.ConanProcess}es that make up this
     * pipeline.  The user must supply all these parameters up front in order to create a task for this pipeline.  Some
     * parameters may not be required by all processes - only the required parameters are passed to each process during
     * execution of the resulting task.
     * <p/>
     * Ideally, a single pipeline will require a minimal number of parameters in order to minimise user overhead.  It is
     * desirable to ensure that process grouped into a pipeline all require the same set of parameters.
     *
     * @return the set of parameters that must be supplied in order to execute this pipeline
     */
    List<ConanParameter> getAllRequiredParameters();
}
