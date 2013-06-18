package uk.ac.ebi.fgpt.conan.web.listener;

/**
 * Created with IntelliJ IDEA.
 * User: emma
 * Date: 18/06/13
 * Time: 13:29
 * Checks the environment at startup and sets some sensible defaults if expected values are missing.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ConanEnvironmentListener implements ServletContextListener {

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String conanPath= servletContextEvent.getServletContext().getContextPath();
        System.setProperty("conan.home", conanPath);
    }
     public void contextDestroyed(ServletContextEvent servletContextEvent) {
        getLog().info("Shutting down Conan.");
    }

}