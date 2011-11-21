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
        
        String path = ConanProperties.getProperty("biosamples.sampletab.path");        

        // deal with parameters
        SampleTabAccessionParameter accession = new SampleTabAccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }
        if (!testIfMageTabAccession(accession)) {
            throw new IllegalArgumentException("Accession must be MAGE-TAB compatible");
        }
        
        //TODO this is equivalent to accession.workFile but points to the biosd copy not the ae exp dir copy
        File outdir  = new File(path, "ae");
        outdir = new File(outdir, "GA"+accession.getAccession().substring(2));
        
        MageTabFTPDownload mtftp = MageTabFTPDownload.getInstance();
        mtftp.download(accession.getAccession().substring(2), outdir);
        
        return true;
    }

    public String getName() {
        return "updatesourcearrayexpress";
    }

    public Collection<ConanParameter> getParameters() {
        return params;
    }
}
