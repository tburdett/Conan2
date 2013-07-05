
package uk.ac.ebi.fgpt.conan.model.context;

/**
 * Interface for a WaitCondition (a command which will wait for another scheduled process to complete before executing)
 *
 * NOTE: This interface may make more sense as an abstract class.
 *
 * @author Dan Mapleson
 */
public interface WaitCondition {

    /**
     * Create an ExitStatus object based on the Type of ExitStatus
     * @param exitStatusType
     * @return
     */
    ExitStatus createExitStatus(ExitStatus.Type exitStatusType);

    /**
     * Retrieves the ExitStatus from this WaitCondition
     * @return
     */
    ExitStatus getExitStatus();

    /**
     * Sets the exit status for this WaitCondition
     * @param exitStatus
     */
    void setExitStatus(ExitStatus exitStatus);

    /**
     * Retrieves the WaitCondition
     * @return
     */
    String getCondition();

    /**
     * Sets the condition
     * @param condition
     */
    void setCondition(String condition);

    /**
     * Retrieves the command defined by the ExitStatus and WaitCondition
     * @return
     */
    String getCommand();
}
