package uk.ac.ebi.fgpt.conan.service;

import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcessRun;
import uk.ac.ebi.fgpt.conan.model.ConanTask;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A service that can be used to acquire report information about processes that have been run in the Conan framework.
 * By supplying a task and a process run, this service should be able to provide reports, where available, on the
 * invocation of that process run.
 *
 * @author Tony Burdett
 * @date 23-Nov-2010
 */
public interface ConanReportService {
    /**
     * Gets the contents of the report with the given report name.  Each entry in the resulting map represents a single
     * line in the report, indexed by the line number.  If this report has been read in from a report file somewhere on
     * disk, the resulting string should contain tabs and newline characters in accordance with the original report
     * file.
     * <p/>
     * If the report file could not be read, either due to a read problem or because the report exists in an unexpected
     * format, an IOException should be thrown.
     *
     * @param reportName the name of the report for which we should acquire contents
     * @return the report contents
     * @throws java.io.IOException if the file could not be read for any reason
     */
    Map<Integer, String> getReport(String reportName) throws IOException;

    /**
     * Returns the name of a single report for the specified task and process run.
     * <p/>
     * If there is no report available for this task and process run, or the report could not be found, this method
     * should return null
     *
     * @param task       the task to obtain a report for
     * @param processRun the specific process run to obtain a report for
     * @return the name of the report for this task and process run
     */
    String getReportName(ConanTask<? extends ConanPipeline> task, ConanProcessRun processRun);

    /**
     * Returns a list of all report names for the supplied task, in execution order.  Each element in the list
     * represents the name of a single report.
     * <p/>
     * If no reports could be found, or no reports have been generated for this task, this method returns an empty
     * list.
     *
     * @param task the task to obtain reports for
     * @return a list of strings representing the report names for each task that has been executed
     */
    List<String> getAllReportNames(ConanTask<? extends ConanPipeline> task);
}
