package uk.ac.ebi.fgpt.conan.process.biosd;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabENASRAAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@ServiceProvider
public class ENASRAWebDownloadLSFProcess extends AbstractBioSDLSFProcess {

	private Logger log = LoggerFactory.getLogger(getClass());

	public String getName() {
		return "updatesourceenasra";
	}

	protected Logger getLog() {
		return log;
	}
	
	protected String getCommand(Map<ConanParameter, String> parameters)
			throws IllegalArgumentException {
		getLog().debug(
				"Executing " + getName() + " with the following parameters: "
						+ parameters.toString());

		// deal with parameters
		SampleTabENASRAAccessionParameter accession = new SampleTabENASRAAccessionParameter();
		accession.setAccession(parameters.get(accessionParameter));
		if (accession.getAccession() == null) {
			throw new IllegalArgumentException("Accession cannot be null");
		}

		String scriptpath = ConanProperties
				.getProperty("biosamples.script.path");
		File script = new File(scriptpath, "ENASRAWebDownload.sh");
		File outdir;
		try {
			outdir = getDirectory(accession);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to create directories for "+accession.getAccession());
		}
		
        File logfile = getDateTimeLogfile(outdir, "ENASRAWebDownload.sh");
		
		// main command to execute script
		String mainCommand = script.getAbsolutePath() + " "
				+ accession.getAccession() + " " + outdir.getAbsolutePath()
                + " &> "+logfile.getAbsolutePath();
		getLog().debug("Command is: <" + mainCommand + ">");
		return mainCommand;
	}
}
