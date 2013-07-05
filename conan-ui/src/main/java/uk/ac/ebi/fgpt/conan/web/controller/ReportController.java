package uk.ac.ebi.fgpt.conan.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.service.ConanReportService;
import uk.ac.ebi.fgpt.conan.service.ConanTaskService;
import uk.ac.ebi.fgpt.conan.web.view.ReportContentBean;
import uk.ac.ebi.fgpt.conan.web.view.ReportNamesBean;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A controller that grants the Conan UI access to server side reporting information, and exposes the content on
 * request.
 *
 * @author Tony Burdett
 * @date 23-Nov-2010
 */
@Controller
@RequestMapping("/reports")
public class ReportController {
    private ConanTaskService taskService;
    private ConanReportService reportService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ConanTaskService getTaskService() {
        return taskService;
    }

    @Autowired
    public void setTaskService(ConanTaskService taskService) {
        this.taskService = taskService;
    }

    public ConanReportService getReportService() {
        return reportService;
    }

    @Autowired(required = false)
    public void setReportService(ConanReportService reportService) {
        this.reportService = reportService;
    }

    @RequestMapping(value = "/names", method = RequestMethod.GET)
    public
    @ResponseBody
    ReportNamesBean getReportNames(@RequestParam String taskID) {
        ConanTask<? extends ConanPipeline> task = getTaskService().getTask(taskID);
        if (getReportService() == null) {
            return new ReportNamesBean(task.getName(), Collections.<String>emptyList());
        } else {
            List<String> reportNames = getReportService().getAllReportNames(task);
            return new ReportNamesBean(task.getName(), reportNames);
        }
    }

    @RequestMapping(value = "/contents", method = RequestMethod.GET)
    public
    @ResponseBody
    ReportContentBean getReportContents(@RequestParam String reportName) {
        try {
            if (getReportService() == null) {
                return new ReportContentBean(reportName, Collections.<Integer, String>emptyMap());
            } else {
                Map<Integer, String> reportContent = getReportService().getReport(reportName);
                return new ReportContentBean(reportName, reportContent);
            }
        } catch (IOException e) {
            Map<Integer, String> response = new HashMap<Integer, String>();
            if (e.getMessage() == null) {
                response.put(1, "Unable to read report file, I/O issues reading from " + reportName);
            } else {
                response.put(1, "Unable to read report file: " + e.getMessage());
            }
            return new ReportContentBean(reportName, response);
        }
    }
}
