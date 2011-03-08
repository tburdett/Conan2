import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.conan.ae.dao.DaemonInputsForAE1DAO;
import uk.ac.ebi.fgpt.conan.ae.dao.DaemonInputsForAE2DAO;

/**
 * Tests for DaemonDAOs
 *
 * @author Natalja Kurbatova
 * @date 22-Nov-2010
 */
public class DaemonInputsDAOTest extends TestCase {
    private DaemonInputsForAE2DAO daemonDao2;
    private DaemonInputsForAE1DAO daemonDao1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("database-application-context.xml");
        daemonDao2 = (DaemonInputsForAE2DAO) ctx.getBean("DaemonInputsForAE2DAO", DaemonInputsForAE2DAO.class);
        daemonDao1 = (DaemonInputsForAE1DAO) ctx.getBean("DaemonInputsForAE1DAO", DaemonInputsForAE1DAO.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        daemonDao2 = null;
        daemonDao1 = null;
    }

    public void testDaemonDao() {
        System.out.println("AE2 - Daemon mode submissions:");
        System.out.println(daemonDao2.getParameterType().getName());
        for (String value : daemonDao2.getParameterValues()) {
            System.out.println(value);
        }

        System.out.println("AE1 - Daemon mode submissions:");
        System.out.println(daemonDao1.getParameterType().getName());
        for (String value : daemonDao1.getParameterValues()) {
            System.out.println(value);
        }
    }

}
