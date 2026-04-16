package com.univ.scheduler.vue;

import com.univ.scheduler.dao.UtilisateurDAO;
import com.univ.scheduler.modele.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class VueUtilisateurs {
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private UtilisateurDAO utilisateurDAO;
    private TableView<Utilisateur> tableUtilisateurs;
    private ObservableList<Utilisateur> tousLesUtilisateurs;
    private FilteredList<Utilisateur> utilisateursFiltres;
    
    // Composants de recherche
    private TextField champRecherche;
    private ComboBox<String> comboFiltreRole;
    private CheckBox checkAfficherInactifs;
    private Label infoLabel;
    
    // Thème
    private final String FOND_PRINCIPAL = "#f5f7fa";
    private final String FOND_CARTE = "#ffffff";
    private final String TEXTE_PRINCIPAL = "#2c3e50";
    private final String TEXTE_SECONDAIRE = "#7f8c8d";
    private final String COULEUR_PRIMAIRE = "#3498db";
    private final String COULEUR_SUCCESS = "#2ecc71";
    private final String COULEUR_WARNING = "#f39c12";
    private final String COULEUR_DANGER = "#e74c3c";
    private final String COULEUR_INFO = "#3498db";
    
    public VueUtilisateurs(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        this.utilisateurDAO = new UtilisateurDAO();
        this.tousLesUtilisateurs = FXCollections.observableArrayList();
        this.racine = new BorderPane();
        creerVue();
        chargerUtilisateurs();
    }
    
    private void creerVue() {
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        // Titre
        Label titreLabel = new Label("👥 Gestion des utilisateurs");
        titreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + TEXTE_PRINCIPAL + ";");
        
        // ============================================
        // BARRE DE RECHERCHE
        // ============================================
        HBox searchBar = new HBox(10);
        searchBar.setPadding(new Insets(10, 0, 15, 0));
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        Label rechercheLabel = new Label("🔍 Rechercher :");
        rechercheLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TEXTE_SECONDAIRE + ";");
        
        champRecherche = new TextField();
        champRecherche.setPromptText("Nom, prénom, email ou nom d'utilisateur...");
        champRecherche.setPrefWidth(300);
        champRecherche.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 8 12;");
        champRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerUtilisateurs());
        
        // Bouton reset recherche
        Button btnResetRecherche = new Button("✖");
        btnResetRecherche.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        btnResetRecherche.setOnAction(e -> {
            champRecherche.clear();
            filtrerUtilisateurs();
        });
        btnResetRecherche.setTooltip(new Tooltip("Effacer la recherche"));
        
        searchBar.getChildren().addAll(rechercheLabel, champRecherche, btnResetRecherche);
        
        // ============================================
        // FILTRES
        // ============================================
        HBox filtresBox = new HBox(20);
        filtresBox.setPadding(new Insets(5, 0, 15, 0));
        filtresBox.setAlignment(Pos.CENTER_LEFT);
        
        // Filtre par rôle
        Label roleLabel = new Label("Rôle :");
        roleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + TEXTE_SECONDAIRE + ";");
        
        comboFiltreRole = new ComboBox<>();
        comboFiltreRole.getItems().addAll("Tous", "admin", "manager", "enseignant", "etudiant");
        comboFiltreRole.setValue("Tous");
        comboFiltreRole.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 5;");
        comboFiltreRole.setOnAction(e -> filtrerUtilisateurs());
        
        // Afficher les inactifs
        checkAfficherInactifs = new CheckBox("Afficher les comptes inactifs");
        checkAfficherInactifs.setStyle("-fx-text-fill: " + TEXTE_SECONDAIRE + ";");
        checkAfficherInactifs.setSelected(true);
        checkAfficherInactifs.setOnAction(e -> filtrerUtilisateurs());
        
        // Label info résultats
        infoLabel = new Label("");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + COULEUR_INFO + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        filtresBox.getChildren().addAll(roleLabel, comboFiltreRole, checkAfficherInactifs, spacer, infoLabel);
        
        // ============================================
        // BARRE D'OUTILS (Boutons d'action)
        // ============================================
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 0, 20, 0));
        
        Button btnAjouter = new Button("➕ Nouvel utilisateur");
        btnAjouter.setStyle("-fx-background-color: " + COULEUR_SUCCESS + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAjouter.setOnAction(e -> ajouterUtilisateur());
        
        Button btnRafraichir = new Button("🔄 Rafraîchir");
        btnRafraichir.setStyle("-fx-background-color: " + COULEUR_INFO + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnRafraichir.setOnAction(e -> chargerUtilisateurs());
        
        Button btnExporter = new Button("📊 Exporter CSV");
        btnExporter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnExporter.setOnAction(e -> exporterCSV());
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(btnAjouter, btnRafraichir, spacer2, btnExporter);
        
        // ============================================
        // TABLEAU DES UTILISATEURS
        // ============================================
        tableUtilisateurs = new TableView<>();
        tableUtilisateurs.setPrefHeight(500);
        tableUtilisateurs.setStyle("-fx-font-size: 13px; -fx-background-color: " + FOND_CARTE + ";");
        
        // Colonne ID
        TableColumn<Utilisateur, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);
        
        // Colonne Nom complet
        TableColumn<Utilisateur, String> colNom = new TableColumn<>("Nom complet");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        colNom.setPrefWidth(150);
        
        // Colonne Nom d'utilisateur
        TableColumn<Utilisateur, String> colUsername = new TableColumn<>("Username");
        colUsername.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
        colUsername.setPrefWidth(120);
        
        // Colonne Email
        TableColumn<Utilisateur, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);
        
        // Colonne Rôle avec couleur
        TableColumn<Utilisateur, String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setPrefWidth(100);
        colRole.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Utilisateur, String> call(TableColumn<Utilisateur, String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            switch (item) {
                                case "admin":
                                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                                    break;
                                case "manager":
                                    setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                                    break;
                                case "enseignant":
                                    setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                                    break;
                                case "etudiant":
                                    setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                                    break;
                                default:
                                    setStyle("");
                            }
                        }
                    }
                };
            }
        });
        
        // Colonne Statut (Actif/Inactif)
        TableColumn<Utilisateur, Boolean> colActif = new TableColumn<>("Statut");
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
        colActif.setPrefWidth(80);
        colActif.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Utilisateur, Boolean> call(TableColumn<Utilisateur, Boolean> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            if (item) {
                                setText("🟢 Actif");
                                setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            } else {
                                setText("🔴 Inactif");
                                setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            }
                        }
                    }
                };
            }
        });
        
        // Colonne Classe (pour étudiants)
        TableColumn<Utilisateur, String> colClasse = new TableColumn<>("Classe");
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colClasse.setPrefWidth(100);
        
        // Colonne Actions
        TableColumn<Utilisateur, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(200);
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Utilisateur, Void> call(TableColumn<Utilisateur, Void> param) {
                return new TableCell<>() {
                    private final Button btnModifier = new Button("✏️");
                    private final Button btnActiver = new Button("🔓");
                    private final Button btnDesactiver = new Button("🔒");
                    private final Button btnSupprimer = new Button("🗑️");
                    private final HBox pane = new HBox(5);
                    
                    {
                        btnModifier.setStyle("-fx-background-color: " + COULEUR_INFO + "; -fx-text-fill: white; -fx-cursor: hand;");
                        btnActiver.setStyle("-fx-background-color: " + COULEUR_SUCCESS + "; -fx-text-fill: white; -fx-cursor: hand;");
                        btnDesactiver.setStyle("-fx-background-color: " + COULEUR_WARNING + "; -fx-text-fill: white; -fx-cursor: hand;");
                        btnSupprimer.setStyle("-fx-background-color: " + COULEUR_DANGER + "; -fx-text-fill: white; -fx-cursor: hand;");
                        
                        btnModifier.setOnAction(e -> modifierUtilisateur(getTableView().getItems().get(getIndex())));
                        btnActiver.setOnAction(e -> activerUtilisateur(getTableView().getItems().get(getIndex())));
                        btnDesactiver.setOnAction(e -> desactiverUtilisateur(getTableView().getItems().get(getIndex())));
                        btnSupprimer.setOnAction(e -> supprimerUtilisateur(getTableView().getItems().get(getIndex())));
                        
                        pane.getChildren().addAll(btnModifier, btnActiver, btnDesactiver, btnSupprimer);
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Utilisateur user = getTableView().getItems().get(getIndex());
                            btnActiver.setDisable(user.isActif());
                            btnDesactiver.setDisable(!user.isActif());
                            setGraphic(pane);
                        }
                    }
                };
            }
        });
        
        tableUtilisateurs.getColumns().addAll(colId, colNom, colUsername, colEmail, colRole, colActif, colClasse, colActions);
        
        // Assemblage
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titreLabel, searchBar, filtresBox, toolbar);
        
        racine.setTop(topBox);
        racine.setCenter(tableUtilisateurs);
    }
    
    private void chargerUtilisateurs() {
        tousLesUtilisateurs.clear();
        tousLesUtilisateurs.addAll(utilisateurDAO.getTousLesUtilisateurs());
        filtrerUtilisateurs();
    }
    
    private void filtrerUtilisateurs() {
        String recherche = champRecherche.getText().toLowerCase();
        String roleFiltre = comboFiltreRole.getValue();
        boolean afficherInactifs = checkAfficherInactifs.isSelected();
        
        ObservableList<Utilisateur> filtres = FXCollections.observableArrayList();
        
        for (Utilisateur u : tousLesUtilisateurs) {
            boolean correspond = true;
            
            // Filtre par recherche (nom, prénom, email, username)
            if (!recherche.isEmpty()) {
                boolean nomMatch = (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(recherche));
                boolean prenomMatch = (u.getNom() != null && u.getNom().toLowerCase().contains(recherche));
                boolean emailMatch = (u.getEmail() != null && u.getEmail().toLowerCase().contains(recherche));
                boolean usernameMatch = (u.getNomUtilisateur() != null && u.getNomUtilisateur().toLowerCase().contains(recherche));
                
                if (!(nomMatch || prenomMatch || emailMatch || usernameMatch)) {
                    correspond = false;
                }
            }
            
            // Filtre par rôle
            if (!roleFiltre.equals("Tous") && !u.getRole().equals(roleFiltre)) {
                correspond = false;
            }
            
            // Filtre par statut actif/inactif
            if (!afficherInactifs && !u.isActif()) {
                correspond = false;
            }
            
            if (correspond) {
                filtres.add(u);
            }
        }
        
        tableUtilisateurs.setItems(filtres);
        
        // Mettre à jour le label d'information
        infoLabel.setText(filtres.size() + " utilisateur(s) trouvé(s)");
    }
    
    private void ajouterUtilisateur() {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Création d'un nouvel utilisateur");
        
        ButtonType btnValider = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: " + FOND_CARTE + ";");
        
        TextField champPrenom = new TextField();
        champPrenom.setPromptText("Prénom");
        
        TextField champNom = new TextField();
        champNom.setPromptText("Nom");
        
        TextField champUsername = new TextField();
        champUsername.setPromptText("Nom d'utilisateur");
        
        PasswordField champMotDePasse = new PasswordField();
        champMotDePasse.setPromptText("Mot de passe");
        
        TextField champEmail = new TextField();
        champEmail.setPromptText("Email");
        
        ComboBox<String> comboRole = new ComboBox<>();
        comboRole.getItems().addAll("admin", "manager", "enseignant", "etudiant");
        comboRole.setValue("etudiant");
        
        TextField champClasse = new TextField();
        champClasse.setPromptText("Classe (pour étudiant)");
        champClasse.setVisible(false);
        
        comboRole.setOnAction(e -> {
            champClasse.setVisible(comboRole.getValue().equals("etudiant"));
        });
        
        CheckBox checkActif = new CheckBox("Actif");
        checkActif.setSelected(true);
        
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.web(COULEUR_DANGER));
        
        int row = 0;
        grid.add(new Label("Prénom:"), 0, row);
        grid.add(champPrenom, 1, row++);
        grid.add(new Label("Nom:"), 0, row);
        grid.add(champNom, 1, row++);
        grid.add(new Label("Nom d'utilisateur:"), 0, row);
        grid.add(champUsername, 1, row++);
        grid.add(new Label("Mot de passe:"), 0, row);
        grid.add(champMotDePasse, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(champEmail, 1, row++);
        grid.add(new Label("Rôle:"), 0, row);
        grid.add(comboRole, 1, row++);
        grid.add(new Label("Classe:"), 0, row);
        grid.add(champClasse, 1, row++);
        grid.add(checkActif, 0, row, 2, 1);
        grid.add(messageLabel, 0, ++row, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                if (champPrenom.getText().isEmpty() || champNom.getText().isEmpty() || 
                    champUsername.getText().isEmpty() || champMotDePasse.getText().isEmpty() || 
                    champEmail.getText().isEmpty()) {
                    messageLabel.setText("❌ Tous les champs sont obligatoires");
                    return null;
                }
                
                if (utilisateurDAO.usernameExiste(champUsername.getText())) {
                    messageLabel.setText("❌ Ce nom d'utilisateur existe déjà");
                    return null;
                }
                
                if (utilisateurDAO.emailExiste(champEmail.getText())) {
                    messageLabel.setText("❌ Cet email existe déjà");
                    return null;
                }
                
                Utilisateur u = new Utilisateur();
                u.setPrenom(champPrenom.getText());
                u.setNom(champNom.getText());
                u.setNomUtilisateur(champUsername.getText());
                u.setMotDePasse(champMotDePasse.getText());
                u.setEmail(champEmail.getText());
                u.setRole(comboRole.getValue());
                u.setActif(checkActif.isSelected());
                if (comboRole.getValue().equals("etudiant")) {
                    u.setClasse(champClasse.getText());
                }
                return u;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(utilisateur -> {
            if (utilisateurDAO.ajouterUtilisateur(utilisateur)) {
                chargerUtilisateurs();
                showAlert("Succès", "Utilisateur créé avec succès", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Échec de la création", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void modifierUtilisateur(Utilisateur utilisateur) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Modifier un utilisateur");
        dialog.setHeaderText("Modification de " + utilisateur.getNomComplet());
        
        ButtonType btnValider = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField champPrenom = new TextField(utilisateur.getPrenom());
        TextField champNom = new TextField(utilisateur.getNom());
        TextField champUsername = new TextField(utilisateur.getNomUtilisateur());
        PasswordField champMotDePasse = new PasswordField();
        champMotDePasse.setPromptText("Laisser vide pour ne pas changer");
        TextField champEmail = new TextField(utilisateur.getEmail());
        
        ComboBox<String> comboRole = new ComboBox<>();
        comboRole.getItems().addAll("admin", "manager", "enseignant", "etudiant");
        comboRole.setValue(utilisateur.getRole());
        
        TextField champClasse = new TextField(utilisateur.getClasse());
        champClasse.setVisible(utilisateur.getRole().equals("etudiant"));
        
        comboRole.setOnAction(e -> {
            champClasse.setVisible(comboRole.getValue().equals("etudiant"));
        });
        
        int row = 0;
        grid.add(new Label("Prénom:"), 0, row);
        grid.add(champPrenom, 1, row++);
        grid.add(new Label("Nom:"), 0, row);
        grid.add(champNom, 1, row++);
        grid.add(new Label("Nom d'utilisateur:"), 0, row);
        grid.add(champUsername, 1, row++);
        grid.add(new Label("Nouveau mot de passe:"), 0, row);
        grid.add(champMotDePasse, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(champEmail, 1, row++);
        grid.add(new Label("Rôle:"), 0, row);
        grid.add(comboRole, 1, row++);
        grid.add(new Label("Classe:"), 0, row);
        grid.add(champClasse, 1, row++);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                utilisateur.setPrenom(champPrenom.getText());
                utilisateur.setNom(champNom.getText());
                utilisateur.setNomUtilisateur(champUsername.getText());
                if (!champMotDePasse.getText().isEmpty()) {
                    utilisateur.setMotDePasse(champMotDePasse.getText());
                }
                utilisateur.setEmail(champEmail.getText());
                utilisateur.setRole(comboRole.getValue());
                if (comboRole.getValue().equals("etudiant")) {
                    utilisateur.setClasse(champClasse.getText());
                }
                return utilisateur;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(u -> {
            if (utilisateurDAO.modifierUtilisateur(u)) {
                chargerUtilisateurs();
                showAlert("Succès", "Utilisateur modifié avec succès", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Échec de la modification", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void activerUtilisateur(Utilisateur utilisateur) {
        if (utilisateurDAO.setActif(utilisateur.getId(), true)) {
            utilisateur.setActif(true);
            tableUtilisateurs.refresh();
            showAlert("Succès", "Compte activé", Alert.AlertType.INFORMATION);
        }
    }
    
    private void desactiverUtilisateur(Utilisateur utilisateur) {
        if (utilisateur.getId() == utilisateurCourant.getId()) {
            showAlert("Erreur", "Vous ne pouvez pas désactiver votre propre compte", Alert.AlertType.ERROR);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Désactiver le compte");
        confirm.setContentText("Voulez-vous vraiment désactiver le compte de " + utilisateur.getNomComplet() + " ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (utilisateurDAO.setActif(utilisateur.getId(), false)) {
                    utilisateur.setActif(false);
                    tableUtilisateurs.refresh();
                    showAlert("Succès", "Compte désactivé", Alert.AlertType.INFORMATION);
                }
            }
        });
    }
    
    private void supprimerUtilisateur(Utilisateur utilisateur) {
        if (utilisateur.getId() == utilisateurCourant.getId()) {
            showAlert("Erreur", "Vous ne pouvez pas supprimer votre propre compte", Alert.AlertType.ERROR);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer définitivement");
        confirm.setContentText("ATTENTION ! Voulez-vous vraiment SUPPRIMER définitivement " + 
                              utilisateur.getNomComplet() + " ?\nCette action est irréversible.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (utilisateurDAO.supprimerUtilisateur(utilisateur.getId())) {
                    chargerUtilisateurs();
                    showAlert("Succès", "Utilisateur supprimé", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Échec de la suppression", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    private void exporterCSV() {
        ObservableList<Utilisateur> utilisateurs = tableUtilisateurs.getItems();
        if (utilisateurs.isEmpty()) {
            showAlert("Attention", "Aucun utilisateur à exporter", Alert.AlertType.WARNING);
            return;
        }
        
        // Créer un export simple
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Nom d'utilisateur;Prénom;Nom;Email;Rôle;Actif;Classe;Filière\n");
        
        for (Utilisateur u : utilisateurs) {
            sb.append(u.getId()).append(";")
              .append(u.getNomUtilisateur()).append(";")
              .append(u.getPrenom()).append(";")
              .append(u.getNom()).append(";")
              .append(u.getEmail()).append(";")
              .append(u.getRole()).append(";")
              .append(u.isActif() ? "Oui" : "Non").append(";")
              .append(u.getClasse() != null ? u.getClasse() : "").append(";")
              .append(u.getFiliere() != null ? u.getFiliere() : "").append("\n");
        }
        
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Exporter les utilisateurs");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        fileChooser.setInitialFileName("utilisateurs_" + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        
        java.io.File file = fileChooser.showSaveDialog(racine.getScene().getWindow());
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                writer.write(sb.toString());
                showAlert("Succès", "Export CSV réussi : " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de l'export : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public BorderPane getRacine() {
        return racine;
    }
}