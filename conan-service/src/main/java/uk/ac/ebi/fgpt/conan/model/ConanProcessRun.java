package uk.ac.ebi.fgpt.conan.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents an execution of a single process by a task in Conan.
 *
 * @author Natalja Kurbatova
 * @author Tony Burdett
 * @date 28-Oct-2010
 */
@JsonSerialize(typing = JsonSerialize.Typing.STATIC)
public interface ConanProcessRun extends Serializable {
    /**
     * Gets the ID of this process run.  IDs will normally be assigned to a process run on creation, once it is saved to
     * a backing datasource, so you do not need to manually created one.
     *
     * @return the process run unique ID, or null if it has not yet been assigned
     */
    String getId();

    /**
     * Sets the ID of this process run.
     *
     * @param id the process run unique ID
     */
    void setId(String id);

    /**
     * The name of this process that generated this run
     *
     * @return the process name
     */
    String getProcessName();

    /**
     * The date at which this process run started.  If the process run hasn't yet started, this will be null.
     *
     * @return the start date of this process
     */
    Date getStartDate();

    /**
     * The date at which this process run ended.  If the process has not yet ended, this will be null
     *
     * @return the end date of tihs process
     */
    Date getEndDate();

    /**
     * The exit value this processes exited with, if complete.  If the process has not yet ended, this will be -1
     *
     * @return the process exit value
     */
    int getExitValue();

    /**
     * The error message this processes exited with, if complete.  If the process has not yet ended, this will be null
     *
     * @return the process error message
     */
    String getErrorMessage();

    /**
     * The user who created this process run.  This is the user who owned the task when the process that generated this
     * run began.
     *
     * @return the user who started this process run
     */
    ConanUser getUser();
}
