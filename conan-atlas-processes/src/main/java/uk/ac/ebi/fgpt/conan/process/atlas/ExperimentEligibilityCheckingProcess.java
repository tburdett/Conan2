package uk.ac.ebi.fgpt.conan.process.atlas;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.mged.magetab.error.ErrorCode;
import org.mged.magetab.error.ErrorItem;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.HybridizationNode;
import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.dao.DatabaseConanControlledVocabularyDAO;
import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.rest.AbstractRESTAPIProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.*;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.*;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import javax.sql.DataSource;
import java.io.File;
import java.util.*;

/**
 * Process to load experiment into Atlas
 *
 * @author Natalja Kurbatova
 * @date 15/02/11
 */

public class ExperimentEligibilityCheckingProcess implements ConanProcess{


  private final Collection<ConanParameter> parameters;
  private final AccessionParameter accessionParameter;

  private List<String> ArrayDesignAccessions = new ArrayList<String>();

  private DatabaseConanControlledVocabularyDAO controlledVocabularyDAO;

  /**
   * Constructor for process. Initializes conan2 parameters for the process.
   */
  public ExperimentEligibilityCheckingProcess() {
    parameters = new ArrayList<ConanParameter>();
    accessionParameter = new AccessionParameter();
    parameters.add(accessionParameter);
  }


  public boolean execute(Map<ConanParameter, String> parameters)
      throws ProcessExecutionException, IllegalArgumentException,
      InterruptedException {

      boolean result = false;
      //deal with parameters
      AccessionParameter accession = new AccessionParameter();
      accession.setAccession(parameters.get(accessionParameter));

      // make a new parser
      MAGETABParser parser = new MAGETABParser();

      // add an error item listener to the parser, this one just reports parsing errors as stdout
      parser.addErrorItemListener(new ErrorItemListener() {

      public void errorOccurred(ErrorItem item) {
        // locate the error code from the enum, to check the generic message
        ErrorCode code = null;
        for (ErrorCode ec : ErrorCode.values()) {
          if (item.getErrorCode() == ec.getIntegerValue()) {
            code = ec;
            break;
          }
        }

        if (code != null) {
          // this just dumps out some info about the type of error
          System.out.println("Listener reported error...");
          System.out.println("\tError Code: " + item.getErrorCode() + " [" +
              code.getErrorMessage() + "]");
          System.out.println("\tError message: " + item.getMesg());
          System.out.println("\tCaller: " + item.getCaller());
        }
      }
    });

    // now, parse from a file
    File idfFile = accession.getFile();

    // print some stdout info
    System.out.println("Parsing " + idfFile.getAbsolutePath() + "...");

    try{
      MAGETABInvestigation investigation = parser.parse(idfFile);
      // I check: experiment types
      boolean isAtlasType=false;
      for (String exptType : investigation.IDF.getComments().get("AEExperimentType")){
        for (String AtlasType : controlledVocabularyDAO.getAEExperimentTypes()) {
           if (exptType.equals(AtlasType))
             isAtlasType=true;
        }
      }

      if (!isAtlasType)
        //not in Atlas Experiment Types
        return false;
      else {
        // II check: array design is in Atlas
        Collection<HybridizationNode> hybridizationNodes =  investigation.SDRF.getNodes(HybridizationNode.class);
        for (HybridizationNode hybNode : hybridizationNodes){
                for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns)
                  if (!ArrayDesignAccessions.contains(arrayDesign.getAttributeValue()))
                        ArrayDesignAccessions.add(arrayDesign.getAttributeValue());
        }
        for (String arrayDesign : ArrayDesignAccessions){
          ArrayDesignExistenceChecking arrayDesignExistenceChecking = new ArrayDesignExistenceChecking();
          String arrayCheckResult = arrayDesignExistenceChecking.execute(arrayDesign);
          if (!arrayCheckResult.equals("empty") && !arrayCheckResult.equals("no"))
            result = false;
          else {
            //III check: if Affy then check for raw data files, else for derived
            //Todo: get array name - check for "affy" in lower case afterwards do files checking
          }
        }
      }
    }
    catch (Exception e) {
      result = false;
    }

