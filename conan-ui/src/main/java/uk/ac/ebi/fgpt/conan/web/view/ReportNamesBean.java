package uk.ac.ebi.fgpt.conan.web.view;

import java.util.List;

/**
 * A simple javabean that contains the names of reports available for a particular task.
 *
 * @author Tony Burdett
 * @date 23-Nov-2010
 */
public class ReportNamesBean {
    private String taskName;
    private List<String> reportNames;

    public ReportNamesBean(String taskName, List<String> reportNames) {
        this.taskName = taskName;
        this.reportNames = reportNames;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public List<String> getReportNames() {
        return reportNames;
    }

    public void setReportNames(List<String> reportNames) {
        this.reportNames = reportNames;
    }
}
