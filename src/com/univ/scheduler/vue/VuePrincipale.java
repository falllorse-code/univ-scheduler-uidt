package com.univ.scheduler.vue;

import com.univ.scheduler.modele.Utilisateur;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
    
    // Thème sombre
    private final String FOND_PRINCIPAL = "#1a1a2e";
    private final String FOND_SIDEBAR = "#16213e";
    private final String TEXTE_PRINCIPAL = "#ffffff";
    private final String TEXTE_SECONDAIRE = "#b0b0b0";
    private final String COULEUR_SURVOL = "#0f3460";
    private final String COULEUR_PRIMAIRE = "#4361ee";
    
    public VuePrincipale(Stage stagePrincipal, Utilisateur utilisateur) {
        this.stagePrincipal = stagePrincipal;
        this.utilisateurCourant = utilisateur;
        creerVue();
    }
    
    private void creerVue() {
        racine = new BorderPane();
        racine.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        // Barre de menu
        MenuBar barreMenu = new MenuBar();
        barreMenu.setStyle("-fx-background-color: " + FOND_SIDEBAR + ";");
        
        Menu menuFichier = new Menu("Fichier");
        menuFichier.setStyle("-fx-text-fill: " + TEXTE_PRINCIPAL + ";");
        
        MenuItem itemDeconnexion = new MenuItem("Déconnexion");
        itemDeconnexion.setOnAction(e -> deconnexion());
        
        MenuItem itemQuitter = new MenuItem("Quitter");
        itemQuitter.setOnAction(e -> {
            arreterToutesSynchronisations();
            stagePrincipal.close();
        });
        
        menuFichier.getItems().addAll(itemDeconnexion, new SeparatorMenuItem(), itemQuitter);
        
        Menu menuAide = new Menu("Aide");
        MenuItem itemAPropos = new MenuItem("À propos");
        itemAPropos.setOnAction(e -> afficherAPropos());
        menuAide.getItems().add(itemAPropos);
        
        barreMenu.getMenus().addAll(menuFichier, menuAide);
        
        // Panneau latéral
        VBox barreLaterale = new VBox(10);
        barreLaterale.setPadding(new Insets(20, 15, 20, 15));
        barreLaterale.setStyle("-fx-background-color: " + FOND_SIDEBAR + ";");
        barreLaterale.setPrefWidth(220);
        
        Label bienvenueLabel = new Label("Bienvenue,\n" + utilisateurCourant.getNomComplet());
        bienvenueLabel.setTextFill(Color.web(TEXTE_PRINCIPAL));
        bienvenueLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        bienvenueLabel.setWrapText(true);
        bienvenueLabel.setPadding(new Insets(0, 0, 20, 0));
        
        // Boutons de navigation
        Button btnTableauBord = creerBoutonLaterale("🏠 Tableau de bord");
        Button btnSalles = creerBoutonLaterale("🏢 Salles");
        Button btnCours = creerBoutonLaterale("📚 Cours");
        Button btnEmploiTemps = creerBoutonLaterale("📅 Emploi du temps");
        Button btnRechercher = creerBoutonLaterale("🔍 Rechercher");
        Button btnReservations = creerBoutonLaterale("📅 Réservations");
        Button btnCarte = creerBoutonLaterale("🗺️ Carte");
        
        barreLaterale.getChildren().addAll(
            bienvenueLabel, btnTableauBord, btnSalles, btnCours, 
            btnEmploiTemps, btnRechercher, btnReservations, btnCarte
        );
        
        // Boutons admin
        if (utilisateurCourant.getRole().equals("admin") || utilisateurCourant.getRole().equals("manager")) {
            Button btnUtilisateurs = creerBoutonLaterale("👥 Utilisateurs");
            Button btnStatistiques = creerBoutonLaterale("📊 Statistiques");
            barreLaterale.getChildren().addAll(btnUtilisateurs, btnStatistiques);
            
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
        
        // Contenu par défaut
        VueTableauBord vueTableauBord = new VueTableauBord(utilisateurCourant);
        racine.setCenter(vueTableauBord.getRacine());
        
        // Actions des boutons avec arrêt des synchronisations
        btnTableauBord.setOnAction(e -> {
            arreterToutesSynchronisations();
            VueTableauBord vue = new VueTableauBord(utilisateurCourant);
            racine.setCenter(vue.getRacine());
        });
        
        btnSalles.setOnAction(e -> {
            arreterToutesSynchronisations();
            VueSalles vue = new VueSalles(utilisateurCourant, stagePrincipal);  // ← Nouveau avec Stage
            racine.setCenter(vue.getRacine());
        });
        
        btnCours.setOnAction(e -> {
            arreterToutesSynchronisations();
            VueCours vue = new VueCours(utilisateurCourant, stagePrincipal);  // Ajouter stagePrincipal
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
        
        racine.setTop(barreMenu);
        racine.setLeft(barreLaterale);
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
    
    private Button creerBoutonLaterale(String texte) {
        Button bouton = new Button(texte);
        bouton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXTE_SECONDAIRE + ";" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10 15;" +
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 5;"
        );
        bouton.setPrefWidth(190);
        bouton.setMaxWidth(190);
        
        bouton.setOnMouseEntered(e -> 
            bouton.setStyle(
                "-fx-background-color: " + COULEUR_SURVOL + ";" +
                "-fx-text-fill: " + TEXTE_PRINCIPAL + ";" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 10 15;" +
                "-fx-alignment: CENTER-LEFT;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5;"
            )
        );
        
        bouton.setOnMouseExited(e -> 
            bouton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + TEXTE_SECONDAIRE + ";" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 10 15;" +
                "-fx-alignment: CENTER-LEFT;" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 5;"
            )
        );
        
        return bouton;
    }
    
    private void deconnexion() {
        arreterToutesSynchronisations();
        VueConnexion vueConnexion = new VueConnexion(stagePrincipal);
        stagePrincipal.getScene().setRoot(vueConnexion.getRacine());
        stagePrincipal.setTitle("UNIV-SCHEDULER - Connexion");
        stagePrincipal.setWidth(400);
        stagePrincipal.setHeight(300);
    }
    
    private void afficherAPropos() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos");
        alert.setHeaderText("UNIV-SCHEDULER");
        alert.setContentText("Application de Gestion des Salles et Emplois du Temps\nVersion 3.0\n© 2026");
        alert.showAndWait();
    }
    
    public BorderPane getRacine() {
        return racine;
    }
}