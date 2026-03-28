package com.univ.scheduler.vue;

import com.univ.scheduler.dao.UtilisateurDAO;
import com.univ.scheduler.modele.Utilisateur;
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
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class VueUtilisateurs {
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private UtilisateurDAO utilisateurDAO;
    private TableView<Utilisateur> tableUtilisateurs;
    private ObservableList<Utilisateur> tousLesUtilisateurs;
    private ComboBox<String> comboFiltreRole;
    private CheckBox checkAfficherInactifs;
    
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
        
        // Barre d'outils
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(20, 0, 20, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        // Filtre par rôle
        Label roleLabel = new Label("Filtrer par rôle:");
        roleLabel.setTextFill(Color.web(TEXTE_SECONDAIRE));
        
        comboFiltreRole = new ComboBox<>();
        comboFiltreRole.getItems().addAll("Tous", "admin", "manager", "enseignant", "etudiant");
        comboFiltreRole.setValue("Tous");
        comboFiltreRole.setStyle("-fx-background-color: " + FOND_CARTE + "; -fx-text-fill: " + TEXTE_PRINCIPAL + ";");
        comboFiltreRole.setOnAction(e -> filtrerUtilisateurs());
        
        // Afficher les inactifs
        checkAfficherInactifs = new CheckBox("Afficher les comptes inactifs");
        checkAfficherInactifs.setTextFill(Color.web(TEXTE_SECONDAIRE));
        checkAfficherInactifs.setSelected(true);
        checkAfficherInactifs.setOnAction(e -> filtrerUtilisateurs());
        
        // Boutons d'action
        Button btnAjouter = new Button("➕ Nouvel utilisateur");
        btnAjouter.setStyle("-fx-background-color: " + COULEUR_SUCCESS + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAjouter.setOnAction(e -> ajouterUtilisateur());
        
        Button btnRafraichir = new Button("🔄 Rafraîchir");
        btnRafraichir.setStyle("-fx-background-color: " + COULEUR_INFO + "; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRafraichir.setOnAction(e -> chargerUtilisateurs());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(roleLabel, comboFiltreRole, checkAfficherInactifs, spacer, btnAjouter, btnRafraichir);
        
        // Tableau des utilisateurs
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
        
        // Colonne Statut (Actif/Inactif) avec indicateur visuel
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
                        btnModifier.setStyle("-fx-background-color: " + COULEUR_INFO + "; -fx-text-fill: white;");
                        btnActiver.setStyle("-fx-background-color: " + COULEUR_SUCCESS + "; -fx-text-fill: white;");
                        btnDesactiver.setStyle("-fx-background-color: " + COULEUR_WARNING + "; -fx-text-fill: white;");
                        btnSupprimer.setStyle("-fx-background-color: " + COULEUR_DANGER + "; -fx-text-fill: white;");
                        
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
        
        tableUtilisateurs.getColumns().addAll(colId, colNom, colUsername, colEmail, colRole, colActif, colActions);
        
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titreLabel, toolbar);
        
        racine.setTop(topBox);
        racine.setCenter(tableUtilisateurs);
    }
    
    private void chargerUtilisateurs() {
        tousLesUtilisateurs.clear();
        tousLesUtilisateurs.addAll(utilisateurDAO.getTousLesUtilisateurs());
        filtrerUtilisateurs();
    }
    
    private void filtrerUtilisateurs() {
        String roleFiltre = comboFiltreRole.getValue();
        boolean afficherInactifs = checkAfficherInactifs.isSelected();
        
        ObservableList<Utilisateur> filtres = FXCollections.observableArrayList();
        
        for (Utilisateur u : tousLesUtilisateurs) {
            boolean correspondRole = roleFiltre.equals("Tous") || u.getRole().equals(roleFiltre);
            boolean correspondActif = afficherInactifs || u.isActif();
            
            if (correspondRole && correspondActif) {
                filtres.add(u);
            }
        }
        
        tableUtilisateurs.setItems(filtres);
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
        grid.add(checkActif, 0, row, 2, 1);
        grid.add(messageLabel, 0, ++row, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnValider) {
                // Vérifications
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