    return result;
  }


   public boolean executeMockup(String file, List<String> types)
      throws ProcessExecutionException, IllegalArgumentException,
      InterruptedException {

      boolean result = false;

      // make a new parser
      MAGETABParser parser = new MAGETABParser();

      // now, parse from a file
      File idfFile = new File(file);


      // print some stdout info
      System.out.println("Parsing " + idfFile.getAbsolutePath() + "...");

      try{
        MAGETABInvestigation investigation = parser.parse(idfFile);
        // I check: experiment types
        boolean isAtlasType=false;
        for (String exptType : investigation.IDF.getComments().get("AEExperimentType")){
          for (String AtlasType : types) {
            System.out.println("Type: " + AtlasType );

             if (exptType.equals(AtlasType))
               isAtlasType=true;
          }
        }
        System.out.println("I Experiment Type checking result: " + isAtlasType);
        if (!isAtlasType)
          //not in Atlas Experiment Types
          return false;
        else {
          // II check: array design is in Atlas
          if (investigation.SDRF.getNumberOfChannels()>1){
            System.out.println("Ia Number of channels: " + investigation.SDRF.getNumberOfChannels());
            return false;
          }
          Collection<HybridizationNode> hybridizationNodes =  investigation.SDRF.getNodes(HybridizationNode.class);
          Collection<ArrayDataNode> rawDataNodes =  investigation.SDRF.getNodes(ArrayDataNode.class);
          Collection<DerivedArrayDataNode> processedDataNodes =  investigation.SDRF.getNodes(DerivedArrayDataNode.class);
          Collection<DerivedArrayDataMatrixNode> processedDataMatrixNodes =  investigation.SDRF.getNodes(DerivedArrayDataMatrixNode.class);
          for (HybridizationNode hybNode : hybridizationNodes){

                  for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns)
                    if (!ArrayDesignAccessions.contains(arrayDesign.getAttributeValue())) {
                          ArrayDesignAccessions.add(arrayDesign.getAttributeValue());

                    }

          }
          for (String arrayDesign : ArrayDesignAccessions){
            System.out.println("ARRAY: " + arrayDesign);
            ArrayDesignExistenceChecking arrayDesignExistenceChecking = new ArrayDesignExistenceChecking();
            String arrayCheckResult = arrayDesignExistenceChecking.execute(arrayDesign);
            if (arrayCheckResult.equals("empty") || arrayCheckResult.equals("no")) {
              System.out.println("II Array design checking result: " + arrayCheckResult);
              return false;
            }
            else {
               System.out.println("II Array design checking result: " + arrayCheckResult);
               Collection<HybridizationNode> hybridizationSubNodes = new ArrayList<HybridizationNode>();
               Collection<Node> rawDataSubNodes = new ArrayList<Node>();
               Collection<Node> processedDataSubNodes =  new ArrayList<Node>();
               Collection<Node> processedDataMatrixSubNodes = new ArrayList<Node>();
               if (ArrayDesignAccessions.size()>1){

                 for (HybridizationNode hybNode : hybridizationNodes){
                      ArrayDesignAttribute attribute = new ArrayDesignAttribute();
                      attribute.setAttributeValue(arrayDesign);

                    if (hybNode.arrayDesigns.contains(attribute)) {
                      //get data nodes for particular array design
                      hybridizationSubNodes.add(hybNode);
                      getNodes(hybNode,ArrayDataNode.class,rawDataSubNodes);
                      getNodes(hybNode,DerivedArrayDataNode.class,processedDataSubNodes);
                      getNodes(hybNode,DerivedArrayDataMatrixNode.class,processedDataMatrixSubNodes);

                    }

                  }
               }
               else {
                hybridizationSubNodes = hybridizationNodes;
                for (ArrayDataNode node : rawDataNodes)
                  rawDataSubNodes.add((Node)node);
                for (DerivedArrayDataNode node : processedDataNodes)
                  processedDataSubNodes.add((Node)node);
                for (DerivedArrayDataMatrixNode node : processedDataMatrixNodes)
                  processedDataMatrixSubNodes.add((Node)node);

               }

               //III check: if Affy then check for raw data files, else for derived
               if (arrayCheckResult.equals("affy") )
                //affy


                if (hybridizationSubNodes.size()==rawDataSubNodes.size()){
                  System.out.println("III Affy raw data files checking result: " + "true");
                }
               else{
                System.out.println("III Affy raw data files checking result: " + "false");
                return false;
               }
               else {
                 //not affy
                 if (processedDataSubNodes.size()==0 && processedDataMatrixSubNodes.size()==0)  {
                     System.out.println("III Derived data files checking result: " + "false");
                     return false;
                 }
                 else {
                    System.out.println("III Derived data files checking result: " + "true");
                 }
               }
            }
          }
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        result = false;
      }

    return result;
  }

  private Collection<Node> getNodes(Node parentNode, Class typeOfNode,Collection<Node> nodes){
    for (Node childNode : parentNode.getChildNodes()){
        if (childNode.getClass().equals(typeOfNode) && !nodes.contains(childNode))
           nodes.add(childNode);
        else
          getNodes(childNode, typeOfNode, nodes);
    }
    return nodes;
  }
  /**
   * Returns the name of this process.
   *
   * @return the name of this process
   */
  public String getName() {
    return "atlas eligibility check";
  }

  /**
   * Returns a collection of strings representing the names of the parameters.
   *
   * @return the parameter names required to generate a task
   */
  public Collection<ConanParameter> getParameters() {
    return parameters;
  }


}
