package com.univ.scheduler.vue;

import com.univ.scheduler.dao.UtilisateurDAO;
import com.univ.scheduler.modele.Utilisateur;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class VueConnexion {
    private VBox racine;
    private UtilisateurDAO utilisateurDAO;
    private Stage stagePrincipal;
    
    // Thème sombre - TOUTES LES CONSTANTES DÉFINIES
    private final String FOND_PRINCIPAL = "#1a1a2e";
    private final String FOND_CARTE = "#16213e";
    private final String TEXTE_PRINCIPAL = "#ffffff";
    private final String TEXTE_SECONDAIRE = "#b0b0b0";
    private final String COULEUR_PRIMAIRE = "#4361ee";
    private final String COULEUR_SECONDAIRE = "#3f37c9";
    private final String COULEUR_SUCCESS = "#2ecc71";
    private final String COULEUR_WARNING = "#f39c12";
    private final String COULEUR_DANGER = "#e74c3c";  // ← C'ÉTAIT CECI QUI MANQUAIT
    private final String COULEUR_INFO = "#3498db";
    private final String BORDURE = "#2a2a4a";
    
    public VueConnexion(Stage stagePrincipal) {
        this.stagePrincipal = stagePrincipal;
        this.utilisateurDAO = new UtilisateurDAO();
        creerVue();
    }
    
    private void creerVue() {
        racine = new VBox(20);
        racine.setAlignment(Pos.CENTER);
        racine.setPadding(new Insets(30));
        racine.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        // Titre
        Label titreLabel = new Label("UNIV-SCHEDULER");
        titreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titreLabel.setTextFill(Color.web(COULEUR_PRIMAIRE));
        
        Label sousTitreLabel = new Label("Gestion des salles et emplois du temps");
        sousTitreLabel.setFont(Font.font("Arial", 14));
        sousTitreLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        // Formulaire
        VBox formulaireBox = new VBox(15);
        formulaireBox.setPadding(new Insets(30));
        formulaireBox.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
        formulaireBox.setMaxWidth(350);
        
        Label utilisateurLabel = new Label("Nom d'utilisateur");
        utilisateurLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        TextField champUtilisateur = new TextField();
        champUtilisateur.setPromptText("Entrez votre identifiant");
        champUtilisateur.setStyle(
            "-fx-background-color: " + FOND_PRINCIPAL + ";" +
            "-fx-text-fill: " + TEXTE_PRINCIPAL + ";" +
            "-fx-prompt-text-fill: " + TEXTE_SECONDAIRE + ";" +
            "-fx-border-color: " + BORDURE + ";" +
            "-fx-border-radius: 5;"
        );
        
        Label motDePasseLabel = new Label("Mot de passe");
        motDePasseLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        PasswordField champMotDePasse = new PasswordField();
        champMotDePasse.setPromptText("Entrez votre mot de passe");
        champMotDePasse.setStyle(
            "-fx-background-color: " + FOND_PRINCIPAL + ";" +
            "-fx-text-fill: " + TEXTE_PRINCIPAL + ";" +
            "-fx-prompt-text-fill: " + TEXTE_SECONDAIRE + ";" +
            "-fx-border-color: " + BORDURE + ";" +
            "-fx-border-radius: 5;"
        );
        
        Button boutonConnexion = new Button("Se connecter");
        boutonConnexion.setStyle(
            "-fx-background-color: " + COULEUR_PRIMAIRE + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        boutonConnexion.setPrefWidth(200);
        
        boutonConnexion.setOnMouseEntered(e -> 
            boutonConnexion.setStyle(
                "-fx-background-color: " + COULEUR_SECONDAIRE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;"
            )
        );
        
        boutonConnexion.setOnMouseExited(e -> 
            boutonConnexion.setStyle(
                "-fx-background-color: " + COULEUR_PRIMAIRE + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 10;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;"
            )
        );
        
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.web(COULEUR_DANGER));  // ← ICI ON UTILISE COULEUR_DANGER
        messageLabel.setWrapText(true);
        
        formulaireBox.getChildren().addAll(
            utilisateurLabel, champUtilisateur,
            motDePasseLabel, champMotDePasse,
            boutonConnexion, messageLabel
        );
        
        racine.getChildren().addAll(titreLabel, sousTitreLabel, formulaireBox);
        
        // Action du bouton
        boutonConnexion.setOnAction(e -> {
            String nomUtilisateur = champUtilisateur.getText();
            String motDePasse = champMotDePasse.getText();
            
            if (nomUtilisateur.isEmpty() || motDePasse.isEmpty()) {
                messageLabel.setText("❌ Veuillez remplir tous les champs");
                return;
            }
            
            Utilisateur utilisateur = utilisateurDAO.authentifier(nomUtilisateur, motDePasse);
            if (utilisateur != null) {
                VuePrincipale vuePrincipale = new VuePrincipale(stagePrincipal, utilisateur);
                stagePrincipal.getScene().setRoot(vuePrincipale.getRacine());
                stagePrincipal.setTitle("UNIV-SCHEDULER - Tableau de bord");
                stagePrincipal.setWidth(1200);
                stagePrincipal.setHeight(800);
            } else {
                messageLabel.setText("❌ Identifiants incorrects");
            }
        });
    }
    
    public VBox getRacine() {
        return racine;
    }
}