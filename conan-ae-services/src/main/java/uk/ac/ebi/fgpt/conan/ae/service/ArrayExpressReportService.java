package uk.ac.ebi.fgpt.conan.ae.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
import uk.ac.ebi.fgpt.conan.ae.MAGEMLAccessionParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcessRun;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.service.ConanReportService;

import java.io.*;
import java.util.*;

/**
 * An implementation of a {@link uk.ac.ebi.fgpt.conan.service.ConanReportService} that provides access to reports from
 * their known locations within the microarray filesystem at the EBI
 *
 * @author Tony Burdett
 * @date 23-Nov-2010
 */
public class ArrayExpressReportService implements ConanReportService {
    public static final int MAX_REPORT_SIZE = 250;
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Map<Integer, String> getReport(String reportName) throws IOException {
        Map<Integer, String> result = new LinkedHashMap<Integer, String>();

        // read the contents of this report
        File f = new File(reportName);
        // read the file
        LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(f)));

        // do a quick readthrough to get the number of lines
        int lineCount = 0;
        while (reader.readLine() != null) {
            lineCount = reader.getLineNumber();
        }

        // only read the last 1000 lines
        int readFrom = 0;
        if (lineCount > MAX_REPORT_SIZE) {
            readFrom = lineCount - MAX_REPORT_SIZE;
            result.put(0, "The contents of this report are too large: " +
                    "only the last " + MAX_REPORT_SIZE + " lines are displayed.  " +
                    "For the full report, go to " + reportName);
        }

        // reset the reader to read from the start of the file again
        reader.close();
        reader = new LineNumberReader(new BufferedReader(new FileReader(f)));

        // and read the last 1000 lines of content
        String line;
        while ((line = reader.readLine()) != null) {
            if (reader.getLineNumber() >= readFrom) {
                result.put(reader.getLineNumber(), line);
            }
        }

        return result;
    }

    public String getReportName(ConanTask<? extends ConanPipeline> task, ConanProcessRun processRun) {
        List<String> allReportNames = getAllReportNames(task);
        for (String reportName : allReportNames) {
            // return first report that contains the process name -
            // they're sorted most recent first, so this is the one we'll want if found
            // todo - enhance this check, report names don't often match the process name so we need better mapping
            if (reportName.contains(processRun.getProcessName())) {
                return reportName;
            }
        }

        // nothing found, return null
        return null;
    }

    public List<String> getAllReportNames(ConanTask<? extends ConanPipeline> task) {
        List<File> reportFiles = new ArrayList<File>();

        // try to extract the accession from the task
        try {
            AccessionParameter key = new AccessionParameter();
            if (task.getParameterValues().containsKey(key)) {
                // we can get the accession, so extract the value
                String value = task.getParameterValues().get(key);
                getLog().debug("Accession parameter is " + value);
                key.setAccession(value);

                // now grab the absolute file and list all *.log files in the same directory
                File fileDir = key.getFile().getAbsoluteFile().getParentFile();
                FilenameFilter logFileFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".log");
                    }
                };
                getLog().debug("Listing *.log files from " + fileDir.getAbsolutePath());
                if (fileDir.isDirectory()) {
                    reportFiles.addAll(Arrays.asList(fileDir.listFiles(logFileFilter)));
                }

                // now switch to /reports directory, if it exists, and list ALL files there
                File reportDir = new File(fileDir, "reports");
                getLog().debug("Listing all files in " + reportDir.getAbsolutePath());
                if (reportDir.isDirectory()) {
                    reportFiles.addAll(Arrays.asList(reportDir.listFiles()));
                }
            }

            // try to extract the mageml accession from the task
            MAGEMLAccessionParameter magemlKey = new MAGEMLAccessionParameter();
            if (task.getParameterValues().containsKey(magemlKey)) {
                // we can get the mageml accession, so extract the value
                String value = task.getParameterValues().get(magemlKey);
                getLog().debug("MAGEML Accession parameter is " + value);
                magemlKey.setAccession(value);

                // now grab the absolute file and list all *.log files in the same directory
                File magemlFileDir = magemlKey.getFile().getAbsoluteFile().getParentFile();
                FilenameFilter logFileFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".log");
                    }
                };

                getLog().debug("Listing *.log files from " + magemlFileDir.getAbsolutePath());
                if (magemlFileDir.isDirectory()) {
                    reportFiles.addAll(Arrays.asList(magemlFileDir.listFiles(logFileFilter)));
                }

                // now switch to /stdout directory, if it exists, and list ALL files there that aren't zipped
                File magemlStdoutDir = new File(magemlFileDir, "stdout");
                FilenameFilter reportFileFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return !name.endsWith(".gz");
                    }
                };
                getLog().debug("Listing all files in " + magemlStdoutDir.getAbsolutePath());
                if (magemlStdoutDir.isDirectory()) {
                    reportFiles.addAll(Arrays.asList(magemlStdoutDir.listFiles(reportFileFilter)));
                }

                // also, try to extract ae2 reports from MAGEML params
                getLog().debug("Accession parameter is " + value);
                key.setAccession(value);

                // now grab the absolute file and list all *.log files in the same directory
                File fileDir = key.getFile().getAbsoluteFile().getParentFile();
                getLog().debug("Listing *.log files from " + fileDir.getAbsolutePath());
                if (fileDir.isDirectory()) {
                    reportFiles.addAll(Arrays.asList(fileDir.listFiles(logFileFilter)));
                }

                // now switch to /reports directory, if it exists, and list ALL files there
                File reportDir = new File(fileDir, "reports");
                getLog().debug("Listing all files in " + reportDir.getAbsolutePath());
                if (reportDir.isDirectory()) {
                    reportFiles.addAll(Arrays.asList(reportDir.listFiles()));
                }
            }
        }
        catch (Exception e) {
            // unexpected runtime exception, handle at this point and return an empty list
            getLog().error("An unexpected internal exception occurred", e);
            reportFiles.clear();
        }

        // sort this list by reverse date
        Collections.sort(reportFiles, new Comparator<File>() {

            public int compare(File f1, File f2) {
                return (int) (f2.lastModified() - f1.lastModified());
            }
        });

        // take sorted file list and copy to a list of strings
        List<String> reportFilePaths = new ArrayList<String>();
        for (File f : reportFiles) {
            reportFilePaths.add(f.getAbsolutePath());
        }

        // return the paths
        return reportFilePaths;
    }
}
