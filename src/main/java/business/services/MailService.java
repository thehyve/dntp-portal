package business.services;

import java.util.Set;

import javax.mail.MessagingException;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import business.exceptions.EmailError;
import business.models.ActivationLink;
import business.models.Lab;
import business.models.LabRequest;
import business.models.NewPasswordRequest;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.representation.LabRequestRepresentation;
import business.representation.RequestRepresentation;

@Service
public class MailService {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    RoleRepository roleRepository;
    
    @Autowired
    JavaMailSender mailSender;

    @Value("${dntp.server-name}")
    String serverName;

    @Value("${dntp.server-port}")
    String serverPort;

    @Value("${dntp.reply-address}")
    String replyAddress;
    
    public void notifyScientificCouncil(@NotNull RequestRepresentation request) {
        log.info("Notify scientic council for request " + request.getProcessInstanceId() + ".");

        Role role = roleRepository.findByName("scientific_council");
        Set<User> members = role.getUsers();
        for (User member: members) {
            log.info("Sending notification to user " + member.getUsername());
            try {
                MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage());
                message.setTo(member.getContactData().getEmail());
                message.setFrom(replyAddress);
                message.setReplyTo(replyAddress);
                message.setSubject("[DNTP portal] New request open for approval.");
                String template =
                        "Request: %s\n"
                    +   "Requester: %s\n"
                    +   "Principal Investigator: %s\n"
                    +   "Title: %s\n"
                    +   "\nBackground:\n%s\n"
                    +   "\nResearch Question:\n%s\n"
                    +   "\nHypothesis:\n%s\n"
                    +   "\nMethods:\n%s\n"
                    ;
                String body = String.format(template,
                        request.getProcessInstanceId(),
                        request.getRequesterName(),
                        request.getContactPersonName(),
                        request.getTitle(),
                        request.getBackground(),
                        request.getResearchQuestion(),
                        request.getHypothesis(),
                        request.getMethods()
                        );
                message.setText(String.format(
                        ""
                        + "Please follow this link to view the new request: http://%s:%s/#/request/view/%s.\n"
                        + "====\n"
                        + body,
                        serverName, serverPort, request.getProcessInstanceId()));
                mailSender.send(message.getMimeMessage());
            } catch (MessagingException e) {
                log.error("MailService: " + e.getMessage());
            }
        }
    }
    
    public void notifyLab(@NotNull LabRequestRepresentation labRequest) {
        log.info("Notify lab for lab request " + labRequest.getId() + ".");

        Lab lab = labRequest.getLab();
        if (lab.getContactData() == null) {
            log.warn("No contact data set for lab " + lab.getNumber());
            return;
        }
        log.info("Sending notification to " + lab.getContactData().getEmail());
        try {
            MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage());
            message.setTo(lab.getContactData().getEmail());
            message.setFrom(replyAddress);
            message.setReplyTo(replyAddress);
            message.setSubject("[DNTP portal] New lab request open for approval.");
            String template =
                    "Lab request: %d\n"
                +   "Requester: %s\n"
                ;
            String body = String.format(template,
                    labRequest.getId(),
                    labRequest.getRequesterName()
                    );
            // FIXME: not url /#/labrequest/view exists yet!
            message.setText(String.format(
                    ""
                    + "Please follow this link to view the new request: http://%s:%s/#/labrequest/view/%d.\n"
                    + "====\n"
                    + body,
                    serverName, serverPort, labRequest.getId()));
            mailSender.send(message.getMimeMessage());
        } catch (MessagingException e) {
            log.error("MailService: " + e.getMessage());
        }
    }
    
    public void sendActivationEmail(@NotNull ActivationLink link) {
        // Send email to user
        try {
            MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage());
            message.setTo(link.getUser().getUsername());
            message.setFrom(replyAddress);
            message.setReplyTo(replyAddress);
            message.setSubject("Account activation");
            message.setText(String.format("Please follow this link to activate your account: http://%s:%s/#/activate/%s", serverName, serverPort, link.getToken()));
            mailSender.send(message.getMimeMessage());
            LogFactory.getLog(this.getClass()).info("Recovery password token generated: " + link.getToken());
        } catch(MessagingException e) {
            throw new EmailError("Email error: " + e.getMessage());
        }
    }
    
    public void sendPasswordRecoveryToken(NewPasswordRequest npr) {
        try {
            MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage());
            message.setTo(npr.getUser().getContactData().getEmail());
            message.setFrom(replyAddress);
            message.setReplyTo(replyAddress);
            message.setSubject("Password recovery");
            message.setText(String.format("Please follow this link to reset your password: http://%s:%s/#/login/reset-password/%s", serverName, serverPort, npr.getToken()));
            log.info("Sending password recovery token. mailSender: " + mailSender.getClass());
            mailSender.send(message.getMimeMessage());
        } catch(MessagingException e) {
            throw new EmailError("Email error: " + e.getMessage());
        }
    }

}
