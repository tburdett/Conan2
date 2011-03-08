//package uk.ac.ebi.fgpt.conan.process.ae2;
//
//import net.sourceforge.fluxion.spi.ServiceProvider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.BeanCreationException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//import uk.ac.ebi.arrayexpress2.AE2ApplicationVersion;
//import uk.ac.ebi.arrayexpress2.AE2DataFormat;
//import uk.ac.ebi.arrayexpress2.AE2DataVersion;
//import uk.ac.ebi.arrayexpress2.MAGETABLoaderParameters;
//import uk.ac.ebi.arrayexpress2.client.JobSubmissionClient;
//import uk.ac.ebi.arrayexpress2.exception.SubmissionFailureException;
//import uk.ac.ebi.arrayexpress2.exception.UnreadableSourceException;
//import uk.ac.ebi.arrayexpress2.exception.UnsupportedFormatException;
//import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
//import uk.ac.ebi.fgpt.conan.ae.lsf.AE2Component;
//import uk.ac.ebi.fgpt.conan.model.ConanParameter;
//import uk.ac.ebi.fgpt.conan.model.ConanProcess;
//import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Map;
//
///**
// * Process for experiment loading by using MAGETABLoader Client
// *
// * @author Natalja Kurbatova
// * @date 22-Nov-2010
// */
//
//@ServiceProvider
//public class ExperimentLoadingWithClientProcess
//    extends JobSubmissionClient<MAGETABLoaderParameters>
//    implements ConanProcess {
//
//  private final Collection<ConanParameter> parameters;
//  private final AccessionParameter accessionParameter;
//
//  private boolean success = false;
//  private boolean completed = false;
//
//  private Logger log = LoggerFactory.getLogger(getClass());
//
//  protected Logger getLog() {
//        return log;
//  }
//
//  public ExperimentLoadingWithClientProcess() {
//        parameters = new ArrayList<ConanParameter>();
//        accessionParameter = new AccessionParameter();
//        parameters.add(accessionParameter);
//  }
//
//  public boolean execute(Map<ConanParameter, String> parameters)
//      throws ProcessExecutionException, IllegalArgumentException,
//      InterruptedException {
//      getLog().debug("Executing experiment loading with MAGETABLoader Client: " + parameters);
//        // deal with parameters
//        AccessionParameter accession = new AccessionParameter();
//        accession.setAccession(parameters.get(accessionParameter));
//        if (accession.getAccession() == null) {
//            throw new IllegalArgumentException("Accession cannot be null");
//        }
//        else {
//            //execution
//            if (accession.isExperiment()) {
//              try {
//                // default values
//                boolean isValidatingFile = true;
//                boolean isLinkingArrayDesign = true;
//                boolean isLinkingArrayDesignWithWarning = false;
//                boolean isCreatingUser = true;
//                boolean isTracking = false;
//                ApplicationContext context =
//                new ClassPathXmlApplicationContext("applicationContext.xml");
//                final JobSubmissionClient loader =
//                  (JobSubmissionClient) context.getBean("mageTabLoader");
//
//                MAGETABLoaderParameters loaderParameters = new MAGETABLoaderParameters();
//                loaderParameters.setMagetabFile(new File(accession.getFile().getParentFile().getAbsolutePath()));
//                loaderParameters.setFileValidation(isValidatingFile);
//                loaderParameters.setArrayDesignLinkage(isLinkingArrayDesign);
//                loaderParameters.setArrayDesignLinkageWithWarnOnly(isLinkingArrayDesignWithWarning);
//                loaderParameters.setUserCreation(isCreatingUser);
//                loaderParameters.setTracking(isTracking);
//
//                final String submissionId = loader.submit(loaderParameters,
//                                                AE2DataFormat.MAGETAB,
//                                                AE2DataVersion.V1_0,
//                                                AE2ApplicationVersion.LATEST);
//
//                getLog().debug(
//                  "Your job was submitted successfully (ID = " + submissionId + ")!\n");
//
//                getLog().debug("Loading...");
//
//                //monitoring
//                Runnable progressUpdater = new Runnable()     {
//                  public void run(){
//                    try {
//                      while (loader.getImportService().
//                          getSubmissionProgress(submissionId) < 100) {
//                        synchronized (this) {
//                          try {
//                            wait(10);
//                          }
//                          catch (InterruptedException e) {
//                            // do nothing, just resume
//                          }
//                        }
//                      }
//                      getLog().debug("100% Submission loaded ok!");
//                      setSuccess(true);
//                     }
//
//                    catch (SubmissionFailureException e) {
//                      int exitValue = 1;
//                      //todo get correct exit value
//                      setSuccess(false);
//                      String message =
//                          "Your submission failed to load for the following reason: " +
//                          e.getCause().getMessage();
//                      Throwable rootCause = e.getCause().getCause();
//                      if (rootCause != null && rootCause.getMessage() != null) {
//                        message = message +
//                            " REASON: " + rootCause.getMessage();
//                      }
//                      try {
//                        throw new ProcessExecutionException(exitValue, "Failed at " + getName() + ": " + message, e);
//                      }
//                      catch (ProcessExecutionException e1) {
//                        //do nothing
//                      }
//                    }
//                    catch (Exception e) {
//                      int exitValue = 1;
//                      //todo get correct exit value
//                      setSuccess(false);
//                      String message =
//                          "Your submission failed to load for the following reason: " +
//                          "An unexpected runtime error occurred - " +
//                          e.getClass().getSimpleName() + " " + e.getMessage();
//                      try {
//                        throw new ProcessExecutionException(exitValue, "Failed at " + getName() + ": " + message, e);
//                      }
//                      catch (ProcessExecutionException e1) {
//                        //do nothing
//                      }
//                    }
//                  }
//                };
//                new Thread(progressUpdater).start();
//            }
//            catch (UnsupportedFormatException e) {
//              setSuccess(false);
//              try {
//                throw new ProcessExecutionException(1, "Failed at " + getName() + ": " + e.getMessage(), e);
//              }
//              catch (ProcessExecutionException e1) {
//                //do nothing
//              }
//            }
//            catch (UnreadableSourceException e) {
//              setSuccess(false);
//              try {
//                throw new ProcessExecutionException(1, "Failed at " + getName() + ": " + e.getMessage(), e);
//              }
//              catch (ProcessExecutionException e1) {
//                //do nothing
//              }
//            }
//            catch (BeanCreationException bce) {
//              setSuccess(false);
//              try {
//                throw new ProcessExecutionException(1, "Failed at " + getName() + ": " + "MAGETABLoader server problems - " +bce.getMessage(), bce);
//              }
//              catch (ProcessExecutionException e1) {
//                //do nothing
//              }
//            }
//            catch (Exception e) {
//              setSuccess(false);
//              try {
//                throw new ProcessExecutionException(1, "Failed at " + getName() + ": " + e.getMessage(), e);
//              }
//              catch (ProcessExecutionException e1) {
//                //do nothing
//              }
//            }
//            }
//        else {
//              setSuccess(false);
//              throw new IllegalArgumentException("Experiment Loader loads experiments, not arrays");
//        }
//     }
//
//     return getSuccess();
//  }
//
//  private synchronized void setSuccess(boolean success) {
//    this.success = success;
//    this.completed = true;
//    notifyAll();
//  }
//
//  /**
//   * Blocks until monitoring is complete
//   *
//   * @return true if completed successfully
//   */
//  private synchronized boolean getSuccess() {
//    while (!completed) {
//      // wait for completion
//      synchronized (this) {
//        try {
//          wait();
//        }
//        catch (InterruptedException e) {
//          // ignore
//        }
//      }
//    }
//
//    return success;
//  }
//
//  public String getName() {
//        return "experiment loading with MAGETABLoader Client";
//  }
//
//  public Collection<ConanParameter> getParameters() {
//    return parameters;
//  }
//
//  protected AE2Component getAE2Component() {
//         return AE2Component.EXPERIMENT_LOADER;
//  }
//
//}
