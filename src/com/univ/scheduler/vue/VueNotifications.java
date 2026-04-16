package com.univ.scheduler.vue;

import com.univ.scheduler.dao.SignalementDAO;
import com.univ.scheduler.modele.Signalement;
import com.univ.scheduler.modele.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class VueNotifications {
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private SignalementDAO signalementDAO;
    private TableView<Signalement> tableSignalements;
    private ObservableList<Signalement> tousLesSignalements;
    private Label infoLabel;
    
    public VueNotifications(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        this.signalementDAO = new SignalementDAO();
        this.tousLesSignalements = FXCollections.observableArrayList();
        creerVue();
        chargerSignalements();
    }
    
    private void creerVue() {
        racine = new BorderPane();
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: #f5f7fa;");
        
        // Titre
        Label titreLabel = new Label("🔔 Signalements des enseignants");
        titreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        infoLabel = new Label("Liste des problèmes signalés par les enseignants");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        // Bouton actualiser
        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnActualiser.setOnAction(e -> chargerSignalements());
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(titreLabel, btnActualiser);
        
        // Tableau des signalements
        tableSignalements = new TableView<>();
        tableSignalements.setPrefHeight(500);
        tableSignalements.setStyle("-fx-font-size: 13px;");
        
        TableColumn<Signalement, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);
        
        TableColumn<Signalement, String> colEnseignant = new TableColumn<>("Enseignant");
        colEnseignant.setCellValueFactory(new PropertyValueFactory<>("enseignant"));
        colEnseignant.setPrefWidth(150);
        
        TableColumn<Signalement, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(180);
        
        TableColumn<Signalement, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salle"));
        colSalle.setPrefWidth(100);
        
        TableColumn<Signalement, String> colDescription = new TableColumn<>("Description");
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDescription.setPrefWidth(300);
        
        TableColumn<Signalement, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateFormatee"));
        colDate.setPrefWidth(130);
        
        TableColumn<Signalement, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);
        colStatut.setCellFactory(column -> new TableCell<Signalement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.equals("en_attente") ? "🔴 En attente" : "🟢 Traité");
                    if (item.equals("en_attente")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        tableSignalements.getColumns().addAll(colId, colEnseignant, colEmail, colSalle, colDescription, colDate, colStatut);
        
        // Boutons d'action
        Button btnMarquerTraite = new Button("✅ Marquer comme traité");
        btnMarquerTraite.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnMarquerTraite.setOnAction(e -> marquerCommeTraite());
        
        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSupprimer.setOnAction(e -> supprimerSignalement());
        
        HBox boutonsBox = new HBox(10);
        boutonsBox.setPadding(new Insets(20, 0, 0, 0));
        boutonsBox.getChildren().addAll(btnMarquerTraite, btnSupprimer);
        
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(headerBox, infoLabel);
        
        racine.setTop(topBox);
        racine.setCenter(tableSignalements);
        racine.setBottom(boutonsBox);
    }
    
    private void chargerSignalements() {
        tousLesSignalements.clear();
        tousLesSignalements.addAll(signalementDAO.getTousLesSignalements());
        tableSignalements.setItems(tousLesSignalements);
        
        int nbNonTraites = signalementDAO.compterNonTraites();
        if (nbNonTraites > 0) {
            infoLabel.setText("📢 " + nbNonTraites + " signalement(s) en attente de traitement");
            infoLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            infoLabel.setText("✅ Aucun signalement en attente");
            infoLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }
    
    private void marquerCommeTraite() {
        Signalement selection = tableSignalements.getSelectionModel().getSelectedItem();
        if (selection == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucun signalement sélectionné");
            alert.setContentText("Veuillez sélectionner un signalement à traiter.");
            alert.showAndWait();
            return;
        }
        
        if (selection.getStatut().equals("traite")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Information");
            alert.setHeaderText("Déjà traité");
            alert.setContentText("Ce signalement est déjà marqué comme traité.");
            alert.showAndWait();
            return;
        }
        
        if (signalementDAO.marquerCommeTraite(selection.getId())) {
            chargerSignalements();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Signalement traité");
            alert.setContentText("Le signalement a été marqué comme traité.");
            alert.showAndWait();
        }
    }
    
    private void supprimerSignalement() {
        Signalement selection = tableSignalements.getSelectionModel().getSelectedItem();
        if (selection == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucun signalement sélectionné");
            alert.setContentText("Veuillez sélectionner un signalement à supprimer.");
            alert.showAndWait();
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le signalement");
        confirmation.setContentText("Voulez-vous vraiment supprimer ce signalement ?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (signalementDAO.supprimerSignalement(selection.getId())) {
                    chargerSignalements();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText("Signalement supprimé");
                    alert.setContentText("Le signalement a été supprimé.");
                    alert.showAndWait();
                }
            }
        });
    }
    
    public BorderPane getRacine() {
        return racine;
    }
}