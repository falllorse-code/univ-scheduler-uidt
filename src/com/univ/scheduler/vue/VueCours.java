package com.univ.scheduler.vue;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.univ.scheduler.dao.CoursDAO;
import com.univ.scheduler.dao.SalleDAO;
import com.univ.scheduler.dao.UtilisateurDAO;
import com.univ.scheduler.modele.Cours;
import com.univ.scheduler.modele.Salle;
import com.univ.scheduler.modele.Utilisateur;
import com.univ.scheduler.utils.NotificationService;
import com.univ.scheduler.utils.ExportPDF;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VueCours {
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private Stage stagePrincipal;
    private CoursDAO coursDAO;
    private SalleDAO salleDAO;
    private UtilisateurDAO utilisateurDAO;
    private TableView<Cours> tableCours;
    private ComboBox<String> comboJour;
    private ObservableList<Cours> tousLesCours;
    private NotificationService notifications;
    private Label infoLabel;
    private Timeline synchronisationTimer;
    
    // Listes pour les combos
    private ObservableList<String> listeEnseignants;
    private ObservableList<String> listeSalles;
    private List<Utilisateur> enseignants;
    private List<Salle> salles;
    
    public VueCours(Utilisateur utilisateur, Stage stage) {
        this.utilisateurCourant = utilisateur;
        this.stagePrincipal = stage;
        this.coursDAO = new CoursDAO();
        this.salleDAO = new SalleDAO();
        this.utilisateurDAO = new UtilisateurDAO();
        this.tousLesCours = FXCollections.observableArrayList();
        this.listeEnseignants = FXCollections.observableArrayList();
        this.listeSalles = FXCollections.observableArrayList();
        this.notifications = NotificationService.getInstance();
        
        chargerEnseignants();
        chargerSalles();
        creerVue();
        demarrerSynchronisationTempsReel();
        chargerCours();
    }
    
    private void chargerEnseignants() {
        enseignants = utilisateurDAO.getUtilisateursParRole("enseignant");
        listeEnseignants.clear();
        for (Utilisateur u : enseignants) {
            listeEnseignants.add(u.getNomComplet() + " (ID: " + u.getId() + ")");
        }
    }
    
    private void chargerSalles() {
        salles = salleDAO.getToutesLesSalles();
        listeSalles.clear();
        for (Salle s : salles) {
            if (!s.getType().equals("RESTAURANT") && !s.getType().equals("BIBLIOTHEQUE")) {
                listeSalles.add(s.getNomCompletSalle() + " (Cap: " + s.getCapacite() + ")");
            }
        }
    }
    
    private void demarrerSynchronisationTempsReel() {
        synchronisationTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            chargerCours();
        }));
        synchronisationTimer.setCycleCount(Timeline.INDEFINITE);
        synchronisationTimer.play();
    }
    
    private void chargerCours() {
        Platform.runLater(() -> {
            tousLesCours.clear();
            tousLesCours.addAll(coursDAO.getTousLesCours());
            filtrerCours();
            infoLabel.setText("Dernière mise à jour: " + 
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + 
                " (" + tousLesCours.size() + " cours)");
        });
    }
    
    private void creerVue() {
        racine = new BorderPane();
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: #f5f7fa;");
        
        // Titre
        Label titreLabel = new Label("📚 Gestion des cours en temps réel");
        titreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        infoLabel = new Label("Mise à jour toutes les 30 secondes");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        // Filtres
        HBox filtresBox = new HBox(10);
        filtresBox.setPadding(new Insets(15, 0, 20, 0));
        filtresBox.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label jourLabel = new Label("Filtrer par jour :");
        jourLabel.setStyle("-fx-font-weight: bold;");
        
        comboJour = new ComboBox<>();
        comboJour.getItems().addAll("Tous", "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI");
        comboJour.setValue("Tous");
        comboJour.setStyle("-fx-min-width: 120px;");
        comboJour.setOnAction(e -> filtrerCours());
        
        Button btnRafraichir = new Button("🔄 Rafraîchir");
        btnRafraichir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnRafraichir.setOnAction(e -> chargerCours());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        filtresBox.getChildren().addAll(jourLabel, comboJour, spacer, btnRafraichir, infoLabel);
        
        // Tableau des cours
        tableCours = new TableView<>();
        tableCours.setPrefHeight(400);
        tableCours.setStyle("-fx-font-size: 13px;");
        
        TableColumn<Cours, String> colNom = new TableColumn<>("Cours");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNom.setPrefWidth(150);
        
        TableColumn<Cours, String> colEnseignant = new TableColumn<>("Enseignant");
        colEnseignant.setCellValueFactory(new PropertyValueFactory<>("nomEnseignant"));
        colEnseignant.setPrefWidth(150);
        
        TableColumn<Cours, String> colClasse = new TableColumn<>("Classe");
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colClasse.setPrefWidth(100);
        
        TableColumn<Cours, String> colGroupe = new TableColumn<>("Groupe");
        colGroupe.setCellValueFactory(new PropertyValueFactory<>("groupe"));
        colGroupe.setPrefWidth(80);
        
        TableColumn<Cours, String> colJour = new TableColumn<>("Jour");
        colJour.setCellValueFactory(new PropertyValueFactory<>("jourSemaine"));
        colJour.setPrefWidth(100);
        
        TableColumn<Cours, String> colHoraire = new TableColumn<>("Horaire");
        colHoraire.setCellValueFactory(new PropertyValueFactory<>("plageHoraire"));
        colHoraire.setPrefWidth(120);
        
        TableColumn<Cours, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(new PropertyValueFactory<>("nomSalle"));
        colSalle.setPrefWidth(150);
        
        tableCours.getColumns().addAll(colNom, colEnseignant, colClasse, colGroupe, colJour, colHoraire, colSalle);
        
        // Boutons d'action
        HBox boutonsBox = new HBox(10);
        boutonsBox.setPadding(new Insets(20, 0, 0, 0));
        
        if (utilisateurCourant.getRole().equals("admin") || utilisateurCourant.getRole().equals("manager")) {
            Button btnAjouter = new Button("➕ Ajouter un cours");
            btnAjouter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnAjouter.setOnAction(e -> ajouterCours());
            
            Button btnModifier = new Button("✏️ Modifier");
            btnModifier.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnModifier.setOnAction(e -> modifierCours());
            
            Button btnSupprimer = new Button("🗑️ Supprimer");
            btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnSupprimer.setOnAction(e -> supprimerCours());
            
            boutonsBox.getChildren().addAll(btnAjouter, btnModifier, btnSupprimer);
        }
        
        Button btnVerifierConflits = new Button("⚠️ Vérifier conflits");
        btnVerifierConflits.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnVerifierConflits.setOnAction(e -> verifierConflits());
        
        Button btnExporter = new Button("📥 Exporter PDF");
        btnExporter.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnExporter.setOnAction(e -> exporterPDF());
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        boutonsBox.getChildren().addAll(btnVerifierConflits, btnExporter, spacer2);
        
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titreLabel, filtresBox);
        
        racine.setTop(topBox);
        racine.setCenter(tableCours);
        racine.setBottom(boutonsBox);
    }
    
    private void filtrerCours() {
        String jourSelectionne = comboJour.getValue();
        ObservableList<Cours> coursFiltres = FXCollections.observableArrayList();
        
        for (Cours cours : tousLesCours) {
            if (jourSelectionne.equals("Tous") || cours.getJourSemaine().equals(jourSelectionne)) {
                coursFiltres.add(cours);
            }
        }
        
        tableCours.setItems(coursFiltres);
    }
    
    // ============================================
    // CREATE - Ajouter un cours
    // ============================================
    private void ajouterCours() {
        Dialog<Cours> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un cours");
        dialog.setHeaderText("📚 Nouveau cours");
        
        ButtonType btnValider = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(500);
        
        // Nom du cours
        Label nomLabel = new Label("Nom du cours:");
        nomLabel.setStyle("-fx-font-weight: bold;");
        TextField champNom = new TextField();
        champNom.setPromptText("Ex: Mathématiques, Programmation Java...");
        
        // Enseignant
        Label enseignantLabel = new Label("Enseignant:");
        enseignantLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboEnseignant = new ComboBox<>();
        comboEnseignant.setItems(listeEnseignants);
        comboEnseignant.setPromptText("Sélectionner un enseignant");
        comboEnseignant.setPrefWidth(300);
        
        // Classe
        Label classeLabel = new Label("Classe:");
        classeLabel.setStyle("-fx-font-weight: bold;");
        TextField champClasse = new TextField();
        champClasse.setPromptText("Ex: L1 INFO, L2 MATH...");
        
        // Groupe
        Label groupeLabel = new Label("Groupe:");
        groupeLabel.setStyle("-fx-font-weight: bold;");
        TextField champGroupe = new TextField();
        champGroupe.setPromptText("Ex: Groupe A, TD1...");
        
        // Jour
        Label jourLabel = new Label("Jour:");
        jourLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboJourInput = new ComboBox<>();
        comboJourInput.getItems().addAll("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI");
        comboJourInput.setValue("LUNDI");
        
        // Heure début
        Label heureDebutLabel = new Label("Heure début:");
        heureDebutLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboHeureDebut = new ComboBox<>();
        for (int h = 8; h <= 18; h++) {
            comboHeureDebut.getItems().add(String.format("%02d:00", h));
            comboHeureDebut.getItems().add(String.format("%02d:30", h));
        }
        comboHeureDebut.setValue("08:00");
        
        // Durée
        Label dureeLabel = new Label("Durée (minutes):");
        dureeLabel.setStyle("-fx-font-weight: bold;");
        Spinner<Integer> spinnerDuree = new Spinner<>(60, 240, 90);
        spinnerDuree.setEditable(true);
        
        // Salle
        Label salleLabel = new Label("Salle:");
        salleLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboSalle = new ComboBox<>();
        comboSalle.setItems(listeSalles);
        comboSalle.setPromptText("Sélectionner une salle");
        comboSalle.setPrefWidth(300);
        
        // Message d'erreur
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        // Bouton vérifier disponibilité
        Button btnVerifier = new Button("🔍 Vérifier disponibilité");
        btnVerifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        
        // Disposition
        int row = 0;
        grid.add(nomLabel, 0, row);
        grid.add(champNom, 1, row++);
        grid.add(enseignantLabel, 0, row);
        grid.add(comboEnseignant, 1, row++);
        grid.add(classeLabel, 0, row);
        grid.add(champClasse, 1, row++);
        grid.add(groupeLabel, 0, row);
        grid.add(champGroupe, 1, row++);
        grid.add(jourLabel, 0, row);
        grid.add(comboJourInput, 1, row++);
        grid.add(heureDebutLabel, 0, row);
        grid.add(comboHeureDebut, 1, row++);
        grid.add(dureeLabel, 0, row);
        grid.add(spinnerDuree, 1, row++);
        grid.add(salleLabel, 0, row);
        grid.add(comboSalle, 1, row++);
        grid.add(btnVerifier, 0, row, 2, 1);
        grid.add(messageLabel, 0, ++row, 2, 1);
        
        // Action du bouton vérifier
        btnVerifier.setOnAction(e -> {
            if (comboSalle.getValue() == null || comboJourInput.getValue() == null) {
                messageLabel.setText("❌ Veuillez sélectionner une salle et un jour");
                return;
            }
            
            LocalTime debut = LocalTime.parse(comboHeureDebut.getValue());
            LocalTime fin = debut.plusMinutes(spinnerDuree.getValue());
            
            boolean disponible = verifierDisponibiliteSalle(
                extraireIdSalle(comboSalle.getValue()),
                comboJourInput.getValue(),
                debut,
                fin
            );
            
            if (disponible) {
                messageLabel.setText("✅ Salle disponible pour ce créneau !");
                messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                messageLabel.setText("❌ Salle déjà occupée sur ce créneau !");
                messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                // Validations
                if (champNom.getText().trim().isEmpty()) {
                    messageLabel.setText("❌ Le nom du cours est obligatoire");
                    return null;
                }
                if (comboEnseignant.getValue() == null) {
                    messageLabel.setText("❌ Veuillez sélectionner un enseignant");
                    return null;
                }
                if (champClasse.getText().trim().isEmpty()) {
                    messageLabel.setText("❌ La classe est obligatoire");
                    return null;
                }
                if (comboSalle.getValue() == null) {
                    messageLabel.setText("❌ Veuillez sélectionner une salle");
                    return null;
                }
                
                Cours cours = new Cours();
                cours.setNom(champNom.getText().trim());
                
                // Extraire l'ID de l'enseignant
                int enseignantId = extraireIdEnseignant(comboEnseignant.getValue());
                cours.setEnseignantId(enseignantId);
                cours.setNomEnseignant(comboEnseignant.getValue().split(" \\(")[0]);
                
                cours.setClasse(champClasse.getText().trim());
                cours.setGroupe(champGroupe.getText().trim());
                cours.setJourSemaine(comboJourInput.getValue());
                
                LocalTime debut = LocalTime.parse(comboHeureDebut.getValue());
                cours.setHeureDebut(debut);
                cours.setDuree(spinnerDuree.getValue());
                
                int salleId = extraireIdSalle(comboSalle.getValue());
                cours.setSalleId(salleId);
                cours.setNomSalle(comboSalle.getValue().split(" \\(")[0]);
                
                return cours;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(cours -> {
            if (verifierConflit(cours)) {
                showAlert("Conflit", "❌ Ce cours crée un conflit avec un cours existant !", Alert.AlertType.WARNING);
            } else {
                if (coursDAO.ajouterCours(cours)) {
                    chargerCours();
                    showAlert("Succès", "✅ Cours ajouté avec succès", Alert.AlertType.INFORMATION);
                    
                    // Notification
                    String sujet = "📚 Nouveau cours programmé";
                    String contenu = String.format(
                        "Un nouveau cours a été ajouté :\n\n" +
                        "Cours: %s\nEnseignant: %s\nClasse: %s\n%s\nSalle: %s",
                        cours.getNom(),
                        cours.getNomEnseignant(),
                        cours.getClasse(),
                        cours.getJourSemaine() + " " + cours.getPlageHoraire(),
                        cours.getNomSalle()
                    );
                    notifications.envoyerNotificationEmail(utilisateurCourant, sujet, contenu);
                    
                } else {
                    showAlert("Erreur", "❌ Échec de l'ajout du cours", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    // ============================================
    // UPDATE - Modifier un cours
    // ============================================
    private void modifierCours() {
        Cours coursSelectionne = tableCours.getSelectionModel().getSelectedItem();
        if (coursSelectionne == null) {
            showAlert("Attention", "⚠️ Veuillez sélectionner un cours à modifier", Alert.AlertType.WARNING);
            return;
        }
        
        Dialog<Cours> dialog = new Dialog<>();
        dialog.setTitle("Modifier un cours");
        dialog.setHeaderText("✏️ Modification de " + coursSelectionne.getNom());
        
        ButtonType btnValider = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(500);
        
        // Nom du cours
        Label nomLabel = new Label("Nom du cours:");
        nomLabel.setStyle("-fx-font-weight: bold;");
        TextField champNom = new TextField(coursSelectionne.getNom());
        
        // Enseignant
        Label enseignantLabel = new Label("Enseignant:");
        enseignantLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboEnseignant = new ComboBox<>();
        comboEnseignant.setItems(listeEnseignants);
        comboEnseignant.setValue(coursSelectionne.getNomEnseignant() + " (ID: " + coursSelectionne.getEnseignantId() + ")");
        
        // Classe
        Label classeLabel = new Label("Classe:");
        classeLabel.setStyle("-fx-font-weight: bold;");
        TextField champClasse = new TextField(coursSelectionne.getClasse());
        
        // Groupe
        Label groupeLabel = new Label("Groupe:");
        groupeLabel.setStyle("-fx-font-weight: bold;");
        TextField champGroupe = new TextField(coursSelectionne.getGroupe());
        
        // Jour
        Label jourLabel = new Label("Jour:");
        jourLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboJourInput = new ComboBox<>();
        comboJourInput.getItems().addAll("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI");
        comboJourInput.setValue(coursSelectionne.getJourSemaine());
        
        // Heure début
        Label heureDebutLabel = new Label("Heure début:");
        heureDebutLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboHeureDebut = new ComboBox<>();
        for (int h = 8; h <= 18; h++) {
            comboHeureDebut.getItems().add(String.format("%02d:00", h));
            comboHeureDebut.getItems().add(String.format("%02d:30", h));
        }
        comboHeureDebut.setValue(coursSelectionne.getHeureDebut().toString());
        
        // Durée
        Label dureeLabel = new Label("Durée (minutes):");
        dureeLabel.setStyle("-fx-font-weight: bold;");
        Spinner<Integer> spinnerDuree = new Spinner<>(60, 240, coursSelectionne.getDuree());
        spinnerDuree.setEditable(true);
        
        // Salle
        Label salleLabel = new Label("Salle:");
        salleLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> comboSalle = new ComboBox<>();
        comboSalle.setItems(listeSalles);
        comboSalle.setValue(coursSelectionne.getNomSalle() + " (Cap: " + getCapaciteSalle(coursSelectionne.getSalleId()) + ")");
        
        // Message d'erreur
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        // Bouton vérifier disponibilité
        Button btnVerifier = new Button("🔍 Vérifier disponibilité");
        btnVerifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        
        // Disposition
        int row = 0;
        grid.add(nomLabel, 0, row);
        grid.add(champNom, 1, row++);
        grid.add(enseignantLabel, 0, row);
        grid.add(comboEnseignant, 1, row++);
        grid.add(classeLabel, 0, row);
        grid.add(champClasse, 1, row++);
        grid.add(groupeLabel, 0, row);
        grid.add(champGroupe, 1, row++);
        grid.add(jourLabel, 0, row);
        grid.add(comboJourInput, 1, row++);
        grid.add(heureDebutLabel, 0, row);
        grid.add(comboHeureDebut, 1, row++);
        grid.add(dureeLabel, 0, row);
        grid.add(spinnerDuree, 1, row++);
        grid.add(salleLabel, 0, row);
        grid.add(comboSalle, 1, row++);
        grid.add(btnVerifier, 0, row, 2, 1);
        grid.add(messageLabel, 0, ++row, 2, 1);
        
        // Action du bouton vérifier
        btnVerifier.setOnAction(e -> {
            if (comboSalle.getValue() == null || comboJourInput.getValue() == null) {
                messageLabel.setText("❌ Veuillez sélectionner une salle et un jour");
                return;
            }
            
            LocalTime debut = LocalTime.parse(comboHeureDebut.getValue());
            LocalTime fin = debut.plusMinutes(spinnerDuree.getValue());
            
            boolean disponible = verifierDisponibiliteSalle(
                extraireIdSalle(comboSalle.getValue()),
                comboJourInput.getValue(),
                debut,
                fin,
                coursSelectionne.getId() // Exclure le cours actuel
            );
            
            if (disponible) {
                messageLabel.setText("✅ Salle disponible pour ce créneau !");
                messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                messageLabel.setText("❌ Salle déjà occupée sur ce créneau !");
                messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                coursSelectionne.setNom(champNom.getText().trim());
                
                int enseignantId = extraireIdEnseignant(comboEnseignant.getValue());
                coursSelectionne.setEnseignantId(enseignantId);
                coursSelectionne.setNomEnseignant(comboEnseignant.getValue().split(" \\(")[0]);
                
                coursSelectionne.setClasse(champClasse.getText().trim());
                coursSelectionne.setGroupe(champGroupe.getText().trim());
                coursSelectionne.setJourSemaine(comboJourInput.getValue());
                
                LocalTime debut = LocalTime.parse(comboHeureDebut.getValue());
                coursSelectionne.setHeureDebut(debut);
                coursSelectionne.setDuree(spinnerDuree.getValue());
                
                int salleId = extraireIdSalle(comboSalle.getValue());
                coursSelectionne.setSalleId(salleId);
                coursSelectionne.setNomSalle(comboSalle.getValue().split(" \\(")[0]);
                
                return coursSelectionne;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(cours -> {
            if (verifierConflit(cours, cours.getId())) {
                showAlert("Conflit", "❌ Cette modification crée un conflit avec un autre cours !", Alert.AlertType.WARNING);
            } else {
                if (coursDAO.modifierCours(cours)) {
                    chargerCours();
                    showAlert("Succès", "✅ Cours modifié avec succès", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "❌ Échec de la modification", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    // ============================================
    // DELETE - Supprimer un cours
    // ============================================
    private void supprimerCours() {
        Cours coursSelectionne = tableCours.getSelectionModel().getSelectedItem();
        if (coursSelectionne == null) {
            showAlert("Attention", "⚠️ Veuillez sélectionner un cours à supprimer", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("🗑️ Supprimer le cours");
        confirm.setContentText("Voulez-vous vraiment supprimer définitivement le cours \n" +
                               "📚 " + coursSelectionne.getNom() + " ?\n\n" +
                               "Cette action est irréversible.");
        
        ButtonType btnOui = new ButtonType("Oui, supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNon = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnOui, btnNon);
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == btnOui) {
                if (coursDAO.supprimerCours(coursSelectionne.getId())) {
                    chargerCours();
                    showAlert("Succès", "✅ Cours supprimé avec succès", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "❌ Échec de la suppression", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    // ============================================
    // VÉRIFICATION DES CONFLITS
    // ============================================
    private boolean verifierConflit(Cours nouveauCours) {
        return verifierConflit(nouveauCours, -1);
    }
    
    private boolean verifierConflit(Cours nouveauCours, int excludeId) {
        for (Cours cours : tousLesCours) {
            if (cours.getId() == excludeId) continue;
            
            if (cours.getNomSalle() != null && nouveauCours.getNomSalle() != null &&
                cours.getNomSalle().equals(nouveauCours.getNomSalle()) &&
                cours.getJourSemaine().equals(nouveauCours.getJourSemaine())) {
                
                LocalTime debut1 = cours.getHeureDebut();
                LocalTime fin1 = cours.getHeureFin();
                LocalTime debut2 = nouveauCours.getHeureDebut();
                LocalTime fin2 = nouveauCours.getHeureFin();
                
                if (debut1.isBefore(fin2) && debut2.isBefore(fin1)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean verifierDisponibiliteSalle(int salleId, String jour, LocalTime debut, LocalTime fin) {
        return verifierDisponibiliteSalle(salleId, jour, debut, fin, -1);
    }
    
    private boolean verifierDisponibiliteSalle(int salleId, String jour, LocalTime debut, LocalTime fin, int excludeId) {
        for (Cours cours : tousLesCours) {
            if (cours.getId() == excludeId) continue;
            
            if (cours.getSalleId() == salleId && cours.getJourSemaine().equals(jour)) {
                LocalTime debut1 = cours.getHeureDebut();
                LocalTime fin1 = cours.getHeureFin();
                
                if (debut.isBefore(fin1) && debut1.isBefore(fin)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void verifierConflits() {
        StringBuilder conflits = new StringBuilder();
        int nbConflits = 0;
        
        for (int i = 0; i < tousLesCours.size(); i++) {
            for (int j = i + 1; j < tousLesCours.size(); j++) {
                Cours c1 = tousLesCours.get(i);
                Cours c2 = tousLesCours.get(j);
                
                if (c1.getNomSalle() != null && c2.getNomSalle() != null &&
                    c1.getNomSalle().equals(c2.getNomSalle()) &&
                    c1.getJourSemaine().equals(c2.getJourSemaine())) {
                    
                    LocalTime debut1 = c1.getHeureDebut();
                    LocalTime fin1 = c1.getHeureFin();
                    LocalTime debut2 = c2.getHeureDebut();
                    LocalTime fin2 = c2.getHeureFin();
                    
                    if (debut1.isBefore(fin2) && debut2.isBefore(fin1)) {
                        nbConflits++;
                        conflits.append("• ")
                                .append(c1.getNom()).append(" et ")
                                .append(c2.getNom()).append(" en ")
                                .append(c1.getNomSalle()).append(" le ")
                                .append(c1.getJourSemaine()).append("\n");
                    }
                }
            }
        }
        
        Alert alert = new Alert(nbConflits > 0 ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION);
        alert.setTitle("Vérification des conflits");
        
        if (nbConflits > 0) {
            alert.setHeaderText("⚠️ " + nbConflits + " conflit(s) détecté(s) !");
            alert.setContentText(conflits.toString());
        } else {
            alert.setHeaderText("✅ Aucun conflit détecté");
            alert.setContentText("L'emploi du temps est cohérent.");
        }
        alert.showAndWait();
    }
    
    // ============================================
    // EXPORT PDF
    // ============================================
    private void exporterPDF() {
        List<Cours> coursList = tableCours.getItems();
        if (coursList.isEmpty()) {
            showAlert("Attention", "Aucun cours à exporter", Alert.AlertType.WARNING);
            return;
        }
        
        ExportPDF export = new ExportPDF(stagePrincipal);
        export.exporterEmploiTemps(coursList, "Emploi du temps");
    }
    
    // ============================================
    // UTILITAIRES
    // ============================================
    private int extraireIdEnseignant(String valeur) {
        try {
            String idStr = valeur.substring(valeur.indexOf("ID: ") + 4, valeur.indexOf(")"));
            return Integer.parseInt(idStr);
        } catch (Exception e) {
            return -1;
        }
    }
    
    private int extraireIdSalle(String valeur) {
        try {
            String nomSalle = valeur.split(" \\(")[0];
            for (Salle s : salles) {
                if (s.getNomCompletSalle().equals(nomSalle)) {
                    return s.getId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    private int getCapaciteSalle(int salleId) {
        for (Salle s : salles) {
            if (s.getId() == salleId) {
                return s.getCapacite();
            }
        }
        return 0;
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