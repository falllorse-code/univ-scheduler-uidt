package com.univ.scheduler.dao;

import com.univ.scheduler.modele.Reservation;
import com.univ.scheduler.modele.Salle;
import com.univ.scheduler.modele.Utilisateur;
import com.univ.scheduler.utils.NotificationService;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    
    // Ajouter une réservation
    public boolean ajouterReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (room_id, user_id, title, description, reservation_date, start_time, end_time, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reservation.getSalleId());
            stmt.setInt(2, reservation.getUtilisateurId());
            stmt.setString(3, reservation.getTitre());
            stmt.setString(4, reservation.getDescription());
            stmt.setDate(5, Date.valueOf(reservation.getDate()));
            stmt.setTime(6, Time.valueOf(reservation.getHeureDebut()));
            stmt.setTime(7, Time.valueOf(reservation.getHeureFin()));
            stmt.setString(8, reservation.getStatut());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    reservation.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Vérifier si une salle est disponible pour une réservation
    public boolean estSalleDisponible(int salleId, LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        String sql = "SELECT * FROM reservations WHERE room_id = ? AND reservation_date = ? AND status = 'confirmed' " +
                     "AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?))";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, Time.valueOf(heureDebut));
            stmt.setTime(4, Time.valueOf(heureDebut));
            stmt.setTime(5, Time.valueOf(heureFin));
            stmt.setTime(6, Time.valueOf(heureFin));
            
            ResultSet rs = stmt.executeQuery();
            return !rs.next(); // Pas de réservation = disponible
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Récupérer toutes les réservations d'une salle
    public List<Reservation> getReservationsParSalle(int salleId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, u.first_name, u.last_name, s.room_number, b.name as building_name " +
                     "FROM reservations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN rooms s ON r.room_id = s.id " +
                     "LEFT JOIN buildings b ON s.building_id = b.id " +
                     "WHERE r.room_id = ? " +
                     "ORDER BY r.reservation_date DESC, r.start_time DESC";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setSalleId(rs.getInt("room_id"));
                r.setNomSalle(rs.getString("building_name") + " - " + rs.getString("room_number"));
                r.setUtilisateurId(rs.getInt("user_id"));
                r.setNomUtilisateur(rs.getString("first_name") + " " + rs.getString("last_name"));
                r.setTitre(rs.getString("title"));
                r.setDescription(rs.getString("description"));
                r.setDate(rs.getDate("reservation_date").toLocalDate());
                r.setHeureDebut(rs.getTime("start_time").toLocalTime());
                r.setHeureFin(rs.getTime("end_time").toLocalTime());
                r.setStatut(rs.getString("status"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }
    
    // Récupérer l'historique des réservations d'un utilisateur
    public List<Reservation> getReservationsParUtilisateur(int utilisateurId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, u.first_name, u.last_name, s.room_number, b.name as building_name " +
                     "FROM reservations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN rooms s ON r.room_id = s.id " +
                     "LEFT JOIN buildings b ON s.building_id = b.id " +
                     "WHERE r.user_id = ? " +
                     "ORDER BY r.reservation_date DESC, r.start_time DESC";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, utilisateurId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setSalleId(rs.getInt("room_id"));
                r.setNomSalle(rs.getString("building_name") + " - " + rs.getString("room_number"));
                r.setUtilisateurId(rs.getInt("user_id"));
                r.setNomUtilisateur(rs.getString("first_name") + " " + rs.getString("last_name"));
                r.setTitre(rs.getString("title"));
                r.setDescription(rs.getString("description"));
                r.setDate(rs.getDate("reservation_date").toLocalDate());
                r.setHeureDebut(rs.getTime("start_time").toLocalTime());
                r.setHeureFin(rs.getTime("end_time").toLocalTime());
                r.setStatut(rs.getString("status"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }
    
    // Annuler une réservation
    public boolean annulerReservation(int reservationId) {
        String sql = "UPDATE reservations SET status = 'annulée' WHERE id = ?";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Mettre à jour le statut des réservations terminées
    public void mettreAJourStatuts() {
        String sql = "UPDATE reservations SET status = 'terminée' " +
                     "WHERE reservation_date < CURDATE() OR " +
                     "(reservation_date = CURDATE() AND end_time < CURTIME())";
        
        try (Statement stmt = ConnexionBD.getConnexion().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // Notifier les utilisateurs d'un changement de salle
    public void notifierChangementSalle(int ancienneSalleId, int nouvelleSalleId, LocalDate date, LocalTime heureDebut) {
        NotificationService notifications = NotificationService.getInstance();
        
        String sql = "SELECT r.*, u.first_name, u.last_name, u.email, s1.room_number as ancienne_salle, " +
                     "s2.room_number as nouvelle_salle, b1.name as ancien_batiment, b2.name as nouveau_batiment " +
                     "FROM reservations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN rooms s1 ON r.room_id = s1.id " +
                     "LEFT JOIN buildings b1 ON s1.building_id = b1.id " +
                     "JOIN rooms s2 ON s2.id = ? " +
                     "LEFT JOIN buildings b2 ON s2.building_id = b2.id " +
                     "WHERE r.room_id = ? AND r.reservation_date = ? AND r.start_time = ?";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, nouvelleSalleId);
            stmt.setInt(2, ancienneSalleId);
            stmt.setDate(3, Date.valueOf(date));
            stmt.setTime(4, Time.valueOf(heureDebut));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Reservation ancienne = new Reservation();
                ancienne.setId(rs.getInt("id"));
                ancienne.setTitre(rs.getString("title"));
                ancienne.setNomSalle(rs.getString("ancien_batiment") + " - " + rs.getString("ancienne_salle"));
                
                Reservation nouvelle = new Reservation();
                nouvelle.setTitre(rs.getString("title"));
                nouvelle.setNomSalle(rs.getString("nouveau_batiment") + " - " + rs.getString("nouvelle_salle"));
                nouvelle.setDate(rs.getDate("reservation_date").toLocalDate());
                nouvelle.setHeureDebut(rs.getTime("start_time").toLocalTime());
                nouvelle.setHeureFin(rs.getTime("end_time").toLocalTime());
                
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setPrenom(rs.getString("first_name"));
                utilisateur.setNom(rs.getString("last_name"));
                utilisateur.setEmail(rs.getString("email"));
                
                notifications.notifierChangementSalle(utilisateur, ancienne, nouvelle);
                
                System.out.println("📧 Notification de changement de salle envoyée à " + utilisateur.getEmail());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // Ajoutez cette méthode dans ReservationDAO.java
    public boolean estSalleOccupeeMaintenant(int salleId, LocalDate date, LocalTime heure) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE room_id = ? AND reservation_date = ? " +
                     "AND ? >= start_time AND ? < end_time AND status = 'confirmée'";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, salleId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, Time.valueOf(heure));
            stmt.setTime(4, Time.valueOf(heure));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // true si occupée
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}