package uk.ac.ebi.fgpt.conan.process.biosd.model;

public class SampleTabENASRAAccessionParameter extends SampleTabAccessionParameter {

    public SampleTabENASRAAccessionParameter() {
        super("SampleTab ENA SRA Accession");
    }
    
    public void setAccession(String accession) throws IllegalArgumentException {
        if (!accession.startsWith("GEN-"))
            throw new IllegalArgumentException("Invalid accession "+accession);
        this.accession = accession;
    }
}
