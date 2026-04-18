package com.univ.scheduler.dao;

import java.sql.*;

public class ConnexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/univ_scheduler";
    private static final String UTILISATEUR = "root";
    private static final String MOT_DE_PASSE = ""; // Mets ton mot de passe ici si nécessaire
    
    private static Connection connexion = null;
    
    public static Connection getConnexion() {
        if (connexion == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connexion = DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE);
                System.out.println("Connexion à la base de données établie!");
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Erreur de connexion: " + e.getMessage());
            }
        }
        return connexion;
    }
    
    public static void fermerConnexion() {
        if (connexion != null) {
            try {
                connexion.close();
                System.out.println("Connexion fermée");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture: " + e.getMessage());
            }
        }
    }
}