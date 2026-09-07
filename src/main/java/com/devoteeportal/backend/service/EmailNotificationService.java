package com.devoteeportal.backend.service;

import com.devoteeportal.backend.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

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
        String subject = "New Referral Request for " + companyName;
        String text = String.format("Hare Krishna %s,\n\nYou have received a new referral request from %s for %s.\nPlease log in to the portal to review the request.\n\nYour Servants,\nDevotee Career Portal Team",
                referrer.getInitiatedName() != null ? referrer.getInitiatedName() : referrer.getName(),
                requester.getName(),
                companyName);
        sendEmail(referrer.getEmail(), subject, text);
    }

    @Override
    public void notifyReferralRequestApproved(User requester, User referrer, String companyName) {
        String subject = "Referral Request Approved for " + companyName;
        String text = String.format("Hare Krishna %s,\n\nYour referral request for %s has been approved by %s.\nPlease log in to the portal to view the details.\n\nYour Servants,\nDevotee Career Portal Team",
                requester.getInitiatedName() != null ? requester.getInitiatedName() : requester.getName(),
                companyName,
                referrer.getName());
        sendEmail(requester.getEmail(), subject, text);
    }

    @Override
    public void notifyReferralRequestRejected(User requester, User referrer, String companyName) {
        String subject = "Referral Request Update for " + companyName;
        String text = String.format("Hare Krishna %s,\n\nWe wanted to let you know that %s is unable to provide a referral for %s at this time.\n\nYour Servants,\nDevotee Career Portal Team",
                requester.getInitiatedName() != null ? requester.getInitiatedName() : requester.getName(),
                referrer.getName(),
                companyName);
        sendEmail(requester.getEmail(), subject, text);
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

    @Override
    public void sendOtpEmail(String email, String otpCode) {
        sendEmail(
                email,
                "Verification Code - Iskcon Devotee Career Portal",
                "Hare Krishna,\n\nYour verification code is: " + otpCode + "\n\nThis code is valid for 10 minutes.\n\nIf you did not request this code, please ignore this email."
        );
    }

    @Value("${app.mail.from:noreply@devoteeportal.com}")
    private String fromEmail;

    private void sendEmail(String to, String subject, String text) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                message.setFrom(fromEmail);

                javaMailSender.send(message);
                log.info("Email sent to {}", to);
            } catch (Exception e) {
                log.warn("Failed to send email to {}. SMTP may not be configured properly. Error: {}", to, e.getMessage());
            }
        });
    }
}
