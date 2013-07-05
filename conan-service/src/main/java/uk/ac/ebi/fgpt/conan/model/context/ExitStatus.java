
package uk.ac.ebi.fgpt.conan.model.context;

/**
 * Describes how a job exits.
 *
 * @author Dan Mapleson
 */
public interface ExitStatus {

    /**
     * Retrieves the ExitStatus Type from this ExistStatus object
     * @return
     */
    Type getExitStatus();

    /**
     * Retrieves the command used to define this ExitStatus
     * @return
     */
    String getCommand();

    /**
     * Create an ExitStatus object from a generic ExitStatus.Type
     * @param exitStatusType
     * @return
     */
    ExitStatus create(Type exitStatusType);

    /**
     * Allowable generic exit types
     */
    public enum Type {

        COMPLETED_SUCCESS,
        COMPLETED_FAILED,
        COMPLETED_ANY
    }
}
