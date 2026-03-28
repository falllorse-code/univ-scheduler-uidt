package com.univ.scheduler.test;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class TestEmailSimple {
    public static void main(String[] args) {
        final String username = "falllorse@gmail.com";
        final String password = "jtjbvijmogdcyzit"; // Ton mot de passe d'application
        
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.connectiontimeout", "10000");
        prop.put("mail.smtp.timeout", "10000");
        
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        
        session.setDebug(true); // Active le debug pour voir
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, 
                InternetAddress.parse("falllorse@gmail.com"));
            message.setSubject("Test Email Simple");
            message.setText("Ceci est un test simple.");
            
            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès !");
            
        } catch (MessagingException e) {
            System.out.println("❌ Erreur détaillée:");
            e.printStackTrace();
        }
    }
}