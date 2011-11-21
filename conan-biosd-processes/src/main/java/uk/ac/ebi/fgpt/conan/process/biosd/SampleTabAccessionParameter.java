package uk.ac.ebi.fgpt.conan.process.biosd;

import uk.ac.ebi.fgpt.conan.model.AbstractConanParameter;

public class SampleTabAccessionParameter extends AbstractConanParameter {

	private String accession;
	
	protected SampleTabAccessionParameter() {
        super("SampleTab Accession");
        this.accession = null;
    }
	
	public void setAccession(String accession){
		//TODO validate
		this.accession = accession;
	}
	
	public String getAccession(){
		return this.accession;
	}
}
