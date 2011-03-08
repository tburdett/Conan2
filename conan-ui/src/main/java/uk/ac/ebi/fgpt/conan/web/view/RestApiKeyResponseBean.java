package uk.ac.ebi.fgpt.conan.web.view;

import uk.ac.ebi.fgpt.conan.model.ConanUser;

/**
 * A simple response bean that encapsulates information about a REST API key request operation.  This bean should be
 * returned in response to a request for a new REST API key, and contains the REST API key itself, the user this key is
 * for, and the number of REST API keys this user now holds.
 *
 * @author Tony Burdett
 * @date 01-Nov-2010
 */
public class RestApiKeyResponseBean {
    private ConanUser conanUser;

    public RestApiKeyResponseBean(ConanUser conanUser) {
        this.conanUser = conanUser;
    }

    /**
     * Gets the actual value of the REST API key requested
     *
     * @return the rest api key
     */
    public String getRestApiKey() {
        return conanUser.getRestApiKey();
    }

    /**
     * Gets the user this REST API key belongs to
     *
     * @return the user who holds the key contained in this bean
     */
    public ConanUser getConanUser() {
        return conanUser;
    }
}
