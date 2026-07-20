package com.example.ats.service.impl;

import com.example.ats.config.MailProperties;
import com.example.ats.service.EmailService;
import com.example.ats.util.EmailTemplateUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service implementation for dispatching HTML emails via JavaMailSender.
 *
 * <p>Enforces transaction independence: SMTP or mail transport errors are caught,
 * logged with full diagnostics (recipient, subject, timestamp), and suppressed so
 * that core business transactions (interview scheduling, stage changes) are never rolled back.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Async
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Override
    public void sendInterviewInvitation(
            String recipientEmail,
            String candidateName,
            String jobTitle,
            String companyName,
            String interviewDate,
            String interviewType,
            String meetingLocationOrLink,
            String recruiterName
    ) {
        String subject = String.format("Interview Invitation: %s at %s", jobTitle, companyName);
        String htmlContent = EmailTemplateUtil.buildInterviewInvitationTemplate(
                candidateName, jobTitle, companyName, interviewDate, interviewType, meetingLocationOrLink, recruiterName);

        sendEmail(recipientEmail, subject, htmlContent);
    }

    @Override
    public void sendApplicationStatusUpdate(
            String recipientEmail,
            String candidateName,
            String jobTitle,
            String companyName,
            String newStatus
    ) {
        if ("OFFER".equalsIgnoreCase(newStatus)) {
            sendOfferNotification(recipientEmail, candidateName, jobTitle, companyName);
            return;
        }
        if ("REJECTED".equalsIgnoreCase(newStatus)) {
            sendRejectionNotification(recipientEmail, candidateName, jobTitle, companyName);
            return;
        }

        String subject = String.format("Update on your application for %s at %s", jobTitle, companyName);
        String htmlContent = EmailTemplateUtil.buildStatusUpdateTemplate(
                candidateName, jobTitle, companyName, newStatus);

        sendEmail(recipientEmail, subject, htmlContent);
    }

    @Override
    public void sendOfferNotification(
            String recipientEmail,
            String candidateName,
            String jobTitle,
            String companyName
    ) {
        String subject = String.format("Job Offer: %s at %s", jobTitle, companyName);
        String htmlContent = EmailTemplateUtil.buildOfferNotificationTemplate(candidateName, jobTitle, companyName);

        sendEmail(recipientEmail, subject, htmlContent);
    }

    @Override
    public void sendRejectionNotification(
            String recipientEmail,
            String candidateName,
            String jobTitle,
            String companyName
    ) {
        String subject = String.format("Application Status Update: %s at %s", jobTitle, companyName);
        String htmlContent = EmailTemplateUtil.buildRejectionNotificationTemplate(candidateName, jobTitle, companyName);

        sendEmail(recipientEmail, subject, htmlContent);
    }

    private void sendEmail(String recipientEmail, String subject, String htmlContent) {
        Instant timestamp = Instant.now();

        if (recipientEmail == null || recipientEmail.trim().isEmpty() || !recipientEmail.contains("@")) {
            log.error("Email sending failed: Invalid recipient email address | Recipient: '{}' | Subject: '{}' | Timestamp: {}",
                    recipientEmail, subject, timestamp);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(mailProperties.getUsername());
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            log.info("Email sent successfully | Recipient: {} | Subject: '{}' | Timestamp: {}",
                    recipientEmail, subject, timestamp);

        } catch (MailException ex) {
            log.error("Email sending failed (MailException) | Recipient: {} | Subject: '{}' | Timestamp: {} | Error: {}",
                    recipientEmail, subject, timestamp, ex.getMessage(), ex);
        } catch (MessagingException ex) {
            log.error("Email sending failed (MessagingException) | Recipient: {} | Subject: '{}' | Timestamp: {} | Error: {}",
                    recipientEmail, subject, timestamp, ex.getMessage(), ex);
        } catch (Throwable ex) {
            log.error("Email sending failed (Unexpected Error) | Recipient: {} | Subject: '{}' | Timestamp: {} | Error: {}",
                    recipientEmail, subject, timestamp, ex.getMessage(), ex);
        }
    }
}
