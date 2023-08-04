package com.example.profileauth;


import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    public static void sendEmail(String toEmail, String subject, String body) {
        String fromEmail = "irfanmohammad.co.1@gmail.com";
        String password = "zkshungejhouizhy";
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Runnable sendEmailRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmail));
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                    message.setSubject(subject);
                    message.setText(body);
                    Transport.send(message);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        };

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(sendEmailRunnable);
    }
}

