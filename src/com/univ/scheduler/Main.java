package com.univ.scheduler;

import com.univ.scheduler.dao.ConnexionBD;
import com.univ.scheduler.vue.VueConnexion;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage stagePrincipal) {
        // Initialiser la connexion à la base de données
        ConnexionBD.getConnexion();
        
        // Afficher la vue de connexion
        VueConnexion vueConnexion = new VueConnexion(stagePrincipal);
        Scene scene = new Scene(vueConnexion.getRacine(), 400, 300);
        
        // Appliquer le thème CSS (si le fichier existe)
        try {
            String cssUrl = getClass().getResource("/com/univ/scheduler/style.css").toExternalForm();
            scene.getStylesheets().add(cssUrl);
            System.out.println("✅ Thème CSS appliqué");
        } catch (Exception e) {
            System.out.println("⚠️ Fichier CSS non trouvé, thème par défaut");
        }
        
        stagePrincipal.setTitle("UNIV-SCHEDULER - Connexion");
        stagePrincipal.setScene(scene);
        stagePrincipal.show();
        
        // Fermer la connexion à la fermeture de l'application
        stagePrincipal.setOnCloseRequest(e -> ConnexionBD.fermerConnexion());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}