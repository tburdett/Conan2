package uk.ac.ebi.fgpt.conan.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanProcessRun;
import uk.ac.ebi.fgpt.conan.model.ConanTask;

import java.util.*;

/**
 * An implementation of {@link uk.ac.ebi.fgpt.conan.dao.ConanTaskDAO} that uses JDBC to access a database storing Conan
 * task information.
 * <p/>
 * This is a very naive implementation that should not be used in production systems, as all created tasks are stored in
 * memory: if the application even needs to be restarted, all provenance information on created tasks will be lost.
 *
 * @author Tony Burdett
 * @date 18-Oct-2010
 */
public class InMemoryConanTaskDAO implements ConanTaskDAO {
    private final Map<String, ConanTask<? extends ConanPipeline>> allTasks;

    private ConanTaskDAO conanTaskPersistenceDAO;

    private Logger log = LoggerFactory.getLogger(getClass());

    public InMemoryConanTaskDAO() {
        this.allTasks = new HashMap<String, ConanTask<? extends ConanPipeline>>();
    }

    protected Logger getLog() {
        return log;
    }

    public ConanTaskDAO getConanTaskPersistenceDAO() {
        return conanTaskPersistenceDAO;
    }

    public void setConanTaskPersistenceDAO(ConanTaskDAO conanTaskDAO) {
        Assert.notNull(conanTaskDAO, "ConanTaskPersistenceDAO must not be null");
        this.conanTaskPersistenceDAO = conanTaskDAO;
    }

    public void init() {
        synchronizeInMemoryTasksWithPersistenceDAO();
    }

    public boolean supportsAutomaticIDAssignment() {
        return true;
    }

    public ConanTask<? extends ConanPipeline> getTask(String taskID) {
        if (allTasks.containsKey(taskID)) {
            return allTasks.get(taskID);
        }
        // no task with this id if we got to here, so return null
        return null;
    }

    public <P extends ConanPipeline> ConanTask<P> saveTask(ConanTask<P> newConanTask) {
        // save task to DB
        ConanTask<P> task = getConanTaskPersistenceDAO().saveTask(newConanTask);
        // update object reference
        synchronized (allTasks) {
            allTasks.put(task.getId(), task);
        }
        // and return the DB result
        return task;
    }

    public <P extends ConanPipeline> ConanTask<P> updateTask(ConanTask<P> conanTask)
            throws IllegalArgumentException {
        // update task in DB
        ConanTask<P> task = getConanTaskPersistenceDAO().updateTask(conanTask);
        // update object reference
        synchronized (allTasks) {
            allTasks.put(task.getId(), task);
        }
        // and return the DB result
        return task;
    }

    public <P extends ConanPipeline> ConanTask<P> saveProcessRun(String conanTaskID,
                                                                 ConanProcessRun conanProcessRun)
            throws IllegalArgumentException {
        // save process run
        ConanTask<P> task = getConanTaskPersistenceDAO().saveProcessRun(conanTaskID, conanProcessRun);
        // update object reference
        synchronized (allTasks) {
            allTasks.put(task.getId(), task);
        }
        // and return the db result
        return task;
    }

    public List<ConanTask<? extends ConanPipeline>> getAllTasks() {
        List<ConanTask<? extends ConanPipeline>> result = new ArrayList<ConanTask<? extends ConanPipeline>>();
        synchronized (allTasks) {
            for (ConanTask<? extends ConanPipeline> task : allTasks.values()) {
                result.add(task);
            }
        }
        return result;
    }

    public List<ConanTask<? extends ConanPipeline>> getAllTasks(int maxRecords, int startingFrom) {
        return getAllTasks().subList(startingFrom, startingFrom + maxRecords);
    }

    public List<ConanTask<? extends ConanPipeline>> getAllTasks(int maxRecords, int startingFrom, String orderBy) {
        return getAllTasks().subList(startingFrom, startingFrom + maxRecords);
    }

