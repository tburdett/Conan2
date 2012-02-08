package uk.ac.ebi.fgpt.conan.process.biosd.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.dao.ConanDaemonInputsDAO;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

public class AccessionDaemonInputs implements ConanDaemonInputsDAO {

	private Logger log = LoggerFactory.getLogger(getClass());

	public Class<? extends ConanParameter> getParameterType() {
		return SampleTabAccessionParameter.class;
	}

	public List<String> getParameterValues() {
		// this must get a list of NEW parameter values to run
		// in this context this means ones that have not been copied yet

		String sampletabpath = ConanProperties
				.getProperty("biosamples.sampletab.path");

		List<String> parametervalues = new ArrayList<String>();

		File dir = new File(sampletabpath, "ae");
		for (File subdir : dir.listFiles()) {
			if (subdir.isDirectory()) {
				String staccession = subdir.getName();
				
				File sampletabpre = new File(subdir, "sampletab.pre.txt");
                File sampletab = new File(subdir, "sampletab.txt");
				
				if (sampletabpre.exists()){
					if (!sampletab.exists() 
							|| sampletab.lastModified() > sampletabpre.lastModified()){
					    //TODO check other parts of this pipeline rather than assume based on first part?
						parametervalues.add(staccession);
					}
				}
			}
		}

		return parametervalues;
	}

}
