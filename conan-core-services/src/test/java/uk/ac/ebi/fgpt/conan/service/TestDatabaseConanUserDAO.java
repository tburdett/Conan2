package uk.ac.ebi.fgpt.conan.service;

import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.conan.core.user.ConanUserWithPermissions;
import uk.ac.ebi.fgpt.conan.dao.DatabaseConanUserDAO;

import java.util.List;

/**
 * Tests a DatabaseConanTaskDAO
 *
 * @author Natalja Kurbatova
 * @date 02-Nov-2010
 */
public class TestDatabaseConanUserDAO extends TestCase {
    private DatabaseConanUserDAO userDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("database-application-context.xml");
        userDao = (DatabaseConanUserDAO) ctx.getBean("DatabaseConanUserDAO", DatabaseConanUserDAO.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        userDao = null;
    }

    public void testConanUser() {
        //Test insert into conan_users
        ConanUserWithPermissions userTest =
                new ConanUserWithPermissions("test", "test", "test", "natalja@ebi.ac.uk", "111111");
        userDao.saveUser(userTest);
        System.out.println("Inserted user database ID: " + userTest.getId());
        String id = userTest.getId();

        //Test update of conan_users
        userTest = new ConanUserWithPermissions("testUpdated", "test", "test", "natalja@ebi.ac.uk", "111111");
        userTest.setId(id);
        userDao.saveUser(userTest);

        System.out.println("GET ALL USERS BY EMAIL:");
        List<ConanUserWithPermissions> users = (List) userDao.getUserByEmail("natalja@ebi.ac.uk");
        for (ConanUserWithPermissions gu : users) {
            System.out.println(gu.getUserName());
        }

        System.out.println("GET USER BY RestApiKey:");
        ConanUserWithPermissions user = (ConanUserWithPermissions) userDao.getUserByRestApiKey("111111");
        System.out.println(user.getUserName() + " " + user.getRestApiKey());

        System.out.println("GET USER BY ID:");
        user = (ConanUserWithPermissions) userDao.getUser(id);
        System.out.println(user.getUserName() + " " + user.getRestApiKey());

        System.out.println("GET USER BY User Name:");
        users = (List) userDao.getUserByUserName(userTest.getUserName());
        for (ConanUserWithPermissions gu : users) {
            System.out.println(gu.getUserName());
        }

        System.out.println("GET ALL USERS:");
        users = (List) userDao.getUsers();
        for (ConanUserWithPermissions gu : users) {
            System.out.println(gu.getUserName());
        }

        userDao.deleteUser(userTest);


    }

}
