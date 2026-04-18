package com.univ.scheduler.vue;

import com.univ.scheduler.dao.UtilisateurDAO;
import com.univ.scheduler.modele.Utilisateur;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class VueConnexion extends VueBase {
    private VBox racine;
    private UtilisateurDAO utilisateurDAO;
    private Stage stagePrincipal;
    
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
        titreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titreLabel.setTextFill(Color.web(TEXTE_PRINCIPAL));
        
        Label sousTitreLabel = new Label("Gestion des salles et emplois du temps");
        sousTitreLabel.setFont(Font.font("Arial", 14));
        sousTitreLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        // Formulaire
        VBox formulaireBox = new VBox(15);
        formulaireBox.setPadding(new Insets(30));
        formulaireBox.setStyle(getStyleCarte());
        formulaireBox.setMaxWidth(400);
        
        Label utilisateurLabel = new Label("Nom d'utilisateur");
        utilisateurLabel.setTextFill(Color.web("#2c3e50"));
        utilisateurLabel.setStyle("-fx-font-weight: bold;");
        
        TextField champUtilisateur = new TextField();
        champUtilisateur.setPromptText("Entrez votre identifiant");
        champUtilisateur.setStyle(getStyleTextField());
        
        Label motDePasseLabel = new Label("Mot de passe");
        motDePasseLabel.setTextFill(Color.web("#2c3e50"));
        motDePasseLabel.setStyle("-fx-font-weight: bold;");
        
        PasswordField champMotDePasse = new PasswordField();
        champMotDePasse.setPromptText("Entrez votre mot de passe");
        champMotDePasse.setStyle(getStyleTextField());
        
        Button boutonConnexion = new Button("Se connecter");
        boutonConnexion.setStyle(getStyleBoutonPrimaire());
        boutonConnexion.setPrefWidth(200);
        
        boutonConnexion.setOnMouseEntered(e -> 
            boutonConnexion.setStyle(
                "-fx-background-color: " + COULEUR_BOUTON_HOVER + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 15;"
            )
        );
        
        boutonConnexion.setOnMouseExited(e -> 
            boutonConnexion.setStyle(getStyleBoutonPrimaire())
        );
        
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.web(COULEUR_DANGER));
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
    
    @Override
    public VBox getRacine() {
        return racine;
    }
}