package uk.ac.ebi.fgpt.conan.process.ae2;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.dao.DatabaseConanControlledVocabularyDAO;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Process to check experiment eligibility for ArrayExpress. Consists of tow
 * steps: check for e-mail address of submitter, check for restricted protocol
 * names
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
        "_AEEligibilityCheck" +
        "_" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()) +
        ".report";
    try {
      log = new BufferedWriter(new FileWriter(fileName));
      log.write("AE eligibility\n");
    }
    catch (IOException e) {
      throw new ProcessExecutionException(1, "Can't create report file '" +
          fileName + "'", e);
    }

    // make a new parser
    MAGETABParser parser = new MAGETABParser();


    try {
      MAGETABInvestigation investigation = parser.parse(accession.getFile());
      // I check: e-mail address of submitter
      boolean submitterWithEmail = false;
      boolean restrictedProtocolNames = false;
      int j = 0;
      for (Iterator i = investigation.IDF.personRoles.iterator();
           i.hasNext(); ) {
        String role = (String) i.next();
        if (role.equals("submitter")) {
          if (!investigation.IDF.personEmail.get(j).isEmpty()) {
            submitterWithEmail = true;
          }
        }
        j++;
      }


      if (!submitterWithEmail)
      {
        log.write("AE Eligibility Check: there are no submitters with e-mail address\n");
        throw new ProcessExecutionException(1,
                                            "There are no submitters with e-mail address");
      }
      else {
        //II check: protocol names
        for (String protocol : investigation.IDF.protocolName) {
          for (String restrictedName : controlledVocabularyDAO
              .getRestrictedProtocolNames()) {
            if (protocol.equals(restrictedName)) {
              restrictedProtocolNames = true;
            }
          }
        }
      }
      if (restrictedProtocolNames) {
        log.write("AE Eligibility Check: restricted protocol names are used\n");
        throw new ProcessExecutionException(1,
                                            "Restricted protocol names are used");
      }
    }
    catch (Exception e) {
      throw new ProcessExecutionException(1,
                                          "AE Eligibility Check: something is wrong in the code",
                                          e);
    }

    return result;
  }

  /**
   * Returns the name of this process.
   *
   * @return the name of this process
   */
  public String getName() {
    return "eligibility";
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
