package uk.ac.ebi.fgpt.conan.process.biosd.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.dao.ConanDaemonInputsDAO;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabAEAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

public class MageTabDaemonInputs implements ConanDaemonInputsDAO {

    private Logger log = LoggerFactory.getLogger(getClass());
    
    public Class<? extends ConanParameter> getParameterType() {
        return SampleTabAEAccessionParameter.class;
    }

    
    public List<String> getParameterValues() {
        //this must get a list of NEW parameter values to run
        //in this context this means ones that have not been copied yet
        

        String sampletabpath = ConanProperties
                .getProperty("biosamples.sampletab.path");
        
        List<String> parametervalues = new ArrayList<String>();
        
        //TODO finish
        
        return parametervalues;
    }

}
