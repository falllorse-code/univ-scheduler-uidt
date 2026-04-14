package com.univ.scheduler.utils;

import com.univ.scheduler.modele.Cours;
import com.univ.scheduler.modele.Salle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExportPDF {
    
    private Stage stage;
    
    public ExportPDF(Stage stage) {
        this.stage = stage;
    }
    
    // Méthode pour obtenir l'ordre des jours
    private int getOrdreJour(String jour) {
        switch (jour) {
            case "LUNDI": return 1;
            case "MARDI": return 2;
            case "MERCREDI": return 3;
            case "JEUDI": return 4;
            case "VENDREDI": return 5;
            case "SAMEDI": return 6;
            default: return 99;
        }
    }
    
    public void exporterEmploiTemps(List<Cours> cours, String titre) {
        if (cours == null || cours.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucun cours à exporter");
            alert.setContentText("Il n'y a pas de cours à exporter.");
            alert.showAndWait();
            return;
        }
        
        // ✅ TRIER LES COURS PAR JOUR PUIS PAR HEURE
        List<Cours> coursTries = new ArrayList<>(cours);
        coursTries.sort((c1, c2) -> {
            int ordre1 = getOrdreJour(c1.getJourSemaine());
            int ordre2 = getOrdreJour(c2.getJourSemaine());
            if (ordre1 != ordre2) {
                return Integer.compare(ordre1, ordre2);
            }
            return c1.getHeureDebut().compareTo(c2.getHeureDebut());
        });
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter l'emploi du temps");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("emploi_du_temps_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                
                Paragraph titlePara = new Paragraph("UNIV-SCHEDULER - " + titre, titleFont);
                titlePara.setAlignment(Element.ALIGN_CENTER);
                document.add(titlePara);
                
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Généré le " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
                document.add(new Paragraph(" "));
                
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);
                
                String[] headers = {"Jour", "Horaire", "Cours", "Enseignant", "Salle"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }
                
                // ✅ Utiliser la liste triée
                for (Cours c : coursTries) {
                    table.addCell(new Phrase(c.getJourSemaine(), normalFont));
                    table.addCell(new Phrase(c.getPlageHoraire(), normalFont));
                    table.addCell(new Phrase(c.getNom(), normalFont));
                    table.addCell(new Phrase(c.getNomEnseignant() != null ? c.getNomEnseignant() : "", normalFont));
                    table.addCell(new Phrase(c.getNomSalle() != null ? c.getNomSalle() : "", normalFont));
                }
                
                document.add(table);
                document.close();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText("✅ PDF généré avec succès");
                alert.setContentText("Le fichier a été sauvegardé : " + file.getAbsolutePath());
                alert.showAndWait();
                
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("❌ Échec de l'export");
                alert.setContentText("Erreur : " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }
    
    public void exporterListeSalles(List<Salle> salles) {
        if (salles == null || salles.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucune salle à exporter");
            alert.setContentText("Il n'y a pas de salle à exporter.");
            alert.showAndWait();
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la liste des salles");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        fileChooser.setInitialFileName("liste_salles_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                Document document = new Document(PageSize.A4, 50, 50, 50, 50);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                
                Paragraph titlePara = new Paragraph("UNIV-SCHEDULER - Liste des salles", titleFont);
                titlePara.setAlignment(Element.ALIGN_CENTER);
                document.add(titlePara);
                
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Généré le " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
                document.add(new Paragraph(" "));
                
                PdfPTable table = new PdfPTable(7);
                table.setWidthPercentage(100);
                
                String[] headers = {"Salle", "Capacité", "Type", "Vidéoproj", "Tableau", "Clim", "Bâtiment"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }
                
                for (Salle s : salles) {
                    table.addCell(new Phrase(s.getNumeroSalle(), normalFont));
                    table.addCell(new Phrase(String.valueOf(s.getCapacite()), normalFont));
                    table.addCell(new Phrase(s.getType(), normalFont));
                    table.addCell(new Phrase(s.isAVideoprojecteur() ? "Oui" : "Non", normalFont));
                    table.addCell(new Phrase(s.isATableauBlanc() ? "Oui" : "Non", normalFont));
                    table.addCell(new Phrase(s.isAClimatisation() ? "Oui" : "Non", normalFont));
                    table.addCell(new Phrase(s.getNomBatiment(), normalFont));
                }
                
                document.add(table);
                document.close();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText("✅ PDF généré avec succès");
                alert.setContentText("Le fichier a été sauvegardé : " + file.getAbsolutePath());
                alert.showAndWait();
                
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("❌ Échec de l'export");
                alert.setContentText("Erreur : " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }
}