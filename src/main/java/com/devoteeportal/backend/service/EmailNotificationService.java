package com.devoteeportal.backend.service;

import com.devoteeportal.backend.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender javaMailSender;

    @Override
    public void notifyRegistrationReceived(User user) {
        sendEmail(
                user.getEmail(),
                "Registration Received - Iskcon Devotee Career Portal",
                "Hare Krishna " + user.getName() + ",\n\nYour registration has been received and is currently pending approval by an administrator."
        );
    }

    @Override
    public void notifyAccountApproved(User user) {
        sendEmail(
                user.getEmail(),
                "Account Approved - Iskcon Devotee Career Portal",
                "Hare Krishna " + user.getName() + ",\n\nYour account has been approved. You may now log in to the portal."
        );
    }

    @Override
    public void notifyAccountRejected(User user) {
        sendEmail(
                user.getEmail(),
                "Account Rejected - Iskcon Devotee Career Portal",
                "Hare Krishna " + user.getName() + ",\n\nUnfortunately, your account registration has been rejected."
        );
    }

    @Override
    public void notifyReferralRequestReceived(User referrer, User requester, String companyName) {
        sendEmail(
                referrer.getEmail(),
                "New Referral Request",
                "Hare Krishna " + referrer.getName() + ",\n\nYou have received a new referral request from " + requester.getName() + " for a position at " + companyName + ".\nPlease log in to review the request."
        );
    }

    @Override
    public void notifyContactRequestReceived(User target, User requester) {
        sendEmail(
                target.getEmail(),
                "New Contact Request",
                "Hare Krishna " + target.getName() + ",\n\nYou have received a new contact request from " + requester.getName() + ".\nPlease log in to review the request."
        );
    }

    @Override
    public void notifyContactRequestApproved(User requester, User target) {
        sendEmail(
                requester.getEmail(),
                "Contact Request Approved",
                "Hare Krishna " + requester.getName() + ",\n\nYour contact request to " + target.getName() + " has been approved. You can now view their contact details on their profile."
        );
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@devoteeportal.com");

            javaMailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send email to {}. SMTP may not be configured properly. Error: {}", to, e.getMessage());
        }
    }
}
