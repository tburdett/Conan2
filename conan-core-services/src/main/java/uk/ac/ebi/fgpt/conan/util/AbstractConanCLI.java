package uk.ac.ebi.fgpt.conan.util;

import org.apache.commons.cli.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExecutionContext;
import uk.ac.ebi.fgpt.conan.core.context.DefaultExternalProcessConfiguration;
import uk.ac.ebi.fgpt.conan.core.context.locality.Local;
import uk.ac.ebi.fgpt.conan.core.context.locality.LocalityFactory;
import uk.ac.ebi.fgpt.conan.core.context.scheduler.SchedulerFactory;
import uk.ac.ebi.fgpt.conan.factory.DefaultTaskFactory;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.model.context.ExecutionContext;
import uk.ac.ebi.fgpt.conan.model.context.ExternalProcessConfiguration;
import uk.ac.ebi.fgpt.conan.model.context.Locality;
import uk.ac.ebi.fgpt.conan.model.context.Scheduler;
import uk.ac.ebi.fgpt.conan.model.param.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.param.ParamMap;
import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * When creating an application that uses conan that can be controlled from the command line, this abstract class provides
 * some commonly used functionality to save reimplementing in each application.  Instead, just extend this class in your
 * client app and pass the apache commons-cli command line object to this abstract classes constructor.
 */
public abstract class AbstractConanCLI {


    private static Logger log = LoggerFactory.getLogger(AbstractConanCLI.class);

