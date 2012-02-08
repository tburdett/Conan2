package uk.ac.ebi.fgpt.conan.process.biosd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

public abstract class AbstractBioSDLSFProcess extends AbstractLSFProcess {
	protected final Collection<ConanParameter> parameters;
	protected final SampleTabAccessionParameter accessionParameter;

	private String getPathPrefix(SampleTabAccessionParameter accession){
		if (accession.getAccession().startsWith("GMS-")) return "imsr";
		else if (accession.getAccession().startsWith("GAE-")) return "ae";
        else if (accession.getAccession().startsWith("GRP-")) return "pride";
		else throw new IllegalArgumentException("Unable to get path prefix for "+accession.getAccession());
	}
	
	public AbstractBioSDLSFProcess() {
		parameters = new ArrayList<ConanParameter>();
		accessionParameter = new SampleTabAccessionParameter();
		parameters.add(accessionParameter);
	}

	protected File getOutputDirectory(SampleTabAccessionParameter accession) throws IOException {
		String sampletabpath = ConanProperties
				.getProperty("biosamples.sampletab.path");
		File sampletab = new File(sampletabpath, getPathPrefix(accession));
		File outdir = new File(sampletab, accession.getAccession());
		if (!outdir.exists()) {
			if (!outdir.mkdirs()) {
				throw new IOException("Unable to create directories: "
						+ outdir.getPath());
			}
		}
		return outdir;
	}

	public Collection<ConanParameter> getParameters() {
		return parameters;
	}
	
	protected String getComponentName() {
		return LSFProcess.UNSPECIFIED_COMPONENT_NAME;
	}


	protected String getLSFOutputFilePath(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		getLog().debug(
				"Executing " + getName() + " with the following parameters: "
						+ parameters.toString());

		// deal with parameters
		SampleTabAccessionParameter accession = new SampleTabAccessionParameter();
		accession.setAccession(parameters.get(accessionParameter));
		if (accession.getAccession() == null) {
			throw new IllegalArgumentException("Accession cannot be null");
		}

		File outDir;
		try {
			outDir = getOutputDirectory(accession);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to create directories for "+accession);
		}
		File conanDir = new File(outDir, ".conan");
		File conanFile = new File(conanDir, getClass().getName());
		return conanFile.getAbsolutePath();

	}

}
