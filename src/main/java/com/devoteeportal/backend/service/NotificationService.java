package com.devoteeportal.backend.service;

import com.devoteeportal.backend.entity.User;

public interface NotificationService {

    void notifyRegistrationReceived(User user);

    void notifyAccountApproved(User user);

    void notifyAccountRejected(User user);

    void notifyReferralRequestReceived(User referrer, User requester, String companyName);

    void notifyContactRequestReceived(User target, User requester);

    void notifyContactRequestApproved(User requester, User target);

    void sendOtpEmail(String email, String otpCode);

}
