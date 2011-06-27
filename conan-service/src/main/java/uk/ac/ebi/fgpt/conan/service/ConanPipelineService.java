package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Collection;
import java.util.List;

/**
 * A service that can be used to explore the available pipelines in the Conan framework.  Within Conan, there are
 * potentially many pipelines that invoke a series of successive tasks, only proceeding to the next when the previous
 * task has run successfully.
 * <p/>
 * Pipeline services are user-specific.  That is, each user can only see their own pipelines, or those that have been
 * made public.
 *
 * @author Tony Burdett
 * @date 26-Jul-2010
 */
public interface ConanPipelineService {
    /**
     * Loads and initializes all pipelines configured for use by Conan.  This operation should be performed once, up
     * front, on initialization of the service.
     */
    void loadPipelines();

    /**
     * Applies a sorting operation to the pipelines served by this service.  The list supplied as a parameter should
     * contain the names of the pipelines being sorted, in the desired order.  If the list passed contains named for
     * which no pipeline could be located, this sort will not be applied.  If the pipelines returned by {@link
     * #getPipelines(uk.ac.ebi.fgpt.conan.model.ConanUser)} contained pipelines that are not included in the sort list,
     * these pipelines will be relegated to the end of the sorted list.
     *
     * @param conanUser           the user making this request: only users with admin permissions can reorder pipelines
     * @param sortedPipelineNames a list of pipeline names in the desired sort order
     */
    void reorderPipelines(ConanUser conanUser, List<String> sortedPipelineNames);

    /**
     * Gets the collection of pipelines known to Conan and accessible to the current user.  This allows scope for
     * different users to configure their own pipelines that are not available to others.
     *
     * @param conanUser the user making this request: only pipelines accessible to this user are returned
     * @return the collection of pipelines this user has access to
     */
    List<ConanPipeline> getPipelines(ConanUser conanUser);

    /**
     * Gets the pipeline with the current name known to Conan and accessible to the current user.
     * <p/>
     * Pipeline names in Conan must be unique, so this method is guaranteed to produce exactly 1 result, or null if the
     * pipeline with the given name is not found or not accessible to this user.
     *
     * @param conanUser    the user making this request: the pipeline will only be returned if accessible to this user
     * @param pipelineName the unique name of this pipeline
     * @return the pipeline with the given name that the user can access
     */
    ConanPipeline getPipeline(ConanUser conanUser, String pipelineName);

    /**
     * Constructs a new pipeline and saves it to the Conan framework.  As pipelines are public by default, calling this
     * method is equivalent to calling <code>createPipeline(name, processes, creator, false)</code>
     *
     * @param name           the name for the newly created pipeline - must be unique
     * @param conanProcesses the list of processes that this pipeline executes
     * @param creator        the creator of this pipeline
     * @return the newly created pipeline
     */
    ConanPipeline createPipeline(String name, List<ConanProcess> conanProcesses, ConanUser creator);

    /**
     * Constructs a new pipeline and saves it to the Conan framework.  As pipelines are public by default, calling this
     * method is equivalent to calling <code>createPipeline(name, processes, creator, false)</code>
     *
     * @param name           the name for the newly created pipeline - must be unique
     * @param conanProcesses the list of processes that this pipeline executes
     * @param creator        the creator of this pipeline
     * @param isPrivate      whether or not this pipeline should be private, accessible only by the creator
     * @return the newly created pipeline
     */
    ConanPipeline createPipeline(String name, List<ConanProcess> conanProcesses, ConanUser creator, boolean isPrivate);
}
