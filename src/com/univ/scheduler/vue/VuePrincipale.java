package com.univ.scheduler.vue;

import com.univ.scheduler.modele.Utilisateur;
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
    
    // Pour garder une référence à la vue actuelle et arrêter sa synchronisation
    private VueCarte vueCarteActuelle;
    private VueStatistiques vueStatistiquesActuelle;
    
    // ===== THÈME CLAIR =====
    private final String FOND_PRINCIPAL = "#f5f7fa";        // Fond gris clair
    private final String FOND_SIDEBAR = "#2c3e50";          // Sidebar bleu foncé
    private final String TEXTE_PRINCIPAL = "#ffffff";       // Texte blanc pour sidebar
    private final String TEXTE_SECONDAIRE = "#bdc3c7";      // Texte gris clair
    private final String COULEUR_SURVOL = "#34495e";        // Survol plus clair
    private final String COULEUR_PRIMAIRE = "#3498db";      // Bleu principal
    private final String COULEUR_BORDURE = "#e0e0e0";       // Bordure grise
    
    public VuePrincipale(Stage stagePrincipal, Utilisateur utilisateur) {
        this.stagePrincipal = stagePrincipal;
        this.utilisateurCourant = utilisateur;
        creerVue();
    }
    
    private void creerVue() {
        racine = new BorderPane();
        racine.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        // ===== EN-TÊTE EN HAUT =====
        HBox headerBar = new HBox();
        headerBar.setPadding(new Insets(10, 20, 10, 20));
        headerBar.setStyle("-fx-background-color: white; -fx-border-color: " + COULEUR_BORDURE + "; -fx-border-width: 0 0 1 0;");
        headerBar.setAlignment(Pos.CENTER_LEFT);
        
        // Titre de l'application
        Label appTitle = new Label("📚 UNIV-SCHEDULER");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        appTitle.setTextFill(Color.web("#2c3e50"));
        
        // Espace entre le titre et le profil
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Profil utilisateur
        Label userLabel = new Label("👤 " + utilisateurCourant.getPrenom() + " " + utilisateurCourant.getNom());
        userLabel.setFont(Font.font("System", 12));
        userLabel.setTextFill(Color.web("#7f8c8d"));
        userLabel.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 12; -fx-background-radius: 15;");
        
        // Bouton Déconnexion à DROITE
        Button btnDeconnexion = new Button("🔓 Déconnexion");
        btnDeconnexion.setStyle(
            "-fx-background-color: #e74c3c;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 6 15;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12px;"
        );
        btnDeconnexion.setOnAction(e -> deconnexion());
        
        headerBar.getChildren().addAll(appTitle, spacer, userLabel, btnDeconnexion);
        
        // ===== SIDEBAR À GAUCHE =====
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
        userNameLabel.setWrapText(true);
        
        VBox welcomeBox = new VBox(2);
        welcomeBox.setPadding(new Insets(0, 0, 20, 0));
        welcomeBox.getChildren().addAll(bienvenueLabel, userNameLabel);
        
        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #34495e;");
        separator.setPadding(new Insets(5, 0, 15, 0));
        
        // Boutons de navigation
        Button btnTableauBord = creerBoutonSidebar("📊", "Tableau de bord");
        Button btnSalles = creerBoutonSidebar("🏢", "Salles");
        Button btnCours = creerBoutonSidebar("📚", "Cours");
        Button btnEmploiTemps = creerBoutonSidebar("📅", "Emploi du temps");
        Button btnRechercher = creerBoutonSidebar("🔍", "Rechercher");
        Button btnReservations = creerBoutonSidebar("📝", "Réservations");
        Button btnCarte = creerBoutonSidebar("🗺️", "Carte");
        
        sidebar.getChildren().addAll(
            welcomeBox, separator,
            btnTableauBord, btnSalles, btnCours, btnEmploiTemps,
            btnRechercher, btnReservations, btnCarte
        );
        
        // Boutons admin (sans le libellé "Administration")
        if (utilisateurCourant.getRole().equals("admin") || utilisateurCourant.getRole().equals("manager")) {
            Separator separatorAdmin = new Separator();
            separatorAdmin.setStyle("-fx-background-color: #34495e;");
            separatorAdmin.setPadding(new Insets(15, 0, 10, 0));
            
            Button btnUtilisateurs = creerBoutonSidebar("👥", "Utilisateurs");
            Button btnStatistiques = creerBoutonSidebar("📈", "Statistiques");
            
            sidebar.getChildren().addAll(separatorAdmin, btnUtilisateurs, btnStatistiques);
            
            btnUtilisateurs.setOnAction(e -> {
                arreterToutesSynchronisations();
                VueUtilisateurs vue = new VueUtilisateurs(utilisateurCourant);
                racine.setCenter(vue.getRacine());
            });
            
            btnStatistiques.setOnAction(e -> {
                arreterToutesSynchronisations();
                vueStatistiquesActuelle = new VueStatistiques(utilisateurCourant);
                racine.setCenter(vueStatistiquesActuelle.getRacine());
            });
        }
        
        // Actions des boutons
        btnTableauBord.setOnAction(e -> {
            arreterToutesSynchronisations();
            VueTableauBord vue = new VueTableauBord(utilisateurCourant);
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
        
        // Assemblage
        racine.setTop(headerBar);
        racine.setLeft(sidebar);
        
        // Contenu par défaut avec padding suffisant
        BorderPane centerContainer = new BorderPane();
        centerContainer.setPadding(new Insets(20));
        
        VueTableauBord vueTableauBord = new VueTableauBord(utilisateurCourant);
        centerContainer.setCenter(vueTableauBord.getRacine());
        
        racine.setCenter(centerContainer);
    }
    
    /**
     * Crée un bouton pour la sidebar
     */
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
    
    /**
     * Méthode pour arrêter toutes les synchronisations en cours
     */
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