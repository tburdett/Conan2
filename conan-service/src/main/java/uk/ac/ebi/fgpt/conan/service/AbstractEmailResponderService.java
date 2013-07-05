package uk.ac.ebi.fgpt.conan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import uk.ac.ebi.fgpt.conan.model.ConanProcess;
import uk.ac.ebi.fgpt.conan.model.ConanTask;
import uk.ac.ebi.fgpt.conan.model.ConanUser;
import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An abstract implementation of a {@link ConanResponderService} that provides feedback by emailing the task submitter.
 * Implementations must provide a means to generate the email content, as a string, given a task and process run object.
 * This allows implementations to tailor their responses based on awareness of specific process implementations.
 *
 * @author Tony Burdett
 * @date 10-Nov-2010
 */
public abstract class AbstractEmailResponderService implements ConanResponderService {
    private MailSender mailSender;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public MailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void generateDaemonModeToggleResponse(boolean daemonModeActive,
                                                 ConanUser userRequestingChange,
                                                 ConanUser daemonOwner) {
        // build an email about the Conan mode switch
        SimpleMailMessage msg = new SimpleMailMessage();

        String userName = userRequestingChange.getFirstName();
        String active = daemonModeActive ? "enabled" : "disabled";
        String time = new SimpleDateFormat("h:mm a").format(new Date());

        String subject = "Conan daemon mode " + active;
        String messageText = "Hi " + userName + ",\n\n" +
                "You " + active + " Conan daemon mode today at " + time + "\n\n" +
                "All daemon mode notifications are currently being sent to " + daemonOwner.getEmail();

        // configure and send the message
        msg.setFrom("conan@ebi.ac.uk");
        msg.setTo(userRequestingChange.getEmail());
        msg.setCc(daemonOwner.getEmail());
        msg.setSubject(subject);
        msg.setText(messageText);
        try {
            getLog().debug(
                    "Sending email...\n" +
                            "\tFrom: " + msg.getFrom() + "\n" +
                            "\tTo: " + msg.getTo()[0] + "\n" +
                            "\tSubject: " + msg.getSubject());
            getMailSender().send(msg);
        } catch (Exception e) {
            // simply log it and go on...
            getLog().error("Failed to send message \"" + msg.getSubject() + "\"!", e);
        }
        getLog().debug("Response sent!");
    }

    public void generateDaemonOwnerChangeResponse(String previousOwnerEmail,
                                                  ConanUser userRequestingChange,
                                                  ConanUser daemonOwner) {
        // build an email about the Conan mode switch
        SimpleMailMessage msg = new SimpleMailMessage();

        String userName = userRequestingChange.getFirstName();
        String time = new SimpleDateFormat("h:mm a").format(new Date());

        String subject = "Conan daemon mode email address change";
        String messageText = "Hi " + userName + ",\n\n" +
                "You changed the email address that Conan daemon mode notifications " +
                "are sent to today at " + time + "\n\n" +
                "All daemon mode notifications will now be sent to " + daemonOwner.getEmail() +
                (previousOwnerEmail.equals("")
                        ? ".  Previously, there was no notification email address configured."
                        : " instead of " + previousOwnerEmail + ".");

        // configure the message
        msg.setFrom("conan@ebi.ac.uk");
        msg.setTo(userRequestingChange.getEmail());
        msg.setSubject(subject);
        msg.setText(messageText);

        // send a copy to old and new owners, if different from the user requesting change
        if (!daemonOwner.getEmail().equals(userRequestingChange.getEmail()) && !daemonOwner.getEmail().equals("")) {
            msg.setCc(daemonOwner.getEmail());
        }
        if (!previousOwnerEmail.equals(userRequestingChange.getEmail()) && !previousOwnerEmail.equals("")) {
            msg.setCc(previousOwnerEmail);
        }

        // send the message
        try {
            getLog().debug(
                    "Sending email...\n" +
                            "\tFrom: " + msg.getFrom() + "\n" +
                            "\tTo: " + msg.getTo()[0] + "\n" +
                            "\tSubject: " + msg.getSubject());
            getMailSender().send(msg);
        } catch (Exception e) {
            // simply log it and go on...
            getLog().error("Failed to send message \"" + msg.getSubject() + "\"!", e);
        }
        getLog().debug("Response sent!");
    }

    public void generateResponse(ConanTask task) {
        if (respondsTo(task)) {
            getLog().debug("Generating response for task '" + task.getId() + "'");
            ConanProcess lastProcess = task.getLastProcess();

            // build an email error message
            SimpleMailMessage msg = new SimpleMailMessage();


            // get subject and message text from implementations
            String subject = getEmailSubject(task, lastProcess);
            String messageText = getEmailContent(task, lastProcess);

            // generate and send the email
            msg.setFrom("conan@ebi.ac.uk");
            msg.setTo(task.getSubmitter().getEmail());
            msg.setSubject(subject);
            msg.setText(messageText);
            try {
                getLog().debug(
                        "Sending email...\n" +
                                "\tFrom: " + msg.getFrom() + "\n" +
                                "\tTo: " + msg.getTo()[0] + "\n" +
                                "\tSubject: " + msg.getSubject());
                getMailSender().send(msg);
            } catch (Exception e) {
                // simply log it and go on...
                getLog().error("Failed to send message \"" + msg.getSubject() + "\"!", e);
            }
            getLog().debug("Response sent!");
        }
    }

    public void generateResponse(ConanTask task, ProcessExecutionException pex) {
        if (respondsTo(task)) {
            getLog().debug("Generating response for task '" + task.getId() + "'");

            // get subject and message text from implementations
            ConanProcess lastProcess = task.getLastProcess();
            String subject = getEmailSubject(task, lastProcess);
            String messageText = getEmailContent(task, lastProcess, pex);

            if (!task.getSubmitter().getEmail().equals("")) {
                // build an email error message
                SimpleMailMessage msg = new SimpleMailMessage();

                // generate and send the email
                msg.setFrom("conan@ebi.ac.uk");
                msg.setTo(task.getSubmitter().getEmail());
                msg.setSubject(subject);
                msg.setText(messageText);
                try {
                    getLog().debug(
                            "Sending email...\n" +
                                    "\tFrom: " + msg.getFrom() + "\n" +
                                    "\tTo: " + msg.getTo()[0] + "\n" +
                                    "\tSubject: " + msg.getSubject());
                    getMailSender().send(msg);
                } catch (Exception e) {
                    // simply log it and go on...
                    getLog().error("Failed to send message \"" + msg.getSubject() + "\"!", e);
                }
                getLog().debug("Response sent!");
            } else {
                getLog().warn("The submitter of task '" + task.getId() + "' " +
                        "(" + task.getSubmitter().getUserName() + ") has no email address.  " +
                        "The current notification, '" + subject + "' will be lost.");
            }
        }
    }

    protected abstract String getEmailSubject(ConanTask task, ConanProcess process);

    protected abstract String getEmailContent(ConanTask task, ConanProcess process);

    protected abstract String getEmailContent(ConanTask task, ConanProcess process, ProcessExecutionException pex);
}
