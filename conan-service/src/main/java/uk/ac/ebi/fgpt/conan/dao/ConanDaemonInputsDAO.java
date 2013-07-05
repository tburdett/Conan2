package uk.ac.ebi.fgpt.conan.dao;

import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;

import java.util.List;

/**
 * A data access object for retrieving {@link ConanParameter}s and their values from a repository of allowed inputs into
 * daemon mode.  This is useful for automation, in the case where you some resource exists for tracking the items that
 * are submitted to Conan pipelines.  Each DAO implementation should advertise the {@link ConanParameter} type it can
 * return values for, and on request should reply with a list of inputs that can be submitted to Conan.  Note that, once
 * submitted to Conan, the first process in a pipeline should somehow update the state of the tracking resource in order
 * to ensure that the same input is not resubmitted to Conan the next time daemon mode requests inputs.
 *
 * @author Tony Burdett
 * @date 19-Nov-2010
 */
public interface ConanDaemonInputsDAO {
    /**
     * The ConanParameter that this DAO can supply values for
     *
     * @return the parameter type this DAO supplies
     */
    Class<? extends ConanParameter> getParameterType();

    /**
     * A list of values, in desired submission order, that should be used to create daemon mode submissions
     *
     * @return the list of parameter values for daemon mode submissions
     */
    List<String> getParameterValues();
}
