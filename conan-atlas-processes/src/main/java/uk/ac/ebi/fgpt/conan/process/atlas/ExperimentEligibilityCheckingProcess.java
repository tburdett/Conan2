package uk.ac.ebi.fgpt.conan.process.atlas;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.HybridizationNode;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.dao.DatabaseConanControlledVocabularyDAO;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.*;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.*;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Process to check experiment eligibility for Atlas. Consists of five steps:
 * check for experiment type, check for two-channel experiment, check for factor
 * values, check for array design existence in Atlas, check for raw data files
 * for Affy and derived data files for all other platforms.
 *
 * @author Natalja Kurbatova
 * @date 15/02/11
 */
@ServiceProvider
public class ExperimentEligibilityCheckingProcess implements ConanProcess {

  // Add to the desired logger
  private BufferedWriter log;

  private final Collection<ConanParameter> parameters;
  private final AccessionParameter accessionParameter;

  private List<String> ArrayDesignAccessions = new ArrayList<String>();

  private final DatabaseConanControlledVocabularyDAO controlledVocabularyDAO;

  /**
   * Constructor for process. Initializes conan2 parameters for the process.
   */
  public ExperimentEligibilityCheckingProcess() {
    parameters = new ArrayList<ConanParameter>();
    accessionParameter = new AccessionParameter();
    parameters.add(accessionParameter);
    ClassPathXmlApplicationContext ctx =
        new ClassPathXmlApplicationContext("controlled-vocabulary-context.xml");
    controlledVocabularyDAO =
        ctx.getBean("databaseConanControlledVocabularyDAO",
                    DatabaseConanControlledVocabularyDAO.class);
  }

  protected void finalize() throws Throwable
  {
    log.close();
  }


  public boolean execute(Map<ConanParameter, String> parameters)
      throws ProcessExecutionException, IllegalArgumentException,
      InterruptedException {

    boolean result = true;
    //deal with parameters
    AccessionParameter accession = new AccessionParameter();
    accession.setAccession(parameters.get(accessionParameter));

    //logging
    String reportsDir =
        accession.getFile().getParentFile().getAbsolutePath() + File.separator +
            "reports";
    String fileName = reportsDir + File.separator + accession.getAccession() +
        "_AtlasEligibilityCheck" +
        "_" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()) +
        ".report";
    try {
      // Add to the desired logger
      log = new BufferedWriter(new FileWriter(fileName));
      log.write("Atlas_eligibility\n");
    }
    catch (IOException e) {
      throw new ProcessExecutionException(1, "Can't create report file '" +
          fileName + "'", e);
    }

    // make a new parser
    MAGETABParser parser = new MAGETABParser();


