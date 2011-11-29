package uk.ac.ebi.fgpt.conan.process.biosd;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@ServiceProvider
public class MageTabToSampleTabLSFProcess extends AbstractLSFProcess {
	private final Collection<ConanParameter> parameters;
	private final SampleTabAccessionParameter accessionParameter;

	private Logger log = LoggerFactory.getLogger(getClass());

	public MageTabToSampleTabLSFProcess() {
		parameters = new ArrayList<ConanParameter>();
		accessionParameter = new SampleTabAccessionParameter();
		parameters.add(accessionParameter);
	}

	private File getOutputDirectory(SampleTabAccessionParameter accession) throws IOException {
		String sampletabpath = ConanProperties
				.getProperty("biosamples.sampletab.path");
		File sampletabAE = new File(sampletabpath, "ae");
		File outdir = new File(sampletabAE, accession.getAccession());
		if (!outdir.exists()) {
			if (!outdir.mkdirs()) {
				throw new IOException("Unable to create directories: "
						+ outdir.getPath());
			}
		}
		return outdir;
	}

	protected Logger getLog() {
		return log;
	}

	public String getName() {
		return "topresampletabarrayexpress";
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

		String scriptpath = ConanProperties
				.getProperty("biosamples.script.path");
		File script = new File(scriptpath, "MageTabToSampleTab.sh");
		
		File outdir;
		try {
			outdir = getOutputDirectory(accession);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to create directories for "+accession);
		}
		
		String idfFilename = accession.getAccession().substring(2)+".idf.txt";
		File idfFile = new File(outdir, idfFilename);
		if (!idfFile.exists()){
			throw new IllegalArgumentException("IDF file does not exist for "+accession);			
		}
		
		File sampletabFile = new File(outdir, "sampletab.pre.txt");

		// main command to execute script
		String mainCommand = script.getAbsolutePath() + " "
				+ idfFile.getAbsolutePath() + " " + sampletabFile.getAbsolutePath();
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
