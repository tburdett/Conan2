package uk.ac.ebi.fgpt.conan.process.biosd.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.conan.dao.ConanDaemonInputsDAO;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabPRIDEAccessionParameter;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;

public class PRIDEDaemonInputs implements ConanDaemonInputsDAO {

    private Logger log = LoggerFactory.getLogger(getClass());
    
    public Class<? extends ConanParameter> getParameterType() {
        return SampleTabPRIDEAccessionParameter.class;
    }
    
    public List<String> getParameterValues() {
        //this must get a list of NEW parameter values to run
        //in this context this means ones that have not been copied yet
        

        String sampletabpath = ConanProperties
                .getProperty("biosamples.sampletab.path");
        
        List<String> parametervalues = new ArrayList<String>();
        
        File pridedir = new File("sampletabpath", "pride");
        File proj = new File(pridedir, "projects.tab.txt");
        Map<String, Set<String>> projects = new HashMap<String, Set<String>>();
        String line;
        try {
            BufferedReader projfile = new BufferedReader(new FileReader(proj));
            while ((line = projfile.readLine()) != null){
                String[] data = line.trim().split("\t");
                String projname = data[0].trim();
                
                if (!projects.containsKey(projname)){
                    projects.put(projname, new HashSet<String>());
                }
                
                for (int i = 1 ; i < data.length; i++){
                    String accession = data[i].trim();
                    projects.get(projname).add(accession);
                }
            }
        } catch (IOException e) {
            log.error("Unable to read "+proj);
            e.printStackTrace();
            return null;
        }
        
        for (String projname : projects.keySet()){
            //TODO finisheme
        }
        
        return parametervalues;
    }

}
