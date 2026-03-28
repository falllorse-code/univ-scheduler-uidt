package com.univ.scheduler.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EmailService {
    
    private Session session;
    private String from;
    private boolean estConfigure = false;
    
    public EmailService() {
        Properties config = chargerConfiguration();
        String host = config.getProperty("email.smtp.host", "smtp.gmail.com");
        String port = config.getProperty("email.smtp.port", "587");
        from = config.getProperty("email.from", "");
        String password = config.getProperty("email.password", "");
        
        if (from.isEmpty() || password.isEmpty()) {
            System.err.println("⚠️ Configuration email incomplète. Vérifie le fichier config.properties");
            estConfigure = false;
        } else {
            estConfigure = initialiserSession(host, port, from, password);
        }
    }
    
    /**
     * Charge la configuration depuis le fichier config.properties
     */
    private Properties chargerConfiguration() {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            System.out.println("✅ Configuration email chargée depuis config.properties");
            System.out.println("📧 Compte configuré: " + props.getProperty("email.from"));
        } catch (IOException e) {
            System.err.println("❌ Erreur: fichier config.properties non trouvé!");
            System.err.println("Crée un fichier config.properties à la racine du projet avec :");
            System.err.println("email.smtp.host=smtp.gmail.com");
            System.err.println("email.smtp.port=587");
            System.err.println("email.from=falllorse@gmail.com");
            System.err.println("email.password=xpzefmnoypdrjctw");
            
            // Valeurs par défaut (vides)
            props.setProperty("email.smtp.host", "smtp.gmail.com");
            props.setProperty("email.smtp.port", "587");
            props.setProperty("email.from", "");
            props.setProperty("email.password", "");
        }
        return props;
    }
    
    /**
     * Initialise la session SMTP avec les paramètres de configuration
     */
    private boolean initialiserSession(String host, String port, String from, String password) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            
            // Détection automatique du protocole selon le port
            if (port.equals("465")) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.port", port);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                System.out.println("📧 Utilisation du port 465 avec SSL");
            } else {
                props.put("mail.smtp.starttls.enable", "true");
                System.out.println("📧 Utilisation du port " + port + " avec STARTTLS");
            }
            
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", "30000");
            props.put("mail.smtp.timeout", "30000");
            
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            });
            
            session.setDebug(true);
            System.out.println("✅ Session SMTP initialisée pour " + from);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation SMTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envoie un email simple en texte
     * @param destinataire Adresse email du destinataire
     * @param sujet Sujet de l'email
     * @param contenu Contenu texte de l'email
     * @return true si l'envoi a réussi, false sinon
     */
    public boolean envoyerEmail(String destinataire, String sujet, String contenu) {
        if (!estConfigure || session == null) {
            System.err.println("❌ Session SMTP non initialisée ou configuration incomplète");
            return false;
        }
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(contenu);
            message.setHeader("X-Mailer", "UNIV-SCHEDULER");
            
            Transport.send(message);
            System.out.println("✅ Email envoyé à " + destinataire);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Envoie un email au format HTML
     * @param destinataire Adresse email du destinataire
     * @param sujet Sujet de l'email
     * @param contenuHtml Contenu HTML de l'email
     * @return true si l'envoi a réussi, false sinon
     */
    public boolean envoyerEmailHtml(String destinataire, String sujet, String contenuHtml) {
        if (!estConfigure || session == null) {
            System.err.println("❌ Session SMTP non initialisée");
            return false;
        }
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setContent(contenuHtml, "text/html; charset=utf-8");
            message.setHeader("X-Mailer", "UNIV-SCHEDULER");
            
            Transport.send(message);
            System.out.println("✅ Email HTML envoyé à " + destinataire);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email HTML: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Vérifie si le service email est correctement configuré
     */
    public boolean estConfigure() {
        return estConfigure && session != null && from != null && !from.isEmpty();
    }
    
    /**
     * Retourne l'adresse email expéditrice
     */
    public String getFrom() {
        return from;
    }
}