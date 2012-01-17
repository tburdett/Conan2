package uk.ac.ebi.fgpt.conan.process.biosd.model;

public class SampleTabIMSRAccessionParameter extends SampleTabAccessionParameter {

    public SampleTabIMSRAccessionParameter() {
        super("SampleTab IMSR Accession");
    }
    
    public void setAccession(String accession) throws IllegalArgumentException {
        if (!accession.startsWith("GMS-"))
            throw new IllegalArgumentException("Invalid accession "+accession);
        this.accession = accession;
    }
}
