package com.devoteeportal.backend.service;

import com.devoteeportal.backend.entity.Notification;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.NotificationRepository;
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
    private final NotificationRepository notificationRepository;

    @Override
    public void notifyRegistrationReceived(User user) {
        String title = "Registration Received";
        String message = "Your registration has been received and is currently pending approval by an administrator.";
        saveInAppNotification(user, title, message);
        sendEmail(user.getEmail(), title + " - Iskcon Devotee Career Portal", "Hare Krishna " + user.getName() + ",\n\n" + message);
    }

    @Override
    public void notifyAccountApproved(User user) {
        String title = "Account Approved";
        String message = "Your account has been approved. You may now log in to the portal.";
        saveInAppNotification(user, title, message);
        sendEmail(user.getEmail(), title + " - Iskcon Devotee Career Portal", "Hare Krishna " + user.getName() + ",\n\n" + message);
    }

    @Override
    public void notifyAccountRejected(User user) {
        String title = "Account Rejected";
        String message = "Unfortunately, your account registration has been rejected.";
        saveInAppNotification(user, title, message);
        sendEmail(user.getEmail(), title + " - Iskcon Devotee Career Portal", "Hare Krishna " + user.getName() + ",\n\n" + message);
    }

    @Override
    public void notifyReferralRequestReceived(User referrer, User requester, String companyName) {
        String title = "New Referral Request";
        String message = "You have received a new referral request from " + requester.getName() + " for " + companyName + ".";
        saveInAppNotification(referrer, title, message);
        
        String subject = "New Referral Request for " + companyName;
        String text = String.format("Hare Krishna %s,\n\n%s\nPlease log in to the portal to review the request.\n\nYour Servants,\nDevotee Career Portal Team",
                referrer.getInitiatedName() != null ? referrer.getInitiatedName() : referrer.getName(),
                message);
        sendEmail(referrer.getEmail(), subject, text);
    }

    @Override
    public void notifyReferralRequestApproved(User requester, User referrer, String companyName) {
        String title = "Referral Request Approved";
        String message = "Your referral request for " + companyName + " has been approved by " + referrer.getName() + ".";
        saveInAppNotification(requester, title, message);
        
        String subject = "Referral Request Approved for " + companyName;
        String text = String.format("Hare Krishna %s,\n\n%s\nPlease log in to the portal to view the details.\n\nYour Servants,\nDevotee Career Portal Team",
                requester.getInitiatedName() != null ? requester.getInitiatedName() : requester.getName(),
                message);
        sendEmail(requester.getEmail(), subject, text);
    }

    @Override
    public void notifyReferralRequestRejected(User requester, User referrer, String companyName) {
        String title = "Referral Request Update";
        String message = "We wanted to let you know that " + referrer.getName() + " is unable to provide a referral for " + companyName + " at this time.";
        saveInAppNotification(requester, title, message);
        
        String subject = "Referral Request Update for " + companyName;
        String text = String.format("Hare Krishna %s,\n\n%s\n\nYour Servants,\nDevotee Career Portal Team",
                requester.getInitiatedName() != null ? requester.getInitiatedName() : requester.getName(),
                message);
        sendEmail(requester.getEmail(), subject, text);
    }

    @Override
    public void notifyContactRequestReceived(User target, User requester) {
        String title = "New Contact Request";
        String message = "You have received a new contact request from " + requester.getName() + ".";
        saveInAppNotification(target, title, message);
        
        sendEmail(
                target.getEmail(),
                title,
                "Hare Krishna " + target.getName() + ",\n\n" + message + "\nPlease log in to review the request."
        );
    }

    @Override
    public void notifyContactRequestApproved(User requester, User target) {
        String title = "Contact Request Approved";
        String message = "Your contact request to " + target.getName() + " has been approved. You can now view their contact details on their profile.";
        saveInAppNotification(requester, title, message);
        
        sendEmail(
                requester.getEmail(),
                title,
                "Hare Krishna " + requester.getName() + ",\n\n" + message
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

    private void saveInAppNotification(User user, String title, String message) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to save in-app notification for user {}", user.getId(), e);
        }
    }

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
