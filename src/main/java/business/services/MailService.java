package business.services;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import business.models.ActivationLink;
import business.models.NewPasswordRequest;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
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
            SimpleMailMessage message = new SimpleMailMessage();
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
            log.info("Mail contents:\n" + message.getText());
            mailSender.send(message);
        }
    }
    
    public void sendActivationEmail(@NotNull ActivationLink link) {
        // Send email to user
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(link.getUser().getUsername());
        message.setFrom(replyAddress);
        message.setReplyTo(replyAddress);
        message.setSubject("Account activation");
        message.setText(String.format("Please follow this link to activate your account: http://%s:%s/#/activate/%s", serverName, serverPort, link.getToken()));
        mailSender.send(message);
        LogFactory.getLog(this.getClass()).info("Recovery password token generated: " + link.getToken());
    }
    
    public void sendPasswordRecoveryToken(NewPasswordRequest npr) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(npr.getUser().getContactData().getEmail());
        message.setFrom(replyAddress);
        message.setReplyTo(replyAddress);
        message.setSubject("Password recovery");
        message.setText(String.format("Please follow this link to reset your password: http://%s:%s/#/login/reset-password/%s", serverName, serverPort, npr.getToken()));
        mailSender.send(message);
    }

}
