package com.univ.scheduler.utils;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import java.util.Optional;

public class ThemeManager {
    
    private static final String CSS_PATH = "/com/univ/scheduler/style.css";
    
    /**
     * Applique le thème moderne à une scène
     */
    public static void appliquerTheme(Scene scene) {
        try {
            String cssUrl = ThemeManager.class.getResource("/com/univ/scheduler/style.css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl);
            System.out.println("✅ Thème CSS appliqué avec succès");
        } catch (Exception e) {
            System.err.println("⚠️ Fichier CSS non trouvé: " + e.getMessage());
        }
    }
    
    /**
     * Crée une alerte avec le thème moderne
     */
    public static Alert creerAlerte(String titre, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Appliquer le style CSS à l'alerte
        try {
            DialogPane dialogPane = alert.getDialogPane();
            String cssUrl = ThemeManager.class.getResource("/com/univ/scheduler/style.css").toExternalForm();
            dialogPane.getStylesheets().add(cssUrl);
            dialogPane.getStyleClass().add("dialog-pane");
        } catch (Exception e) {
            // Ignorer si CSS non trouvé
        }
        
        return alert;
    }
    
    /**
     * Crée une alerte de confirmation
     */
    public static boolean confirmer(String titre, String message) {
        Alert alert = creerAlerte(titre, "Confirmation", message, Alert.AlertType.CONFIRMATION);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Affiche une notification de succès
     */
    public static void notifierSucces(String message) {
        Alert alert = creerAlerte("Succès", "✅ Opération réussie", message, Alert.AlertType.INFORMATION);
        alert.showAndWait();
    }
    
    /**
     * Affiche une notification d'erreur
     */
    public static void notifierErreur(String message) {
        Alert alert = creerAlerte("Erreur", "❌ Échec de l'opération", message, Alert.AlertType.ERROR);
        alert.showAndWait();
    }
    
    /**
     * Affiche un avertissement
     */
    public static void avertir(String message) {
        Alert alert = creerAlerte("Attention", "⚠️ Information importante", message, Alert.AlertType.WARNING);
        alert.showAndWait();
    }
}