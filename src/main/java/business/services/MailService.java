/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
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
import business.models.NewPasswordRequest;
import business.models.RequestProperties;
import business.models.User;
import business.representation.LabRequestRepresentation;
import business.representation.RequestRepresentation;

@Service
public class MailService {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    UserService userService;
    
    @Autowired
    JavaMailSender mailSender;

    @Value("${dntp.server-name}")
    String serverName;

    @Value("${dntp.server-port}")
    String serverPort;

    @Value("${dntp.reply-address}")
    String replyAddress;

    static final String replyName = "Stichting PALGA";

    static final String fromName = "Stichting PALGA";

    private String getFrom() {
        return "no-reply@" + serverName;
    }

    private String getLink(String relativeURI) {
        String protocol = "http";
        boolean writePort = true;
        if (serverPort.equals("443")) {
            protocol = "https";
            writePort = false;
        } else if (serverPort.equals("80")) {
            writePort = false;
        }
        return String.format("%s://%s%s%s", 
                protocol, serverName, (writePort ? ":"+serverPort : ""), relativeURI);
    }

    static final String requesterAgreementFormLinkTemplate = 
            "Geachte heer/mevrouw,\n"
          + "\n"
          + "PALGA heeft uw aanvraag ontvangen.\n"
          + "Vul het handtekeningformulier in (%1$s) en stuur het naar PALGA, aanvraag@palga.nl.\n"
          + "\n"
          + "Met vriendelijke groet,\n"
          + "Stichting PALGA\n"
          + "088-0402700 / aanvraag@palga.nl\n"
          + "\n"
          + "--\n"
          + "Dit is een automatisch gegenereerd bericht. "
          + "Heeft u vragen, stuur dan een mail naar: aanvraag@palga.nl.\n"
          + "\n"
          + "=========================\n"
          + "\n"
          + "Dear Sir/Madam,\n"
          + "\n"
          + "PALGA has received your request.\n"
          + "Please complete the agreement form (%1$s) and send it to PALGA, aanvraag@palga.nl.\n"
          + "\n"
          + "With kind regards,\n"
          + "Stichting PALGA\n"
          + "088-0402700 / aanvraag@palga.nl\n"
          + "\n"
          + "--\n"
          + "This is an automatically generated message. "
          + "If you have questions, please send an email to aanvraag@palga.nl.\n"
          ;

    @Transactional
    public void sendAgreementFormLink(@NotNull User requester,
            @NotNull RequestProperties request) {
        log.info("Send agreement form link to requester for request "
                + request.getRequestNumber() + ".");

        log.info("Sending link to user " + requester.getUsername());
        try {
            MimeMessageHelper message = new MimeMessageHelper(
                    mailSender.createMimeMessage());
            message.setTo(requester.getContactData().getEmail());
            message.setFrom(getFrom(), fromName);
            message.setReplyTo(replyAddress, replyName);
            message.setSubject(String.format("Nieuwe PALGA-aanvraag ontvangen, aanvraagnummer: %s", request.getRequestNumber()));
            String agreementFormLink = getLink(
                    "/#/request/"
                    + request.getProcessInstanceId()
                    + "/agreementform");
            message.setText(String.format(
                    requesterAgreementFormLinkTemplate, agreementFormLink));
            mailSender.send(message.getMimeMessage());
        } catch (MessagingException e) {
            log.error(e.getMessage());
            throw new EmailError("Email error: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            throw new EmailError("Email error: " + e.getMessage());
        }
    }

    static final String scientificCouncilNotificationTemplate = 
              "Geachte leden van de wetenschappelijke raad,\n"
            + "\n"
            + "Graag uw beoordeling van de volgende aanvraag: %1$s.\n"
            + "\n"
            + "Bovenstaande link verwijst naar het beoordelingssysteem. "
            + "Daar kunt u de aanvraag beoordelen en eventuele opmerkingen plaatsen.\n"
            + "U kunt inloggen met uw gebruikersnaam en wachtwoord.\n"
            + "\n"
            + "--\n"
            + "Dit is een automatisch gegenereerd bericht. "
            + "Indien u vragen heeft kunt u contact opnemen met Stichting PALGA,\n"
            + "088-0402700 / aanvraag@palga.nl.\n"
            ;

    @Transactional
    public void notifyScientificCouncil(@NotNull RequestRepresentation request) {
        log.info("Notify scientic council for request " + request.getProcessInstanceId() + ".");

        List<User> members = userService.findScientificCouncilMembers();
        for (User member: members) {
            log.info("Sending notification to user " + member.getUsername());
            try {
                MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage());
                message.setTo(member.getContactData().getEmail());
                message.setFrom(getFrom(), fromName);
                message.setReplyTo(replyAddress, replyName);
                message.setSubject(String.format("Nieuwe PALGA-aanvraag aan u voorgelegd, aanvraagnummer: %s", request.getRequestNumber()));
                String requestLink = getLink("/#/request/view/" + request.getProcessInstanceId());
                message.setText(String.format(scientificCouncilNotificationTemplate, requestLink));
                mailSender.send(message.getMimeMessage());
            } catch(MessagingException e) {
                log.error(e.getMessage());
                throw new EmailError("Email error: " + e.getMessage());
            } catch(UnsupportedEncodingException e) {
                log.error(e.getMessage());
                throw new EmailError("Email error: " + e.getMessage());
            }
        }
    }

