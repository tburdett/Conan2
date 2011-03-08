package uk.ac.ebi.fgpt.conan.ae.dao;

import java.util.Collection;

/**
 * DAO class that can retrieve important details and permissions for submitters from AE2.  These details are
 * encapsulated in a SubmitterDetails object
 *
 * @author Tony Burdett
 * @date 10-Nov-2010
 */
public interface SubmitterDetailsDAO {
    /**
     * Returns all {@link SubmitterDetails} for the experiment or array design with the given accession.  The ObjectType
     * parameter declares whether to fetch experiments or array designs.
     *
     * @param accession the accession to query AE2 for
     * @param type      the type of object to fetch the submitter details for
     * @return the list of submitter details obtained
     */
    Collection<SubmitterDetails> getSubmitterDetailsByAccession(String accession, SubmitterDetails.ObjectType type);
}
