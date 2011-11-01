package uk.ac.ebi.fgpt.conan.dao;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Collection;
import java.util.List;

/**
 * A data access object for retrieving {@link ConanPipeline}s and associated details from some datasource used to
 * persist this information.
 *
 * @author Tony Burdett
 * @date 25-Nov-2010
 */
public interface ConanPipelineDAO {
    /**
     * Gets a single pipeline, given the unique pipeline name.  If no such pipeline exists, this returns null.
     *
     * @param pipelineName the name of the pipeline to retrieve
     * @return the pipeline with this unique name
     */
    ConanPipeline getPipeline(String pipelineName);

    /**
     * Gets a single pipeline, given the unique pipeline name and the user hoping to access it.  Private pipelines that
     * cannot be viewd by the supplied user should not be returned by this method.
     *
     * @param conanUser    the user trying to access this pipeline
     * @param pipelineName the unique name of the pipeline
     * @return the named pipeline, as long as the given user has permission to access it
     */
    ConanPipeline getPipelineForUser(ConanUser conanUser, String pipelineName);

    /**
     * Gets all pipelines known by this DAO.  If there are no pipelines available, an empty list is returned
     *
     * @return the collection of pipelines available
     */
    Collection<ConanPipeline> getPipelines();

    /**
     * Creates a new pipeline with the supplied attributes.  THis pipeline should be added to the backing datasource so
     * it can be reused by others and persists across Conan sessions.  By default, new pipelines should be public, so
     * this method is equivalent to calling {@link #createPipeline(String, java.util.List,
     * uk.ac.ebi.fgpt.conan.model.ConanUser, boolean)} with a value of "false" for the isPrivate attribute.
     *
     * @param name           the name of the pipeline to store, which should be unique
     * @param conanProcesses the list of processes this pipeline runs
     * @param creator        the creator of this pipeline
     * @return the newly created pipeline
     */
    ConanPipeline createPipeline(String name, List<ConanProcess> conanProcesses, ConanUser creator);

    /**
     * Creates a new pipeline with the supplied attributes.  THis pipeline should be added to the backing datasource so
     * it can be reused by others and persists across Conan sessions.  Supplying true for "isPrivate" designates this a
     * private pipeline that can only be run by it's creator.
     *
     * @param name           the name of the pipeline
     * @param conanProcesses the processes this pipeline executes
     * @param creator        the creator of this pipeline
     * @param isPrivate      whether this pipeline should be private
     * @return the newly created pipeline
     */
    ConanPipeline createPipeline(String name,
                                 List<ConanProcess> conanProcesses,
                                 ConanUser creator,
                                 boolean isPrivate);
}