    static final String labNotificationTemplate = 
            "Geachte heer/mevrouw,\n"
          + "\n"
          + "PALGA heeft een aanvraag ontvangen bestemd voor uw laboratorium.\n"
          + "Deze link verwijst naar het verzoek van PALGA aan uw laboratorium:\n"
          + "  %1$s.\n"
          + "\n"
          + "Aanvraagnummer:\t%2$s\n"
          + "Titel project:\t%3$s\n"
          + "Onderzoeker:\t%4$s\n"
          + "Patholoog:\t%5$s\n"
          + "Instituut:\t%6$s\n"
          + "\n"
          + "Met vriendelijke groet,\n"
          + "Stichting PALGA\n"
          + "088-0402700 / aanvraag@palga.nl\n"
          + "\n"
          + "--\n"
          + "Dit is een automatisch gegenereerd bericht. "
          + "Heeft u vragen, stuur dan een mail naar: aanvraag@palga.nl.\n"
          + "\n"
          + "=========================\n"
          + "\n"
          + "Dear Sir/Madam,\n"
          + "\n"
          + "PALGA has received a request for your lab.\n"
          + "Please follow this link to view the request: %1$s.\n"
          + "\n"
          + "Lab request:\t%2$s\n"
          + "Project title:\t%3$s\n"
          + "Requester:\t%4$s\n"
          + "Pathologist:\t%5$s\n"
          + "Institute:\t%6$s\n"
          + "\n"
          + "With kind regards,\n"
          + "Stichting PALGA\n"
          + "088-0402700 / aanvraag@palga.nl\n"
          + "\n"
          + "--\n"
          + "This is an automatically generated message. "
          + "If you have questions, please send an email to aanvraag@palga.nl.\n"
          ;

