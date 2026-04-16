package com.univ.scheduler.vue;

import com.univ.scheduler.modele.Utilisateur;
import com.univ.scheduler.modele.Signalement;
import com.univ.scheduler.dao.SignalementDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class VuePrincipale {
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private Stage stagePrincipal;
    
    private VueCarte vueCarteActuelle;
    private VueStatistiques vueStatistiquesActuelle;
    
    // Thème clair
    private final String FOND_PRINCIPAL = "#f5f7fa";
    private final String FOND_SIDEBAR = "#2c3e50";
    private final String TEXTE_PRINCIPAL = "#ffffff";
    private final String TEXTE_SECONDAIRE = "#bdc3c7";
    private final String COULEUR_SURVOL = "#34495e";
    private final String COULEUR_BORDURE = "#e0e0e0";
    
    public VuePrincipale(Stage stagePrincipal, Utilisateur utilisateur) {
        this.stagePrincipal = stagePrincipal;
        this.utilisateurCourant = utilisateur;
        creerVue();
    }
    
    private void creerVue() {
        racine = new BorderPane();
        racine.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        // ===== EN-TÊTE =====
        HBox headerBar = new HBox();
        headerBar.setPadding(new Insets(10, 20, 10, 20));
        headerBar.setStyle("-fx-background-color: white; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 0 1 0;");
        headerBar.setAlignment(Pos.CENTER_LEFT);
        
        Label appTitle = new Label("📚 UNIV-SCHEDULER");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        appTitle.setTextFill(Color.web("#2c3e50"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label("👤 " + utilisateurCourant.getPrenom() + " " + utilisateurCourant.getNom());
        userLabel.setFont(Font.font("System", 12));
        userLabel.setTextFill(Color.web("#7f8c8d"));
        userLabel.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 12; -fx-background-radius: 15;");
        
        Button btnDeconnexion = new Button("🔓 Déconnexion");
        btnDeconnexion.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 15; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 12px;");
        btnDeconnexion.setOnAction(e -> deconnexion());
        
        headerBar.getChildren().addAll(appTitle, spacer, userLabel, btnDeconnexion);
        
        // ===== SIDEBAR =====
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: " + FOND_SIDEBAR + ";");
        
        // Bienvenue
        Label bienvenueLabel = new Label("Bienvenue,");
        bienvenueLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        bienvenueLabel.setFont(Font.font("System", 12));
        
        Label userNameLabel = new Label(utilisateurCourant.getPrenom() + " " + utilisateurCourant.getNom());
        userNameLabel.setTextFill(Color.web(TEXTE_PRINCIPAL));
        userNameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        VBox welcomeBox = new VBox(2);
        welcomeBox.setPadding(new Insets(0, 0, 20, 0));
        welcomeBox.getChildren().addAll(bienvenueLabel, userNameLabel);
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #34495e;");
        separator.setPadding(new Insets(5, 0, 15, 0));
        
        // Boutons communs à tous (consultation)
        Button btnEmploiTemps = creerBoutonSidebar("📅", "Emploi du temps");
        Button btnRechercher = creerBoutonSidebar("🔍", "Rechercher salle");
        
        sidebar.getChildren().addAll(welcomeBox, separator, btnEmploiTemps, btnRechercher);
        
        String role = utilisateurCourant.getRole();
        
        // ============================================
        // ADMIN - Gestion globale (accès à tout)
        // ============================================
        if (role.equals("admin")) {
            Button btnTableauBord = creerBoutonSidebar("📊", "Tableau de bord");
            Button btnUtilisateurs = creerBoutonSidebar("👥", "Gestion utilisateurs");
            Button btnSalles = creerBoutonSidebar("🏢", "Configurer salles");
            Button btnCours = creerBoutonSidebar("📚", "Gestion cours");
            Button btnStatistiques = creerBoutonSidebar("📈", "Statistiques");
            Button btnReservations = creerBoutonSidebar("📝", "Réservations");
            Button btnCarte = creerBoutonSidebar("🗺️", "Carte");
            Button btnNotifications = creerBoutonSidebar("🔔", "Notifications");
            
            sidebar.getChildren().addAll(
                btnTableauBord, btnUtilisateurs, btnSalles, btnCours, 
                btnStatistiques, btnReservations, btnCarte, btnNotifications
            );
            
            btnTableauBord.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueTableauBord vue = new VueTableauBord(utilisateurCourant);
                racine.setCenter(vue.getRacine());
            });
            
            btnUtilisateurs.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueUtilisateurs vue = new VueUtilisateurs(utilisateurCourant);
                racine.setCenter(vue.getRacine());
            });
            
            btnSalles.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueSalles vue = new VueSalles(utilisateurCourant, stagePrincipal);
                racine.setCenter(vue.getRacine());
            });
            
            btnCours.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueCours vue = new VueCours(utilisateurCourant, stagePrincipal);
                racine.setCenter(vue.getRacine());
            });
            
            btnStatistiques.setOnAction(e -> {
                arreterToutesSynchronisations();
                vueStatistiquesActuelle = new VueStatistiques(utilisateurCourant);
                racine.setCenter(vueStatistiquesActuelle.getRacine());
            });
            
            btnReservations.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueReservation vue = new VueReservation(utilisateurCourant);
                racine.setCenter(vue.getRacine());
            });
            
            btnCarte.setOnAction(e -> {
                arreterToutesSynchronisations();
                vueCarteActuelle = new VueCarte(utilisateurCourant);
                racine.setCenter(vueCarteActuelle.getRacine());
            });
            
            btnNotifications.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueNotifications vue = new VueNotifications(utilisateurCourant);
                racine.setCenter(vue.getRacine());
            });
        }
        
        // ============================================
        // GESTIONNAIRE - Planification uniquement
        // (NE peut PAS ajouter/modifier des salles)
        // ============================================
        if (role.equals("manager")) {
            Button btnCours = creerBoutonSidebar("📚", "Créer/modifier cours");
            Button btnSalles = creerBoutonSidebar("🏢", "Consulter salles");
            Button btnConflits = creerBoutonSidebar("⚠️", "Résoudre conflits");
            Button btnEmploiGen = creerBoutonSidebar("📅", "Générer emploi du temps");
            
            sidebar.getChildren().addAll(btnCours, btnSalles, btnConflits, btnEmploiGen);
            
            btnCours.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueCours vue = new VueCours(utilisateurCourant, stagePrincipal);
                racine.setCenter(vue.getRacine());
            });
            
            btnSalles.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueSalles vue = new VueSalles(utilisateurCourant, stagePrincipal);
                racine.setCenter(vue.getRacine());
            });
            
            btnConflits.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueCours vue = new VueCours(utilisateurCourant, stagePrincipal);
                racine.setCenter(vue.getRacine());
                javafx.application.Platform.runLater(() -> vue.verifierConflits());
            });
            
            btnEmploiGen.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueEmploiTemps vue = new VueEmploiTemps(utilisateurCourant, stagePrincipal);
                racine.setCenter(vue.getRacine());
            });
        }
        
        // ============================================
        // ENSEIGNANT - Consultation et réservation
        // ============================================
        if (role.equals("enseignant")) {
            Button btnReservations = creerBoutonSidebar("📝", "Réserver salle");
            Button btnCarte = creerBoutonSidebar("🗺️", "Carte");
            Button btnProbleme = creerBoutonSidebar("🛠️", "Signaler problème");
            
            sidebar.getChildren().addAll(btnReservations, btnCarte, btnProbleme);
            
            btnReservations.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueReservation vue = new VueReservation(utilisateurCourant);
                racine.setCenter(vue.getRacine());
            });
            
            btnCarte.setOnAction(e -> {
                arreterToutesSynchronisations();
                vueCarteActuelle = new VueCarte(utilisateurCourant);
                racine.setCenter(vueCarteActuelle.getRacine());
            });
            
            btnProbleme.setOnAction(e -> {
                Dialog<Signalement> dialog = new Dialog<>();
                dialog.setTitle("🛠️ Signaler un problème");
                dialog.setHeaderText("Formulaire de signalement");
                
                ButtonType btnEnvoyer = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(btnEnvoyer, ButtonType.CANCEL);
                
                VBox content = new VBox(10);
                content.setPadding(new Insets(20));
                content.setPrefWidth(400);
                
                Label problemeLabel = new Label("Décrivez le problème :");
                problemeLabel.setStyle("-fx-font-weight: bold;");
                
                TextArea textProbleme = new TextArea();
                textProbleme.setPromptText("Ex: Vidéoprojecteur ne fonctionne pas, tableau blanc abîmé, etc.");
                textProbleme.setPrefRowCount(5);
                textProbleme.setWrapText(true);
                
                Label salleLabel = new Label("Salle concernée (optionnel) :");
                salleLabel.setStyle("-fx-font-weight: bold;");
                
                ComboBox<String> comboSalle = new ComboBox<>();
                comboSalle.getItems().addAll("Non précisé", "A101", "A102", "A103", "B201", "B202", "C301", "Amphi 1", "Amphi 2");
                comboSalle.setValue("Non précisé");
                
                Label infoLabel = new Label("Le signalement sera envoyé à l'administrateur.");
                infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                
                content.getChildren().addAll(problemeLabel, textProbleme, salleLabel, comboSalle, infoLabel);
                
                dialog.getDialogPane().setContent(content);
                
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == btnEnvoyer) {
                        String probleme = textProbleme.getText();
                        if (probleme == null || probleme.trim().isEmpty()) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Erreur");
                            alert.setHeaderText("Description obligatoire");
                            alert.setContentText("Veuillez décrire le problème.");
                            alert.showAndWait();
                            return null;
                        }
                        Signalement s = new Signalement(
                            utilisateurCourant.getNomComplet(),
                            utilisateurCourant.getEmail(),
                            comboSalle.getValue(),
                            probleme
                        );
                        return s;
                    }
                    return null;
                });
                
                dialog.showAndWait().ifPresent(signalement -> {
                    SignalementDAO dao = new SignalementDAO();
                    if (dao.ajouterSignalement(signalement)) {
                        Alert confirm = new Alert(Alert.AlertType.INFORMATION);
                        confirm.setTitle("Signalement envoyé");
                        confirm.setHeaderText("✅ Merci pour votre signalement");
                        confirm.setContentText("Un administrateur traitera votre demande rapidement.");
                        confirm.showAndWait();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur");
                        error.setHeaderText("❌ Échec de l'envoi");
                        error.setContentText("Le signalement n'a pas pu être envoyé.");
                        error.showAndWait();
                    }
                });
            });
        }
        
        // ============================================
        // ÉTUDIANT - Consultation uniquement
        // ============================================
        if (role.equals("etudiant")) {
            Button btnCarte = creerBoutonSidebar("🗺️", "Carte des salles");
            sidebar.getChildren().add(btnCarte);
            
            btnCarte.setOnAction(e -> {
                arreterToutesSynchronisations();
                vueCarteActuelle = new VueCarte(utilisateurCourant);
                racine.setCenter(vueCarteActuelle.getRacine());
            });
        }
        
        // Actions des boutons communs
        btnEmploiTemps.setOnAction(e -> {
            arreterToutesSynchronisations();
            VueEmploiTemps vue = new VueEmploiTemps(utilisateurCourant, stagePrincipal);
            racine.setCenter(vue.getRacine());
        });
        
        btnRechercher.setOnAction(e -> {
            arreterToutesSynchronisations();
            VueRecherche vue = new VueRecherche(utilisateurCourant);
            racine.setCenter(vue.getRacine());
        });
        
        // Assemblage
        racine.setTop(headerBar);
        racine.setLeft(sidebar);
        
        // Contenu par défaut
        racine.setCenter(new VueEmploiTemps(utilisateurCourant, stagePrincipal).getRacine());
    }
    
    private Button creerBoutonSidebar(String icone, String texte) {
        Button bouton = new Button(icone + "  " + texte);
        bouton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXTE_SECONDAIRE + ";" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10 15;" +
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8;" +
            "-fx-pref-width: 190;"
        );
        
        bouton.setOnMouseEntered(e -> 
            bouton.setStyle(
                "-fx-background-color: " + COULEUR_SURVOL + ";" +
                "-fx-text-fill: " + TEXTE_PRINCIPAL + ";" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 10 15;" +
                "-fx-alignment: CENTER-LEFT;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8;"
            )
        );
        
        bouton.setOnMouseExited(e -> 
            bouton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + TEXTE_SECONDAIRE + ";" +
                "-fx-font-size: 13px;" +
                "-fx-padding: 10 15;" +
                "-fx-alignment: CENTER-LEFT;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 8;"
            )
        );
        
        return bouton;
    }
    
    private void arreterToutesSynchronisations() {
        if (vueCarteActuelle != null) {
            vueCarteActuelle.arreterSynchronisation();
            vueCarteActuelle = null;
        }
        if (vueStatistiquesActuelle != null) {
            vueStatistiquesActuelle.arreterSynchronisation();
            vueStatistiquesActuelle = null;
        }
    }
    
    private void deconnexion() {
        arreterToutesSynchronisations();
        VueConnexion vueConnexion = new VueConnexion(stagePrincipal);
        stagePrincipal.getScene().setRoot(vueConnexion.getRacine());
        stagePrincipal.setTitle("UNIV-SCHEDULER - Connexion");
        stagePrincipal.setWidth(400);
        stagePrincipal.setHeight(300);
    }
    
    public BorderPane getRacine() {
        return racine;
    }
}