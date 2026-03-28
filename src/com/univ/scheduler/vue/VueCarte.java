package com.univ.scheduler.vue;

import com.univ.scheduler.dao.ReservationDAO;
import com.univ.scheduler.dao.SalleDAO;
import com.univ.scheduler.modele.Reservation;
import com.univ.scheduler.modele.Salle;
import com.univ.scheduler.modele.Utilisateur;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.layout.Pane;
import javafx.scene.control.Tooltip;
import javafx.application.Platform;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class VueCarte {
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private SalleDAO salleDAO;
    private ReservationDAO reservationDAO;
    private Map<String, List<Salle>> sallesParBatiment;
    private Map<String, Integer> capaciteBatiments;
    private Label infoLabel;
    private VBox infoBox;
    private Label nomBatimentLabel;
    private ListView<String> listeSalles;
    private Map<String, List<Circle>> cerclesSallesParBatiment;
    private Map<String, List<Salle>> sallesReelles;
    private Timeline synchronisationTimer;
    
    // Coordonnées des bâtiments sur la carte (x, y, largeur, hauteur)
    private final Map<String, int[]> positionsBatiments = new HashMap<>();
    
    public VueCarte(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        this.salleDAO = new SalleDAO();
        this.reservationDAO = new ReservationDAO();
        this.sallesParBatiment = new HashMap<>();
        this.capaciteBatiments = new HashMap<>();
        this.cerclesSallesParBatiment = new HashMap<>();
        this.sallesReelles = new HashMap<>();
        
        // Définir les positions des bâtiments
        positionsBatiments.put("Bâtiment A", new int[]{80, 70, 130, 160});
        positionsBatiments.put("Bâtiment B", new int[]{280, 50, 130, 180});
        positionsBatiments.put("Bâtiment C", new int[]{480, 90, 130, 140});
        positionsBatiments.put("Amphithéâtres", new int[]{100, 280, 200, 90});
        positionsBatiments.put("Bibliothèque", new int[]{400, 270, 180, 100});
        positionsBatiments.put("Restaurants", new int[]{200, 400, 250, 80});
        
        chargerSallesReelles();
        creerVue();
        demarrerSynchronisationTempsReel();
    }
    
    private void chargerSallesReelles() {
        List<Salle> toutesSalles = salleDAO.getToutesLesSalles();
        
        sallesReelles.clear();
        // Initialiser tous les bâtiments même s'ils n'ont pas de salles
        sallesReelles.put("Bâtiment A", new ArrayList<>());
        sallesReelles.put("Bâtiment B", new ArrayList<>());
        sallesReelles.put("Bâtiment C", new ArrayList<>());
        sallesReelles.put("Amphithéâtres", new ArrayList<>());
        sallesReelles.put("Bibliothèque", new ArrayList<>());
        sallesReelles.put("Restaurants", new ArrayList<>());
        
        // Remplir avec les vraies salles
        for (Salle salle : toutesSalles) {
            String nomBatiment = salle.getNomBatiment();
            if (sallesReelles.containsKey(nomBatiment)) {
                sallesReelles.get(nomBatiment).add(salle);
            }
        }
    }
    
    private void demarrerSynchronisationTempsReel() {
        synchronisationTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            actualiserStatutsSalles();
        }));
        synchronisationTimer.setCycleCount(Timeline.INDEFINITE);
        synchronisationTimer.play();
    }
    
    private void actualiserStatutsSalles() {
        Platform.runLater(() -> {
            LocalDate aujourdHui = LocalDate.now();
            LocalTime maintenant = LocalTime.now();
            
            for (Map.Entry<String, List<Circle>> entry : cerclesSallesParBatiment.entrySet()) {
                String nomBatiment = entry.getKey();
                List<Circle> cercles = entry.getValue();
                List<Salle> salles = sallesReelles.get(nomBatiment);
                
                if (salles == null || cercles == null || salles.size() != cercles.size()) continue;
                
                for (int i = 0; i < salles.size() && i < cercles.size(); i++) {
                    Salle salle = salles.get(i);
                    Circle cercle = cercles.get(i);
                    
                    boolean estOccupee = estSalleOccupee(salle.getId(), aujourdHui, maintenant);
                    
                    if (estOccupee) {
                        cercle.setFill(Color.rgb(231, 76, 60));
                        Tooltip tooltip = new Tooltip(salle.getNumeroSalle() + " - " + 
                                                      salle.getType() + " - 🔴 OCCUPÉE");
                        Tooltip.install(cercle, tooltip);
                    } else {
                        cercle.setFill(Color.rgb(46, 204, 113));
                        Tooltip tooltip = new Tooltip(salle.getNumeroSalle() + " - " + 
                                                      salle.getType() + " - 🟢 LIBRE");
                        Tooltip.install(cercle, tooltip);
                    }
                }
            }
            
            if (nomBatimentLabel != null && !nomBatimentLabel.getText().equals("Aucun bâtiment sélectionné")) {
                afficherSallesBatiment(nomBatimentLabel.getText());
            }
            
            infoLabel.setText("Dernière mise à jour: " + 
                LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
    }
    
    private boolean estSalleOccupee(int salleId, LocalDate date, LocalTime heure) {
        return reservationDAO.estSalleOccupeeMaintenant(salleId, date, heure);
    }
    
    private void initialiserSalles() {
        for (Map.Entry<String, List<Salle>> entry : sallesReelles.entrySet()) {
            String nomBatiment = entry.getKey();
            List<Salle> salles = entry.getValue();
            
            sallesParBatiment.put(nomBatiment, salles);
            capaciteBatiments.put(nomBatiment, salles.size());
        }
    }
    
    private void creerVue() {
        initialiserSalles();
        
        racine = new BorderPane();
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: #f5f7fa;");
        
        Label titreLabel = new Label("🗺️ Carte interactive du campus");
        titreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titreLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        
        Label sousTitreLabel = new Label("Statuts des salles en temps réel (mise à jour toutes les 30 secondes)");
        sousTitreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 10 0;");
        
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 0, 20, 0));
        
        Button btnActualiser = new Button("🔄 Actualiser maintenant");
        btnActualiser.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnActualiser.setOnAction(e -> actualiserStatutsSalles());
        
        Button btnLegende = new Button("📖 Légende");
        btnLegende.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        btnLegende.setOnAction(e -> afficherLegende());
        
        infoLabel = new Label("Sélectionnez un bâtiment");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e; -fx-padding: 0 20 0 0;");
        
        toolbar.getChildren().addAll(btnActualiser, btnLegende, infoLabel);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        Pane paneCarte = new Pane();
        paneCarte.setPrefSize(900, 600);
        paneCarte.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #bdc3c7; -fx-border-width: 2;");
        
        Rectangle pelouse = new Rectangle(0, 0, 900, 600);
        pelouse.setFill(Color.rgb(200, 230, 200));
        paneCarte.getChildren().add(pelouse);
        
        for (int i = 0; i < 5; i++) {
            Rectangle chemin = new Rectangle(0, 100 + i*100, 900, 10);
            chemin.setFill(Color.rgb(180, 180, 180));
            paneCarte.getChildren().add(chemin);
        }
        
        for (int i = 0; i < 4; i++) {
            Rectangle chemin = new Rectangle(150 + i*200, 0, 10, 600);
            chemin.setFill(Color.rgb(180, 180, 180));
            paneCarte.getChildren().add(chemin);
        }
        
        ajouterBatiments(paneCarte);
        
        for (int i = 0; i < 10; i++) {
            Circle arbre = new Circle(50 + i*80, 500, 8);
            arbre.setFill(Color.rgb(34, 139, 34));
            paneCarte.getChildren().add(arbre);
        }
        
        scrollPane.setContent(paneCarte);
        infoBox = creerPanneauInformation();
        
        VBox topBox = new VBox(5);
        topBox.getChildren().addAll(titreLabel, sousTitreLabel, toolbar);
        
        racine.setTop(topBox);
        racine.setCenter(scrollPane);
        racine.setRight(infoBox);
        
        Platform.runLater(() -> actualiserStatutsSalles());
    }
    
    private void ajouterBatiments(Pane pane) {
        for (Map.Entry<String, int[]> entry : positionsBatiments.entrySet()) {
            String nomBatiment = entry.getKey();
            int[] pos = entry.getValue();
            
            Rectangle batiment = new Rectangle(pos[0], pos[1], pos[2], pos[3]);
            batiment.setFill(getCouleurBatiment(nomBatiment));
            batiment.setStroke(Color.DARKBLUE);
            batiment.setStrokeWidth(2);
            batiment.setArcWidth(15);
            batiment.setArcHeight(15);
            batiment.setOpacity(0.9);
            
            batiment.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 2, 2);");
            
            batiment.setOnMouseEntered(e -> {
                batiment.setOpacity(1.0);
                batiment.setStrokeWidth(3);
                batiment.setStroke(Color.RED);
            });
            batiment.setOnMouseExited(e -> {
                batiment.setOpacity(0.9);
                batiment.setStrokeWidth(2);
                batiment.setStroke(Color.DARKBLUE);
            });
            
            batiment.setOnMouseClicked(e -> {
                afficherSallesBatiment(nomBatiment);
                
                batiment.setFill(Color.YELLOW);
                new Thread(() -> {
                    try { Thread.sleep(200); } catch (InterruptedException ex) { ex.printStackTrace(); }
                    Platform.runLater(() -> batiment.setFill(getCouleurBatiment(nomBatiment)));
                }).start();
            });
            
            Text nomText = new Text(pos[0] + 10, pos[1] + 25, nomBatiment);
            nomText.setFill(Color.WHITE);
            nomText.setFont(Font.font("System", FontWeight.BOLD, 13));
            
            pane.getChildren().addAll(batiment, nomText);
            
            // Récupérer les salles de ce bâtiment
            List<Salle> salles = sallesReelles.getOrDefault(nomBatiment, new ArrayList<>());
            
            // ✅ NE PAS AJOUTER DE CERCLES pour Bibliothèque et Restaurants
            if (nomBatiment.equals("Bibliothèque") || nomBatiment.equals("Restaurants")) {
                continue;
            }
            
            if (salles.isEmpty()) {
                continue;
            }
            
            List<Circle> cercles = new ArrayList<>();
            
            int espacement, tailleCercle, decalageX;
            
            switch (nomBatiment) {
                case "Bâtiment A":
                    espacement = 11; tailleCercle = 4; decalageX = 12; break;
                case "Bâtiment B":
                    espacement = 13; tailleCercle = 5; decalageX = 15; break;
                case "Bâtiment C":
                    espacement = 18; tailleCercle = 5; decalageX = 20; break;
                case "Amphithéâtres":
                    espacement = 35; tailleCercle = 6; decalageX = 30; break;
                default:
                    espacement = 15; tailleCercle = 5; decalageX = 20;
            }
            
            for (int i = 0; i < salles.size(); i++) {
                int x = pos[0] + decalageX + (i * espacement);
                int y = pos[1] + 60;
                
                Circle sallePoint = new Circle(x, y, tailleCercle);
                sallePoint.setFill(Color.GRAY);
                sallePoint.setStroke(Color.WHITE);
                sallePoint.setStrokeWidth(1.5);
                
                final Salle salleInfo = salles.get(i);
                
                Tooltip tooltip = new Tooltip(salleInfo.getNumeroSalle() + " - " + 
                                             salleInfo.getType() + " - Cap: " + salleInfo.getCapacite());
                Tooltip.install(sallePoint, tooltip);
                
                sallePoint.setOnMouseClicked(event -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Détails de la salle");
                    alert.setHeaderText(salleInfo.getNumeroSalle());
                    
                    StringBuilder equipements = new StringBuilder();
                    equipements.append("Capacité: ").append(salleInfo.getCapacite()).append(" personnes\n");
                    equipements.append("Type: ").append(salleInfo.getType()).append("\n");
                    equipements.append("Bâtiment: ").append(salleInfo.getNomBatiment()).append("\n");
                    equipements.append("Équipements: ");
                    if (salleInfo.isAVideoprojecteur()) equipements.append("Vidéoprojecteur ");
                    if (salleInfo.isATableauBlanc()) equipements.append("Tableau blanc ");
                    if (salleInfo.isAClimatisation()) equipements.append("Climatisation");
                    
                    alert.setContentText(equipements.toString());
                    alert.showAndWait();
                });
                
                cercles.add(sallePoint);
                pane.getChildren().add(sallePoint);
            }
            
            if (!cercles.isEmpty()) {
                cerclesSallesParBatiment.put(nomBatiment, cercles);
            }
        }
    }
    
    private VBox creerPanneauInformation() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setPrefWidth(350);
        box.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        Label titrePanneau = new Label("📋 Détails du bâtiment");
        titrePanneau.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        nomBatimentLabel = new Label("Aucun bâtiment sélectionné");
        nomBatimentLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        Label sallesLabel = new Label("Salles disponibles:");
        sallesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        listeSalles = new ListView<>();
        listeSalles.setPrefHeight(250);
        listeSalles.setPlaceholder(new Label("Cliquez sur un bâtiment"));
        
        listeSalles.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (item.contains("🟢")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (item.contains("🔴")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });
        
        Label statsLabel = new Label("📊 Statistiques du bâtiment");
        statsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 15 0 5 0;");
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(8);
        statsGrid.setPadding(new Insets(5));
        
        statsGrid.add(new Label("Total salles:"), 0, 0);
        Label totalSallesVal = new Label("0");
        totalSallesVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");
        statsGrid.add(totalSallesVal, 1, 0);
        
        statsGrid.add(new Label("Capacité totale:"), 0, 1);
        Label capaciteTotalVal = new Label("0");
        capaciteTotalVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
        statsGrid.add(capaciteTotalVal, 1, 1);
        
        statsGrid.add(new Label("Salles libres:"), 0, 2);
        Label libresVal = new Label("0");
        libresVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");
        statsGrid.add(libresVal, 1, 2);
        
        statsGrid.add(new Label("Salles occupées:"), 0, 3);
        Label occupeesVal = new Label("0");
        occupeesVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        statsGrid.add(occupeesVal, 1, 3);
        
        statsGrid.add(new Label("Taux d'occupation:"), 0, 4);
        Label tauxVal = new Label("0%");
        tauxVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #f39c12;");
        statsGrid.add(tauxVal, 1, 4);
        
        box.getChildren().addAll(titrePanneau, nomBatimentLabel, sallesLabel, 
                                 listeSalles, statsLabel, statsGrid);
        
        return box;
    }
    
    private void afficherSallesBatiment(String nomBatiment) {
        infoLabel.setText("Bâtiment: " + nomBatiment);
        nomBatimentLabel.setText(nomBatiment);
        
        listeSalles.getItems().clear();
        
        if (sallesParBatiment.containsKey(nomBatiment)) {
            List<Salle> salles = sallesParBatiment.get(nomBatiment);
            LocalDate aujourdHui = LocalDate.now();
            LocalTime maintenant = LocalTime.now();
            
            if (salles.isEmpty()) {
                listeSalles.getItems().add("Ce bâtiment ne contient pas de salles de cours");
            } else {
                int libres = 0;
                int occupees = 0;
                int capaciteTotale = 0;
                
                for (Salle salle : salles) {
                    boolean estOccupee = estSalleOccupee(salle.getId(), aujourdHui, maintenant);
                    String statut = estOccupee ? "🔴 Occupée" : "🟢 Libre";
                    if (estOccupee) occupees++; else libres++;
                    capaciteTotale += salle.getCapacite();
                    
                    String info = salle.getNumeroSalle() + " - Cap: " + salle.getCapacite() + 
                                 " - " + salle.getType() + " - " + statut;
                    listeSalles.getItems().add(info);
                }
                
                GridPane statsGrid = (GridPane) ((VBox) racine.getRight()).getChildren().get(5);
                
                ((Label) statsGrid.getChildren().get(1)).setText(String.valueOf(salles.size()));
                ((Label) statsGrid.getChildren().get(3)).setText(String.valueOf(capaciteTotale));
                ((Label) statsGrid.getChildren().get(5)).setText(String.valueOf(libres));
                ((Label) statsGrid.getChildren().get(7)).setText(String.valueOf(occupees));
                
                int taux = salles.size() > 0 ? (occupees * 100 / salles.size()) : 0;
                ((Label) statsGrid.getChildren().get(9)).setText(taux + "%");
            }
        }
    }
    
    private void afficherLegende() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Légende de la carte");
        alert.setHeaderText("Signification des couleurs");
        
        String legende = 
            "🏢 BÂTIMENTS:\n" +
            "   • Bâtiment A (10 salles) - Bleu\n" +
            "   • Bâtiment B (8 salles) - Vert\n" +
            "   • Bâtiment C (5 salles) - Violet\n" +
            "   • Amphithéâtres (2 salles) - Rouge\n" +
            "   • Bibliothèque (pas de salles) - Jaune\n" +
            "   • Restaurants (pas de salles) - Orange\n\n" +
            "🟢🔴 SALLES:\n" +
            "   • 🟢 Vert: Salle libre\n" +
            "   • 🔴 Rouge: Salle occupée\n\n" +
            "⏱️ Mise à jour: Toutes les 30 secondes";
        
        alert.setContentText(legende);
        alert.showAndWait();
    }
    
    private Color getCouleurBatiment(String nomBatiment) {
        switch (nomBatiment) {
            case "Bâtiment A": return Color.rgb(52, 152, 219);
            case "Bâtiment B": return Color.rgb(46, 204, 113);
            case "Bâtiment C": return Color.rgb(155, 89, 182);
            case "Bibliothèque": return Color.rgb(241, 196, 15);
            case "Restaurants": return Color.rgb(230, 126, 34);
            case "Amphithéâtres": return Color.rgb(231, 76, 60);
            default: return Color.GRAY;
        }
    }
    
    public BorderPane getRacine() {
        return racine;
    }
    
    public void arreterSynchronisation() {
        if (synchronisationTimer != null) {
            synchronisationTimer.stop();
        }
    }
}