    /**
     * Gets the likely root of the application on the file system.  Warning: use this with caution!
     */
    public static final File APPLICATION_DIR =
            new File(AbstractConanCLI.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile()
                    .getParentFile();

    // **** Option parameter names ****
    public static final String OPT_ENV_CONFIG = "env_config";
    public static final String OPT_LOG_CONFIG = "log_config";
    public static final String OPT_OUTPUT_DIR = "output";
    public static final String OPT_JOB_PREFIX = "job_prefix";
    public static final String OPT_VERBOSE = "verbose";
    public static final String OPT_HELP = "help";

    // **** Defaults ****
    public static final String  DEFAULT_ENV_CONFIG_FILENAME = "conan.properties";
    public static final String  DEFAULT_LOG_CONFIG_FILENAME = "log4j.properties";


    // **** Options ****
    private String appName;
    private File defaultSettingsDir;

    private File environmentConfig;
    private File logConfig;
    private File outputDir;
    private String jobPrefix;

    private boolean verbose;
    private boolean help;


    protected AbstractConanCLI(String appName, File defaultSettingsDir) throws IOException {

        this(
            appName,
            defaultSettingsDir,
            new File(defaultSettingsDir, DEFAULT_ENV_CONFIG_FILENAME),
            new File(defaultSettingsDir, DEFAULT_LOG_CONFIG_FILENAME),
            currentWorkingDir(),
            appName + createTimestamp(),
            false,
            false);
    }


    protected AbstractConanCLI(String appName, File defaultSettingsDir,
                               File environmentConfig, File logConfig,
                               File outputDir, String jobPrefix,
                               boolean verbose, boolean help)
            throws IOException {

        // Set member vars
        this.appName = appName;
        this.defaultSettingsDir = defaultSettingsDir;

        this.environmentConfig = environmentConfig;
        this.logConfig = logConfig;
        this.outputDir = outputDir;
        this.jobPrefix = jobPrefix;

        this.verbose = false;
        this.help = false;
    }

    public void parse(CommandLine commandLine) throws ParseException {

        this.environmentConfig = commandLine.hasOption(OPT_ENV_CONFIG) ?
                new File(commandLine.getOptionValue(OPT_ENV_CONFIG)) :
                this.environmentConfig;

        this.logConfig = commandLine.hasOption(OPT_LOG_CONFIG) ?
                new File(commandLine.getOptionValue(OPT_LOG_CONFIG)) :
                this.logConfig;

        this.outputDir = commandLine.hasOption(OPT_OUTPUT_DIR) ?
                new File(commandLine.getOptionValue(OPT_OUTPUT_DIR)) :
                this.outputDir;

        this.jobPrefix = commandLine.hasOption(OPT_JOB_PREFIX) ?
                commandLine.getOptionValue(OPT_JOB_PREFIX) :
                this.jobPrefix;

        this.verbose = commandLine.hasOption(OPT_VERBOSE);
        this.help = commandLine.hasOption(OPT_HELP);

        // Only bother to parse other options if help is not set.  User may have some positional requirements on their
        // arguments, which might prevent help from running.
        if (!this.help) {

            // Sets any extra variables in the child class.
            this.parseExtra(commandLine);

            log.debug("Parsed command line.");
        }
    }

    /**
     * This initialises the conan application by retrieving the conan properties and setting up the logging system.
     * @throws IOException
     */
    public void init() throws IOException {

        // If logging file exists use settings from that, otherwise use basic settings.
        if (this.logConfig.exists()) {
            PropertyConfigurator.configure(this.logConfig.getAbsolutePath());
            log.debug("Using user specified logging properties: " + this.logConfig.getAbsolutePath());
        }
        else {
            BasicConfigurator.configure();
            log.debug("No log4j properties file found.  Using default logging properties.");
        }

        // Load Conan properties
        if (this.environmentConfig.exists()) {
            ConanProperties.getConanProperties().setPropertiesFile(this.environmentConfig);
        }
        else {
            throw new IOException("Could not find conan.properties file at: " + this.environmentConfig.getAbsolutePath() +
                    "; A valid conan.properties file is required to run a conan pipeline.");
        }
    }


    public String getAppName() {
        return appName;
    }

    public File getDefaultSettingsDir() {
        return defaultSettingsDir;
    }

    public File getEnvironmentConfig() {
        return environmentConfig;
    }

    public File getLogConfig() {
        return logConfig;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public String getJobPrefix() {
        return jobPrefix;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isHelp() {
        return help;
    }

    /**
     * Returns the current working directory as an absolute file
     * @return The current working directory
     */
    protected static File currentWorkingDir() {
        return new File(".").getAbsoluteFile().getParentFile();
    }

    /**
     * Creates a timestamp as a String that can be sorted into chronological order based on the numbers in the String
     * @return
     */
    protected static final String createTimestamp() {
        Format formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return formatter.format(new Date());
    }


    /**
     * Constructs an execution context from details discovered from the environment configuration file.
     * @return An execution content build from the environment configuration file
     * @throws IOException
     */
    public ExecutionContext buildExecutionContext() throws IOException {

        // Get external process pre-commands
        ExternalProcessConfiguration externalProcessConfiguration = new DefaultExternalProcessConfiguration();
        if (ConanProperties.containsKey("externalProcessConfigFile")) {
            externalProcessConfiguration.setProcessConfigFilePath(ConanProperties.getProperty("externalProcessConfigFile"));
            externalProcessConfiguration.load();
        }

        // Get execution context
        Locality locality = ConanProperties.containsKey("executionContext.locality") ?
                LocalityFactory.createLocality(ConanProperties.getProperty("executionContext.locality")) :
                new Local();

        log.info("Execution Context: Locality: \"" + locality.getDescription() + "\"");

        Scheduler scheduler = ConanProperties.containsKey("executionContext.scheduler") ?
                SchedulerFactory.createScheduler(ConanProperties.getProperty("executionContext.scheduler")) :
                null;

        String schedulerName = scheduler == null ? "Unscheduled" : scheduler.getName();
        log.info("Execution Context: Scheduler Name: \"" + schedulerName + "\"");

        if (scheduler != null && ConanProperties.containsKey("executionContext.scheduler.queue")) {
            String queueName = ConanProperties.getProperty("executionContext.scheduler.queue");
            scheduler.getArgs().setQueueName(queueName);
            log.info("Execution Context: Scheduler Queue: \"" + queueName + "\"");
        }

        if (scheduler != null && ConanProperties.containsKey("executionContext.scheduler.extraArgs")) {
            String extraArgs = ConanProperties.getProperty("executionContext.scheduler.extraArgs");
            scheduler.getArgs().setExtraArgs(extraArgs);
            log.info("Execution Context: Scheduler Args: \"" + extraArgs + "\"");
        }

        DefaultExecutionContext dec = new DefaultExecutionContext(locality, scheduler, externalProcessConfiguration);
        dec.setApplicationDir(APPLICATION_DIR);
        return dec;
    }


    /**
     * This creates a set of commonly used command line options that you will likely want to use if you wish to control
     * a conan pipeline from the command line.
     * @return A set of common command line options
     */
    public Options createOptions() {

        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(new Option("v", OPT_VERBOSE, false, "Output extra information while running."));
        options.addOption(new Option("?", OPT_HELP, false, "Print this message."));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_ENV_CONFIG).hasArg()
                        .withDescription("The environment configuration file.  Default: " +
                                this.environmentConfig.getAbsolutePath()).create("e"));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_LOG_CONFIG).hasArg()
                        .withDescription("The logging configuration file.  Default: " +
                                this.logConfig.getAbsolutePath()).create("l"));

