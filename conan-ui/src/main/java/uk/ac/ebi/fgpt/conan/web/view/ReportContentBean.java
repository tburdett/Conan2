package uk.ac.ebi.fgpt.conan.web.view;

import java.util.Map;

/**
 * A simple bean that encapsulates the contents of a report into a single, long string.  This bean also wraps the name
 * of the original report.
 *
 * @author Tony Burdett
 * @date 23-Nov-2010
 */
public class ReportContentBean {
    private String reportName;
    private Map<Integer, String> reportContent;

    public ReportContentBean(String reportName, Map<Integer, String> reportContent) {
        this.reportName = reportName;
        this.reportContent = reportContent;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public Map<Integer, String> getReportContent() {
        return reportContent;
    }

    public void setReportContent(Map<Integer, String> reportContent) {
        this.reportContent = reportContent;
    }
}
