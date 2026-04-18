package com.univ.scheduler.vue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.univ.scheduler.dao.ReservationDAO;
import com.univ.scheduler.dao.SalleDAO;
import com.univ.scheduler.modele.Reservation;
import com.univ.scheduler.modele.Salle;
import com.univ.scheduler.modele.Utilisateur;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class VueRecherche extends VueBase {
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private SalleDAO salleDAO;
    private ReservationDAO reservationDAO;
    private TableView<Salle> tableResultats;
    private ObservableList<Salle> toutesSalles;
    private Label infoLabel;
    private Timeline synchronisationTimer;
    
    // Composants de recherche
    private ComboBox<String> comboBatiment;
    private DatePicker datePicker;
    private ComboBox<String> comboHeureDebut;
    private ComboBox<String> comboHeureFin;
    private Spinner<Integer> spinnerCapacite;
    private ComboBox<String> comboType;
    private CheckBox checkVideoproj;
    private CheckBox checkTableau;
    private CheckBox checkClim;
    
    public VueRecherche(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        this.salleDAO = new SalleDAO();
        this.reservationDAO = new ReservationDAO();
        this.toutesSalles = FXCollections.observableArrayList();
        creerVue();
        demarrerSynchronisationTempsReel();
        chargerSalles();
    }
    
    private void demarrerSynchronisationTempsReel() {
        synchronisationTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            chargerSalles();
        }));
        synchronisationTimer.setCycleCount(Timeline.INDEFINITE);
        synchronisationTimer.play();
    }
    
    private void chargerSalles() {
        Platform.runLater(() -> {
            toutesSalles.clear();
            toutesSalles.addAll(salleDAO.getToutesLesSalles());
            rechercherSalles(); // Re-appliquer les filtres
            infoLabel.setText("Dernière mise à jour: " + 
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
    }
    
    private void creerVue() {
        racine = new BorderPane();
        appliquerFond(racine);
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: #f5f7fa;");
        
        // Titre
        Label titreLabel = new Label("🔍 Recherche de salles disponibles");
        titreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        infoLabel = new Label("Mise à jour toutes les 30 secondes");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        // Panneau de recherche
        VBox rechercheBox = new VBox(15);
        rechercheBox.setPadding(new Insets(20));
        rechercheBox.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label criteresLabel = new Label("Critères de recherche");
        criteresLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        GridPane grilleCriteres = new GridPane();
        grilleCriteres.setHgap(15);
        grilleCriteres.setVgap(10);
        
        // Bâtiment
        Label batimentLabel = new Label("Bâtiment :");
        comboBatiment = new ComboBox<>();
        comboBatiment.getItems().add("Tous");
        comboBatiment.getItems().addAll(
            "Bâtiment A", "Bâtiment B", "Bâtiment C", "Bâtiment D", "Bâtiment E", 
            "Amphithéâtres", "Bibliothèque", "Restaurants"
        );
        comboBatiment.setValue("Tous");
        
        // Date
        Label dateLabel = new Label("Date :");
        datePicker = new DatePicker(LocalDate.now());
        
        // Heure début
        Label heureDebutLabel = new Label("Heure début :");
        comboHeureDebut = new ComboBox<>();
        for (int h = 8; h <= 20; h++) {
            comboHeureDebut.getItems().add(String.format("%02d:00", h));
            comboHeureDebut.getItems().add(String.format("%02d:30", h));
        }
        comboHeureDebut.setValue("08:00");
        
        // Heure fin
        Label heureFinLabel = new Label("Heure fin :");
        comboHeureFin = new ComboBox<>();
        for (int h = 9; h <= 21; h++) {
            comboHeureFin.getItems().add(String.format("%02d:00", h));
            comboHeureFin.getItems().add(String.format("%02d:30", h));
        }
        comboHeureFin.setValue("10:00");
        
        // Capacité
        Label capaciteLabel = new Label("Capacité min :");
        spinnerCapacite = new Spinner<>(1, 200, 20);
        spinnerCapacite.setEditable(true);
        
        // Type
        Label typeLabel = new Label("Type :");
        comboType = new ComboBox<>();
        comboType.getItems().addAll("Tous", "TD", "TP", "AMPHI");
        comboType.setValue("Tous");
        
        // Équipements
        Label equipLabel = new Label("Équipements :");
        checkVideoproj = new CheckBox("Vidéoprojecteur");
        checkTableau = new CheckBox("Tableau blanc");
        checkClim = new CheckBox("Climatisation");
        
        // Boutons
        Button btnRechercher = new Button("🔍 Rechercher");
        btnRechercher.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnRechercher.setPrefWidth(200);
        btnRechercher.setOnAction(e -> rechercherSalles());
        
        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        btnActualiser.setOnAction(e -> chargerSalles());
        
        Button btnReinitialiser = new Button("Réinitialiser");
        btnReinitialiser.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        btnReinitialiser.setOnAction(e -> reinitialiser());
        
        // Disposition
        grilleCriteres.add(batimentLabel, 0, 0);
        grilleCriteres.add(comboBatiment, 1, 0);
        grilleCriteres.add(dateLabel, 2, 0);
        grilleCriteres.add(datePicker, 3, 0);
        
        grilleCriteres.add(heureDebutLabel, 0, 1);
        grilleCriteres.add(comboHeureDebut, 1, 1);
        grilleCriteres.add(heureFinLabel, 2, 1);
        grilleCriteres.add(comboHeureFin, 3, 1);
        
        grilleCriteres.add(capaciteLabel, 0, 2);
        grilleCriteres.add(spinnerCapacite, 1, 2);
        grilleCriteres.add(typeLabel, 2, 2);
        grilleCriteres.add(comboType, 3, 2);
        
        grilleCriteres.add(equipLabel, 0, 3);
        grilleCriteres.add(checkVideoproj, 1, 3);
        grilleCriteres.add(checkTableau, 2, 3);
        grilleCriteres.add(checkClim, 3, 3);
        
        HBox boutonsBox = new HBox(10);
        boutonsBox.setAlignment(javafx.geometry.Pos.CENTER);
        boutonsBox.getChildren().addAll(btnRechercher, btnActualiser, btnReinitialiser, infoLabel);
        
        rechercheBox.getChildren().addAll(criteresLabel, grilleCriteres, boutonsBox);
        
        // Tableau des résultats
        tableResultats = new TableView<>();
        tableResultats.setPrefHeight(350);
        tableResultats.setStyle("-fx-font-size: 13px;");
        tableResultats.setPlaceholder(new Label("Aucune salle trouvée"));
        
        TableColumn<Salle, String> colBatiment = new TableColumn<>("Bâtiment");
        colBatiment.setCellValueFactory(new PropertyValueFactory<>("nomBatiment"));
        colBatiment.setPrefWidth(120);
        
        TableColumn<Salle, String> colNom = new TableColumn<>("Salle");
        colNom.setCellValueFactory(new PropertyValueFactory<>("numeroSalle"));
        colNom.setPrefWidth(100);
        
        TableColumn<Salle, Integer> colCapacite = new TableColumn<>("Capacité");
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colCapacite.setPrefWidth(80);
        
        TableColumn<Salle, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setPrefWidth(80);
        
        TableColumn<Salle, Boolean> colVideoproj = new TableColumn<>("Vidéoproj");
        colVideoproj.setCellValueFactory(new PropertyValueFactory<>("aVideoprojecteur"));
        colVideoproj.setPrefWidth(80);
        
        TableColumn<Salle, Boolean> colTableau = new TableColumn<>("Tableau");
        colTableau.setCellValueFactory(new PropertyValueFactory<>("aTableauBlanc"));
        colTableau.setPrefWidth(80);
        
        TableColumn<Salle, Boolean> colClim = new TableColumn<>("Clim");
        colClim.setCellValueFactory(new PropertyValueFactory<>("aClimatisation"));
        colClim.setPrefWidth(60);
        
        tableResultats.getColumns().addAll(colBatiment, colNom, colCapacite, colType, colVideoproj, colTableau, colClim);
        
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titreLabel, rechercheBox);
        
        racine.setTop(topBox);
        racine.setCenter(tableResultats);
    }
    
    private void rechercherSalles() {
        String batiment = comboBatiment.getValue();
        int capaciteMin = spinnerCapacite.getValue();
        String type = comboType.getValue();
        boolean videoproj = checkVideoproj.isSelected();
        boolean tableau = checkTableau.isSelected();
        boolean clim = checkClim.isSelected();
        
        ObservableList<Salle> resultats = FXCollections.observableArrayList();
        
        for (Salle salle : toutesSalles) {
            boolean correspond = true;
            
            if (!batiment.equals("Tous") && !salle.getNomBatiment().equals(batiment)) {
                correspond = false;
            }
            
            if (salle.getCapacite() < capaciteMin) correspond = false;
            
            if (!type.equals("Tous") && !salle.getType().equals(type)) correspond = false;
            
            if (videoproj && !salle.isAVideoprojecteur()) correspond = false;
            if (tableau && !salle.isATableauBlanc()) correspond = false;
            if (clim && !salle.isAClimatisation()) correspond = false;
            
            if (correspond) {
                resultats.add(salle);
            }
        }
        
        tableResultats.setItems(resultats);
        
        if (resultats.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Résultat");
            alert.setHeaderText("Aucune salle trouvée");
            alert.setContentText("Aucune salle ne correspond à vos critères.");
            alert.showAndWait();
        }
    }
    
    private void reinitialiser() {
        comboBatiment.setValue("Tous");
        datePicker.setValue(LocalDate.now());
        comboHeureDebut.setValue("08:00");
        comboHeureFin.setValue("10:00");
        spinnerCapacite.getValueFactory().setValue(20);
        comboType.setValue("Tous");
        checkVideoproj.setSelected(false);
        checkTableau.setSelected(false);
        checkClim.setSelected(false);
        tableResultats.setItems(toutesSalles);
    }
    
    public void arreterSynchronisation() {
        if (synchronisationTimer != null) {
            synchronisationTimer.stop();
        }
    }
    
    public BorderPane getRacine() {
        return racine;
    }
}