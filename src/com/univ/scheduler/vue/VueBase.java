package com.univ.scheduler.vue;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

public abstract class VueBase {
    
    protected static final String FOND_PRINCIPAL = "linear-gradient(from 0% 0% to 100% 100%, #0b3b3f 0%, #1a6b5a 50%, #2d8f6e 100%)";
    protected static final String FOND_CARTE = "rgba(255,255,255,0.95)";
    protected static final String TEXTE_PRINCIPAL = "#ffffff";
    protected static final String TEXTE_SECONDAIRE = "#d4e6e1";
    protected static final String COULEUR_BOUTON = "#2ecc71";
    protected static final String COULEUR_BOUTON_HOVER = "#27ae60";
    protected static final String COULEUR_DANGER = "#e74c3c";
    protected static final String COULEUR_INFO = "#3498db";
    
    /**
     * Applique le fond dégradé à un BorderPane
     */
    protected void appliquerFond(BorderPane pane) {
        pane.setStyle("-fx-background-color: " + FOND_PRINCIPAL + ";");
    }
    
    /**
     * Crée une carte avec fond blanc et ombre
     */
    protected String getStyleCarte() {
        return "-fx-background-color: " + FOND_CARTE + ";" +
               "-fx-background-radius: 15;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);";
    }
    
    /**
     * Style pour les boutons primaires
     */
    protected String getStyleBoutonPrimaire() {
        return "-fx-background-color: " + COULEUR_BOUTON + ";" +
               "-fx-text-fill: white;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 25;" +
               "-fx-cursor: hand;" +
               "-fx-padding: 8 15;";
    }
    
    /**
     * Style pour les boutons danger
     */
    protected String getStyleBoutonDanger() {
        return "-fx-background-color: " + COULEUR_DANGER + ";" +
               "-fx-text-fill: white;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 25;" +
               "-fx-cursor: hand;" +
               "-fx-padding: 8 15;";
    }
    
    /**
     * Style pour les champs de texte
     */
    protected String getStyleTextField() {
        return "-fx-background-color: white;" +
               "-fx-background-radius: 10;" +
               "-fx-border-color: #e0e0e0;" +
               "-fx-border-radius: 10;" +
               "-fx-padding: 8 12;";
    }
    
    public abstract Region getRacine();
}