package uk.ac.ebi.fgpt.conan.model.context;

import java.io.File;

/**
 * This abstract class defines properties that should be common to all Schedulers
 *
 * @author Dan Mapleson
 */
public abstract class SchedulerArgs {

    private String jobName;
    private String queueName;
    private String projectName;
    private int threads;
    private int memoryMB;
    private String extraArgs;
    private boolean openmpi;
    private String waitCondition;
    private File monitorFile;
    private int monitorInterval;
    private JobArrayArgs jobArrayArgs;

    protected SchedulerArgs() {
        this.jobName = "";
        this.queueName = "";
        this.projectName = "";
        this.threads = 0;
        this.memoryMB = 0;
        this.extraArgs = "";
        this.openmpi = false;
        this.monitorFile = null;
        this.monitorInterval = 15;
        this.jobArrayArgs = null;
    }

    protected SchedulerArgs(SchedulerArgs args) {
        this.jobName = args.getJobName();
        this.queueName = args.getQueueName();
        this.projectName = args.getProjectName();
        this.threads = args.getThreads();
        this.memoryMB = args.getMemoryMB();
        this.extraArgs = args.getExtraArgs();
        this.openmpi = args.isOpenmpi();
        this.waitCondition = args.getWaitCondition();
        this.monitorFile = args.getMonitorFile();
        this.monitorInterval = args.getMonitorInterval();
        this.jobArrayArgs = args.getJobArrayArgs() == null ? null : new JobArrayArgs(args.getJobArrayArgs());
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public String getWaitCondition() {
        return waitCondition;
    }

    public void setWaitCondition(String waitCondition) {
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

    public JobArrayArgs getJobArrayArgs() {
        return jobArrayArgs;
    }

    public void setJobArrayArgs(JobArrayArgs jobArrayArgs) {
        this.jobArrayArgs = jobArrayArgs;
    }

    public static class JobArrayArgs {
        private int minIndex;
        private int maxIndex;
        private int stepIndex;
        private int maxSimultaneousJobs;

        public JobArrayArgs() {
            this(1, 1, 1, 1);
        }

        public JobArrayArgs(int minIndex, int maxIndex, int stepIndex, int maxSimultaneousJobs) {
            this.minIndex = minIndex;
            this.maxIndex = maxIndex;
            this.stepIndex = stepIndex;
            this.maxSimultaneousJobs = maxSimultaneousJobs;
        }

        public JobArrayArgs(JobArrayArgs args) {
            this(args.minIndex, args.maxIndex, args.stepIndex, args.maxSimultaneousJobs);
        }

        public int getMinIndex() {
            return minIndex;
        }

        public int getMaxIndex() {
            return maxIndex;
        }

        public int getStepIndex() {
            return stepIndex;
        }

        public int getMaxSimultaneousJobs() {
            return maxSimultaneousJobs;
        }
    }
}
