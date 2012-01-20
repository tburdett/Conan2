package uk.ac.ebi.fgpt.conan.process.ae2;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
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

      BufferedWriter log;

      String error_val = "";

      int exitValue = 0;

      //deal with parameters
      final AccessionParameter accession = new AccessionParameter();
      accession.setAccession(parameters.get(accessionParameter));

      //logging
      String reportsDir =
          accession.getFile().getParentFile().getAbsolutePath() + File.separator +
              "reports";
      File reportsDirFile = new File(reportsDir);
      if (!reportsDirFile.exists())
        reportsDirFile.mkdirs();

      String fileName = reportsDir + File.separator + accession.getAccession() +
          "_AEEligibilityCheck" +
          "_" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()) +
          ".report";
      try {
        log = new BufferedWriter(new FileWriter(fileName));
        log.write("AE Eligibility Check: START\n");
      }
      catch (IOException e) {
        exitValue = 1;
        e.printStackTrace();

        ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
        "Can't create report file '" + fileName + "'");
        String[] errors = new String[1];
        errors[0] =  "Can't create report file '" + fileName + "'";
        pex.setProcessOutput(errors);
        throw pex;
      }

      // make a new parser
      MAGETABParser parser = new MAGETABParser();

      try {
        MAGETABInvestigation investigation = parser.parse(accession.getFile().getAbsoluteFile());
        // I check: e-mail address of submitter
        boolean submitterWithEmail = false;
        boolean restrictedProtocolNames = false;
        List<String> protocolNames = new ArrayList<String>();
        int j = 0;
        for (Iterator i = investigation.IDF.personRoles.iterator();
             i.hasNext(); ) {
          String role = (String) i.next();
          //changed to "role.contains" since person could have multiple roles
          if (role.contains("submitter")) {
            try{
              if (!investigation.IDF.personEmail.get(j).isEmpty())
                submitterWithEmail = true;
            }
            catch(Exception e){
              exitValue = 1;
              System.out.println("There are no submitters with e-mail address");
              error_val = "There are no submitters with e-mail address.\n";
            }
          }
          j++;
        }


        if (!submitterWithEmail)
        {
          exitValue = 1;
          log.write("There are no submitters with e-mail address\n");
          System.out.println("There are no submitters with e-mail address");
          error_val = "There are no submitters with e-mail address.\n";
        }
        
        //II check: protocol names
        for (String protocol : investigation.IDF.protocolName) {
            for (String restrictedName : controlledVocabularyDAO
                .getRestrictedProtocolNames()) {
              if (protocol.toLowerCase().equals(restrictedName.toLowerCase())) {
                restrictedProtocolNames = true;
                protocolNames.add(protocol);
              }
            }
        }
        
        if (restrictedProtocolNames) {
          exitValue = 1;
          log.write("Restricted protocol names are used: " + protocolNames + "\n");
          System.out.println("Restricted protocol names are used: " + protocolNames);
          error_val = error_val + "Restricted protocol names are used: " + protocolNames + ".\n";
        }

        if (exitValue == 1){
            ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                   error_val);

            String[] errors = new String[1];
            errors[0] = "There are following problems in your experiment:\n" + error_val;
            pex.setProcessOutput(errors);
            throw pex;
        }
      }
      catch (ParseException e) {
        exitValue = 1;
        e.printStackTrace();
        ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                  e.getMessage());

        String[] errors = new String[1];
        errors[0] = "Please check MAGE-TAB files and/or run validation process.\n" + e.getMessage();
        pex.setProcessOutput(errors);
        throw pex;
    }
    catch (IOException e) {
      exitValue = 1;
      e.printStackTrace();
      ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                e.getMessage());

      String[] errors = new String[1];
      errors[0] = e.getMessage();
      pex.setProcessOutput(errors);
      throw pex;
    }
    catch (RuntimeException e) {
      exitValue = 1;
      e.printStackTrace();
      ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                e.getMessage());

      String[] errors = new String[1];
      errors[0] = e.getMessage();
      pex.setProcessOutput(errors);
      throw pex;
    }
      finally {
        try{
          if (exitValue ==0)
            log.write("Experiment \"" + accession.getAccession() +
                          "\" is eligible for ArrayExpress\n");
          else
             log.write("Experiment \"" + accession.getAccession() +
                          "\" is NOT eligible for ArrayExpress\n");
          log.write("AE Eligibility Check: FINISHED\n");
          log.close();
        }
        catch(IOException e){
          e.printStackTrace();
          ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                e.getMessage());

          String[] errors = new String[1];
          errors[0] = e.getMessage();
          pex.setProcessOutput(errors);
          throw pex;
        }
      }

    ProcessExecutionException pex =  new ProcessExecutionException(exitValue,"Something wrong in the code ");
    if (exitValue == 0) {
      return true;
    }
    else {
      String[] errors = new String[1];
      errors[0] = "Something wrong in the code!\n " + error_val;
      pex.setProcessOutput(errors);
      throw pex;
    }
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

   public boolean executeMockup(File file)
      throws ProcessExecutionException, IllegalArgumentException,
      InterruptedException {


      BufferedWriter log;

      String error_val = "";

      int exitValue = 0;

      //logging
      String reportsDir =
          file.getParentFile().getAbsolutePath() + File.separator +
              "reports";
      File reportsDirFile = new File(reportsDir);
      if (!reportsDirFile.exists())
        reportsDirFile.mkdirs();

      String fileName = reportsDir + File.separator + file.getName() +
          "_AEEligibilityCheck" +
          "_" + new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date()) +
          ".report";
      try {
        log = new BufferedWriter(new FileWriter(fileName));
        log.write("AE Eligibility Check: START\n");
      }
      catch (IOException e) {
        exitValue = 1;
        System.out.println("Can't create report file '" +
            fileName + "'");
        throw new ProcessExecutionException(1, "Can't create report file '" +
            fileName + "'", e);
      }

      // make a new parser
      MAGETABParser parser = new MAGETABParser();


       try {
           MAGETABInvestigation investigation = parser.parse(file.getAbsoluteFile());
           // I check: e-mail address of submitter
           boolean submitterWithEmail = false;
           boolean restrictedProtocolNames = false;
           List<String> protocolNames = new ArrayList<String>();
           int j = 0;
           for (Iterator i = investigation.IDF.personRoles.iterator();
                i.hasNext(); ) {
               String role = (String) i.next();
               //changed to "role.contains" since person could have multiple roles
               if (role.contains("submitter")) {
                   try{
                       if (!investigation.IDF.personEmail.get(j).isEmpty())
                           submitterWithEmail = true;
                   }
                   catch(Exception e){
                       exitValue = 1;
                       System.out.println("There are no submitters with e-mail address");
                       error_val = "There are no submitters with e-mail address.\n";
                   }
               }
               j++;
           }


           if (!submitterWithEmail)
           {
               exitValue = 1;
               log.write("There are no submitters with e-mail address\n");
               System.out.println("There are no submitters with e-mail address");
               error_val = "There are no submitters with e-mail address.\n";
           }

           //II check: protocol names
           for (String protocol : investigation.IDF.protocolName) {
               for (String restrictedName : controlledVocabularyDAO
                       .getRestrictedProtocolNames()) {
                   if (protocol.toLowerCase().equals(restrictedName.toLowerCase())) {
                       restrictedProtocolNames = true;
                       protocolNames.add(protocol);
                   }
               }
           }

           if (restrictedProtocolNames) {
               exitValue = 1;
               log.write("Restricted protocol names are used: " + protocolNames + "\n");
               System.out.println("Restricted protocol names are used: " + protocolNames);
               error_val = error_val + "Restricted protocol names are used: " + protocolNames + ".\n";
           }
           if (exitValue == 1){
               ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                       error_val);

               String[] errors = new String[1];
               errors[0] = "There are following problems in your experiment:\n" + error_val;
               pex.setProcessOutput(errors);
               throw pex;
           }
       }
       catch (ParseException e) {
           exitValue = 1;
           e.printStackTrace();
           ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                   e.getMessage());

           String[] errors = new String[1];
           errors[0] = "Please check MAGE-TAB files and/or run validation process.\n" + e.getMessage();
           pex.setProcessOutput(errors);
           throw pex;
       }
       catch (IOException e) {
           exitValue = 1;
           e.printStackTrace();
           ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                   e.getMessage());

           String[] errors = new String[1];
           errors[0] = e.getMessage();
           pex.setProcessOutput(errors);
           throw pex;
       }
       catch (RuntimeException e) {
           exitValue = 1;
           e.printStackTrace();
           ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                   e.getMessage());

           String[] errors = new String[1];
           errors[0] = e.getMessage();
           pex.setProcessOutput(errors);
           throw pex;
       }
       finally {
           try{
               if (exitValue ==0)
                   log.write("Experiment \"" +
                           "\" is eligible for ArrayExpress\n");
               else
                   log.write("Experiment \"" +
                           "\" is NOT eligible for ArrayExpress\n");
               log.write("AE Eligibility Check: FINISHED\n");
               log.close();
           }
           catch(IOException e){
               e.printStackTrace();
               ProcessExecutionException pex =  new ProcessExecutionException(exitValue,
                       e.getMessage());

               String[] errors = new String[1];
               errors[0] = e.getMessage();
               pex.setProcessOutput(errors);
               throw pex;
           }
       }

       ProcessExecutionException pex =  new ProcessExecutionException(exitValue,"Something wrong in the code ");
       if (exitValue == 0) {
           return true;
       }
       else {
           String[] errors = new String[1];
           errors[0] = "Something wrong in the code!\n " + error_val;
           pex.setProcessOutput(errors);
           throw pex;
       }
   }

}
