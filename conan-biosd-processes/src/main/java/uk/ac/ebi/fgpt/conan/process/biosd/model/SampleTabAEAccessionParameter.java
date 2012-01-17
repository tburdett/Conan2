package uk.ac.ebi.fgpt.conan.process.biosd.model;

public class SampleTabAEAccessionParameter extends SampleTabAccessionParameter {

    public SampleTabAEAccessionParameter() {
        super("SampleTab ArrayExpress Accession");
    }
    
    public void setAccession(String accession) throws IllegalArgumentException {
        if (!accession.startsWith("GAE-"))
            throw new IllegalArgumentException("Invalid accession "+accession);
        this.accession = accession;
    }
}
