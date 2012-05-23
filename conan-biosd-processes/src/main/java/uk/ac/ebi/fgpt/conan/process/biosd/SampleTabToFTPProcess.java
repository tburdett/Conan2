package uk.ac.ebi.fgpt.conan.process.biosd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Map;

import net.sourceforge.fluxion.spi.ServiceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

@ServiceProvider
public class SampleTabToFTPProcess extends AbstractBioSDProcess {

    private Logger log = LoggerFactory.getLogger(getClass());


    public String getName() {
        return "toftp";
    }
    
    public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException,
            IllegalArgumentException, InterruptedException {

        log.info(
                "Executing " + getName() + " with the following parameters: "
                        + parameters.toString());

        // deal with parameters
        SampleTabAccessionParameter accession = new SampleTabAccessionParameter();
        accession.setAccession(parameters.get(accessionParameter));
        if (accession.getAccession() == null) {
            throw new IllegalArgumentException("Accession cannot be null");
        }
        
        File dir;
        try {
            dir = getDirectory(accession);
        } catch (IOException e) {
            throw new ProcessExecutionException(1, "Error while creating directories for " + accession, e);
        }

        File sampletabFile = new File(dir, "sampletab.txt");

        File ftpDir;
        try {
            ftpDir = getFTPDirectory(accession);
        } catch (IOException e) {
            throw new ProcessExecutionException(2, "Error while creating FTP directories for " + accession, e);
        }
        
        File ftpSampletabFile = new File(ftpDir, "sampletab.txt");
        
        try {
            copyFile(sampletabFile, ftpSampletabFile);
        } catch (IOException e) {
            throw new ProcessExecutionException(2, "Error while copying to FTP for " + accession, e);
        }
        
        return true;
        
    }
    

    private File getFTPDirectory(SampleTabAccessionParameter accession) throws IOException {
        String sampletabpath = ConanProperties
                .getProperty("biosamples.ftp.path");
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
        
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        //copied from http://stackoverflow.com/a/115086/932342
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

}
