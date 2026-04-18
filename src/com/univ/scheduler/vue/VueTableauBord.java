package com.univ.scheduler.vue;

import com.univ.scheduler.dao.SalleDAO;

import com.univ.scheduler.dao.ReservationDAO;
import com.univ.scheduler.dao.UtilisateurDAO;
import com.univ.scheduler.modele.Salle;
import com.univ.scheduler.modele.Reservation;
import com.univ.scheduler.modele.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VueTableauBord extends VueBase{
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private SalleDAO salleDAO;
    private ReservationDAO reservationDAO;
    private UtilisateurDAO utilisateurDAO;
    
    // Composants à mettre à jour
    private GridPane statsGrid;
    private VBox chartBox;
    private VBox tableBox;
    private Label dateLabel;
    private Timeline synchronisationTimer;
    
    // Thème sombre
    private final String FOND_PRINCIPAL = "#1a1a2e";
    private final String FOND_CARTE = "#16213e";
    private final String TEXTE_PRINCIPAL = "#ffffff";
    private final String TEXTE_SECONDAIRE = "#b0b0b0";
    private final String COULEUR_PRIMAIRE = "#4361ee";
    private final String COULEUR_SUCCESS = "#2ecc71";
    private final String COULEUR_WARNING = "#f39c12";
    private final String COULEUR_DANGER = "#e74c3c";
    private final String COULEUR_INFO = "#3498db";
    private final String COULEUR_VIOLET = "#9b59b6";
    private final String COULEUR_ORANGE = "#e67e22";
    
    public VueTableauBord(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        this.salleDAO = new SalleDAO();
        this.reservationDAO = new ReservationDAO();
        this.utilisateurDAO = new UtilisateurDAO();
        creerVue();
        demarrerSynchronisationTempsReel();
        actualiserDonnees();
    }
    
    private void demarrerSynchronisationTempsReel() {
        synchronisationTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            actualiserDonnees();
        }));
        synchronisationTimer.setCycleCount(Timeline.INDEFINITE);
        synchronisationTimer.play();
    }
    
    private void actualiserDonnees() {
        Platform.runLater(() -> {
            // Récupérer les données en temps réel
            List<Salle> salles = salleDAO.getToutesLesSalles();
            List<Utilisateur> utilisateurs = utilisateurDAO.getTousLesUtilisateurs();
            List<Reservation> reservations = getToutesReservations();
            
            // Mettre à jour l'interface
            mettreAJourStatistiques(salles, utilisateurs, reservations);
            mettreAJourGraphique(salles);
            mettreAJourTableauReservations(reservations);
            
            // Mettre à jour la date
            dateLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy - HH:mm:ss")));
        });
    }
    
    private void creerVue() {
        racine = new BorderPane();
        appliquerFond(racine);
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        // En-tête
        VBox headerBox = creerEnTete();
        
        // Section 1: Cartes de statistiques
        statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(20, 0, 20, 0));
        
        // Section 2: Graphiques et tableaux
        HBox contentBox = new HBox(20);
        contentBox.setPadding(new Insets(20, 0, 0, 0));
        
        chartBox = new VBox(10);
        chartBox.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-padding: 15; -fx-background-radius: 10;");
        chartBox.setPrefWidth(400);
        
        tableBox = new VBox(10);
        tableBox.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-padding: 15; -fx-background-radius: 10;");
        tableBox.setPrefWidth(500);
        
        contentBox.getChildren().addAll(chartBox, tableBox);
        
        // Assemblage
        VBox centreBox = new VBox(10);
        centreBox.getChildren().addAll(statsGrid, contentBox);
        
        racine.setTop(headerBox);
        racine.setCenter(centreBox);
    }
    
    private VBox creerEnTete() {
        VBox headerBox = new VBox(5);
        
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titreLabel = new Label("📊 Tableau de bord en temps réel");
        titreLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titreLabel.setTextFill(Color.web(TEXTE_PRINCIPAL));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        dateLabel = new Label();
        dateLabel.setFont(Font.font("System", 14));
        dateLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle("-fx-background-color: " + COULEUR_INFO + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btnActualiser.setOnAction(e -> actualiserDonnees());
        
        titleBox.getChildren().addAll(titreLabel, spacer, dateLabel, btnActualiser);
        
        String message;
        int heure = LocalDateTime.now().getHour();
        if (heure < 12) message = "Bonjour";
        else if (heure < 18) message = "Bon après-midi";
        else message = "Bonsoir";
        
        Label bienvenueLabel = new Label(message + ", " + utilisateurCourant.getPrenom() + " !");
        bienvenueLabel.setFont(Font.font("System", 16));
        bienvenueLabel.setTextFill(Color.web(COULEUR_PRIMAIRE));
        
        headerBox.getChildren().addAll(titleBox, bienvenueLabel);
        return headerBox;
    }
    
    private void mettreAJourStatistiques(List<Salle> salles, List<Utilisateur> utilisateurs, List<Reservation> reservations) {
        statsGrid.getChildren().clear();
        
        long totalSalles = salles.size();
        long sallesVideoproj = salles.stream().filter(Salle::isAVideoprojecteur).count();
        long sallesAmphi = salles.stream().filter(s -> s.getType().equals("AMPHI")).count();
        long sallesTD = salles.stream().filter(s -> s.getType().equals("TD")).count();
        long sallesTP = salles.stream().filter(s -> s.getType().equals("TP")).count();
        
        long totalUsers = utilisateurs.size();
        long reservationsJour = reservations.stream()
                .filter(r -> r.getDate().equals(LocalDate.now()))
                .count();
        long reservationsEnCours = reservations.stream()
                .filter(r -> "confirmée".equals(r.getStatut()) && 
                        r.getDate().equals(LocalDate.now()) &&
                        r.getHeureDebut().isBefore(LocalTime.now()) &&
                        r.getHeureFin().isAfter(LocalTime.now()))
                .count();
        
        statsGrid.add(creerCarteStat("Salles totales", String.valueOf(totalSalles), COULEUR_PRIMAIRE, "🏢"), 0, 0);
        statsGrid.add(creerCarteStat("Avec vidéoproj", String.valueOf(sallesVideoproj), COULEUR_SUCCESS, "📽️"), 1, 0);
        statsGrid.add(creerCarteStat("Utilisateurs", String.valueOf(totalUsers), COULEUR_VIOLET, "👥"), 2, 0);
        statsGrid.add(creerCarteStat("Réservations/jour", String.valueOf(reservationsJour), COULEUR_ORANGE, "📅"), 3, 0);
        statsGrid.add(creerCarteStat("En cours", String.valueOf(reservationsEnCours), COULEUR_WARNING, "⏳"), 0, 1);
        statsGrid.add(creerCarteStat("Amphithéâtres", String.valueOf(sallesAmphi), COULEUR_DANGER, "🎭"), 1, 1);
        statsGrid.add(creerCarteStat("Salles TD", String.valueOf(sallesTD), COULEUR_INFO, "📚"), 2, 1);
        statsGrid.add(creerCarteStat("Salles TP", String.valueOf(sallesTP), COULEUR_SUCCESS, "💻"), 3, 1);
    }
    
    private void mettreAJourGraphique(List<Salle> salles) {
        chartBox.getChildren().clear();
        
        Label titre = new Label("Répartition des salles par bâtiment");
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setTextFill(Color.web(TEXTE_PRINCIPAL));
        
        // Grouper par bâtiment
        Map<String, Long> sallesParBatiment = salles.stream()
            .collect(Collectors.groupingBy(
                Salle::getNomBatiment,
                Collectors.counting()
            ));
        
        ObservableList<PieChart.Data> donnees = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Long> entry : sallesParBatiment.entrySet()) {
            if (entry.getValue() > 0) {
                donnees.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }
        
        PieChart pieChart = new PieChart(donnees);
        pieChart.setPrefWidth(350);
        pieChart.setPrefHeight(300);
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setStyle("-fx-background-color: transparent;");
        
        chartBox.getChildren().addAll(titre, pieChart);
    }
    
    private void mettreAJourTableauReservations(List<Reservation> reservations) {
        tableBox.getChildren().clear();
        
        Label titre = new Label("Prochaines réservations");
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setTextFill(Color.web(TEXTE_PRINCIPAL));
        
        TableView<Reservation> table = new TableView<>();
        table.setPrefHeight(300);
        table.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-table-cell-border-color: " + FOND_CARTE + ";");
        
        TableColumn<Reservation, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(new PropertyValueFactory<>("nomSalle"));
        colSalle.setPrefWidth(150);
        
        TableColumn<Reservation, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setPrefWidth(150);
        
        TableColumn<Reservation, LocalDate> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(100);
        
        TableColumn<Reservation, String> colHoraire = new TableColumn<>("Horaire");
        colHoraire.setCellValueFactory(new PropertyValueFactory<>("plageHoraire"));
        colHoraire.setPrefWidth(100);
        
        table.getColumns().addAll(colSalle, colTitre, colDate, colHoraire);
        
        // Afficher les 5 prochaines réservations
        ObservableList<Reservation> donnees = FXCollections.observableArrayList();
        reservations.stream()
                .filter(r -> r.getDate().isAfter(LocalDate.now()) || 
                           (r.getDate().equals(LocalDate.now()) && r.getHeureDebut().isAfter(LocalTime.now())))
                .sorted((r1, r2) -> {
                    if (r1.getDate().equals(r2.getDate())) {
                        return r1.getHeureDebut().compareTo(r2.getHeureDebut());
                    }
                    return r1.getDate().compareTo(r2.getDate());
                })
                .limit(5)
                .forEach(donnees::add);
        
        table.setItems(donnees);
        table.setPlaceholder(new Label("Aucune réservation à venir"));
        
        tableBox.getChildren().addAll(titre, table);
    }
    
    private VBox creerCarteStat(String titre, String valeur, String couleur, String icone) {
        VBox carte = new VBox(5);
        carte.setPadding(new Insets(15));
        carte.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
        carte.setPrefWidth(180);
        carte.setPrefHeight(100);
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label iconeLabel = new Label(icone);
        iconeLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        Label titreLabel = new Label(titre);
        titreLabel.setFont(Font.font("System", 12));
        titreLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        headerBox.getChildren().addAll(iconeLabel, titreLabel);
        
        Label valeurLabel = new Label(valeur);
        valeurLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valeurLabel.setTextFill(Color.web(couleur));
        
        carte.getChildren().addAll(headerBox, valeurLabel);
        return carte;
    }
    
    private List<Reservation> getToutesReservations() {
        List<Reservation> toutes = new java.util.ArrayList<>();
        List<Salle> salles = salleDAO.getToutesLesSalles();
        
        for (Salle salle : salles) {
            toutes.addAll(reservationDAO.getReservationsParSalle(salle.getId()));
        }
        
        return toutes;
    }
    
    public void arreterSynchronisation() {
        if (synchronisationTimer != null) {
            synchronisationTimer.stop();
        }
    }
    
    public BorderPane getRacine() {
        return racine;
    }
    private VBox creerGraphiqueBatiments(Map<String, List<Salle>> sallesParBatiment) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-padding: 15; -fx-background-radius: 10;");
        box.setPrefWidth(400);
        
        Label titre = new Label("Répartition par bâtiment");
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setTextFill(Color.web(TEXTE_PRINCIPAL));
        
        ObservableList<PieChart.Data> donnees = FXCollections.observableArrayList();
        
        for (Map.Entry<String, List<Salle>> entry : sallesParBatiment.entrySet()) {
            String batiment = entry.getKey();
            int nbSalles = entry.getValue().size();
            if (nbSalles > 0) {
                donnees.add(new PieChart.Data(batiment + " (" + nbSalles + ")", nbSalles));
            }
        }
        
        PieChart pieChart = new PieChart(donnees);
        pieChart.setPrefWidth(350);
        pieChart.setPrefHeight(300);
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(false);
        pieChart.setStyle("-fx-background-color: transparent;");
        
        // ✅ FORCER LE TEXTE EN BLANC
        Platform.runLater(() -> {
            pieChart.lookupAll(".chart-pie-label").forEach(node -> {
                node.setStyle("-fx-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
            });
        });
        
        box.getChildren().addAll(titre, pieChart);
        return box;
    }
}