package uk.ac.ebi.fgpt.conan.process.biosd;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@ServiceProvider
public class MageTabCopyLSFProcess extends AbstractLSFProcess {
	private final Collection<ConanParameter> parameters;
	private final SampleTabAccessionParameter accessionParameter;

	private Logger log = LoggerFactory.getLogger(getClass());

	public MageTabCopyLSFProcess() {
		parameters = new ArrayList<ConanParameter>();
		accessionParameter = new SampleTabAccessionParameter();
		parameters.add(accessionParameter);
	}

	private File getOutputDirectory(SampleTabAccessionParameter accession) {
		String sampletabpath = ConanProperties
				.getProperty("biosamples.sampletab.path");
		File sampletabAE = new File(sampletabpath, "ae");
		File outdir = new File(sampletabAE, accession.getAccession());
		if (!outdir.exists()) {
			if (!outdir.mkdirs()) {
				throw new RuntimeException("Unable to create directories: "
						+ outdir.getPath());
			}
		}
		return outdir;
	}

	protected Logger getLog() {
		return log;
	}

	public String getName() {
		return "updatesourcearrayexpress";
	}

	public Collection<ConanParameter> getParameters() {
		return parameters;
	}

	protected String getComponentName() {
		return LSFProcess.UNSPECIFIED_COMPONENT_NAME;
	}

	protected String getCommand(Map<ConanParameter, String> parameters)
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

		String sampletabpath = ConanProperties
				.getProperty("biosamples.sampletab.path");
		String scriptpath = ConanProperties
				.getProperty("biosamples.script.path");
		File script = new File(scriptpath, "MageTabFTPDownload.sh");
		File outdir = getOutputDirectory(accession);
		// main command to execute script
		String mainCommand = script.getAbsolutePath() + " "
				+ accession.getAccession() + " " + outdir.getAbsolutePath();
		getLog().debug("Command is: <" + mainCommand + ">");
		return mainCommand;
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

		File outDir = getOutputDirectory(accession);
		File conanDir = new File(outDir, ".conan");
		File conanFile = new File(conanDir, getClass().getName());
		return conanFile.getAbsolutePath();

	}
}
