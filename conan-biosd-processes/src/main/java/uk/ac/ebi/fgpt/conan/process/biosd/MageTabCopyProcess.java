package uk.ac.ebi.fgpt.conan.process.biosd;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.sampletab.MageTabFTPDownload;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@ServiceProvider
public class MageTabCopyProcess implements ConanProcess {
    private final Collection<ConanParameter> params;
    private final SampleTabAccessionParameter accessionParameter;

    private Logger log = LoggerFactory.getLogger(getClass());

    public MageTabCopyProcess() {
        params = new ArrayList<ConanParameter>();
        accessionParameter = new SampleTabAccessionParameter();
    	params.add(accessionParameter);
    }

    protected Logger getLog() {
        return log;
    }

	public boolean testIfMageTabAccession(SampleTabAccessionParameter accession) {
		String regex = "GAE-[A-Z]+-[0-9]+";
		if (accession.getAccession().matches(regex)){
			return true;
		} else {
			return false;
		}
	}

    public boolean execute(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException, ProcessExecutionException, InterruptedException {
        getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());
             

        // deal with parameters
        getLog().debug("Checking SampleTabAccessionParameter...");
        SampleTabAccessionParameter accession = new SampleTabAccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        getLog().debug("SampleTabAccessionParameter is "+parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }
        if (!testIfMageTabAccession(accession)) {
            throw new IllegalArgumentException("Accession must be MAGE-TAB compatible");
        }
        
        //TODO this is equivalent to accession.workFile but points to the biosd copy not the ae exp dir copy
        getLog().debug("Checking biosamples.sampletab.path property...");
        String path = ConanProperties.getProperty("biosamples.sampletab.path");  
        getLog().debug("Using path "+path);
        File outdir  = new File(path, "ae");
        outdir = new File(outdir, accession.getAccession());  
        getLog().debug("Using outdir "+outdir.toString());
        
        //MageTabFTPDownload mtftp = MageTabFTPDownload.getInstance();
        getLog().debug("Before getting MageTabFTPDownload instance");
        MageTabFTPDownload mtftp = new MageTabFTPDownload();
        getLog().debug("Got MageTabFTPDownload instance");
        String aeaccession = accession.getAccession().substring(2);
        getLog().debug("Starting to download ArrayExpress submission "+aeaccession);
        mtftp.download(aeaccession, outdir);
        getLog().debug("Finished downloading ArrayExpress submission "+aeaccession);
        
        return true;
    }

    public String getName() {
        return "updatesourcearrayexpress_deprecated";
    }

    public Collection<ConanParameter> getParameters() {
        return params;
    }
}
