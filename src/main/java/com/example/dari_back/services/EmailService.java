package com.example.dari_back.services;


import com.example.dari_back.MFA.EmailConfirmationToken;
import org.springframework.messaging.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailService {
    void sendConfirmationEmail(EmailConfirmationToken emailConfirmationToken) throws MessagingException, jakarta.mail.MessagingException;
    void sendPasswordResetEmail(String to, String token) throws MessagingException, jakarta.mail.MessagingException;
    public void sendEmail1(String recipientEmail, String link) throws MessagingException, UnsupportedEncodingException, jakarta.mail.MessagingException;
}