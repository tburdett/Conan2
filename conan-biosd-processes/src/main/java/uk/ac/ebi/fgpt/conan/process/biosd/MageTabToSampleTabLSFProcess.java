package uk.ac.ebi.fgpt.conan.process.biosd;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabAEAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@ServiceProvider
public class MageTabToSampleTabLSFProcess extends AbstractBioSDLSFProcess {

	private Logger log = LoggerFactory.getLogger(getClass());


	public String getName() {
		return "topresampletabarrayexpress";
	}

	protected Logger getLog() {
		return log;
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
		SampleTabAEAccessionParameter accession = new SampleTabAEAccessionParameter();
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
				+ idfFile.getAbsolutePath() + " " + sampletabFile.getAbsolutePath()
				+ " | tee "+sampletabFile.getAbsolutePath()+".log";
		getLog().debug("Command is: <" + mainCommand + ">");
		return mainCommand;
	}
}
