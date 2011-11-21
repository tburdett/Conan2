package uk.ac.ebi.fgpt.conan.process.biosd;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
import uk.ac.ebi.fgpt.sampletab.MageTabToSampleTab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@ServiceProvider
public class MageTabToSampleTabProcess implements ConanProcess {
    private final Collection<ConanParameter> params;
    private final SampleTabAccessionParameter accessionParameter;

    private Logger log = LoggerFactory.getLogger(getClass());

    public MageTabToSampleTabProcess() {
        params = new ArrayList<ConanParameter>();
        accessionParameter = new SampleTabAccessionParameter();
    	params.add(accessionParameter);
    }

    protected Logger getLog() {
        return log;
    }

    public boolean execute(Map<ConanParameter, String> parameters)
            throws IllegalArgumentException, ProcessExecutionException, InterruptedException {
        getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());
        
        String path = ConanProperties.getProperty("biosamples.sampletab.path");
        MageTabToSampleTab mt2st = MageTabToSampleTab.getInstance();
        

        // deal with parameters
        SampleTabAccessionParameter accession = new SampleTabAccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }
        if (!accession.testIfMageTabAccession()) {
            throw new IllegalArgumentException("Accession must be MAGE-TAB compatible");
        }
        
        //TODO this is equivalent to accession.workFile but points to the biosd copy not the ae exp dir copy
        File outdir  = new File(path, "ae");
        outdir = new File(outdir, "GA"+accession.getMageTabAccession());
        File mtfile = new File(outdir, accession.getMageTabAccession() + ".idf.txt");
        File stfile = new File(outdir, "sampletab.pre.txt");
        
        try {
			mt2st.convert(mtfile, stfile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ProcessExecutionException(999, "Error accessing files");
		} catch (ParseException e) {
			e.printStackTrace();
			throw new ProcessExecutionException(998, "Error parsing files");
		}
        
        return true;
    }

    public String getName() {
        return "topresampletabarrayexpress";
    }

    public Collection<ConanParameter> getParameters() {
        return params;
    }
}