        options.addOption(OptionBuilder.withArgName("file").withLongOpt(OPT_OUTPUT_DIR).hasArg()
                        .withDescription("The directory to put output from this job.  Default: " + currentWorkingDir().getAbsolutePath()).create("o"));

        options.addOption(OptionBuilder.withArgName("string").withLongOpt(OPT_JOB_PREFIX).hasArg()
                        .withDescription("The job prefix descriptor to use when scheduling.  " +
                                "WARNING: Be careful what you set this to.  It's possible that if you run multiple jobs in succession " +
                                "with the same job prefix, the scheduler may confuse old recently completed jobs with the same name " +
                                "with this one.  This can cause some job's wait conditions to never be fulfilled.  Ideally, include " +
                                "some kind of unique identifier with this option such as a timestamp.  To help with this, any instances " +
                                "of \"TIMESTAMP\" in this argument with an actual timestamp will be automatically replaced.  Default: " +
                                this.appName + "-<timestamp>").create("p"));

        // Add extra options from the child class
        for(Option option : this.createExtraOptions()) {
            options.addOption(option);
        }

        return options;
    }

    /**
     * Creates a help message using the provided input
     * @param outputStream
     * @param title
     * @param description
     * @param footer
     * @param options
     */
    public static void helpHelper(OutputStream outputStream, String title, String description, String footer, Options options) {
        final PrintWriter writer = new PrintWriter(outputStream);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                95,
                title,
                description,
                options,
                3,
                3,
                footer,
                true);
        writer.flush();
    }


    /**
     * The child tool must be able to print some help message
     */
    protected abstract void printHelp();

    /**
     * This abstract class assumes certain options will be available to the application, however the child app may wish
     * to add more.  That can be done in this method.
     * @return
     */
    protected abstract List<Option> createExtraOptions();

    /**
     * This calls the sub class and sets any additional variables from the command line.
     * @param commandLine
     */
    protected abstract void parseExtra(CommandLine commandLine) throws ParseException;

    /**
     * Create the argument map that describes the values for the conan parameters used in this pipeline
     * @return
     */
    protected abstract ParamMap createArgMap();

    /**
     * Create the conan pipeline that is to be executed
     * @return
     */
    protected abstract ConanPipeline createPipeline() throws IOException;

    /**
     * Executes this application
     * @param conanUser
     * @param priority
     * @param executionContext
     * @throws InterruptedException
     * @throws TaskExecutionException
     * @throws IOException
     */
    public void execute(ConanUser conanUser, ConanTask.Priority priority, ExecutionContext executionContext)
            throws InterruptedException, TaskExecutionException, IOException {

        if (this.help) {
            printHelp();
        }
        else {

            // Create the pipeline from the child class
            ConanPipeline conanPipeline = this.createPipeline();
            log.debug("Created pipeline: " + conanPipeline.getName());

            // Creates the argument map containing the settings for this pipeline
            ParamMap argMap = this.createArgMap();

            // Create the RAMPART task
            ConanTask conanTask = new DefaultTaskFactory().createTask(
                    conanPipeline,
                    0,
                    argMap,
                    priority,
                    conanUser);

            conanTask.setId(this.appName);
            conanTask.submit();
            log.debug("Incorporated pipeline '" + conanPipeline.getName() + "' into task '" + conanTask.getId() + "'");

            // Ensure the output directory exists before we start
            if (!this.getOutputDir().exists()) {
                this.getOutputDir().mkdirs();
                log.info("Created output directory: \"" + this.getOutputDir().getAbsolutePath() + "\"");
            }

            log.info("Executing task: '" + conanTask.getId() + "'");
            conanTask.execute(executionContext);
            log.info("Finished executing task: '" + conanTask.getId() + "'");
        }
    }
}