    public List<ConanTask<? extends ConanPipeline>> getPendingTasks() {
        List<ConanTask<? extends ConanPipeline>> results = new ArrayList<ConanTask<? extends ConanPipeline>>();
        for (ConanTask<? extends ConanPipeline> task : getAllTasks()) {
            if (task.getCurrentState().compareTo(ConanTask.State.RUNNING) < 0) {
                results.add(task);
            }
        }

        // sort based on most recent first
        Collections.sort(results, new Comparator<ConanTask<? extends ConanPipeline>>() {
            public int compare(ConanTask<? extends ConanPipeline> task1, ConanTask<? extends ConanPipeline> task2) {
                if (task1.getCreationDate().before(task2.getCreationDate())) {
                    return -1;
                }
                else {
                    if (task1.getCreationDate().after(task2.getCreationDate())) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            }
        });
        Collections.reverse(results);

        return results;
    }

    public List<ConanTask<? extends ConanPipeline>> getRunningTasks() {
        List<ConanTask<? extends ConanPipeline>> results = new ArrayList<ConanTask<? extends ConanPipeline>>();
        for (ConanTask<? extends ConanPipeline> task : getAllTasks()) {
            if (task.getCurrentState() == ConanTask.State.RUNNING) {
                results.add(task);
            }
        }
        return results;
    }

    public List<ConanTask<? extends ConanPipeline>> getCompletedTasks() {
        List<ConanTask<? extends ConanPipeline>> results = new ArrayList<ConanTask<? extends ConanPipeline>>();
        for (ConanTask<? extends ConanPipeline> task : getAllTasks()) {
            if (task.getCurrentState() == ConanTask.State.COMPLETED ||
                    task.getCurrentState() == ConanTask.State.ABORTED) {
                // todo - remove this hack which filters out everything older than 72 hours old, replace with sane paging strategy
                int seventytwoHours = 60 * 60 * 72 * 1000;
                Date pastDate = new Date(System.currentTimeMillis() - seventytwoHours);
                if (task.getCompletionDate().after(pastDate)) {
                    results.add(task);
                }
            }
        }
        return results;
    }

    public List<ConanTask<? extends ConanPipeline>> getCompletedTasks(int maxRecords, int startingFrom) {
        List<ConanTask<? extends ConanPipeline>> results = new ArrayList<ConanTask<? extends ConanPipeline>>();
        for (ConanTask<? extends ConanPipeline> task : getAllTasks()) {
            if (task.getCurrentState() == ConanTask.State.COMPLETED ||
                    task.getCurrentState() == ConanTask.State.ABORTED) {
                results.add(task);
            }
        }
        return results.subList(startingFrom, startingFrom + maxRecords);
    }

    public List<ConanTask<? extends ConanPipeline>> getCompletedTasks(int maxRecords,
                                                                      int startingFrom,
                                                                      String orderBy) {
        List<ConanTask<? extends ConanPipeline>> results = new ArrayList<ConanTask<? extends ConanPipeline>>();
        for (ConanTask<? extends ConanPipeline> task : getAllTasks()) {
            if (task.getCurrentState() == ConanTask.State.COMPLETED ||
                    task.getCurrentState() == ConanTask.State.ABORTED) {
                results.add(task);
            }
        }
        return results.subList(startingFrom, startingFrom + maxRecords);
    }

    protected void synchronizeInMemoryTasksWithPersistenceDAO() {
        synchronized (allTasks) {
            // synch up the contents of our cache (i.e. the map "allTasks") with all tasks from the database
            allTasks.clear();
            getLog().debug("Rebuilding cache by recovering tasks from the persistence DAO");
            long start = System.currentTimeMillis();
            List<ConanTask<? extends ConanPipeline>> dbTasks = getConanTaskPersistenceDAO().getAllTasks();
            for (ConanTask<? extends ConanPipeline> task : dbTasks) {
                allTasks.put(task.getId(), task);
            }
            long end = System.currentTimeMillis();
            double time = ((double) (end - start)) / 1000;
            getLog().info("In memory task cache rebuilt - recovered " + dbTasks.size() + " tasks in  " + time + " s.");
        }
    }
}
