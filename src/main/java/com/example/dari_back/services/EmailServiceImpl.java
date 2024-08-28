package com.example.dari_back.services;

import com.example.dari_back.MFA.EmailConfirmationToken;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender sender;

    public EmailServiceImpl(JavaMailSender sender) {
        this.sender = sender;
    }


    @Override
    public void sendConfirmationEmail(EmailConfirmationToken emailConfirmationToken) throws MessagingException, jakarta.mail.MessagingException {
        // Création du message MIME HTML
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(emailConfirmationToken.getUser().getUsername());
        helper.setSubject("Confirmez votre adresse e-mail - Inscription à l'application MFA");

        // Contenu HTML de l'e-mail avec le style CSS et l'image
        String emailContent = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #f4f4f4; padding: 20px; text-align: center; }" +
                ".content { padding: 20px; }" +
                "img.logo { display: block; margin: 0 auto; max-width: 200px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<img class='logo' src='https://th.bing.com/th/id/OIP.u5P9HWmtuZwI_sDrSqEFfAHaHa?rs=1&pid=ImgDetMain' alt='Logo'>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Cher "+ emailConfirmationToken.getUser().getFirstname() + ",</h2>" +
                "<p>Nous sommes ravis de vous accueillir pour commencer. Veuillez cliquer sur le lien ci-dessous pour confirmer votre compte.</p>" +
                "<p><a href=\"" + generateConfirmationLink(emailConfirmationToken.getToken()) + "\" class=\"button\">Confirmer l'e-mail</a></p>" +
                "<p>Cordialement,<br/>L'équipe d'inscription MFA</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(emailContent, true);
        sender.send(message);
    }
    private String generateConfirmationLink(String token) {
        return "http://localhost:8080/confirm-email?token=" + token;
    }





    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendPasswordResetEmail(String to, String token) throws MessagingException, jakarta.mail.MessagingException {
        String subject = "Password Reset";
        String body = "Dear User,\n\nPlease click on the following link to reset your password:\n\n"
                + "http://localhost:4200/reset-password?token=" + token + "\n\nThank you.";



        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException, jakarta.mail.MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        javaMailSender.send(message);
    }

    public void sendEmail1(String recipientEmail, String link)
            throws MessagingException, UnsupportedEncodingException, jakarta.mail.MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("contact@shopme.com", "Shopme Support");
        helper.setTo(recipientEmail);

        String subject = "Here's the link to reset your password";

        String content = "<p>Hello,</p>"
                + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>"
                + "<p><a href=\"" + link + "\">Change my password</a></p>"
                + "<br>"
                + "<p>Ignore this email if you do remember your password, "
                + "or you have not made the request.</p>";

        helper.setSubject(subject);

        helper.setText(content, true);

        javaMailSender.send(message);
    }
}


