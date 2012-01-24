package uk.ac.ebi.fgpt.conan.process.biosd.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.dao.ConanDaemonInputsDAO;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabAEAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

public class MageTabDaemonInputs implements ConanDaemonInputsDAO {

	private Logger log = LoggerFactory.getLogger(getClass());

	public Class<? extends ConanParameter> getParameterType() {
		return SampleTabAEAccessionParameter.class;
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
				String aeaccession = subdir.getName().substring(2);
				
				File idf = new File(subdir, aeaccession + ".idf.txt");
				File sdrf = new File(subdir, aeaccession + ".sdrf.txt");
				File sampletab = new File(subdir, "sampletab.pre.txt");
				
				if (idf.exists() && sdrf.exists()){
					if (!sampletab.exists() 
							|| idf.lastModified() > sampletab.lastModified() 
							|| sdrf.lastModified() > sampletab.lastModified() ){
						parametervalues.add(staccession);
					}
				}
			}
		}

		return parametervalues;
	}

}
