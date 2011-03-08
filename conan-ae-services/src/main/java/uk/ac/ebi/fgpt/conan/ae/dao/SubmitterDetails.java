package uk.ac.ebi.fgpt.conan.ae.dao;

/**
 * A simple object that encapsulates the details relevant for submitters to ArrayExpress.  Details include the accession
 * and name of the object, the release and activation date, and the name, password and email of these submitter details
 *
 * @author Tony Burdett
 * @date 10-Nov-2010
 */
public class SubmitterDetails {
    private String accession;
    private String name;
    private String releaseDate;
    private String activationDate;

    private String username;
    private String password;
    private String email;

    private ObjectType objectType;

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(String activationDate) {
        this.activationDate = activationDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public enum ObjectType {
        EXPERIMENT,
        ARRAY_DESIGN,
        UNKNOWN
    }
}
