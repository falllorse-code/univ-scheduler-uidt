package com.univ.scheduler;

import com.univ.scheduler.dao.ConnexionBD;
import com.univ.scheduler.vue.VueConnexion;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;

public class Main extends Application {
    
    @Override
    public void start(Stage stagePrincipal) {
        // Initialiser la connexion à la base de données
        ConnexionBD.getConnexion();
        
        // Afficher la vue de connexion
        VueConnexion vueConnexion = new VueConnexion(stagePrincipal);
        Scene scene = new Scene(vueConnexion.getRacine(), 400, 300);
        
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