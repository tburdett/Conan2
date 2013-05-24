package uk.ac.ebi.fgpt.conan.process.biosd;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

public abstract class AbstractBioSDProcess implements ConanProcess {
    
	protected final Collection<ConanParameter> parameters;
	protected final SampleTabAccessionParameter accessionParameter;

    //make sure this is kept in sync with uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils.getPathPrefix
    public static String getPathPrefix(SampleTabAccessionParameter submissionParameter) {
        return getPathPrefixFile(submissionParameter).getPath();
    }

    //make sure this is kept in sync with uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils.getPathPrefix
    public static File getPathPrefixFile(SampleTabAccessionParameter submissionParameter) {
        String submissionID = submissionParameter.getAccession();
        if (submissionID.startsWith("GMS-")) { 
            return new File("imsr", submissionID);
        } else if (submissionID.startsWith("GAE-")) {
            //split by pipeline
            String pipe = submissionID.split("-")[1];
            String ident = submissionID.split("-")[2];
            File targetfile = new File("ae", "GAE-"+pipe);
            int i = 7;
            int groupsize = 3;
            while (i < ident.length()) {
                targetfile = new File(targetfile, submissionID);
                i += groupsize;   
            }
            //return targetfile.getPath();
            return new File("ae", submissionID);
        }
        else if (submissionID.startsWith("GPR-")) {
            return new File("pride", submissionID);
        } else if (submissionID.startsWith("GVA-")) { 
            return new File("dgva", submissionID);
        } else if (submissionID.startsWith("GCR-")) { 
            return new File("coriell", submissionID);
        } else if (submissionID.startsWith("GEN-")) { 
            return new File("sra", submissionID);
        } else if (submissionID.startsWith("GEM-")) {
            //EMBLbank
            File targetfile = new File("GEM");
            int i = 7;
            int groupsize = 3;
            while (i < submissionID.length()){
                targetfile = new File(targetfile, submissionID.substring(0,i));
                i += groupsize;   
            }
            return new File(targetfile, submissionID);
        } else if (submissionID.startsWith("GNC-")) {
            //NCBI biosamples
            File targetfile = new File("GNC");
            int i = 7;
            int groupsize = 3;
            while (i < submissionID.length()){
                targetfile = new File(targetfile, submissionID.substring(0,i));
                i += groupsize;   
            }
            return new File(targetfile, submissionID);
        }  else if (submissionID.startsWith("GCM-")) {
            //COSMIC
            File targetfile = new File("GCM");
            int i = 7;
            int groupsize = 3;
            while (i < submissionID.length()){
                targetfile = new File(targetfile, submissionID.substring(0,i));
                i += groupsize;   
            }
            return new File(targetfile, submissionID);
        }  else if (submissionID.startsWith("GCG-")) {
            //TCGA - The Cancer Genome Atlas
            return new File("GCG", submissionID);
        }  else if (submissionID.startsWith("GSB-")) {
            return new File("GSB", submissionID);
        } else if (submissionID.equals("GEN")) { 
            return new File("encode", submissionID);
        } else if (submissionID.equals("G1K")) { 
            return new File("g1k", submissionID);
        } else if (submissionID.startsWith("GHM")) { 
            return new File("hapmap", submissionID);
        } else {
            throw new IllegalArgumentException("Unable to get path prefix for "+submissionID);
        }
	}
	
	public AbstractBioSDProcess() {
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
	
	public static File getDateTimeLogfile(File outdir, String prefix){
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        File logfile = new File(outdir, prefix+"_"+simpledateformat.format(new Date())+".log");
        return logfile;
	}
}
