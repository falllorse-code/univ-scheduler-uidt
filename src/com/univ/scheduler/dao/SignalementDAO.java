package com.univ.scheduler.dao;

import com.univ.scheduler.modele.Signalement;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SignalementDAO {
    
    // Ajouter un signalement
    public boolean ajouterSignalement(Signalement signalement) {
        String sql = "INSERT INTO signalements (enseignant, email, salle, description, date_signalement, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, signalement.getEnseignant());
            stmt.setString(2, signalement.getEmail());
            stmt.setString(3, signalement.getSalle());
            stmt.setString(4, signalement.getDescription());
            stmt.setTimestamp(5, Timestamp.valueOf(signalement.getDateSignalement()));
            stmt.setString(6, signalement.getStatut());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    signalement.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Récupérer tous les signalements
    public List<Signalement> getTousLesSignalements() {
        List<Signalement> signalements = new ArrayList<>();
        String sql = "SELECT * FROM signalements ORDER BY date_signalement DESC";
        
        try (Statement stmt = ConnexionBD.getConnexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Signalement s = new Signalement();
                s.setId(rs.getInt("id"));
                s.setEnseignant(rs.getString("enseignant"));
                s.setEmail(rs.getString("email"));
                s.setSalle(rs.getString("salle"));
                s.setDescription(rs.getString("description"));
                s.setDateSignalement(rs.getTimestamp("date_signalement").toLocalDateTime());
                s.setStatut(rs.getString("statut"));
                signalements.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return signalements;
    }
    
    // Récupérer les signalements non traités
    public List<Signalement> getSignalementsNonTraites() {
        List<Signalement> signalements = new ArrayList<>();
        String sql = "SELECT * FROM signalements WHERE statut = 'en_attente' ORDER BY date_signalement DESC";
        
        try (Statement stmt = ConnexionBD.getConnexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Signalement s = new Signalement();
                s.setId(rs.getInt("id"));
                s.setEnseignant(rs.getString("enseignant"));
                s.setEmail(rs.getString("email"));
                s.setSalle(rs.getString("salle"));
                s.setDescription(rs.getString("description"));
                s.setDateSignalement(rs.getTimestamp("date_signalement").toLocalDateTime());
                s.setStatut(rs.getString("statut"));
                signalements.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return signalements;
    }
    
    // Marquer un signalement comme traité
    public boolean marquerCommeTraite(int id) {
        String sql = "UPDATE signalements SET statut = 'traite' WHERE id = ?";
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Supprimer un signalement
    public boolean supprimerSignalement(int id) {
        String sql = "DELETE FROM signalements WHERE id = ?";
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Compter les signalements non traités
    public int compterNonTraites() {
        String sql = "SELECT COUNT(*) FROM signalements WHERE statut = 'en_attente'";
        try (Statement stmt = ConnexionBD.getConnexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}