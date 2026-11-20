package com.love.joy.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService
{

    //my base URL so I can build links inside emails
    private final JavaMailSender mailSender;
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender)
    {
        this.mailSender = mailSender;
    }

    // to send emails
    public void sendingMessage(String to, String subject, String text)
    {
        SimpleMailMessage msg = new SimpleMailMessage();   // creating Spring email message
        msg.setTo(to);               //who the email is going to
        msg.setSubject(subject);     //subject line of the email
        msg.setText(text);           //the message
        mailSender.send(msg);     //sending the email using the configured mail server (MailHog)
    }
}
