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
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class VueStatistiques extends VueBase{
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private SalleDAO salleDAO;
    private UtilisateurDAO utilisateurDAO;
    private ReservationDAO reservationDAO;
    
    // Composants à mettre à jour en temps réel
    private GridPane statsGrid;
    private BarChart<String, Number> barChartSalles;
    private PieChart pieChartBatiments;
    private BarChart<String, Number> barChartUtilisateurs;
    private GridPane statsReservations;
    private VBox content;
    private Label dateLabel;
    private Timeline synchronisationTimer;
    
    // Thème sombre
    private final String FOND_PRINCIPAL = "#1a1a2e";
    private final String FOND_CARTE = "#16213e";
    private final String FOND_GRAPHIQUE = "#0f3460";
    private final String TEXTE_PRINCIPAL = "#ffffff";
    private final String TEXTE_SECONDAIRE = "#b0b0b0";
    private final String BORDURE = "#2a2a4a";
    private final String COULEUR_PRIMAIRE = "#4361ee";
    private final String COULEUR_SUCCESS = "#2ecc71";
    private final String COULEUR_WARNING = "#f39c12";
    private final String COULEUR_DANGER = "#e74c3c";
    private final String COULEUR_INFO = "#3498db";
    private final String COULEUR_VIOLET = "#9b59b6";
    private final String COULEUR_ORANGE = "#e67e22";
    private final String COULEUR_TURQUOISE = "#1abc9c";
    
    public VueStatistiques(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        this.salleDAO = new SalleDAO();
        this.utilisateurDAO = new UtilisateurDAO();
        this.reservationDAO = new ReservationDAO();
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
            // Récupérer les données en temps réel depuis la BD
            List<Salle> salles = salleDAO.getToutesLesSalles();
            List<Utilisateur> utilisateurs = utilisateurDAO.getTousLesUtilisateurs();
            List<Reservation> reservations = getToutesReservations();
            
            // Calculer les stats en temps réel
            StatsGlobales stats = calculerStatsGlobales(salles, utilisateurs, reservations);
            
            // Mettre à jour l'interface
            mettreAJourIndicateurs(stats);
            mettreAJourGraphiqueSalles(stats);
            mettreAJourGraphiqueBatiments(salles);
            mettreAJourGraphiqueUtilisateurs(stats);
            mettreAJourReservations(stats);
            
            // Mettre à jour la date
            dateLabel.setText("Données en temps réel - " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        });
    }
    
    private void creerVue() {
        racine = new BorderPane();
        appliquerFond(racine);
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        // Titre principal
        Label titreLabel = new Label("📊 Statistiques en temps réel");
        titreLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titreLabel.setTextFill(Color.web(TEXTE_PRINCIPAL));
        
        // Sous-titre avec date dynamique
        dateLabel = new Label("Données en temps réel - " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        dateLabel.setFont(Font.font("System", 14));
        dateLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        // Bouton d'actualisation manuelle
        Button btnActualiser = new Button("🔄 Actualiser maintenant");
        btnActualiser.setStyle("-fx-background-color: " + COULEUR_INFO + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btnActualiser.setOnAction(e -> actualiserDonnees());
        
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(titreLabel, dateLabel, btnActualiser);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        // Conteneur défilant
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: null;");
        
        content = new VBox(25);
        content.setPadding(new Insets(10, 10, 30, 10));
        content.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        // Créer les sections vides (seront remplies par actualiserDonnees)
        statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(15);
        
        barChartSalles = creerBarChartVide("Répartition par type de salle", "Type", "Nombre");
        pieChartBatiments = creerPieChartVide("Répartition par bâtiment");
        barChartUtilisateurs = creerBarChartVide("Répartition par rôle", "Rôle", "Nombre");
        statsReservations = new GridPane();
        statsReservations.setHgap(15);
        statsReservations.setVgap(15);
        
        content.getChildren().addAll(
            creerSection("📈 Indicateurs clés", statsGrid),
            creerSeparateur(),
            creerSection("🏢 Analyse des salles", barChartSalles),
            creerSeparateur(),
            creerSection("🏛️ Analyse par bâtiment", pieChartBatiments),
            creerSeparateur(),
            creerSection("👥 Analyse des utilisateurs", barChartUtilisateurs),
            creerSeparateur(),
            creerSection("📅 Analyse des réservations", statsReservations)
        );
        Platform.runLater(() -> ajouterRapportHebdomadaire());
        
        scrollPane.setContent(content);
        racine.setTop(headerBox);
        racine.setCenter(scrollPane);
    }
    
    private VBox creerSection(String titre, javafx.scene.Node contenu) {
        VBox section = new VBox(15);
        Label labelTitre = new Label(titre);
        labelTitre.setFont(Font.font("System", FontWeight.BOLD, 20));
        labelTitre.setTextFill(Color.web(TEXTE_PRINCIPAL));
        section.getChildren().addAll(labelTitre, contenu);
        return section;
    }
    
    private BarChart<String, Number> creerBarChartVide(String titre, String xLabel, String yLabel) {
        CategoryAxis axeX = new CategoryAxis();
        NumberAxis axeY = new NumberAxis();
        axeX.setLabel(xLabel);
        axeY.setLabel(yLabel);
        axeX.setTickLabelFill(Color.web(TEXTE_SECONDAIRE));
        axeY.setTickLabelFill(Color.web(TEXTE_SECONDAIRE));
        
        BarChart<String, Number> chart = new BarChart<>(axeX, axeY);
        chart.setTitle(titre);
        chart.setPrefWidth(800);
        chart.setPrefHeight(300);
        chart.setStyle("-fx-background-color: " + FOND_GRAPHIQUE + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
        return chart;
    }
    
    private PieChart creerPieChartVide(String titre) {
        PieChart chart = new PieChart();
        chart.setTitle(titre);
        chart.setPrefWidth(600);
        chart.setPrefHeight(300);
        chart.setLabelsVisible(true);
        chart.setStyle("-fx-background-color: " + FOND_GRAPHIQUE + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
        return chart;
    }
    
    private void mettreAJourIndicateurs(StatsGlobales stats) {
        statsGrid.getChildren().clear();
        
        // Ligne 1
        statsGrid.add(creerCarteStat("🏢 Salles", String.valueOf(stats.totalSalles), COULEUR_PRIMAIRE, "🏢"), 0, 0);
        statsGrid.add(creerCarteStat("👥 Utilisateurs", String.valueOf(stats.totalUsers), COULEUR_SUCCESS, "👥"), 1, 0);
        statsGrid.add(creerCarteStat("📅 Réservations", String.valueOf(stats.totalReservations), COULEUR_WARNING, "📅"), 2, 0);
        statsGrid.add(creerCarteStat("⏳ En cours", String.valueOf(stats.reservationsEnCours), COULEUR_DANGER, "⏳"), 3, 0);
        
        // Ligne 2
        statsGrid.add(creerCarteStat("📽️ Vidéoproj", stats.sallesVideoproj + "/" + stats.totalSalles, COULEUR_INFO, "📽️"), 0, 1);
        statsGrid.add(creerCarteStat("🎓 Étudiants", String.valueOf(stats.students), COULEUR_VIOLET, "🎓"), 1, 1);
        statsGrid.add(creerCarteStat("👨‍🏫 Enseignants", String.valueOf(stats.teachers), COULEUR_ORANGE, "👨‍🏫"), 2, 1);
        statsGrid.add(creerCarteStat("📌 Aujourd'hui", String.valueOf(stats.reservationsAJour), COULEUR_TURQUOISE, "📌"), 3, 1);
    }
    
    private void mettreAJourGraphiqueSalles(StatsGlobales stats) {
        barChartSalles.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de salles");
        series.getData().add(new XYChart.Data<>("TD", stats.sallesTD));
        series.getData().add(new XYChart.Data<>("TP", stats.sallesTP));
        series.getData().add(new XYChart.Data<>("AMPHI", stats.sallesAmphi));
        
        barChartSalles.getData().add(series);
        
        // Ajouter les tooltips
        for (XYChart.Data<String, Number> data : series.getData()) {
            Tooltip tooltip = new Tooltip(data.getYValue() + " salles");
            Tooltip.install(data.getNode(), tooltip);
        }
    }
    
    private void mettreAJourGraphiqueBatiments(List<Salle> salles) {
        // Grouper les salles par bâtiment
        Map<String, Long> sallesParBatiment = salles.stream()
            .collect(Collectors.groupingBy(
                Salle::getNomBatiment,
                Collectors.counting()
            ));
        
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Long> entry : sallesParBatiment.entrySet()) {
            if (entry.getValue() > 0) {
                pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }
        
        pieChartBatiments.setData(pieData);
    }
    
    private void mettreAJourGraphiqueUtilisateurs(StatsGlobales stats) {
        barChartUtilisateurs.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre d'utilisateurs");
        series.getData().add(new XYChart.Data<>("Admin", stats.admins));
        series.getData().add(new XYChart.Data<>("Manager", stats.managers));
        series.getData().add(new XYChart.Data<>("Enseignant", stats.teachers));
        series.getData().add(new XYChart.Data<>("Étudiant", stats.students));
        
        barChartUtilisateurs.getData().add(series);
        
        // Ajouter les tooltips
        for (XYChart.Data<String, Number> data : series.getData()) {
            Tooltip tooltip = new Tooltip(data.getYValue() + " utilisateurs");
            Tooltip.install(data.getNode(), tooltip);
        }
    }
    
    private void mettreAJourReservations(StatsGlobales stats) {
        statsReservations.getChildren().clear();
        
        statsReservations.add(creerCarteStat("Total", String.valueOf(stats.totalReservations), COULEUR_PRIMAIRE, "📊"), 0, 0);
        statsReservations.add(creerCarteStat("En cours", String.valueOf(stats.reservationsEnCours), COULEUR_SUCCESS, "⏳"), 1, 0);
        statsReservations.add(creerCarteStat("Aujourd'hui", String.valueOf(stats.reservationsAJour), COULEUR_WARNING, "📌"), 2, 0);
        statsReservations.add(creerCarteStat("Cette semaine", String.valueOf(stats.reservationsASemaine), COULEUR_INFO, "📅"), 3, 0);
    }
    
    private VBox creerCarteStat(String titre, String valeur, String couleur, String icone) {
        VBox carte = new VBox(5);
        carte.setPadding(new Insets(15));
        carte.setStyle("-fx-background-color: " + FOND_CARTE + 
                       "; -fx-background-radius: 10; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
        carte.setPrefWidth(180);
        carte.setPrefHeight(100);
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label iconeLabel = new Label(icone);
        iconeLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        Label titreLabel = new Label(titre);
        titreLabel.setFont(Font.font("System", 12));
        titreLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        headerBox.getChildren().addAll(iconeLabel, titreLabel);
        
        Label valeurLabel = new Label(valeur);
        valeurLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        valeurLabel.setTextFill(Color.web(couleur));
        
        carte.getChildren().addAll(headerBox, valeurLabel);
        return carte;
    }
    
    private Separator creerSeparateur() {
        Separator sep = new Separator();
        sep.setPadding(new Insets(10, 0, 10, 0));
        sep.setStyle("-fx-background-color: " + BORDURE + ";");
        return sep;
    }
    
    private StatsGlobales calculerStatsGlobales(List<Salle> salles, List<Utilisateur> utilisateurs, List<Reservation> reservations) {
        StatsGlobales stats = new StatsGlobales();
        
        // Stats salles
        stats.totalSalles = salles.size();
        stats.sallesVideoproj = salles.stream().filter(Salle::isAVideoprojecteur).count();
        stats.sallesTableau = salles.stream().filter(Salle::isATableauBlanc).count();
        stats.sallesClim = salles.stream().filter(Salle::isAClimatisation).count();
        stats.sallesTD = salles.stream().filter(s -> "TD".equals(s.getType())).count();
        stats.sallesTP = salles.stream().filter(s -> "TP".equals(s.getType())).count();
        stats.sallesAmphi = salles.stream().filter(s -> "AMPHI".equals(s.getType())).count();
        
        // Stats utilisateurs
        stats.totalUsers = utilisateurs.size();
        stats.admins = utilisateurs.stream().filter(u -> "admin".equals(u.getRole())).count();
        stats.managers = utilisateurs.stream().filter(u -> "manager".equals(u.getRole())).count();
        stats.teachers = utilisateurs.stream().filter(u -> "enseignant".equals(u.getRole())).count();
        stats.students = utilisateurs.stream().filter(u -> "etudiant".equals(u.getRole())).count();
        
        // Stats réservations
        stats.totalReservations = reservations.size();
        stats.reservationsEnCours = reservations.stream()
                .filter(r -> "confirmée".equals(r.getStatut()) && 
                        r.getDate().equals(LocalDate.now()) &&
                        r.getHeureDebut().isBefore(LocalTime.now()) &&
                        r.getHeureFin().isAfter(LocalTime.now()))
                .count();
        
        stats.reservationsAJour = reservations.stream()
                .filter(r -> "confirmée".equals(r.getStatut()) && 
                        r.getDate().equals(LocalDate.now()))
                .count();
        
        stats.reservationsASemaine = reservations.stream()
                .filter(r -> "confirmée".equals(r.getStatut()) && 
                        r.getDate().isAfter(LocalDate.now()) &&
                        r.getDate().isBefore(LocalDate.now().plusDays(7)))
                .count();
        
        return stats;
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
    
    // Classe interne pour regrouper les statistiques
    private class StatsGlobales {
        long totalSalles;
        long sallesVideoproj;
        long sallesTableau;
        long sallesClim;
        long sallesTD;
        long sallesTP;
        long sallesAmphi;
        
        long totalUsers;
        long admins;
        long managers;
        long teachers;
        long students;
        
        long totalReservations;
        long reservationsEnCours;
        long reservationsAJour;
        long reservationsASemaine;
    }
    
    public BorderPane getRacine() {
        return racine;
    }
 // Ajoutez cette méthode dans VueStatistiques.java
    private void ajouterRapportHebdomadaire() {
        VBox rapportBox = new VBox(10);
        rapportBox.setPadding(new Insets(15));
        rapportBox.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-background-radius: 10;");
        
        Label titreRapport = new Label("📊 Rapport d'utilisation - Semaine du " + 
            LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
            " au " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        titreRapport.setFont(Font.font("System", FontWeight.BOLD, 16));
        titreRapport.setTextFill(Color.web(TEXTE_PRINCIPAL));
        
        // Statistiques de la semaine
        List<Reservation> reservations = getToutesReservations();
        List<Salle> salles = salleDAO.getToutesLesSalles();
        LocalDate debutSemaine = LocalDate.now().minusDays(7);
        
        long reservationsSemaine = reservations.stream()
            .filter(r -> r.getDate().isAfter(debutSemaine) && r.getDate().isBefore(LocalDate.now().plusDays(1)))
            .count();
        
        long sallesUtilisees = reservations.stream()
            .filter(r -> r.getDate().isAfter(debutSemaine))
            .map(Reservation::getSalleId)
            .distinct()
            .count();
        
        long heuresTotal = reservations.stream()
            .filter(r -> r.getDate().isAfter(debutSemaine))
            .mapToLong(r -> java.time.Duration.between(r.getHeureDebut(), r.getHeureFin()).toHours())
            .sum();
        
        double tauxOccupation = salles.isEmpty() ? 0 : (sallesUtilisees * 100.0 / salles.size());
        
        GridPane statsRapport = new GridPane();
        statsRapport.setHgap(20);
        statsRapport.setVgap(10);
        
        statsRapport.add(creerStatRapport("📅 Réservations", String.valueOf(reservationsSemaine), "#3498db"), 0, 0);
        statsRapport.add(creerStatRapport("🏢 Salles utilisées", String.valueOf(sallesUtilisees), "#2ecc71"), 1, 0);
        statsRapport.add(creerStatRapport("⏱️ Heures totales", String.valueOf(heuresTotal), "#e67e22"), 2, 0);
        statsRapport.add(creerStatRapport("📈 Taux occupation", String.format("%.1f%%", tauxOccupation), "#9b59b6"), 3, 0);
        
        // Bouton pour exporter le rapport en PDF
        Button btnExporterRapport = new Button("📥 Exporter rapport PDF");
        btnExporterRapport.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnExporterRapport.setOnAction(e -> exporterRapportHebdo(reservationsSemaine, sallesUtilisees, heuresTotal, tauxOccupation));
        
        rapportBox.getChildren().addAll(titreRapport, statsRapport, btnExporterRapport);
        
        // Ajouter à la section réservations
        statsReservations.add(rapportBox, 0, 5, 4, 1);
    }

    private VBox creerStatRapport(String titre, String valeur, String couleur) {
        VBox carte = new VBox(5);
        carte.setPadding(new Insets(10));
        carte.setAlignment(Pos.CENTER);
        carte.setStyle("-fx-background-color: " + FOND_PRINCIPAL + "; -fx-background-radius: 10;");
        
        Label titreLabel = new Label(titre);
        titreLabel.setFont(Font.font("System", 12));
        titreLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        Label valeurLabel = new Label(valeur);
        valeurLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        valeurLabel.setTextFill(Color.web(couleur));
        
        carte.getChildren().addAll(titreLabel, valeurLabel);
        return carte;
    }

    private void exporterRapportHebdo(long reservations, long sallesUtilisees, long heuresTotal, double tauxOccupation) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le rapport hebdomadaire");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("rapport_hebdo_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        
        File file = fileChooser.showSaveDialog(racine.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("=".repeat(80));
                writer.println("████████████████████████████████████████████████████████████████████");
                writer.println("█                                                                  █");
                writer.println("█                 RAPPORT D'UTILISATION HEBDOMADAIRE                █");
                writer.println("█                    UNIVERSITÉ IBA DER THIAM                       █");
                writer.println("█                            UIDT - Thiès                           █");
                writer.println("█                                                                  █");
                writer.println("████████████████████████████████████████████████████████████████████");
                writer.println();
                writer.println("Période : " + LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                              " au " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                writer.println("Date d'édition : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                writer.println("=".repeat(80));
                writer.println();
                writer.println("📊 STATISTIQUES GÉNÉRALES");
                writer.println("-".repeat(80));
                writer.println("Réservations totales : " + reservations);
                writer.println("Salles utilisées : " + sallesUtilisees);
                writer.println("Heures de réservation : " + heuresTotal);
                writer.println("Taux d'occupation : " + String.format("%.1f", tauxOccupation) + "%");
                writer.println();
                writer.println("-".repeat(80));
                writer.println("🔚 Fin du rapport");
                writer.println("=".repeat(80));
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText("✅ Rapport hebdomadaire exporté");
                alert.setContentText("Le fichier a été sauvegardé : " + file.getAbsolutePath());
                alert.showAndWait();
                
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("❌ Échec de l'export");
                alert.setContentText("Erreur : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}