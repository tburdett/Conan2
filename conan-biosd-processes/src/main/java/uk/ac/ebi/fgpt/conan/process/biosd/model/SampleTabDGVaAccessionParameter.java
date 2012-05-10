package uk.ac.ebi.fgpt.conan.process.biosd.model;

public class SampleTabDGVaAccessionParameter extends SampleTabAccessionParameter {

    public SampleTabDGVaAccessionParameter() {
        super("SampleTab DGVa Accession");
    }
    
    public void setAccession(String accession) throws IllegalArgumentException {
        if (!accession.startsWith("GVA-"))
            throw new IllegalArgumentException("Invalid accession "+accession);
        this.accession = accession;
    }
}
