package uk.ac.ebi.fgpt.conan.service;

import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.conan.core.process.DefaultProcessRun;
import uk.ac.ebi.fgpt.conan.core.user.ConanUserWithPermissions;
import uk.ac.ebi.fgpt.conan.dao.DatabaseConanTaskDAO;
import uk.ac.ebi.fgpt.conan.dao.DatabaseConanUserDAO;
import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.ebi.fgpt.conan.model.ConanPipeline;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests a DatabaseConanTaskDAO
 *
 * @author Natalja Kurbatova
 * @date 01-Nov-2010
 */
public class TestDatabaseConanTaskDAO extends TestCase {
    private DatabaseConanTaskDAO taskDao;
    private DatabaseConanUserDAO userDao;
    private ConanTaskService taskService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("database-application-context.xml");
        taskDao = ctx.getBean("DatabaseConanTaskDAO", DatabaseConanTaskDAO.class);
        taskService = ctx.getBean("taskService", ConanTaskService.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        taskDao = null;
        userDao = null;
    }

    public void testTaskGetAndSave() {
        //Create test user
        ConanUserWithPermissions userTest =
                new ConanUserWithPermissions("natalja", "test", "test", "natalja@ebi.ac.uk", "54784cccc2238667623332",
                                             ConanUser.Permissions.SUBMITTER);
        userDao = (DatabaseConanUserDAO) taskDao.getUserDAO();

        ConanTask.Priority priority = ConanTask.Priority.MEDIUM;
        String pipelineName = "demo pipeline";
        ConanPipeline pipelineTest = ((List<ConanPipeline>) taskDao.getPipelineDAO().getPipelines()).get(0);
        Map<String, String> paramsTest = new HashMap<String, String>();
        for (ConanParameter cp : pipelineTest.getProcesses().get(0).getParameters()) {
            paramsTest.put(cp.getName(), "test");
        }


        // generate task
        ConanTask conanTask = taskService.createNewTask(pipelineName,
                                                        0,
                                                        paramsTest,
                                                        priority,
                                                        userTest);


        ConanTask taskDB = taskDao.getTask(conanTask.getId());

        System.out.println("CREATION DATE: " + taskDB.getCreationDate());
        System.out.println("ID: " + taskDB.getId());
        System.out.println("NAME: " + taskDB.getName());
        System.out.println("STATUS MESSAGE: " + taskDB.getStatusMessage());
        System.out.println("PRIORITY: " + taskDB.getPriority());
        System.out.println("PARAMETERS: " + taskDB.getParameterValues());
        DefaultProcessRun dpr = new DefaultProcessRun(taskDB.getFirstProcess().getName(), taskDB.getSubmitter());
        dpr.setStartDate(new Date());
        taskDao.saveProcessRun(taskDB.getId(), dpr);
        try {

            taskDB.getFirstProcess().execute(taskDB.getParameterValues());

        }
        catch (Exception e) {

        }
        dpr.setEndDate(new Date());
        System.out.println("STORE PROCESS");
        taskDao.saveProcessRun(taskDB.getId(), dpr);


        System.out.println("UPDATE TASK");
        taskDao.updateTask(taskDB);
        System.out.println("CREATION DATE: " + taskDB.getCreationDate());
        System.out.println("ID: " + taskDB.getId());
        System.out.println("NAME: " + taskDB.getName());
        System.out.println("STATUS MESSAGE: " + taskDB.getStatusMessage());
        System.out.println("PRIORITY: " + taskDB.getPriority());
        System.out.println("PARAMETERS: " + taskDB.getParameterValues());

        System.out.println("GET ALL TASKS");
        List<ConanTask<? extends ConanPipeline>> conanTasks = taskDao.getAllTasks();
        for (ConanTask ct : conanTasks) {
            System.out.println("CREATION DATE: " + ct.getCreationDate());
            System.out.println("ID: " + ct.getId());
            System.out.println("NAME: " + ct.getName());
            System.out.println("STATUS MESSAGE: " + ct.getStatusMessage());
            System.out.println("PRIORITY: " + ct.getPriority());
            System.out.println("PARAMETERS: " + ct.getParameterValues());
        }


    }

}
