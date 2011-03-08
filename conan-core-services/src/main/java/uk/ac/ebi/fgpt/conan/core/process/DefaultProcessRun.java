package uk.ac.ebi.fgpt.conan.core.process;

import uk.ac.ebi.fgpt.conan.model.ConanProcessRun;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Date;

/**
 * To represent process execution in Conan
 *
 * @author Natalja Kurbatova
 * @author Tony Burdett
 * @date 28-Oct-2010
 */
public class DefaultProcessRun implements ConanProcessRun {
    private String id;
    private String processName = null;
    private Date startDate;
    private Date endDate;
    private int exitValue = -1;
    private ConanUser submitter;

    public DefaultProcessRun(String processName, ConanUser submitter) {
        this.processName = processName;
        this.submitter = submitter;
    }

    public DefaultProcessRun(String processName, Date startDate, Date endDate, ConanUser submitter) {
        this(processName, submitter);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getExitValue() {
        return exitValue;
    }

    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }

    public ConanUser getUser() {
        return submitter;
    }

    public void getUser(ConanUser submitter) {
        this.submitter = submitter;
    }
}
