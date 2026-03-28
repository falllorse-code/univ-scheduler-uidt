package com.univ.scheduler.utils;

import com.univ.scheduler.modele.Cours;
import com.univ.scheduler.modele.Salle;
import com.univ.scheduler.modele.Utilisateur;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportPDF {
    
    private Stage stage;
    
    public ExportPDF(Stage stage) {
        this.stage = stage;
    }
    
    public void exporterEmploiTemps(List<Cours> cours, String titre) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter l'emploi du temps");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("emploi_du_temps_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // En-tête du fichier (simulé PDF)
                writer.println("=".repeat(80));
                writer.println("UNIV-SCHEDULER - " + titre);
                writer.println("Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                writer.println("=".repeat(80));
                writer.println();
                
                // Contenu
                writer.println("EMPLOI DU TEMPS");
                writer.println("-".repeat(80));
                writer.printf("%-15s %-15s %-25s %-15s %-15s%n", "Jour", "Horaire", "Cours", "Salle", "Enseignant");
                writer.println("-".repeat(80));
                
                for (Cours c : cours) {
                    writer.printf("%-15s %-15s %-25s %-15s %-15s%n",
                        c.getJourSemaine(),
                        c.getHeureDebut() + "-" + c.getHeureFin(),
                        c.getNom(),
                        c.getNomSalle(),
                        c.getNomEnseignant());
                }
                
                writer.println("-".repeat(80));
                writer.println("Fin du document");
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText("Export PDF terminé");
                alert.setContentText("Le fichier a été sauvegardé : " + file.getAbsolutePath());
                alert.showAndWait();
                
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Échec de l'export");
                alert.setContentText("Erreur : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    public void exporterListeSalles(List<Salle> salles) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des salles");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("liste_salles_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("=".repeat(80));
                writer.println("UNIV-SCHEDULER - Liste des salles");
                writer.println("Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                writer.println("=".repeat(80));
                writer.println();
                
                writer.printf("%-20s %-10s %-10s %-15s %-10s %-10s%n", 
                    "Salle", "Capacité", "Type", "Vidéoproj", "Tableau", "Clim");
                writer.println("-".repeat(80));
                
                for (Salle s : salles) {
                    writer.printf("%-20s %-10d %-10s %-15s %-10s %-10s%n",
                        s.getNomCompletSalle(),
                        s.getCapacite(),
                        s.getType(),
                        s.isAVideoprojecteur() ? "Oui" : "Non",
                        s.isATableauBlanc() ? "Oui" : "Non",
                        s.isAClimatisation() ? "Oui" : "Non");
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText("Export PDF terminé");
                alert.setContentText("Le fichier a été sauvegardé : " + file.getAbsolutePath());
                alert.showAndWait();
                
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Échec de l'export");
                alert.setContentText("Erreur : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}