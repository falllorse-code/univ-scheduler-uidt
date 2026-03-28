package com.univ.scheduler.utils;

import com.univ.scheduler.modele.Reservation;
import com.univ.scheduler.modele.Utilisateur;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService {
    
    private static NotificationService instance;
    private Timer timer;
    private EmailService emailService;
    
    private NotificationService() {
        timer = new Timer(true);
        emailService = new EmailService();
        if (emailService.estConfigure()) {
            System.out.println("✅ Service email réel activé");
        } else {
            System.out.println("⚠️ Service email en mode simulation");
        }
    }
    
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    // ===== ALERTES CONFLIT =====
    public void afficherAlerteConflit(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ Conflit détecté");
            alert.setHeaderText("Problème de planification");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // ===== NOTIFICATIONS EMAIL (réelles) =====
    public void envoyerNotificationEmail(Utilisateur utilisateur, String sujet, String contenu) {
        String nom = utilisateur.getNomComplet();
        String email = utilisateur.getEmail();
        
        if (emailService.estConfigure() && email != null && !email.isEmpty()) {
            // Envoyer un vrai email
            boolean success = emailService.envoyerEmail(email, sujet, contenu);
            
            if (success) {
                System.out.println("✅ Email réel envoyé à " + email);
            } else {
                System.err.println("❌ Échec envoi email réel à " + email);
                // Fallback : afficher dans la console
                afficherSimulationEmail(nom, email, sujet, contenu);
            }
        } else {
            // Simulation si email non configuré
            afficherSimulationEmail(nom, email, sujet, contenu);
        }
        
        // Toujours afficher l'alerte dans l'interface
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("📧 Notification");
            alert.setHeaderText("Notification pour " + nom);
            alert.setContentText("Sujet: " + sujet + "\n\n" + contenu);
            alert.showAndWait();
        });
    }
    
    private void afficherSimulationEmail(String nom, String email, String sujet, String contenu) {
        System.out.println("=".repeat(60));
        System.out.println("📧 NOTIFICATION EMAIL (SIMULATION)");
        System.out.println("=".repeat(60));
        System.out.println("Destinataire: " + nom + " <" + email + ">");
        System.out.println("Sujet: " + sujet);
        System.out.println("Contenu: " + contenu);
        System.out.println("Date d'envoi: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("=".repeat(60));
    }
    
    public void notifierChangementSalle(Utilisateur utilisateur, Reservation ancienne, Reservation nouvelle) {
        String sujet = "🔄 Changement de salle - " + ancienne.getTitre();
        String contenu = String.format(
            "Bonjour %s,\n\n" +
            "Votre réservation '%s' a été modifiée.\n\n" +
            "Ancienne salle: %s\n" +
            "Nouvelle salle: %s\n" +
            "Date: %s\n" +
            "Horaire: %s - %s\n\n" +
            "Merci de votre compréhension.\n" +
            "L'équipe UNIV-SCHEDULER",
            utilisateur.getPrenom(),
            ancienne.getTitre(),
            ancienne.getNomSalle(),
            nouvelle.getNomSalle(),
            nouvelle.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            nouvelle.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")),
            nouvelle.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
        
        envoyerNotificationEmail(utilisateur, sujet, contenu);
    }
    
    // ===== RAPPELS DE FIN DE RÉSERVATION =====
    public void programmerRappelFinReservation(Reservation reservation, Utilisateur utilisateur) {
        LocalDateTime finReservation = LocalDateTime.of(reservation.getDate(), reservation.getHeureFin());
        LocalDateTime maintenant = LocalDateTime.now();
        
        // Calculer le délai jusqu'à la fin de la réservation
        long delay = java.time.Duration.between(maintenant, finReservation).toMillis();
        
        if (delay > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("⏰ Rappel de fin de réservation");
                        alert.setHeaderText("Votre réservation se termine bientôt");
                        alert.setContentText(String.format(
                            "La réservation '%s' dans la salle %s se termine à %s.\n\n" +
                            "Pensez à libérer la salle.",
                            reservation.getTitre(),
                            reservation.getNomSalle(),
                            reservation.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm"))
                        ));
                        alert.showAndWait();
                        
                        // Envoyer aussi un email de rappel
                        String sujet = "⏰ Rappel - Fin de réservation";
                        String contenu = String.format(
                            "Bonjour %s,\n\n" +
                            "Votre réservation '%s' dans la salle %s se termine à %s.\n\n" +
                            "Merci de libérer la salle.\n" +
                            "L'équipe UNIV-SCHEDULER",
                            utilisateur.getPrenom(),
                            reservation.getTitre(),
                            reservation.getNomSalle(),
                            reservation.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm"))
                        );
                        envoyerNotificationEmail(utilisateur, sujet, contenu);
                    });
                }
            }, delay);
            
            System.out.println("⏰ Rappel fin programmé pour: " + finReservation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
    }
    
    // ===== RAPPELS DE DÉBUT DE RÉSERVATION (10 min avant) =====
    public void programmerRappelDebutReservation(Reservation reservation, Utilisateur utilisateur) {
        LocalDateTime debutReservation = LocalDateTime.of(reservation.getDate(), reservation.getHeureDebut());
        LocalDateTime rappel = debutReservation.minusMinutes(10);
        LocalDateTime maintenant = LocalDateTime.now();
        
        long delay = java.time.Duration.between(maintenant, rappel).toMillis();
        
        if (delay > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("🔔 Rappel de réservation");
                        alert.setHeaderText("Votre réservation commence dans 10 minutes");
                        alert.setContentText(String.format(
                            "Réservation: %s\nSalle: %s\nHoraire: %s - %s",
                            reservation.getTitre(),
                            reservation.getNomSalle(),
                            reservation.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")),
                            reservation.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm"))
                        ));
                        alert.showAndWait();
                        
                        // Email de rappel
                        String sujet = "🔔 Rappel - Réservation imminente";
                        String contenu = String.format(
                            "Bonjour %s,\n\n" +
                            "Votre réservation '%s' dans la salle %s commence dans 10 minutes (%s).\n\n" +
                            "L'équipe UNIV-SCHEDULER",
                            utilisateur.getPrenom(),
                            reservation.getTitre(),
                            reservation.getNomSalle(),
                            reservation.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm"))
                        );
                        envoyerNotificationEmail(utilisateur, sujet, contenu);
                    });
                }
            }, delay);
            
            System.out.println("⏰ Rappel début programmé pour: " + rappel.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
    }
}