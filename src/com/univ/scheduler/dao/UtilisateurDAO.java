package com.univ.scheduler.dao;

import com.univ.scheduler.modele.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {
    
    // Authentification (ne prend que les utilisateurs actifs)
    public Utilisateur authentifier(String nomUtilisateur, String motDePasse) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND actif = true";
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setString(1, nomUtilisateur);
            stmt.setString(2, motDePasse);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setNomUtilisateur(rs.getString("username"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setRole(rs.getString("role"));
                utilisateur.setPrenom(rs.getString("first_name"));
                utilisateur.setNom(rs.getString("last_name"));
                utilisateur.setActif(rs.getBoolean("actif"));
                
                // Charger la classe et la filière pour les étudiants
                if ("etudiant".equals(utilisateur.getRole())) {
                    try {
                        utilisateur.setClasse(rs.getString("classe"));
                        utilisateur.setFiliere(rs.getString("filiere"));
                    } catch (SQLException e) {
                        // Les colonnes n'existent peut-être pas encore
                        System.out.println("⚠️ Colonnes classe/filiere non trouvées");
                    }
                }
                
                return utilisateur;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Récupérer tous les utilisateurs
    public List<Utilisateur> getTousLesUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Statement stmt = ConnexionBD.getConnexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setNomUtilisateur(rs.getString("username"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setRole(rs.getString("role"));
                utilisateur.setPrenom(rs.getString("first_name"));
                utilisateur.setNom(rs.getString("last_name"));
                utilisateur.setActif(rs.getBoolean("actif"));
                
                // Charger la classe pour les étudiants
                if ("etudiant".equals(utilisateur.getRole())) {
                    try {
                        utilisateur.setClasse(rs.getString("classe"));
                        utilisateur.setFiliere(rs.getString("filiere"));
                    } catch (SQLException e) {
                        // Ignorer
                    }
                }
                
                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }
    
    // Récupérer les utilisateurs par rôle
    public List<Utilisateur> getUtilisateursParRole(String role) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? AND actif = true ORDER BY first_name, last_name";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setNomUtilisateur(rs.getString("username"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setRole(rs.getString("role"));
                utilisateur.setPrenom(rs.getString("first_name"));
                utilisateur.setNom(rs.getString("last_name"));
                utilisateur.setActif(rs.getBoolean("actif"));
                
                if ("etudiant".equals(role)) {
                    try {
                        utilisateur.setClasse(rs.getString("classe"));
                        utilisateur.setFiliere(rs.getString("filiere"));
                    } catch (SQLException e) {
                        // Ignorer
                    }
                }
                
                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }
    
    // Récupérer uniquement les utilisateurs actifs
    public List<Utilisateur> getUtilisateursActifs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE actif = true ORDER BY id";
        try (Statement stmt = ConnexionBD.getConnexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setNomUtilisateur(rs.getString("username"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setRole(rs.getString("role"));
                utilisateur.setPrenom(rs.getString("first_name"));
                utilisateur.setNom(rs.getString("last_name"));
                utilisateur.setActif(rs.getBoolean("actif"));
                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }
    
    // Ajouter un utilisateur
    public boolean ajouterUtilisateur(Utilisateur utilisateur) {
        String sql = "INSERT INTO users (username, password, email, role, first_name, last_name, actif, classe, filiere) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, utilisateur.getNomUtilisateur());
            stmt.setString(2, utilisateur.getMotDePasse());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setString(4, utilisateur.getRole());
            stmt.setString(5, utilisateur.getPrenom());
            stmt.setString(6, utilisateur.getNom());
            stmt.setBoolean(7, utilisateur.isActif());
            stmt.setString(8, utilisateur.getClasse());
            stmt.setString(9, utilisateur.getFiliere());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    utilisateur.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Modifier un utilisateur
    public boolean modifierUtilisateur(Utilisateur utilisateur) {
        String sql = "UPDATE users SET username = ?, email = ?, role = ?, first_name = ?, last_name = ?, classe = ?, filiere = ? WHERE id = ?";
        
        // Ne pas modifier le mot de passe si vide
        if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
            sql = "UPDATE users SET username = ?, password = ?, email = ?, role = ?, first_name = ?, last_name = ?, classe = ?, filiere = ? WHERE id = ?";
        }
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
                stmt.setString(1, utilisateur.getNomUtilisateur());
                stmt.setString(2, utilisateur.getMotDePasse());
                stmt.setString(3, utilisateur.getEmail());
                stmt.setString(4, utilisateur.getRole());
                stmt.setString(5, utilisateur.getPrenom());
                stmt.setString(6, utilisateur.getNom());
                stmt.setString(7, utilisateur.getClasse());
                stmt.setString(8, utilisateur.getFiliere());
                stmt.setInt(9, utilisateur.getId());
            } else {
                stmt.setString(1, utilisateur.getNomUtilisateur());
                stmt.setString(2, utilisateur.getEmail());
                stmt.setString(3, utilisateur.getRole());
                stmt.setString(4, utilisateur.getPrenom());
                stmt.setString(5, utilisateur.getNom());
                stmt.setString(6, utilisateur.getClasse());
                stmt.setString(7, utilisateur.getFiliere());
                stmt.setInt(8, utilisateur.getId());
            }
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Activer/Désactiver un utilisateur
    public boolean setActif(int userId, boolean actif) {
        String sql = "UPDATE users SET actif = ? WHERE id = ?";
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setBoolean(1, actif);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Supprimer définitivement un utilisateur
    public boolean supprimerUtilisateur(int userId) {
        String deleteReservations = "DELETE FROM reservations WHERE user_id = ?";
        String updateMatieres = "UPDATE matieres SET enseignant_id = NULL WHERE enseignant_id = ?";
        String deleteUser = "DELETE FROM users WHERE id = ?";
        
        try {
            ConnexionBD.getConnexion().setAutoCommit(false);
            
            try (PreparedStatement stmt1 = ConnexionBD.getConnexion().prepareStatement(deleteReservations)) {
                stmt1.setInt(1, userId);
                stmt1.executeUpdate();
            }
            
            try (PreparedStatement stmt2 = ConnexionBD.getConnexion().prepareStatement(updateMatieres)) {
                stmt2.setInt(1, userId);
                stmt2.executeUpdate();
            }
            
            try (PreparedStatement stmt3 = ConnexionBD.getConnexion().prepareStatement(deleteUser)) {
                stmt3.setInt(1, userId);
                int result = stmt3.executeUpdate();
                ConnexionBD.getConnexion().commit();
                return result > 0;
            }
            
        } catch (SQLException e) {
            try {
                ConnexionBD.getConnexion().rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                ConnexionBD.getConnexion().setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    // Vérifier si un username existe déjà
    public boolean usernameExiste(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Vérifier si un email existe déjà
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}