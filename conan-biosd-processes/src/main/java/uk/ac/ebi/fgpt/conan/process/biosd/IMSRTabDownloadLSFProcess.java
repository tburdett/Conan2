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
public class IMSRTabDownloadLSFProcess extends AbstractBioSDLSFProcess {

	private Logger log = LoggerFactory.getLogger(getClass());

	public String getName() {
		return "updatesourceimsr";
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
		SampleTabAccessionParameter accession = new SampleTabAccessionParameter();
		accession.setAccession(parameters.get(accessionParameter));
		if (accession.getAccession() == null) {
			throw new IllegalArgumentException("Accession cannot be null");
		}
		//IMSR web interface takes a number, not the site code
		//However, this is not a straightforward lookup on the summary.
		//For the moment, it is hardcoded here.
		//TODO unhardcode this
		int accessionid = 0;
		/*
		GMS-JAX
		GMS-HAR
		GMS-MMRRC
		GMS-ORNL
		GMS-CARD
		GMS-EM
		GMS-NMICE
		GMS-RBRC
		GMS-NCIMR
		GMS-CMMR
		GMS-APB
		GMS-EMS
		GMS-HLB
		GMS-NIG
		GMS-TAC
		GMS-MUGEN
		GMS-TIGM
		GMS-KOMP
		GMS-RMRC-NLAC
		GMS-OBS
		GMS-WTSI
		 */
		
		if      (accession.equals("GMS-JAX")) accessionid = 1;
		else if (accession.equals("GMS-HAR")) accessionid = 2;
		else if (accession.equals("GMS-MMRRC")) accessionid = 3;
		else if (accession.equals("GMS-ORNL")) accessionid = 4;
		else if (accession.equals("GMS-CARD")) accessionid = 5;
		else if (accession.equals("GMS-EM")) accessionid = 6;
		else if (accession.equals("GMS-NMICE")) accessionid = 7;
		else if (accession.equals("GMS-RBRC")) accessionid = 9;
		else if (accession.equals("GMS-NCIMR")) accessionid = 10;
		else if (accession.equals("GMS-CMMR")) accessionid = 11;
		else if (accession.equals("GMS-APB")) accessionid = 12;
		else if (accession.equals("GMS-EMS")) accessionid = 13;
		else if (accession.equals("GMS-HLB")) accessionid = 14;
		else if (accession.equals("GMS-NIG")) accessionid = 17;
		else if (accession.equals("GMS-TAC")) accessionid = 20;
		else if (accession.equals("GMS-MUGEN")) accessionid = 21;
		else if (accession.equals("GMS-TIGM")) accessionid = 22; //This is the really big one
		else if (accession.equals("GMS-KOMP")) accessionid = 23;
		else if (accession.equals("GMS-RMRC-NLAC")) accessionid = 24;
		else if (accession.equals("GMS-OBS")) accessionid = 25;
		else if (accession.equals("GMS-WTSI")) accessionid = 26;

		if (accessionid == 0) {
			throw new IllegalArgumentException("Accession ("+accession+") is not valid");
		}

		String scriptpath = ConanProperties
				.getProperty("biosamples.script.path");
		File script = new File(scriptpath, "IMSRTabDownload.sh");
		File outdir;
		File outfile;
		try {
			outdir = getOutputDirectory(accession);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to create directories for "+accession);
		}
		outfile = new File(outdir, "raw.tab.txt");
		
		// main command to execute script
		String mainCommand = script.getAbsolutePath() + " "
				+ accessionid + " " + outfile.getAbsolutePath();
		getLog().debug("Command is: <" + mainCommand + ">");
		return mainCommand;
	}
}
