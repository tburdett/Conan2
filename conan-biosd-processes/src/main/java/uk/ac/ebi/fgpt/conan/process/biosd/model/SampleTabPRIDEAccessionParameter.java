package uk.ac.ebi.fgpt.conan.process.biosd.model;

public class SampleTabPRIDEAccessionParameter extends SampleTabAccessionParameter {

    public SampleTabPRIDEAccessionParameter() {
        super("SampleTab PRIDE Accession");
    }
    
    public void setAccession(String accession) throws IllegalArgumentException {
        if (!accession.startsWith("GPR-"))
            throw new IllegalArgumentException("Invalid accession "+accession);
        this.accession = accession;
    }
}
