package uk.ac.ebi.fgpt.conan.process.biosd;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

    private Logger log = LoggerFactory.getLogger(getClass());

	public static String getPathPrefix(SampleTabAccessionParameter accession) {
		return AbstractBioSDProcess.getPathPrefix(accession);
	}
	
	public AbstractBioSDLSFProcess() {
		parameters = new ArrayList<ConanParameter>();
		accessionParameter = new SampleTabAccessionParameter();
		parameters.add(accessionParameter);
	}

	protected File getDirectory(SampleTabAccessionParameter accession) throws IOException {
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
			outDir = getDirectory(accession);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to create directories for "+accession);
		}
		File conanDir = new File(outDir, ".conan");
		File conanFile = new File(conanDir, getClass().getName());
		return conanFile.getAbsolutePath();

	}

	
	public static File getDateTimeLogfile(File outdir, String prefix){
	    return AbstractBioSDProcess.getDateTimeLogfile(outdir, prefix);
	}

    /**
     * Returns the memory requirements, in MB, for the LSF process that will be dispatched.  By default, this is not
     * used and therefore processes run with environment defaults (8GB for the EBI LSF at the time of writing).  You can
     * override this for more memory hungry (or indeed, less memory hungry!) jobs.
     *
     * @param parameterStringMap the parameters supplied to this process, as this may potentially alter the
     *                           requirements
     * @return the number of MB required to run this process
     */
	@Override
    protected int getMemoryRequirement(Map<ConanParameter, String> parameterStringMap) {
        return 8192;
    }
}
