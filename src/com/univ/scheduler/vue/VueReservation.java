package com.univ.scheduler.vue;

import com.univ.scheduler.dao.ReservationDAO;
import com.univ.scheduler.dao.SalleDAO;
import com.univ.scheduler.modele.Reservation;
import com.univ.scheduler.modele.Salle;
import com.univ.scheduler.modele.Utilisateur;
import com.univ.scheduler.utils.NotificationService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VueReservation {
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private ReservationDAO reservationDAO;
    private SalleDAO salleDAO;
    private TableView<Reservation> tableReservations;
    private ComboBox<String> comboPeriode;
    private ObservableList<Reservation> toutesReservations;
    private Label infoLabel;
    private Timeline synchronisationTimer;
    
    public VueReservation(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        this.reservationDAO = new ReservationDAO();
        this.salleDAO = new SalleDAO();
        this.toutesReservations = FXCollections.observableArrayList();
        creerVue();
        demarrerSynchronisationTempsReel();
        chargerReservations();
    }
    
    private void demarrerSynchronisationTempsReel() {
        synchronisationTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            chargerReservations();
        }));
        synchronisationTimer.setCycleCount(Timeline.INDEFINITE);
        synchronisationTimer.play();
    }
    
    private void chargerReservations() {
        Platform.runLater(() -> {
            reservationDAO.mettreAJourStatuts();
            toutesReservations.clear();
            toutesReservations.addAll(reservationDAO.getReservationsParUtilisateur(utilisateurCourant.getId()));
            filtrerReservations();
            infoLabel.setText("Dernière mise à jour: " + 
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
    }
    
    private void creerVue() {
        racine = new BorderPane();
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: #f5f7fa;");
        
        // Titre
        Label titreLabel = new Label("📅 Mes réservations en temps réel");
        titreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        infoLabel = new Label("Mise à jour toutes les 30 secondes");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        // Filtres
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(15, 0, 20, 0));
        toolbar.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label periodeLabel = new Label("Filtrer :");
        periodeLabel.setStyle("-fx-font-weight: bold;");
        
        comboPeriode = new ComboBox<>();
        comboPeriode.getItems().addAll("Toutes", "À venir", "En cours", "Terminées");
        comboPeriode.setValue("Toutes");
        comboPeriode.setStyle("-fx-min-width: 120px;");
        comboPeriode.setOnAction(e -> filtrerReservations());
        
        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnActualiser.setOnAction(e -> chargerReservations());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnNouvelleReservation = new Button("➕ Nouvelle réservation");
        btnNouvelleReservation.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnNouvelleReservation.setOnAction(e -> nouvelleReservation());
        
        toolbar.getChildren().addAll(periodeLabel, comboPeriode, btnActualiser, spacer, btnNouvelleReservation, infoLabel);
        
        // Tableau des réservations
        tableReservations = new TableView<>();
        tableReservations.setPrefHeight(400);
        tableReservations.setPlaceholder(new Label("Aucune réservation pour le moment"));
        tableReservations.setStyle("-fx-font-size: 13px;");
        
        TableColumn<Reservation, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(new PropertyValueFactory<>("nomSalle"));
        colSalle.setPrefWidth(180);
        
        TableColumn<Reservation, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setPrefWidth(150);
        
        TableColumn<Reservation, LocalDate> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setPrefWidth(100);
        
        TableColumn<Reservation, String> colHoraire = new TableColumn<>("Horaire");
        colHoraire.setCellValueFactory(new PropertyValueFactory<>("plageHoraire"));
        colHoraire.setPrefWidth(150);
        
        TableColumn<Reservation, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);
        colStatut.setCellFactory(column -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "confirmée":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            break;
                        case "annulée":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "terminée":
                            setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        
        tableReservations.getColumns().addAll(colSalle, colTitre, colDate, colHoraire, colStatut);
        
        // Bouton Annuler
        Button btnAnnuler = new Button("🗑️ Annuler la réservation");
        btnAnnuler.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAnnuler.setOnAction(e -> annulerReservation());
        
        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.getChildren().add(btnAnnuler);
        
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titreLabel, toolbar);
        
        racine.setTop(topBox);
        racine.setCenter(tableReservations);
        racine.setBottom(bottomBox);
    }
    
    private void filtrerReservations() {
        String filtre = comboPeriode.getValue();
        LocalDate aujourdHui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();
        
        ObservableList<Reservation> filtrees = FXCollections.observableArrayList();
        
        for (Reservation r : toutesReservations) {
            switch (filtre) {
                case "Toutes":
                    filtrees.add(r);
                    break;
                case "À venir":
                    if (r.getDate().isAfter(aujourdHui) || 
                        (r.getDate().equals(aujourdHui) && r.getHeureDebut().isAfter(maintenant))) {
                        filtrees.add(r);
                    }
                    break;
                case "En cours":
                    if (r.getDate().equals(aujourdHui) && 
                        r.getHeureDebut().isBefore(maintenant) && 
                        r.getHeureFin().isAfter(maintenant)) {
                        filtrees.add(r);
                    }
                    break;
                case "Terminées":
                    if (r.getDate().isBefore(aujourdHui) || 
                        (r.getDate().equals(aujourdHui) && r.getHeureFin().isBefore(maintenant))) {
                        filtrees.add(r);
                    }
                    break;
            }
        }
        
        tableReservations.setItems(filtrees);
    }
    
    private void nouvelleReservation() {
        // Vérifier que l'utilisateur est un enseignant
        if (!"enseignant".equals(utilisateurCourant.getRole())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Accès refusé");
            alert.setHeaderText("Réservation impossible");
            alert.setContentText("Seuls les enseignants peuvent effectuer des réservations.");
            alert.showAndWait();
            return;
        }
        
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle réservation");
        dialog.setHeaderText("Réserver une salle");
        
        ButtonType btnValider = new ButtonType("Réserver", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(500);
        
        List<Salle> salles = salleDAO.getToutesLesSalles();
        ComboBox<String> comboSalle = new ComboBox<>();
        for (Salle s : salles) {
            if (!s.getType().equals("RESTAURANT") && !s.getType().equals("BIBLIOTHEQUE")) {
                comboSalle.getItems().add(s.getNomCompletSalle());
            }
        }
        if (!comboSalle.getItems().isEmpty()) {
            comboSalle.setValue(comboSalle.getItems().get(0));
        }
        comboSalle.setPrefWidth(300);
        
        TextField champTitre = new TextField();
        champTitre.setPromptText("Titre de la réservation");
        
        TextArea champDescription = new TextArea();
        champDescription.setPromptText("Description (optionnel)");
        champDescription.setPrefRowCount(3);
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now()));
            }
        });
        
        ComboBox<String> comboHeureDebut = new ComboBox<>();
        for (int h = 8; h <= 20; h++) {
            comboHeureDebut.getItems().add(String.format("%02d:00", h));
            comboHeureDebut.getItems().add(String.format("%02d:30", h));
        }
        comboHeureDebut.setValue("08:00");
        
        ComboBox<String> comboHeureFin = new ComboBox<>();
        for (int h = 9; h <= 21; h++) {
            comboHeureFin.getItems().add(String.format("%02d:00", h));
            comboHeureFin.getItems().add(String.format("%02d:30", h));
        }
        comboHeureFin.setValue("10:00");
        
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        Button btnVerifier = new Button("Vérifier disponibilité");
        btnVerifier.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnVerifier.setOnAction(e -> {
            String salleNom = comboSalle.getValue();
            Salle salle = trouverSalleParNom(salleNom, salles);
            
            if (salle != null) {
                LocalDate date = datePicker.getValue();
                LocalTime debut = LocalTime.parse(comboHeureDebut.getValue());
                LocalTime fin = LocalTime.parse(comboHeureFin.getValue());
                
                if (reservationDAO.estSalleDisponible(salle.getId(), date, debut, fin)) {
                    messageLabel.setText("✅ Salle disponible !");
                    messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } else {
                    messageLabel.setText("❌ Salle déjà réservée à ce créneau !");
                    messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }
            }
        });
        
        int row = 0;
        grid.add(new Label("Salle:"), 0, row);
        grid.add(comboSalle, 1, row++);
        grid.add(new Label("Titre:"), 0, row);
        grid.add(champTitre, 1, row++);
        grid.add(new Label("Date:"), 0, row);
        grid.add(datePicker, 1, row++);
        grid.add(new Label("Heure début:"), 0, row);
        grid.add(comboHeureDebut, 1, row++);
        grid.add(new Label("Heure fin:"), 0, row);
        grid.add(comboHeureFin, 1, row++);
        grid.add(new Label("Description:"), 0, row);
        grid.add(champDescription, 1, row++);
        grid.add(btnVerifier, 0, row, 2, 1);
        grid.add(messageLabel, 0, ++row, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                String salleNom = comboSalle.getValue();
                Salle salle = trouverSalleParNom(salleNom, salles);
                
                if (salle != null) {
                    LocalDate date = datePicker.getValue();
                    LocalTime debut = LocalTime.parse(comboHeureDebut.getValue());
                    LocalTime fin = LocalTime.parse(comboHeureFin.getValue());
                    
                    if (!reservationDAO.estSalleDisponible(salle.getId(), date, debut, fin)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText("Salle non disponible");
                        alert.setContentText("Cette salle est déjà réservée pour ce créneau.");
                        alert.showAndWait();
                        return null;
                    }
                    
                    if (debut.isAfter(fin) || debut.equals(fin)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText("Horaire invalide");
                        alert.setContentText("L'heure de fin doit être après l'heure de début.");
                        alert.showAndWait();
                        return null;
                    }
                    
                    Reservation reservation = new Reservation();
                    reservation.setSalleId(salle.getId());
                    reservation.setUtilisateurId(utilisateurCourant.getId());
                    reservation.setTitre(champTitre.getText());
                    reservation.setDescription(champDescription.getText());
                    reservation.setDate(date);
                    reservation.setHeureDebut(debut);
                    reservation.setHeureFin(fin);
                    reservation.setStatut("confirmée");
                    reservation.setNomSalle(salle.getNomCompletSalle());
                    
                    return reservation;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(reservation -> {
            if (reservationDAO.ajouterReservation(reservation)) {
                NotificationService notifications = NotificationService.getInstance();
                notifications.programmerRappelDebutReservation(reservation, utilisateurCourant);
                notifications.programmerRappelFinReservation(reservation, utilisateurCourant);
                
                String sujet = "✅ Confirmation de réservation - " + reservation.getTitre();
                String contenu = String.format(
                    "Bonjour %s,\n\n" +
                    "Votre réservation a été confirmée :\n\n" +
                    "Titre: %s\n" +
                    "Salle: %s\n" +
                    "Date: %s\n" +
                    "Horaire: %s - %s\n\n" +
                    "L'équipe UNIV-SCHEDULER",
                    utilisateurCourant.getPrenom(),
                    reservation.getTitre(),
                    reservation.getNomSalle(),
                    reservation.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    reservation.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")),
                    reservation.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm"))
                );
                
                notifications.envoyerNotificationEmail(utilisateurCourant, sujet, contenu);
                
                chargerReservations();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Réservation confirmée");
                alert.setContentText("Votre réservation a été enregistrée.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Échec de la réservation");
                alert.setContentText("La réservation n'a pas pu être effectuée.");
                alert.showAndWait();
            }
        });
    }
    
    private Salle trouverSalleParNom(String nomComplet, List<Salle> salles) {
        for (Salle s : salles) {
            if (s.getNomCompletSalle().equals(nomComplet)) {
                return s;
            }
        }
        return null;
    }
    
    private void annulerReservation() {
        Reservation selection = tableReservations.getSelectionModel().getSelectedItem();
        if (selection == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucune réservation sélectionnée");
            alert.setContentText("Veuillez sélectionner une réservation à annuler.");
            alert.showAndWait();
            return;
        }
        
        if (selection.getUtilisateurId() != utilisateurCourant.getId()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Annulation impossible");
            alert.setContentText("Vous ne pouvez annuler que vos propres réservations.");
            alert.showAndWait();
            return;
        }
        
        LocalDate aujourdHui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();
        
        if (selection.getDate().isBefore(aujourdHui) || 
            (selection.getDate().equals(aujourdHui) && selection.getHeureFin().isBefore(maintenant))) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Annulation impossible");
            alert.setContentText("Cette réservation est déjà terminée.");
            alert.showAndWait();
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Annuler la réservation");
        confirmation.setContentText("Voulez-vous vraiment annuler cette réservation ?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (reservationDAO.annulerReservation(selection.getId())) {
                    chargerReservations();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText("Réservation annulée");
                    alert.setContentText("La réservation a été annulée.");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Échec de l'annulation");
                    alert.setContentText("La réservation n'a pas pu être annulée.");
                    alert.showAndWait();
                }
            }
        });
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