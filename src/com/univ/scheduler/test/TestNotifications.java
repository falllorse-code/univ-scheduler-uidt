package com.univ.scheduler.test;

import com.univ.scheduler.modele.Reservation;
import com.univ.scheduler.modele.Utilisateur;
import com.univ.scheduler.utils.NotificationService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;

public class TestNotifications extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        NotificationService notifications = NotificationService.getInstance();
        
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPrenom("Jean");
        utilisateur.setNom("Dupont");
        utilisateur.setEmail("falllorse@gmail.com");
        
        Reservation reservation = new Reservation();
        reservation.setTitre("Réunion pédagogique");
        reservation.setNomSalle("Bâtiment A - A101");
        reservation.setDate(LocalDate.now().plusDays(1));
        reservation.setHeureDebut(LocalTime.of(10, 0));
        reservation.setHeureFin(LocalTime.of(12, 0));
        
        Button btnTestConflit = new Button("Test Alerte Conflit");
        btnTestConflit.setOnAction(e -> 
            notifications.afficherAlerteConflit("Conflit entre 'Mathématiques' et 'Algorithmique' en salle A101")
        );
        
        Button btnTestEmail = new Button("Test Notification Email");
        btnTestEmail.setOnAction(e -> 
            notifications.envoyerNotificationEmail(utilisateur, "Test Notification", "Ceci est un test de notification")
        );
        
        Button btnTestRappel = new Button("Test Rappel (10 min)");
        btnTestRappel.setOnAction(e -> 
            notifications.programmerRappelDebutReservation(reservation, utilisateur)
        );
        
        Button btnTestFin = new Button("Test Rappel Fin");
        btnTestFin.setOnAction(e -> 
            notifications.programmerRappelFinReservation(reservation, utilisateur)
        );
        
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(20));
        root.getChildren().addAll(btnTestConflit, btnTestEmail, btnTestRappel, btnTestFin);
        
        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("Test Notifications");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}