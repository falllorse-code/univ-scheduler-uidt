package com.univ.scheduler.vue;

import com.univ.scheduler.dao.SalleDAO;
import com.univ.scheduler.modele.Salle;
import com.univ.scheduler.modele.Utilisateur;
import com.univ.scheduler.utils.ExportPDF;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VueSalles extends VueBase{
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private Stage stagePrincipal;
    private SalleDAO salleDAO;
    private TableView<Salle> tableSalles;
    private TextField champRecherche;
    private ComboBox<String> comboType;
    private Spinner<Integer> spinnerCapacite;
    private CheckBox checkVideoproj;
    private ComboBox<String> comboBatiment;
    private ObservableList<Salle> toutesSalles;
    private Label infoLabel;
    private Timeline synchronisationTimer;
    private final String FOND_PRINCIPAL = "linear-gradient(from 0% 0% to 100% 100%, #0b3b3f 0%, #1a6b5a 50%, #2d8f6e 100%)";
    
    public VueSalles(Utilisateur utilisateur, Stage stage) {
        this.utilisateurCourant = utilisateur;
        this.stagePrincipal = stage;
        this.salleDAO = new SalleDAO();
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
            rechercherSalles();
            infoLabel.setText("Dernière mise à jour: " + 
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
    }
    
    private void creerVue() {
        racine = new BorderPane();
        appliquerFond(racine);
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        // Titre
        Label titreLabel = new Label("🏢 Gestion des salles");
        if (!utilisateurCourant.getRole().equals("admin")) {
            titreLabel.setText("🏢 Consultation des salles");
        }
        titreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        
        infoLabel = new Label("Mise à jour toutes les 30 secondes");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d4e6e1;");
        
        // Panneau de recherche (fond blanc)
        VBox rechercheBox = new VBox(10);
        rechercheBox.setPadding(new Insets(15, 0, 20, 0));
        rechercheBox.setStyle(
            "-fx-background-color: rgba(255,255,255,0.95);" +
            "-fx-background-radius: 15;" +
            "-fx-padding: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        
        Label rechercheLabel = new Label("🔍 Rechercher une salle :");
        rechercheLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        champRecherche = new TextField();
        champRecherche.setPromptText("Numéro de salle...");
        champRecherche.setPrefWidth(300);
        champRecherche.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #e0e0e0;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 8 12;"
        );
        champRecherche.textProperty().addListener((obs, oldVal, newVal) -> rechercherSalles());
        
        // Filtres avancés
        GridPane filtresPane = new GridPane();
        filtresPane.setHgap(15);
        filtresPane.setVgap(10);
        filtresPane.setPadding(new Insets(15, 0, 5, 0));
        
        Label batimentLabel = new Label("Bâtiment :");
        batimentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        comboBatiment = new ComboBox<>();
        comboBatiment.getItems().add("Tous");
        comboBatiment.getItems().addAll(
            "Bâtiment A", "Bâtiment B", "Bâtiment C", "Bâtiment D", "Bâtiment E", 
            "Amphithéâtres", "Bibliothèque", "Restaurants"
        );
        comboBatiment.setValue("Tous");
        comboBatiment.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-padding: 5;");
        comboBatiment.setOnAction(e -> rechercherSalles());
        
        Label typeLabel = new Label("Type :");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        comboType = new ComboBox<>();
        comboType.getItems().addAll("Tous", "TD", "TP", "AMPHI", "BIBLIOTHEQUE", "RESTAURANT");
        comboType.setValue("Tous");
        comboType.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-padding: 5;");
        comboType.setOnAction(e -> rechercherSalles());
        
        Label capaciteLabel = new Label("Capacité min :");
        capaciteLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        spinnerCapacite = new Spinner<>(0, 500, 0);
        spinnerCapacite.setEditable(true);
        spinnerCapacite.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");
        spinnerCapacite.valueProperty().addListener((obs, oldVal, newVal) -> rechercherSalles());
        
        checkVideoproj = new CheckBox("Avec vidéoprojecteur");
        checkVideoproj.setStyle("-fx-text-fill: #2c3e50;");
        checkVideoproj.setOnAction(e -> rechercherSalles());
        
        Button btnReinitialiser = new Button("Réinitialiser");
        btnReinitialiser.setStyle(
            "-fx-background-color: #95a5a6;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 15;"
        );
        btnReinitialiser.setOnAction(e -> reinitialiserFiltres());
        
        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 15;"
        );
        btnActualiser.setOnAction(e -> chargerSalles());
        
        filtresPane.add(batimentLabel, 0, 0);
        filtresPane.add(comboBatiment, 1, 0);
        filtresPane.add(typeLabel, 2, 0);
        filtresPane.add(comboType, 3, 0);
        filtresPane.add(capaciteLabel, 4, 0);
        filtresPane.add(spinnerCapacite, 5, 0);
        filtresPane.add(checkVideoproj, 6, 0);
        filtresPane.add(btnReinitialiser, 7, 0);
        filtresPane.add(btnActualiser, 8, 0);
        
        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.CENTER_RIGHT);
        infoBox.getChildren().add(infoLabel);
        
        rechercheBox.getChildren().addAll(rechercheLabel, champRecherche, filtresPane, infoBox);
        
        // Tableau des salles
        tableSalles = new TableView<>();
        tableSalles.setPrefHeight(400);
        tableSalles.setStyle(
            "-fx-background-color: rgba(255,255,255,0.95);" +
            "-fx-background-radius: 15;" +
            "-fx-font-size: 13px;"
        );
        
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
        
        tableSalles.getColumns().addAll(colBatiment, colNom, colCapacite, colType, colVideoproj, colTableau, colClim);
        
        // ============================================
        // BOUTONS D'ACTION - SELON LE RÔLE
        // ============================================
        HBox boutonsBox = new HBox(10);
        boutonsBox.setPadding(new Insets(20, 0, 0, 0));
        
        // Seul l'ADMIN peut ajouter, modifier et supprimer des salles
        if (utilisateurCourant.getRole().equals("admin")) {
            Button btnAjouter = new Button("➕ Ajouter une salle");
            btnAjouter.setStyle(
                "-fx-background-color: #2ecc71;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 15;"
            );
            btnAjouter.setOnAction(e -> ajouterSalle());
            
            Button btnModifier = new Button("✏️ Modifier");
            btnModifier.setStyle(
                "-fx-background-color: #f39c12;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 15;"
            );
            btnModifier.setOnAction(e -> modifierSalle());
            
            Button btnSupprimer = new Button("🗑️ Supprimer");
            btnSupprimer.setStyle(
                "-fx-background-color: #e74c3c;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 25;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 15;"
            );
            btnSupprimer.setOnAction(e -> supprimerSalle());
            
            boutonsBox.getChildren().addAll(btnAjouter, btnModifier, btnSupprimer);
        } else {
            // Pour les autres rôles (manager, enseignant, étudiant) : consultation seule
            Label consultationLabel = new Label("ℹ️ Mode consultation - Vous ne pouvez pas modifier les salles");
            consultationLabel.setStyle("-fx-text-fill: #d4e6e1; -fx-font-style: italic; -fx-padding: 8 0;");
            boutonsBox.getChildren().add(consultationLabel);
        }
        
        Button btnExporter = new Button("📥 Exporter PDF");
        btnExporter.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 25;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 15;"
        );
        btnExporter.setOnAction(e -> exporterSalles());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        boutonsBox.getChildren().addAll(spacer, btnExporter);
        
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titreLabel, rechercheBox);
        
        racine.setTop(topBox);
        racine.setCenter(tableSalles);
        racine.setBottom(boutonsBox);
    }
    
    private void rechercherSalles() {
        String recherche = champRecherche.getText().toLowerCase();
        String batiment = comboBatiment.getValue();
        String type = comboType.getValue();
        int capaciteMin = spinnerCapacite.getValue();
        boolean videoproj = checkVideoproj.isSelected();
        
        ObservableList<Salle> filtrees = FXCollections.observableArrayList();
        
        for (Salle salle : toutesSalles) {
            boolean correspond = true;
            
            if (!recherche.isEmpty() && !salle.getNumeroSalle().toLowerCase().contains(recherche)) {
                correspond = false;
            }
            
            if (!batiment.equals("Tous") && !salle.getNomBatiment().equals(batiment)) {
                correspond = false;
            }
            
            if (!type.equals("Tous") && !salle.getType().equals(type)) {
                correspond = false;
            }
            
            if (capaciteMin > 0 && salle.getCapacite() < capaciteMin) {
                correspond = false;
            }
            
            if (videoproj && !salle.isAVideoprojecteur()) {
                correspond = false;
            }
            
            if (correspond) {
                filtrees.add(salle);
            }
        }
        
        tableSalles.setItems(filtrees);
    }
    
    private void reinitialiserFiltres() {
        champRecherche.clear();
        comboBatiment.setValue("Tous");
        comboType.setValue("Tous");
        spinnerCapacite.getValueFactory().setValue(0);
        checkVideoproj.setSelected(false);
        rechercherSalles();
    }
    
    // ============================================
    // CRUD - AJOUTER UNE SALLE (admin uniquement)
    // ============================================
    private void ajouterSalle() {
        if (!utilisateurCourant.getRole().equals("admin")) {
            showAlert("Accès refusé", "Vous n'avez pas les droits pour ajouter une salle.", Alert.AlertType.ERROR);
            return;
        }
        
        Dialog<Salle> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une salle");
        dialog.setHeaderText("🏢 Nouvelle salle");
        
        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(400);
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        Label batimentLabel = new Label("Bâtiment:");
        batimentLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> comboBatimentInput = new ComboBox<>();
        comboBatimentInput.getItems().addAll(
            "Bâtiment A", "Bâtiment B", "Bâtiment C", "Bâtiment D", "Bâtiment E",
            "Amphithéâtres", "Bibliothèque", "Restaurants"
        );
        comboBatimentInput.setValue("Bâtiment A");
        comboBatimentInput.setPrefWidth(250);
        
        Label numeroLabel = new Label("Numéro salle:");
        numeroLabel.setStyle("-fx-font-weight: bold;");
        TextField champNumero = new TextField();
        champNumero.setPromptText("Ex: A101, B201, Amphi 1...");
        
        Label capaciteLabel = new Label("Capacité:");
        capaciteLabel.setStyle("-fx-font-weight: bold;");
        Spinner<Integer> spinnerCapaciteInput = new Spinner<>(1, 500, 30);
        spinnerCapaciteInput.setEditable(true);
        
        Label typeLabel = new Label("Type:");
        typeLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboTypeInput = new ComboBox<>();
        comboTypeInput.getItems().addAll("TD", "TP", "AMPHI", "BIBLIOTHEQUE", "RESTAURANT");
        comboTypeInput.setValue("TD");
        comboTypeInput.setPrefWidth(150);
        
        Label equipLabel = new Label("Équipements:");
        equipLabel.setStyle("-fx-font-weight: bold;");
        CheckBox checkVideoprojInput = new CheckBox("Vidéoprojecteur");
        CheckBox checkTableauInput = new CheckBox("Tableau blanc");
        CheckBox checkClimInput = new CheckBox("Climatisation");
        VBox equipBox = new VBox(5);
        equipBox.getChildren().addAll(checkVideoprojInput, checkTableauInput, checkClimInput);
        
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        int row = 0;
        grid.add(batimentLabel, 0, row);
        grid.add(comboBatimentInput, 1, row++);
        grid.add(numeroLabel, 0, row);
        grid.add(champNumero, 1, row++);
        grid.add(capaciteLabel, 0, row);
        grid.add(spinnerCapaciteInput, 1, row++);
        grid.add(typeLabel, 0, row);
        grid.add(comboTypeInput, 1, row++);
        grid.add(equipLabel, 0, row);
        grid.add(equipBox, 1, row++);
        grid.add(messageLabel, 0, row, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                if (champNumero.getText().trim().isEmpty()) {
                    messageLabel.setText("❌ Le numéro de salle est obligatoire");
                    return null;
                }
                
                Salle salle = new Salle();
                int batimentId = getBatimentId(comboBatimentInput.getValue());
                salle.setBatimentId(batimentId);
                salle.setNomBatiment(comboBatimentInput.getValue());
                salle.setNumeroSalle(champNumero.getText().trim());
                salle.setCapacite(spinnerCapaciteInput.getValue());
                salle.setType(comboTypeInput.getValue());
                salle.setAVideoprojecteur(checkVideoprojInput.isSelected());
                salle.setATableauBlanc(checkTableauInput.isSelected());
                salle.setAClimatisation(checkClimInput.isSelected());
                return salle;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(salle -> {
            if (salleDAO.ajouterSalle(salle)) {
                chargerSalles();
                showAlert("Succès", "✅ Salle ajoutée avec succès", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "❌ Échec de l'ajout de la salle", Alert.AlertType.ERROR);
            }
        });
    }
    
    // ============================================
    // CRUD - MODIFIER UNE SALLE (admin uniquement)
    // ============================================
    private void modifierSalle() {
        if (!utilisateurCourant.getRole().equals("admin")) {
            showAlert("Accès refusé", "Vous n'avez pas les droits pour modifier une salle.", Alert.AlertType.ERROR);
            return;
        }
        
        Salle selection = tableSalles.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert("Attention", "⚠️ Veuillez sélectionner une salle à modifier", Alert.AlertType.WARNING);
            return;
        }
        
        Dialog<Salle> dialog = new Dialog<>();
        dialog.setTitle("Modifier une salle");
        dialog.setHeaderText("✏️ Modification de " + selection.getNomCompletSalle());
        
        ButtonType btnValider = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(400);
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        Label batimentLabel = new Label("Bâtiment:");
        batimentLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> comboBatimentInput = new ComboBox<>();
        comboBatimentInput.getItems().addAll(
            "Bâtiment A", "Bâtiment B", "Bâtiment C", "Bâtiment D", "Bâtiment E",
            "Amphithéâtres", "Bibliothèque", "Restaurants"
        );
        comboBatimentInput.setValue(selection.getNomBatiment());
        comboBatimentInput.setPrefWidth(250);
        
        Label numeroLabel = new Label("Numéro salle:");
        numeroLabel.setStyle("-fx-font-weight: bold;");
        TextField champNumero = new TextField(selection.getNumeroSalle());
        
        Label capaciteLabel = new Label("Capacité:");
        capaciteLabel.setStyle("-fx-font-weight: bold;");
        Spinner<Integer> spinnerCapaciteInput = new Spinner<>(1, 500, selection.getCapacite());
        spinnerCapaciteInput.setEditable(true);
        
        Label typeLabel = new Label("Type:");
        typeLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboTypeInput = new ComboBox<>();
        comboTypeInput.getItems().addAll("TD", "TP", "AMPHI", "BIBLIOTHEQUE", "RESTAURANT");
        comboTypeInput.setValue(selection.getType());
        comboTypeInput.setPrefWidth(150);
        
        Label equipLabel = new Label("Équipements:");
        equipLabel.setStyle("-fx-font-weight: bold;");
        CheckBox checkVideoprojInput = new CheckBox("Vidéoprojecteur");
        checkVideoprojInput.setSelected(selection.isAVideoprojecteur());
        CheckBox checkTableauInput = new CheckBox("Tableau blanc");
        checkTableauInput.setSelected(selection.isATableauBlanc());
        CheckBox checkClimInput = new CheckBox("Climatisation");
        checkClimInput.setSelected(selection.isAClimatisation());
        VBox equipBox = new VBox(5);
        equipBox.getChildren().addAll(checkVideoprojInput, checkTableauInput, checkClimInput);
        
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        int row = 0;
        grid.add(batimentLabel, 0, row);
        grid.add(comboBatimentInput, 1, row++);
        grid.add(numeroLabel, 0, row);
        grid.add(champNumero, 1, row++);
        grid.add(capaciteLabel, 0, row);
        grid.add(spinnerCapaciteInput, 1, row++);
        grid.add(typeLabel, 0, row);
        grid.add(comboTypeInput, 1, row++);
        grid.add(equipLabel, 0, row);
        grid.add(equipBox, 1, row++);
        grid.add(messageLabel, 0, row, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                if (champNumero.getText().trim().isEmpty()) {
                    messageLabel.setText("❌ Le numéro de salle est obligatoire");
                    return null;
                }
                
                selection.setBatimentId(getBatimentId(comboBatimentInput.getValue()));
                selection.setNomBatiment(comboBatimentInput.getValue());
                selection.setNumeroSalle(champNumero.getText().trim());
                selection.setCapacite(spinnerCapaciteInput.getValue());
                selection.setType(comboTypeInput.getValue());
                selection.setAVideoprojecteur(checkVideoprojInput.isSelected());
                selection.setATableauBlanc(checkTableauInput.isSelected());
                selection.setAClimatisation(checkClimInput.isSelected());
                return selection;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(salle -> {
            if (salleDAO.modifierSalle(salle)) {
                chargerSalles();
                showAlert("Succès", "✅ Salle modifiée avec succès", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "❌ Échec de la modification", Alert.AlertType.ERROR);
            }
        });
    }
    
    // ============================================
    // CRUD - SUPPRIMER UNE SALLE (admin uniquement)
    // ============================================
    private void supprimerSalle() {
        if (!utilisateurCourant.getRole().equals("admin")) {
            showAlert("Accès refusé", "Vous n'avez pas les droits pour supprimer une salle.", Alert.AlertType.ERROR);
            return;
        }
        
        Salle selection = tableSalles.getSelectionModel().getSelectedItem();
        if (selection == null) {
            showAlert("Attention", "⚠️ Veuillez sélectionner une salle à supprimer", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("🗑️ Supprimer la salle");
        confirm.setContentText("Voulez-vous vraiment supprimer définitivement la salle " + 
                               selection.getNomCompletSalle() + " ?\n\n" +
                               "⚠️ Cette action est irréversible.");
        
        ButtonType btnOui = new ButtonType("Oui, supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNon = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnOui, btnNon);
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == btnOui) {
                if (salleDAO.supprimerSalle(selection.getId())) {
                    chargerSalles();
                    showAlert("Succès", "✅ Salle supprimée avec succès", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "❌ Échec de la suppression", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    // ============================================
    // EXPORTER LA LISTE DES SALLES
    // ============================================
    private void exporterSalles() {
        List<Salle> salles = tableSalles.getItems();
        if (salles.isEmpty()) {
            showAlert("Attention", "Aucune salle à exporter", Alert.AlertType.WARNING);
            return;
        }
        
        ExportPDF export = new ExportPDF(stagePrincipal);
        export.exporterListeSalles(salles);
    }
    
    // ============================================
    // UTILITAIRES
    // ============================================
    private int getBatimentId(String nomBatiment) {
        switch (nomBatiment) {
            case "Bâtiment A": return 1;
            case "Bâtiment B": return 2;
            case "Bâtiment C": return 3;
            case "Amphithéâtres": return 4;
            case "Bibliothèque": return 5;
            case "Restaurants": return 6;
            case "Bâtiment D": return 7;
            case "Bâtiment E": return 8;
            default: return 1;
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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