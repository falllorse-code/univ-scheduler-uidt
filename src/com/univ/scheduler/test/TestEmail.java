package com.univ.scheduler.test;

import com.univ.scheduler.utils.EmailService;

public class TestEmail {
    public static void main(String[] args) {
        System.out.println("=== TEST ENVOI EMAIL ===");
        System.out.println("Configuration pour: falllorse@gmail.com");
        
        EmailService emailService = new EmailService();
        
        if (emailService.estConfigure()) {
            System.out.println("✅ Service email correctement configuré");
            
            boolean success = emailService.envoyerEmail(
                "falllorse@gmail.com", // À toi-même
                "Test UNIV-SCHEDULER - " + java.time.LocalDateTime.now(),
                "Bonjour,\n\nCeci est un test d'envoi d'email depuis mon application UNIV-SCHEDULER.\n\n" +
                "L'email a été envoyé le " + java.time.LocalDateTime.now() + "\n\n" +
                "Cordialement,\nL'équipe UNIV-SCHEDULER"
            );
            
            if (success) {
                System.out.println("✅ Email envoyé avec succès !");
                System.out.println("📧 Vérifie ta boîte mail: falllorse@gmail.com");
            } else {
                System.out.println("❌ Échec de l'envoi");
            }
        } else {
            System.out.println("❌ Service email non configuré");
            System.out.println("Vérifie que le fichier config.properties existe à la racine du projet");
        }
    }
}