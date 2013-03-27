package uk.ac.ebi.fgpt.conan.model.context;

import java.io.File;

public abstract class SchedulerArgs {

    private String jobName;
    private String queueName;
    private int threads;
    private int memoryMB;
    private String extraArgs;
    private boolean openmpi;
    private WaitCondition waitCondition;
    private File monitorFile;
    private int monitorInterval;

    protected SchedulerArgs() {
        this.jobName = "";
        this.queueName = "";
        this.threads = 0;
        this.memoryMB = 0;
        this.extraArgs = "";
        this.openmpi = false;
        this.monitorFile = null;
        this.monitorInterval = 15;
    }

    protected SchedulerArgs(SchedulerArgs args) {
        this.jobName = args.getJobName();
        this.queueName = args.getQueueName();
        this.threads = args.getThreads();
        this.memoryMB = args.getMemoryMB();
        this.extraArgs = args.getExtraArgs();
        this.openmpi = args.isOpenmpi();
        this.waitCondition = args.getWaitCondition();
        this.monitorFile = args.getMonitorFile();
        this.monitorInterval = args.getMonitorInterval();
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getMemoryMB() {
        return memoryMB;
    }

    public int getMemoryGB() {
        return (int)(memoryMB / 1000);
    }

    public void setMemoryMB(int memoryMB) {
        this.memoryMB = memoryMB;
    }

    public String getExtraArgs() {
        return extraArgs;
    }

    public void setExtraArgs(String extraArgs) {
        this.extraArgs = extraArgs;
    }

    public boolean isOpenmpi() {
        return openmpi;
    }

    public void setOpenmpi(boolean openmpi) {
        this.openmpi = openmpi;
    }

    public WaitCondition getWaitCondition() {
        return waitCondition;
    }

    public void setWaitCondition(WaitCondition waitCondition) {
        this.waitCondition = waitCondition;
    }

    public void setMonitorFile(File monitorFile) {
        this.monitorFile = monitorFile;
    }

    public File getMonitorFile() {
        return this.monitorFile;
    }

    public int getMonitorInterval() {
        return monitorInterval;
    }

    public void setMonitorInterval(int monitorInterval) {
        this.monitorInterval = monitorInterval;
    }

    public abstract SchedulerArgs copy();
}