    try {
      MAGETABInvestigation investigation = parser.parse(accession.getFile());
      // I check: experiment types
      boolean isAtlasType = false;
      for (String exptType : investigation.IDF.getComments()
          .get("AEExperimentType")) {
        for (String AtlasType : controlledVocabularyDAO
            .getAtlasExperimentTypes()) {
          if (exptType.equals(AtlasType)) {
            isAtlasType = true;
          }
        }
      }

      if (!isAtlasType)
      //not in Atlas Experiment Types
      {
        log.write("Atlas Eligibility Check: 'Experiment Type' is not accepted by Atlas\n");
        throw new ProcessExecutionException(1,"Atlas Eligibility Check: 'Experiment Type' is not accepted by Atlas");
      }
      else {
        //Ia two-channel experiment
        if (investigation.SDRF.getNumberOfChannels() > 1) {
          log.write("Atlas Eligibility Check: two-channel experiment is not accepted by Atlas\n");
          throw new ProcessExecutionException(1,"Atlas Eligibility Check: two-channel experiment is not accepted by Atlas");
        }

        // II check: array design is in Atlas
        Collection<HybridizationNode> hybridizationNodes =
            investigation.SDRF.getNodes(HybridizationNode.class);
        Collection<ArrayDataNode> rawDataNodes =
            investigation.SDRF.getNodes(ArrayDataNode.class);
        Collection<DerivedArrayDataNode> processedDataNodes =
            investigation.SDRF.getNodes(DerivedArrayDataNode.class);
        Collection<DerivedArrayDataMatrixNode> processedDataMatrixNodes =
            investigation.SDRF.getNodes(DerivedArrayDataMatrixNode.class);
        int factorValues = 0;
        for (HybridizationNode hybNode : hybridizationNodes)

        {
          if (hybNode.factorValues.size() > 0) {
            factorValues++;
          }
          for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns) {
            if (!ArrayDesignAccessions
                .contains(arrayDesign.getAttributeValue())) {
              ArrayDesignAccessions.add(arrayDesign.getAttributeValue());
            }
          }
        }

        //Ib presence pf factor values
        if (factorValues == 0) {
          log.write("Atlas Eligibility Check: experiment does not have Factor Values\n");
          throw new ProcessExecutionException(1,"Atlas Eligibility Check: experiment does not have Factor Values");
        }
        for (String arrayDesign : ArrayDesignAccessions) {
          ArrayDesignExistenceChecking arrayDesignExistenceChecking =
              new ArrayDesignExistenceChecking();
          String arrayCheckResult =
              arrayDesignExistenceChecking.execute(arrayDesign);
          if (arrayCheckResult.equals("empty") ||
              arrayCheckResult.equals("no")) {
            log.write("Atlas Eligibility Check: array design '" +
                arrayDesign + "' used in experiment is not in Atlas\n");
            throw new ProcessExecutionException(1,"Atlas Eligibility Check: array design '" +
                arrayDesign + "' used in experiment is not in Atlas");
          }

          else {
            //Array Design is in Atlas
            Collection<HybridizationNode> hybridizationSubNodes =
                new ArrayList<HybridizationNode>();
            Collection<Node> rawDataSubNodes = new ArrayList<Node>();
            Collection<Node> processedDataSubNodes = new ArrayList<Node>();
            Collection<Node> processedDataMatrixSubNodes =
                new ArrayList<Node>();
            //Number of arrays in experiment
            if (ArrayDesignAccessions.size() > 1) {

              for (HybridizationNode hybNode : hybridizationNodes) {
                ArrayDesignAttribute attribute = new ArrayDesignAttribute();
                attribute.setAttributeValue(arrayDesign);

                if (hybNode.arrayDesigns.contains(attribute)) {
                  //get data nodes for particular array design
                  hybridizationSubNodes.add(hybNode);
                  getNodes(hybNode, ArrayDataNode.class, rawDataSubNodes);
                  getNodes(hybNode, DerivedArrayDataNode.class,
                           processedDataSubNodes);
                  getNodes(hybNode, DerivedArrayDataMatrixNode.class,
                           processedDataMatrixSubNodes);

                }

              }
            }
            else {
              //one array design in experiment
              hybridizationSubNodes = hybridizationNodes;
              for (ArrayDataNode node : rawDataNodes) {
                rawDataSubNodes.add(node);
              }
              for (DerivedArrayDataNode node : processedDataNodes) {
                processedDataSubNodes.add(node);
              }
              for (DerivedArrayDataMatrixNode node : processedDataMatrixNodes) {
                processedDataMatrixSubNodes.add(node);
              }

            }

            //III check: if Affy then check for raw data files, else for derived
            if (arrayCheckResult.equals("affy") &&
                hybridizationSubNodes.size() != rawDataSubNodes.size())
            //affy
            {
              log.write("Atlas Eligibility Check: Affymetrix experiment without raw data files\n");
              throw new ProcessExecutionException(1,"Atlas Eligibility Check: Affymetrix experiment without raw data files");
            }
            else
              //not affy
              if (processedDataSubNodes.size() == 0 &&
                  processedDataMatrixSubNodes.size() == 0) {
                log.write("Atlas Eligibility Check: non-Affymetrix experiment without processed data files\n");
                throw new ProcessExecutionException(1,"Atlas Eligibility Check: non-Affymetrix experiment without processed data files");
              }
          }
        }
      }
    }

    catch (Exception e) {
      throw new ProcessExecutionException(1, "Atlas Eligibility Check: something is wrong in the code", e);
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

    try {
      MAGETABInvestigation investigation = parser.parse(idfFile);
      // I check: experiment types
      boolean isAtlasType = false;
      for (String exptType : investigation.IDF.getComments()
          .get("AEExperimentType")) {
        for (String AtlasType : types) {
          System.out.println("Type: " + AtlasType);

          if (exptType.equals(AtlasType)) {
            isAtlasType = true;
          }
        }
      }
      System.out.println("I Experiment Type checking result: " + isAtlasType);
      if (!isAtlasType)
      //not in Atlas Experiment Types
      {
        return false;
      }
      else {
        // II check: array design is in Atlas
        if (investigation.SDRF.getNumberOfChannels() > 1) {
          System.out.println("Ia Number of channels: " +
                                 investigation.SDRF.getNumberOfChannels());
          return false;
        }
        Collection<HybridizationNode> hybridizationNodes =
            investigation.SDRF.getNodes(HybridizationNode.class);
        Collection<ArrayDataNode> rawDataNodes =
            investigation.SDRF.getNodes(ArrayDataNode.class);
        Collection<DerivedArrayDataNode> processedDataNodes =
            investigation.SDRF.getNodes(DerivedArrayDataNode.class);
        Collection<DerivedArrayDataMatrixNode> processedDataMatrixNodes =
            investigation.SDRF.getNodes(DerivedArrayDataMatrixNode.class);
        for (HybridizationNode hybNode : hybridizationNodes) {

          for (ArrayDesignAttribute arrayDesign : hybNode.arrayDesigns) {
            if (!ArrayDesignAccessions
                .contains(arrayDesign.getAttributeValue())) {
              ArrayDesignAccessions.add(arrayDesign.getAttributeValue());

            }
          }

        }
        for (String arrayDesign : ArrayDesignAccessions) {
          System.out.println("ARRAY: " + arrayDesign);
          ArrayDesignExistenceChecking arrayDesignExistenceChecking =
              new ArrayDesignExistenceChecking();
          String arrayCheckResult =
              arrayDesignExistenceChecking.execute(arrayDesign);
          if (arrayCheckResult.equals("empty") ||
              arrayCheckResult.equals("no")) {
            System.out.println(
                "II Array design checking result: " + arrayCheckResult);
            return false;
          }
          else {
            System.out.println(
                "II Array design checking result: " + arrayCheckResult);
            Collection<HybridizationNode> hybridizationSubNodes =
                new ArrayList<HybridizationNode>();
            Collection<Node> rawDataSubNodes = new ArrayList<Node>();
            Collection<Node> processedDataSubNodes = new ArrayList<Node>();
            Collection<Node> processedDataMatrixSubNodes =
                new ArrayList<Node>();
            if (ArrayDesignAccessions.size() > 1) {

              for (HybridizationNode hybNode : hybridizationNodes) {
                ArrayDesignAttribute attribute = new ArrayDesignAttribute();
                attribute.setAttributeValue(arrayDesign);

                if (hybNode.arrayDesigns.contains(attribute)) {
                  //get data nodes for particular array design
                  hybridizationSubNodes.add(hybNode);
                  getNodes(hybNode, ArrayDataNode.class, rawDataSubNodes);
                  getNodes(hybNode, DerivedArrayDataNode.class,
                           processedDataSubNodes);
                  getNodes(hybNode, DerivedArrayDataMatrixNode.class,
                           processedDataMatrixSubNodes);

                }

              }
            }
            else {
              hybridizationSubNodes = hybridizationNodes;
              for (ArrayDataNode node : rawDataNodes) {
                rawDataSubNodes.add(node);
              }
              for (DerivedArrayDataNode node : processedDataNodes) {
                processedDataSubNodes.add(node);
              }
              for (DerivedArrayDataMatrixNode node : processedDataMatrixNodes) {
                processedDataMatrixSubNodes.add(node);
              }

            }

            //III check: if Affy then check for raw data files, else for derived
            if (arrayCheckResult.equals("affy"))
            //affy


            {
              if (hybridizationSubNodes.size() == rawDataSubNodes.size()) {
                System.out.println(
                    "III Affy raw data files checking result: " + "true");
              }
              else {
                System.out.println(
                    "III Affy raw data files checking result: " + "false");
                return false;
              }
            }
            else {
              //not affy
              if (processedDataSubNodes.size() == 0 &&
                  processedDataMatrixSubNodes.size() == 0) {
                System.out.println(
                    "III Derived data files checking result: " + "false");
                return false;
              }
              else {
                System.out.println(
                    "III Derived data files checking result: " + "true");
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

  private Collection<Node> getNodes(Node parentNode, Class typeOfNode,
                                    Collection<Node> nodes) {
    for (Node childNode : parentNode.getChildNodes()) {
      if (childNode.getClass().equals(typeOfNode) &&
          !nodes.contains(childNode)) {
        nodes.add(childNode);
      }
      else {
        getNodes(childNode, typeOfNode, nodes);
      }
    }
    return nodes;
  }

  /**
   * Returns the name of this process.
   *
   * @return the name of this process
   */
  public String getName() {
    return "atlas eligibility";
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
