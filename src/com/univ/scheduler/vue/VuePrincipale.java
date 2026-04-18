package com.univ.scheduler.vue;

import com.univ.scheduler.modele.Utilisateur;
import com.univ.scheduler.modele.Signalement;
import com.univ.scheduler.dao.SignalementDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VuePrincipale extends VueBase {
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private Stage stagePrincipal;
    
    private VueCarte vueCarteActuelle;
    private VueStatistiques vueStatistiquesActuelle;
    
    // Composants notification
    private Button btnNotification;
    private Label badgeNotification;
    private SignalementDAO signalementDAO;
    private Timeline notificationTimer;
    private ContextMenu notificationMenu;
    
    // Thème sidebar
    private final String FOND_SIDEBAR = "rgba(10, 50, 45, 0.95)";
    private final String COULEUR_SURVOL = "rgba(46, 204, 113, 0.2)";
    private final String COULEUR_BORDURE = "rgba(255,255,255,0.2)";
    
    public VuePrincipale(Stage stagePrincipal, Utilisateur utilisateur) {
        this.stagePrincipal = stagePrincipal;
        this.utilisateurCourant = utilisateur;
        this.signalementDAO = new SignalementDAO();
        creerVue();
        demarrerVerificationNotifications();
    }
    
    private void demarrerVerificationNotifications() {
        notificationTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            mettreAJourBadgeNotifications();
        }));
        notificationTimer.setCycleCount(Timeline.INDEFINITE);
        notificationTimer.play();
    }
    
    private void mettreAJourBadgeNotifications() {
        int nbNonTraites = signalementDAO.compterNonTraites();
        if (nbNonTraites > 0) {
            badgeNotification.setText(String.valueOf(Math.min(nbNonTraites, 99)));
            badgeNotification.setVisible(true);
            btnNotification.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                "-fx-background-radius: 50;" +
                "-fx-cursor: hand;" +
                "-fx-min-width: 36;" +
                "-fx-min-height: 36;" +
                "-fx-max-width: 36;" +
                "-fx-max-height: 36;"
            );
        } else {
            badgeNotification.setVisible(false);
            btnNotification.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 50;" +
                "-fx-cursor: hand;" +
                "-fx-min-width: 36;" +
                "-fx-min-height: 36;" +
                "-fx-max-width: 36;" +
                "-fx-max-height: 36;"
            );
        }
    }
    
    private void creerMenuNotifications() {
        notificationMenu = new ContextMenu();
        notificationMenu.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);" +
            "-fx-min-width: 320;" +
            "-fx-max-width: 350;"
        );
        
        actualiserMenuNotifications();
    }
    
    private void actualiserMenuNotifications() {
        notificationMenu.getItems().clear();
        
        java.util.List<Signalement> signalements = signalementDAO.getSignalementsNonTraites();
        
        // En-tête du menu
        Label headerLabel = new Label("Notifications");
        headerLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 15 5 15;");
        
        CustomMenuItem headerItem = new CustomMenuItem(headerLabel);
        headerItem.setDisable(true);
        headerItem.setHideOnClick(false);
        notificationMenu.getItems().add(headerItem);
        
        notificationMenu.getItems().add(new SeparatorMenuItem());
        
        if (signalements.isEmpty()) {
            Label emptyLabel = new Label("  📭 Aucune notification");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-padding: 20;");
            CustomMenuItem emptyItem = new CustomMenuItem(emptyLabel);
            emptyItem.setDisable(true);
            emptyItem.setHideOnClick(false);
            notificationMenu.getItems().add(emptyItem);
        } else {
            for (Signalement s : signalements) {
                CustomMenuItem customItem = new CustomMenuItem();
                
                VBox itemContent = new VBox(5);
                itemContent.setPadding(new Insets(8, 12, 8, 12));
                itemContent.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent #ecf0f1 transparent; -fx-border-width: 0 0 1 0;");
                
                // En-tête du signalement
                HBox headerBox = new HBox(8);
                headerBox.setAlignment(Pos.CENTER_LEFT);
                
                Label iconLabel = new Label("🔔");
                iconLabel.setStyle("-fx-font-size: 14px;");
                
                Label nameLabel = new Label(s.getEnseignant());
                nameLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 12px;");
                
                Label salleLabel = new Label("• " + s.getSalle());
                salleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label dateLabel = new Label(s.getDateFormatee().substring(0, 10));
                dateLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px;");
                
                headerBox.getChildren().addAll(iconLabel, nameLabel, salleLabel, spacer, dateLabel);
                
                // Description
                String desc = s.getDescription();
                if (desc.length() > 60) {
                    desc = desc.substring(0, 60) + "...";
                }
                Label descLabel = new Label(desc);
                descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px; -fx-wrap-text: true;");
                
                // Boutons d'action
                HBox actionBox = new HBox(8);
                actionBox.setPadding(new Insets(5, 0, 0, 0));
                
                Button btnTraiter = new Button("✅ Marquer traité");
                btnTraiter.setStyle(
                    "-fx-background-color: #2ecc71;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 10px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 4 12;" +
                    "-fx-cursor: hand;"
                );
                btnTraiter.setOnAction(event -> {
                    if (signalementDAO.marquerCommeTraite(s.getId())) {
                        actualiserMenuNotifications();
                        mettreAJourBadgeNotifications();
                        
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Succès");
                        alert.setHeaderText("✅ Signalement traité");
                        alert.setContentText("Le signalement a été marqué comme traité.");
                        alert.showAndWait();
                    }
                    notificationMenu.hide();
                });
                
                Button btnDetails = new Button("👁️ Détails");
                btnDetails.setStyle(
                    "-fx-background-color: #3498db;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 10px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 12;" +
                    "-fx-padding: 4 12;" +
                    "-fx-cursor: hand;"
                );
                btnDetails.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Détails du signalement");
                    alert.setHeaderText("Signalement de " + s.getEnseignant());
                    alert.setContentText(
                        "👤 Enseignant: " + s.getEnseignant() + "\n" +
                        "📧 Email: " + s.getEmail() + "\n" +
                        "🏢 Salle: " + s.getSalle() + "\n" +
                        "📅 Date: " + s.getDateFormatee() + "\n\n" +
                        "📝 Description:\n" + s.getDescription()
                    );
                    alert.showAndWait();
                    notificationMenu.hide();
                });
                
                actionBox.getChildren().addAll(btnTraiter, btnDetails);
                itemContent.getChildren().addAll(headerBox, descLabel, actionBox);
                
                customItem.setContent(itemContent);
                customItem.setHideOnClick(false);
                
                notificationMenu.getItems().add(customItem);
            }
        }
        
        notificationMenu.getItems().add(new SeparatorMenuItem());
        
        MenuItem voirToutItem = new MenuItem("📋 Voir tous les signalements");
        voirToutItem.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-padding: 10;");
        voirToutItem.setOnAction(e -> {
            VueNotifications vueNotifications = new VueNotifications(utilisateurCourant);
            racine.setCenter(vueNotifications.getRacine());
            notificationMenu.hide();
        });
        notificationMenu.getItems().add(voirToutItem);
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
    
    private void creerVue() {
        racine = new BorderPane();
        appliquerFond(racine);
        
        // ===== EN-TÊTE MODERNE =====
        HBox headerBar = new HBox();
        headerBar.setPadding(new Insets(10, 20, 10, 20));
        headerBar.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-border-color: " + COULEUR_BORDURE + ";" + 
            "-fx-border-width: 0 0 1 0;"
        );
        headerBar.setAlignment(Pos.CENTER_LEFT);
        
        Label appTitle = new Label("📚 UNIV-SCHEDULER");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        appTitle.setTextFill(Color.web(TEXTE_PRINCIPAL));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // ===== BOUTON NOTIFICATION (en haut à droite) =====
        StackPane notificationStack = new StackPane();
        
        btnNotification = new Button("🔔");
        btnNotification.setFont(Font.font("System", 16));
        btnNotification.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 50;" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 36;" +
            "-fx-min-height: 36;" +
            "-fx-max-width: 36;" +
            "-fx-max-height: 36;"
        );
        
        btnNotification.setOnMouseEntered(e -> 
            btnNotification.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                "-fx-background-radius: 50;" +
                "-fx-cursor: hand;" +
                "-fx-min-width: 36;" +
                "-fx-min-height: 36;" +
                "-fx-max-width: 36;" +
                "-fx-max-height: 36;"
            )
        );
        
        btnNotification.setOnMouseExited(e -> {
            int nb = signalementDAO.compterNonTraites();
            if (nb > 0) {
                btnNotification.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.2);" +
                    "-fx-background-radius: 50;" +
                    "-fx-cursor: hand;" +
                    "-fx-min-width: 36;" +
                    "-fx-min-height: 36;" +
                    "-fx-max-width: 36;" +
                    "-fx-max-height: 36;"
                );
            } else {
                btnNotification.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-background-radius: 50;" +
                    "-fx-cursor: hand;" +
                    "-fx-min-width: 36;" +
                    "-fx-min-height: 36;" +
                    "-fx-max-width: 36;" +
                    "-fx-max-height: 36;"
                );
            }
        });
        
        // Badge de notification
        badgeNotification = new Label("0");
        badgeNotification.setStyle(
            "-fx-background-color: #e74c3c;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 50;" +
            "-fx-padding: 2 5;" +
            "-fx-min-width: 18;" +
            "-fx-min-height: 18;" +
            "-fx-alignment: center;"
        );
        badgeNotification.setVisible(false);
        
        StackPane.setAlignment(badgeNotification, Pos.TOP_RIGHT);
        StackPane.setMargin(badgeNotification, new Insets(-5, -5, 0, 0));
        
        notificationStack.getChildren().addAll(btnNotification, badgeNotification);
        
        // Créer le menu contextuel
        creerMenuNotifications();
        
        // Afficher le menu au clic
        btnNotification.setOnAction(e -> {
            actualiserMenuNotifications();
            notificationMenu.show(btnNotification, 
                btnNotification.localToScreen(btnNotification.getBoundsInLocal()).getMinX(),
                btnNotification.localToScreen(btnNotification.getBoundsInLocal()).getMaxY());
        });
        
        Label userLabel = new Label("👤 " + utilisateurCourant.getPrenom() + " " + utilisateurCourant.getNom());
        userLabel.setFont(Font.font("System", 12));
        userLabel.setTextFill(Color.web(TEXTE_PRINCIPAL));
        userLabel.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-padding: 5 12; -fx-background-radius: 15;");
        
        Button btnDeconnexion = new Button("🔓 Déconnexion");
        btnDeconnexion.setStyle(getStyleBoutonDanger());
        btnDeconnexion.setOnAction(e -> deconnexion());
        
        headerBar.getChildren().addAll(appTitle, spacer, notificationStack, userLabel, btnDeconnexion);
        
        // ===== SIDEBAR MODERNE =====
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: " + FOND_SIDEBAR + ";");
        
        // Bienvenue
        Label bienvenueLabel = new Label("Bienvenue,");
        bienvenueLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        bienvenueLabel.setFont(Font.font("System", 12));
        
        Label userNameLabel = new Label(utilisateurCourant.getPrenom() + " " + utilisateurCourant.getNom());
        userNameLabel.setTextFill(Color.web(COULEUR_BOUTON));
        userNameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        VBox welcomeBox = new VBox(2);
        welcomeBox.setPadding(new Insets(0, 0, 20, 0));
        welcomeBox.getChildren().addAll(bienvenueLabel, userNameLabel);
        
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.2);");
        separator.setPadding(new Insets(5, 0, 15, 0));
        
        // Boutons communs
        Button btnEmploiTemps = creerBoutonSidebar("📅", "Emploi du temps");
        Button btnRechercher = creerBoutonSidebar("🔍", "Rechercher salle");
        
        sidebar.getChildren().addAll(welcomeBox, separator, btnEmploiTemps, btnRechercher);
        
        String role = utilisateurCourant.getRole();
        
        // ADMIN - (SANS le bouton Notifications dans le sidebar)
        if (role.equals("admin")) {
            Button btnTableauBord = creerBoutonSidebar("📊", "Tableau de bord");
            Button btnUtilisateurs = creerBoutonSidebar("👥", "Gestion utilisateurs");
            Button btnSalles = creerBoutonSidebar("🏢", "Configurer salles");
            Button btnCours = creerBoutonSidebar("📚", "Gestion cours");
            Button btnStatistiques = creerBoutonSidebar("📈", "Statistiques");
            Button btnReservations = creerBoutonSidebar("📝", "Réservations");
            Button btnCarte = creerBoutonSidebar("🗺️", "Carte");
            // ⚠️ SUPPRIMÉ : Button btnNotifications = creerBoutonSidebar("🔔", "Notifications");
            
            sidebar.getChildren().addAll(
                btnTableauBord, btnUtilisateurs, btnSalles, btnCours, 
                btnStatistiques, btnReservations, btnCarte
                // ⚠️ btnNotifications SUPPRIMÉ
            );
            
            btnTableauBord.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueTableauBord(utilisateurCourant).getRacine()); });
            btnUtilisateurs.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueUtilisateurs(utilisateurCourant).getRacine()); });
            btnSalles.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueSalles(utilisateurCourant, stagePrincipal).getRacine()); });
            btnCours.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueCours(utilisateurCourant, stagePrincipal).getRacine()); });
            btnStatistiques.setOnAction(e -> { arreterToutesSynchronisations(); vueStatistiquesActuelle = new VueStatistiques(utilisateurCourant); racine.setCenter(vueStatistiquesActuelle.getRacine()); });
            btnReservations.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueReservation(utilisateurCourant).getRacine()); });
            btnCarte.setOnAction(e -> { arreterToutesSynchronisations(); vueCarteActuelle = new VueCarte(utilisateurCourant); racine.setCenter(vueCarteActuelle.getRacine()); });
        }
        
        // MANAGER
        if (role.equals("manager")) {
            Button btnCours = creerBoutonSidebar("📚", "Créer/modifier cours");
            Button btnSalles = creerBoutonSidebar("🏢", "Consulter salles");
            Button btnConflits = creerBoutonSidebar("⚠️", "Résoudre conflits");
            Button btnEmploiGen = creerBoutonSidebar("📅", "Générer emploi du temps");
            
            sidebar.getChildren().addAll(btnCours, btnSalles, btnConflits, btnEmploiGen);
            
            btnCours.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueCours(utilisateurCourant, stagePrincipal).getRacine()); });
            btnSalles.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueSalles(utilisateurCourant, stagePrincipal).getRacine()); });
            btnConflits.setOnAction(e -> { arreterToutesSynchronisations(); VueCours vue = new VueCours(utilisateurCourant, stagePrincipal); racine.setCenter(vue.getRacine()); javafx.application.Platform.runLater(() -> vue.verifierConflits()); });
            btnEmploiGen.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueEmploiTemps(utilisateurCourant, stagePrincipal).getRacine()); });
        }
        
        // ENSEIGNANT
        if (role.equals("enseignant")) {
            Button btnReservations = creerBoutonSidebar("📝", "Réserver salle");
            Button btnCarte = creerBoutonSidebar("🗺️", "Carte");
            Button btnProbleme = creerBoutonSidebar("🛠️", "Signaler problème");
            
            sidebar.getChildren().addAll(btnReservations, btnCarte, btnProbleme);
            
            btnReservations.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueReservation(utilisateurCourant).getRacine()); });
            btnCarte.setOnAction(e -> { arreterToutesSynchronisations(); vueCarteActuelle = new VueCarte(utilisateurCourant); racine.setCenter(vueCarteActuelle.getRacine()); });
            
            btnProbleme.setOnAction(e -> {
                Dialog<Signalement> dialog = new Dialog<>();
                dialog.setTitle("🛠️ Signaler un problème");
                dialog.setHeaderText("Formulaire de signalement");
                
                ButtonType btnEnvoyer = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(btnEnvoyer, ButtonType.CANCEL);
                
                VBox content = new VBox(10);
                content.setPadding(new Insets(20));
                content.setPrefWidth(400);
                content.setStyle(getStyleCarte());
                
                Label problemeLabel = new Label("Décrivez le problème :");
                problemeLabel.setStyle("-fx-font-weight: bold;");
                
                TextArea textProbleme = new TextArea();
                textProbleme.setPromptText("Ex: Vidéoprojecteur ne fonctionne pas, tableau blanc abîmé, etc.");
                textProbleme.setPrefRowCount(5);
                textProbleme.setWrapText(true);
                textProbleme.setStyle(getStyleTextField());
                
                Label salleLabel = new Label("Salle concernée (optionnel) :");
                salleLabel.setStyle("-fx-font-weight: bold;");
                
                ComboBox<String> comboSalle = new ComboBox<>();
                comboSalle.getItems().addAll("Non précisé", "A101", "A102", "A103", "B201", "B202", "C301", "Amphi 1", "Amphi 2");
                comboSalle.setValue("Non précisé");
                comboSalle.setStyle(getStyleTextField());
                
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
                        mettreAJourBadgeNotifications();
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
        
        // ÉTUDIANT
        if (role.equals("etudiant")) {
            Button btnCarte = creerBoutonSidebar("🗺️", "Carte des salles");
            sidebar.getChildren().add(btnCarte);
            
            btnCarte.setOnAction(e -> { arreterToutesSynchronisations(); vueCarteActuelle = new VueCarte(utilisateurCourant); racine.setCenter(vueCarteActuelle.getRacine()); });
        }
        
        btnEmploiTemps.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueEmploiTemps(utilisateurCourant, stagePrincipal).getRacine()); });
        btnRechercher.setOnAction(e -> { arreterToutesSynchronisations(); racine.setCenter(new VueRecherche(utilisateurCourant).getRacine()); });
        
        racine.setTop(headerBar);
        racine.setLeft(sidebar);
        racine.setCenter(new VueEmploiTemps(utilisateurCourant, stagePrincipal).getRacine());
        
        // Initialiser le badge
        mettreAJourBadgeNotifications();
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
        if (notificationTimer != null) {
            notificationTimer.stop();
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
    
    @Override
    public BorderPane getRacine() {
        return racine;
    }
}