    public void notifyLab(@NotNull LabRequestRepresentation labRequest) {
        log.info("Notify lab for lab request " + labRequest.getId() + ".");

        Lab lab = labRequest.getLab();
        if (lab.getContactData() == null || lab.getContactData().getEmail() == null) {
            log.warn("No email address set for lab " + lab.getNumber());
            return;
        }
        log.info("Sending notification to " + lab.getContactData().getEmail());
        try {
            MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage());
            message.setTo(lab.getContactData().getEmail());
            message.setFrom(getFrom(), fromName);
            message.setReplyTo(replyAddress, replyName);
            message.setSubject(String.format("PALGA-verzoek aan laboratorium, aanvraagnummer: %s", labRequest.getLabRequestCode()));
            String labRequestLink = getLink("/#/lab-request/view/" + labRequest.getId());
            String body = String.format(labNotificationTemplate,
                    labRequestLink, // %1
                    labRequest.getLabRequestCode(), // %2
                    labRequest.getRequest().getTitle(), // %3
                    labRequest.getRequesterName(), // %4
                    labRequest.getRequest().getPathologistName() == null ? "" : labRequest.getRequest().getPathologistName(), // %5
                    labRequest.getRequesterLab().getName() // %6
                    );
            message.setText(body);
            mailSender.send(message.getMimeMessage());
        } catch(MessagingException e) {
            log.error(e.getMessage());
            throw new EmailError("Email error: " + e.getMessage());
        } catch(UnsupportedEncodingException e) {
            log.error(e.getMessage());
            throw new EmailError("Email error: " + e.getMessage());
        }
    }

    static final String activationEmailTemplate = 
              "Geachte heer/mevrouw,\n"
            + "\n"
            + "U heeft een account aangemaakt bij Stichting PALGA. "
            + "Voordat u uw aanvraag bij PALGA in kunt dienen moet dit account nog geactiveerd worden.\n"
            + "Via deze link activeert u uw account: %1$s.\n"
            + "\n"
            + "Met vriendelijke groet,\n"
            + "Stichting PALGA\n"
            + "088-0402700 / aanvraag@palga.nl\n"
            + "\n"
            + "--\n"
            + "Dit is een automatisch gegenereerd bericht. "
            + "Heeft u vragen, stuur dan een mail naar: aanvraag@palga.nl.\n"
            + "\n"
            + "=========================\n"
            + "\n"
            + "Dear Sir/Madam,\n"
            + "\n"
            + "You have created an account at PALGA. "
            + "Before you can submit your request to PALGA, your account needs to be activated.\n"
            + "Please follow this link to activate your account: %1$s.\n"
            + "\n"
            + "With kind regards,\n"
            + "Stichting PALGA\n"
            + "088-0402700 / aanvraag@palga.nl\n"
            + "\n"
            + "--\n"
            + "This is an automatically generated message. "
            + "If you have questions, please send an email to aanvraag@palga.nl.\n"
            ;

    public void sendActivationEmail(@NotNull ActivationLink link) {
        // Send email to user
        try {
            MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage());
            String recipient = link.getUser().getUsername();
            message.setTo(recipient);
            message.setFrom(getFrom(), fromName);
            message.setReplyTo(replyAddress, replyName);
            message.setSubject("PALGA-account activeren / Activate PALGA account");
            String activationLink = getLink("/#/activate/" + link.getToken());
            message.setText(String.format(activationEmailTemplate, activationLink));
            mailSender.send(message.getMimeMessage());
            log.info("Activation link token generated for " + recipient +
                    ": " + link.getToken());
        } catch(MessagingException e) {
            log.error(e.getMessage());
            throw new EmailError("Email error: " + e.getMessage());
        } catch(UnsupportedEncodingException e) {
            log.error(e.getMessage());
            throw new EmailError("Email error: " + e.getMessage());
        }
    }

    static final String passwordRecoveryTemplate = 
              "Geachte heer/mevrouw,\n"
            + "\n"
            + "Via deze link kunt u uw PALGA wachtwoord opnieuw instellen: %1$s.\n"
            + "\n"
            + "Met vriendelijke groet,\n"
            + "Stichting PALGA\n"
            + "088-0402700 / aanvraag@palga.nl\n"
            + "\n"
            + "--\n"
            + "Dit is een automatisch gegenereerd bericht. "
            + "Heeft u vragen, stuur dan een mail naar aanvraag@palga.nl.\n"
            + "\n"
            + "=========================\n"
            + "\n"
            + "Dear Sir/Madam,\n"
            + "\n"
            + "Please follow this link to reset your PALGA password: %1$s.\n"
            + "\n"
            + "With kind regards,\n"
            + "Stichting PALGA\n"
            + "088-0402700 / aanvraag@palga.nl\n"
            + "\n"
            + "--\n"
            + "This is an automatically generated message. "
            + "If you have questions, please send an email to aanvraag@palga.nl.\n"
            ;

    public static final String passwordRecoverySubject = "PALGA-wachtwoord opnieuw instellen / Reset PALGA password";

    public void sendPasswordRecoveryToken(NewPasswordRequest npr) {
        try {
            MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage());
            String recipient = npr.getUser().getContactData().getEmail();
            message.setTo(recipient);
            message.setFrom(getFrom(), fromName);
            message.setReplyTo(replyAddress, replyName);
            message.setSubject(passwordRecoverySubject);
            String passwordRecoveryLink = getLink("/#/login/reset-password/" + npr.getToken());
            message.setText(String.format(passwordRecoveryTemplate, passwordRecoveryLink));
            log.info("Sending password recovery token to " + recipient + ".");
            mailSender.send(message.getMimeMessage());
        } catch(MessagingException e) {
            log.error(e.getMessage());
            throw new EmailError("Email error: " + e.getMessage());
        } catch(UnsupportedEncodingException e) {
            log.error(e.getMessage());
            throw new EmailError("Email error: " + e.getMessage());
        }
    }
    
    public boolean checkMailSender() {
        if (mailSender == null) {
            return false;
        }
        mailSender.createMimeMessage();
        return true;
    }

}
