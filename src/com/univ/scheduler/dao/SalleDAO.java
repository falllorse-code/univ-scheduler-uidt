package com.univ.scheduler.dao;

import com.univ.scheduler.modele.Salle;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SalleDAO {
    
    // Récupérer toutes les salles
    public List<Salle> getToutesLesSalles() {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT r.*, b.name as building_name FROM rooms r " +
                     "LEFT JOIN buildings b ON r.building_id = b.id";
        try (Statement stmt = ConnexionBD.getConnexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Salle salle = new Salle();
                salle.setId(rs.getInt("id"));
                salle.setBatimentId(rs.getInt("building_id"));
                salle.setNomBatiment(rs.getString("building_name"));
                salle.setNumeroSalle(rs.getString("room_number"));
                salle.setCapacite(rs.getInt("capacity"));
                salle.setType(rs.getString("type"));
                salle.setAVideoprojecteur(rs.getBoolean("has_projector"));
                salle.setATableauBlanc(rs.getBoolean("has_whiteboard"));
                salle.setAClimatisation(rs.getBoolean("has_air_conditioning"));
                salles.add(salle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salles;
    }
    
    // Rechercher des salles disponibles
    public List<Salle> trouverSallesDisponibles(String jourSemaine, LocalTime heureDebut, LocalTime heureFin, 
                                                Integer capaciteMin, String type, Boolean aVideoprojecteur) {
        List<Salle> sallesDisponibles = new ArrayList<>();
        List<Salle> toutesSalles = getToutesLesSalles();
        
        for (Salle salle : toutesSalles) {
            // Vérifier les critères de base
            if (capaciteMin != null && salle.getCapacite() < capaciteMin) continue;
            if (type != null && !type.isEmpty() && !salle.getType().equals(type)) continue;
            if (aVideoprojecteur != null && aVideoprojecteur && !salle.isAVideoprojecteur()) continue;
            
            // Vérifier la disponibilité (pas de cours à ce créneau)
            if (estSalleDisponible(salle.getId(), jourSemaine, heureDebut, heureFin)) {
                sallesDisponibles.add(salle);
            }
        }
        
        return sallesDisponibles;
    }
    
    // Vérifier si une salle est disponible
    private boolean estSalleDisponible(int salleId, String jourSemaine, LocalTime heureDebut, LocalTime heureFin) {
        String sql = "SELECT * FROM courses WHERE room_id = ? AND day_of_week = ? " +
                     "AND ((start_time <= ? AND ADDTIME(start_time, SEC_TO_TIME(duration*60)) > ?) " +
                     "OR (start_time < ? AND ADDTIME(start_time, SEC_TO_TIME(duration*60)) >= ?))";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            stmt.setString(2, jourSemaine);
            stmt.setTime(3, Time.valueOf(heureDebut));
            stmt.setTime(4, Time.valueOf(heureDebut));
            stmt.setTime(5, Time.valueOf(heureFin));
            stmt.setTime(6, Time.valueOf(heureFin));
            
            ResultSet rs = stmt.executeQuery();
            return !rs.next(); // Pas de cours = disponible
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // === NOUVELLES MÉTHODES POUR LA SAUVEGARDE ===
    
    // Ajouter une salle dans la base de données
    public boolean ajouterSalle(Salle salle) {
        String sql = "INSERT INTO rooms (building_id, room_number, capacity, type, has_projector, has_whiteboard, has_air_conditioning) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, salle.getBatimentId());
            stmt.setString(2, salle.getNumeroSalle());
            stmt.setInt(3, salle.getCapacite());
            stmt.setString(4, salle.getType());
            stmt.setBoolean(5, salle.isAVideoprojecteur());
            stmt.setBoolean(6, salle.isATableauBlanc());
            stmt.setBoolean(7, salle.isAClimatisation());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    salle.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Modifier une salle dans la base de données
    public boolean modifierSalle(Salle salle) {
        String sql = "UPDATE rooms SET building_id = ?, room_number = ?, capacity = ?, type = ?, has_projector = ?, has_whiteboard = ?, has_air_conditioning = ? WHERE id = ?";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, salle.getBatimentId());
            stmt.setString(2, salle.getNumeroSalle());
            stmt.setInt(3, salle.getCapacite());
            stmt.setString(4, salle.getType());
            stmt.setBoolean(5, salle.isAVideoprojecteur());
            stmt.setBoolean(6, salle.isATableauBlanc());
            stmt.setBoolean(7, salle.isAClimatisation());
            stmt.setInt(8, salle.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Supprimer une salle de la base de données
    public boolean supprimerSalle(int salleId) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}