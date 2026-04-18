package com.univ.scheduler.vue;

import com.univ.scheduler.dao.CoursDAO;
import com.univ.scheduler.dao.SalleDAO;
import com.univ.scheduler.modele.Cours;
import com.univ.scheduler.modele.Utilisateur;
import com.univ.scheduler.utils.ExportPDF;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class VueEmploiTemps extends VueBase{
    private BorderPane racine;
    private Utilisateur utilisateurCourant;
    private Stage stagePrincipal;
    private CoursDAO coursDAO;
    private SalleDAO salleDAO;
    
    private ComboBox<String> comboClasse;
    private ComboBox<String> comboVue;
    private DatePicker datePicker;
    private GridPane grilleEmploiTemps;
    private ScrollPane scrollPane;
    private Label infoLabel;
    private Timeline synchronisationTimer;
    private Label classeLabelInfo;
    
    private final String[] JOURS_ORDONNES = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"};
    private final String[] CRENEAUX = {"08h-10h", "10h-12h", "12h-14h", "14h-16h", "16h-18h", "18h-20h"};
    
    private ObservableList<String> classes;
    private List<Cours> tousLesCours;
    private LocalDate dateSelectionnee;
    private String classeEtudiant;
    private boolean estEnseignant;
    private int enseignantId;
    
    public VueEmploiTemps(Utilisateur utilisateur, Stage stage) {
        this.utilisateurCourant = utilisateur;
        this.stagePrincipal = stage;
        this.coursDAO = new CoursDAO();
        this.salleDAO = new SalleDAO();
        this.classes = FXCollections.observableArrayList();
        this.tousLesCours = new ArrayList<>();
        this.dateSelectionnee = LocalDate.now();
        
        // Déterminer le rôle
        this.estEnseignant = "enseignant".equals(utilisateurCourant.getRole());
        if (estEnseignant) {
            this.enseignantId = utilisateurCourant.getId();
        }
        
        // Déterminer la classe de l'étudiant
        if ("etudiant".equals(utilisateurCourant.getRole())) {
            this.classeEtudiant = utilisateurCourant.getClasse();
            System.out.println("=== ÉTUDIANT CONNECTÉ ===");
            System.out.println("Classe: " + classeEtudiant);
        }
        
        creerVue();
        chargerClasses();
        demarrerSynchronisationTempsReel();
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
            tousLesCours = coursDAO.getTousLesCours();
            
            System.out.println("=== CHARGEMENT DES COURS ===");
            System.out.println("Total cours dans BD: " + tousLesCours.size());
            
            // Si c'est un enseignant, filtrer ses propres cours
            if (estEnseignant) {
                tousLesCours = tousLesCours.stream()
                    .filter(c -> c.getEnseignantId() == enseignantId)
                    .collect(Collectors.toList());
                System.out.println("Cours pour enseignant: " + tousLesCours.size());
            }
            
            // Si c'est un étudiant, on ne filtre pas ici (on filtre dans chargerEmploiTemps)
            
            chargerEmploiTemps();
        });
    }
    
    private void chargerClasses() {
        Platform.runLater(() -> {
            List<Cours> coursSource;
            
            if (estEnseignant) {
                coursSource = tousLesCours;
            } else {
                coursSource = coursDAO.getTousLesCours();
            }
            
            // Extraire les classes uniques
            Set<String> classesSet = coursSource.stream()
                .map(Cours::getClasse)
                .filter(Objects::nonNull)
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
            
            // Organiser par UFR
            List<String> classesOrdonnees = new ArrayList<>();
            classesOrdonnees.add("--- UFR SET ---");
            classesSet.stream()
                .filter(c -> c.contains("INFO") || c.contains("LMI") || c.contains("LPC") || c.contains("LSEE"))
                .sorted()
                .forEach(classesOrdonnees::add);
            
            classes.clear();
            classes.addAll(classesOrdonnees);
            
            String role = utilisateurCourant.getRole();
            
            if ("etudiant".equals(role)) {
                // ÉTUDIANT : ne voit que sa classe
                if (classeEtudiant != null && !classeEtudiant.isEmpty()) {
                    comboClasse.setItems(FXCollections.observableArrayList(classeEtudiant));
                    comboClasse.setValue(classeEtudiant);
                    comboClasse.setDisable(true);
                    classeLabelInfo.setText("📌 Votre classe: " + classeEtudiant);
                } else {
                    comboClasse.setItems(FXCollections.observableArrayList("Aucune classe"));
                    comboClasse.setValue("Aucune classe");
                    comboClasse.setDisable(true);
                    classeLabelInfo.setText("⚠️ Aucune classe trouvée");
                }
            } else if (estEnseignant) {
                // ENSEIGNANT : voit "Mes cours"
                comboClasse.setItems(FXCollections.observableArrayList("Mes cours"));
                comboClasse.setValue("Mes cours");
                comboClasse.setDisable(true);
                classeLabelInfo.setText("👨‍🏫 Vos cours (" + tousLesCours.size() + " cours)");
            } else {
                // ADMIN/MANAGER : voit toutes les classes
                if (classesOrdonnees.isEmpty()) {
                    comboClasse.setItems(FXCollections.observableArrayList("Aucune classe"));
                    comboClasse.setValue("Aucune classe");
                } else {
                    comboClasse.setItems(FXCollections.observableArrayList(classesOrdonnees));
                    comboClasse.setValue(classesOrdonnees.get(0));
                }
                comboClasse.setDisable(false);
                classeLabelInfo.setText("🏛️ Université Iba Der Thiam (UIDT)");
            }
            
            chargerEmploiTemps();
        });
    }
    
    private void creerVue() {
        racine = new BorderPane();
        appliquerFond(racine);
        racine.setPadding(new Insets(20));
        racine.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #e4e8ec);");
        
        // EN-TÊTE
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        String titre = "📅 Emploi du temps";
        if ("etudiant".equals(utilisateurCourant.getRole()) && classeEtudiant != null) {
            titre += " - " + classeEtudiant;
        } else if (estEnseignant) {
            titre += " - Mes cours";
        }
        
        Label titreLabel = new Label(titre);
        titreLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titreLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        
        Label sousTitreLabel = new Label("Données actualisées toutes les 30 secondes");
        sousTitreLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic; -fx-text-fill: #7f8c8d;");
        
        headerBox.getChildren().addAll(titreLabel, sousTitreLabel);
        
        // BARRE D'OUTILS
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10, 0, 25, 0));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Label classeLabel = new Label("Classe :");
        classeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        comboClasse = new ComboBox<>();
        comboClasse.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-padding: 5; -fx-min-width: 200px;");
        comboClasse.setOnAction(e -> chargerEmploiTemps());
        
        classeLabelInfo = new Label();
        classeLabelInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");
        
        Label vueLabel = new Label("Vue :");
        vueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        comboVue = new ComboBox<>();
        comboVue.getItems().addAll("Semaine", "Jour", "Mois");
        comboVue.setValue("Semaine");
        comboVue.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-padding: 5; -fx-min-width: 100px;");
        comboVue.setOnAction(e -> chargerEmploiTemps());
        
        Label dateLabel = new Label("Date :");
        dateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        datePicker = new DatePicker(dateSelectionnee);
        datePicker.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        datePicker.setOnAction(e -> {
            dateSelectionnee = datePicker.getValue();
            chargerEmploiTemps();
        });
        
        Button btnExporter = new Button("📥 Exporter PDF");
        btnExporter.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        btnExporter.setOnAction(e -> exporterPDF());
        
        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        btnActualiser.setOnAction(e -> chargerCours());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        infoLabel = new Label("");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");
        
        // Assemblage selon le rôle
        if ("etudiant".equals(utilisateurCourant.getRole())) {
            toolbar.getChildren().addAll(
                vueLabel, comboVue,
                dateLabel, datePicker,
                btnExporter, btnActualiser,
                spacer, classeLabelInfo, infoLabel
            );
        } else {
            toolbar.getChildren().addAll(
                classeLabel, comboClasse,
                vueLabel, comboVue,
                dateLabel, datePicker,
                btnExporter, btnActualiser,
                spacer, classeLabelInfo, infoLabel
            );
        }
        
        // GRILLE
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        grilleEmploiTemps = new GridPane();
        grilleEmploiTemps.setHgap(5);
        grilleEmploiTemps.setVgap(5);
        grilleEmploiTemps.setPadding(new Insets(15));
        grilleEmploiTemps.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        
        construireGrille();
        scrollPane.setContent(grilleEmploiTemps);
        
        // LÉGENDE
        HBox legendeBox = new HBox(20);
        legendeBox.setPadding(new Insets(15, 0, 0, 0));
        legendeBox.setAlignment(Pos.CENTER_LEFT);
        
        legendeBox.getChildren().addAll(
            creerLegendeItem("🔵 Cours", "#3498db"),
            creerLegendeItem("🟢 TD", "#2ecc71"),
            creerLegendeItem("🟠 TP", "#e67e22"),
            creerLegendeItem("🟣 Amphi", "#9b59b6"),
            creerLegendeItem("⏸️ Pause", "#f39c12")
        );
        
        VBox topBox = new VBox(5);
        topBox.getChildren().addAll(headerBox, toolbar, legendeBox);
        
        racine.setTop(topBox);
        racine.setCenter(scrollPane);
    }
    
    private HBox creerLegendeItem(String texte, String couleur) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        Label colorLabel = new Label("■");
        colorLabel.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label textLabel = new Label(texte);
        textLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");
        box.getChildren().addAll(colorLabel, textLabel);
        return box;
    }
    
    private void construireGrille() {
        grilleEmploiTemps.getChildren().clear();
        
        String styleEnTete = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 12; -fx-border-radius: 5; -fx-background-radius: 5;";
        String styleJour = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 12; -fx-border-radius: 5; -fx-background-radius: 5;";
        String styleCellule = "-fx-background-color: white; -fx-border-color: #ecf0f1; -fx-border-width: 1; -fx-alignment: top-left; -fx-padding: 8; -fx-wrap-text: true;";
        String stylePause = "-fx-background-color: #fef9e7; -fx-border-color: #f39c12; -fx-border-width: 2; -fx-alignment: center; -fx-padding: 8; -fx-font-weight: bold; -fx-text-fill: #e67e22;";
        
        Label coinVide = new Label(comboVue.getValue().equals("Jour") ? "Heure" : "Horaire");
        coinVide.setStyle(styleEnTete);
        coinVide.setMinWidth(100);
        coinVide.setMinHeight(50);
        grilleEmploiTemps.add(coinVide, 0, 0);
        
        if (comboVue.getValue().equals("Jour")) {
            String jourSemaine = dateSelectionnee.format(DateTimeFormatter.ofPattern("EEEE")).toUpperCase();
            Label jourLabel = new Label(jourSemaine + " " + dateSelectionnee.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            jourLabel.setStyle(styleJour);
            jourLabel.setMinWidth(600);
            jourLabel.setMinHeight(50);
            grilleEmploiTemps.add(jourLabel, 1, 0);
        } else {
            for (int j = 0; j < JOURS_ORDONNES.length; j++) {
                Label jourLabel = new Label(JOURS_ORDONNES[j]);
                jourLabel.setStyle(styleJour);
                jourLabel.setMinWidth(160);
                jourLabel.setMinHeight(50);
                grilleEmploiTemps.add(jourLabel, j + 1, 0);
            }
        }
        
        for (int i = 0; i < CRENEAUX.length; i++) {
            String creneau = CRENEAUX[i];
            
            Label heureLabel = new Label(creneau);
            if (creneau.equals("12h-14h")) {
                heureLabel.setText("PAUSE");
                heureLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 12; -fx-border-radius: 5; -fx-background-radius: 5;");
            } else {
                heureLabel.setStyle(styleEnTete);
            }
            heureLabel.setMinWidth(100);
            heureLabel.setMinHeight(80);
            grilleEmploiTemps.add(heureLabel, 0, i + 1);
            
            int nbJours = comboVue.getValue().equals("Jour") ? 1 : JOURS_ORDONNES.length;
            for (int j = 0; j < nbJours; j++) {
                Label caseLabel = new Label();
                caseLabel.setWrapText(true);
                caseLabel.setMinWidth(comboVue.getValue().equals("Jour") ? 600 : 160);
                caseLabel.setMinHeight(80);
                caseLabel.setMaxHeight(80);
                
                if (creneau.equals("12h-14h")) {
                    caseLabel.setText("⏸️ PAUSE DÉJEUNER ⏸️");
                    caseLabel.setStyle(stylePause);
                } else {
                    caseLabel.setStyle(styleCellule);
                }
                
                grilleEmploiTemps.add(caseLabel, j + 1, i + 1);
            }
        }
    }
    
    private void chargerEmploiTemps() {
        Platform.runLater(() -> {
            String classeSelectionnee;
            
            if ("etudiant".equals(utilisateurCourant.getRole())) {
                classeSelectionnee = utilisateurCourant.getClasse();
                
                // DEBUG
                System.out.println("=== CHARGEMENT EMPLOI DU TEMPS ÉTUDIANT ===");
                System.out.println("Classe étudiante: " + classeSelectionnee);
            } else if (estEnseignant) {
                classeSelectionnee = "Mes cours";
            } else {
                classeSelectionnee = comboClasse.getValue();
            }
            
            if (classeSelectionnee == null) return;
            
            construireGrille();
            
            List<Cours> coursFiltres;
            
            if ("etudiant".equals(utilisateurCourant.getRole())) {
                // ÉTUDIANT : filtrer par sa classe
                coursFiltres = tousLesCours.stream()
                    .filter(c -> classeSelectionnee != null && classeSelectionnee.equals(c.getClasse()))
                    .collect(Collectors.toList());
                
                System.out.println("Cours trouvés pour " + classeSelectionnee + ": " + coursFiltres.size());
                
            } else if (estEnseignant) {
                // ENSEIGNANT : déjà filtré dans chargerCours()
                coursFiltres = tousLesCours;
            } else {
                // ADMIN/MANAGER
                if (classeSelectionnee.equals("Toutes les classes") || classeSelectionnee.startsWith("---")) {
                    coursFiltres = tousLesCours;
                } else {
                    coursFiltres = tousLesCours.stream()
                        .filter(c -> classeSelectionnee.equals(c.getClasse()))
                        .collect(Collectors.toList());
                }
            }
            
            // Trier par jour
            coursFiltres.sort((c1, c2) -> Integer.compare(
                getJourIndex(c1.getJourSemaine()), 
                getJourIndex(c2.getJourSemaine())
            ));
            
            String vue = comboVue.getValue();
            List<Cours> coursAffiches = new ArrayList<>();
            
            switch (vue) {
                case "Jour":
                    String jourSemaine = dateSelectionnee.format(DateTimeFormatter.ofPattern("EEEE")).toUpperCase();
                    coursAffiches = coursFiltres.stream()
                        .filter(c -> jourSemaine.equals(c.getJourSemaine()))
                        .collect(Collectors.toList());
                    break;
                case "Semaine":
                case "Mois":
                    coursAffiches = coursFiltres;
                    break;
            }
            
            for (Cours c : coursAffiches) {
                String jour = c.getJourSemaine();
                LocalTime debut = c.getHeureDebut();
                LocalTime fin = c.getHeureFin();
                
                String creneau = trouverCreneau(debut, fin);
                if (creneau == null || creneau.equals("12h-14h")) continue;
                
                int jourIndex = vue.equals("Jour") ? 0 : getJourIndex(jour);
                if (jourIndex == -1) continue;
                
                int creneauIndex = -1;
                for (int i = 0; i < CRENEAUX.length; i++) {
                    if (CRENEAUX[i].equals(creneau)) {
                        creneauIndex = i;
                        break;
                    }
                }
                if (creneauIndex == -1) continue;
                
                Label cellLabel = (Label) getCellFromGridPane(jourIndex + 1, creneauIndex + 1);
                if (cellLabel != null) {
                    String contenu = c.getNom() + "\n" + 
                                    (c.getNomEnseignant() != null ? c.getNomEnseignant() : "À définir") + "\n" +
                                    (c.getNomSalle() != null ? c.getNomSalle() : "Salle non définie") +
                                    (c.getGroupe() != null && !c.getGroupe().isEmpty() ? "\n" + c.getGroupe() : "");
                    cellLabel.setText(contenu);
                    
                    if (c.getNom().contains("TD")) {
                        cellLabel.setStyle("-fx-background-color: #d4edda; -fx-border-color: #2ecc71; -fx-border-width: 2; -fx-alignment: top-left; -fx-padding: 8; -fx-wrap-text: true; -fx-font-weight: bold;");
                    } else if (c.getNom().contains("TP")) {
                        cellLabel.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #e67e22; -fx-border-width: 2; -fx-alignment: top-left; -fx-padding: 8; -fx-wrap-text: true; -fx-font-weight: bold;");
                    } else if (c.getNom().contains("Amphi") || c.getNom().contains("AMPHI")) {
                        cellLabel.setStyle("-fx-background-color: #e1d5e7; -fx-border-color: #9b59b6; -fx-border-width: 2; -fx-alignment: top-left; -fx-padding: 8; -fx-wrap-text: true; -fx-font-weight: bold;");
                    } else {
                        cellLabel.setStyle("-fx-background-color: #d1ecf1; -fx-border-color: #3498db; -fx-border-width: 2; -fx-alignment: top-left; -fx-padding: 8; -fx-wrap-text: true; -fx-font-weight: bold;");
                    }
                }
            }
            
            String message;
            if ("etudiant".equals(utilisateurCourant.getRole())) {
                message = "📚 Votre emploi du temps: " + classeSelectionnee + " (" + coursAffiches.size() + " cours)";
            } else if (estEnseignant) {
                message = "👨‍🏫 Vos cours (" + coursAffiches.size() + " cours)";
            } else {
                message = "📚 Affichage: " + classeSelectionnee + " (" + coursAffiches.size() + " cours)";
            }
            
            infoLabel.setText(message + " - " + dateSelectionnee.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
    }
    
    private int getJourIndex(String jour) {
        switch (jour) {
            case "LUNDI": return 0;
            case "MARDI": return 1;
            case "MERCREDI": return 2;
            case "JEUDI": return 3;
            case "VENDREDI": return 4;
            case "SAMEDI": return 5;
            default: return -1;
        }
    }
    
    private String trouverCreneau(LocalTime debut, LocalTime fin) {
        int debutHeure = debut.getHour();
        int finHeure = fin.getHour();
        if (debutHeure >= 8 && finHeure <= 10) return "08h-10h";
        if (debutHeure >= 10 && finHeure <= 12) return "10h-12h";
        if (debutHeure >= 12 && finHeure <= 14) return "12h-14h";
        if (debutHeure >= 14 && finHeure <= 16) return "14h-16h";
        if (debutHeure >= 16 && finHeure <= 18) return "16h-18h";
        if (debutHeure >= 18 && finHeure <= 20) return "18h-20h";
        return null;
    }
    
    private javafx.scene.Node getCellFromGridPane(int col, int row) {
        for (javafx.scene.Node node : grilleEmploiTemps.getChildren()) {
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (colIndex != null && colIndex == col && rowIndex != null && rowIndex == row) {
                return node;
            }
        }
        return null;
    }
    
    private void exporterPDF() {
        if (tousLesCours.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucun cours à exporter");
            alert.setContentText("Il n'y a pas de cours à exporter.");
            alert.showAndWait();
            return;
        }
        
        ExportPDF export = new ExportPDF(stagePrincipal);
        String titre;
        if ("etudiant".equals(utilisateurCourant.getRole())) {
            titre = "Emploi du temps - " + utilisateurCourant.getClasse();
        } else if (estEnseignant) {
            titre = "Mes cours";
        } else {
            titre = comboClasse.getValue() != null ? comboClasse.getValue() : "Emploi du temps";
        }
        export.exporterEmploiTemps(tousLesCours, titre);
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