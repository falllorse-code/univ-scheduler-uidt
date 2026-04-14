package com.univ.scheduler.dao;

import com.univ.scheduler.modele.Cours;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {
    
    public List<Cours> getTousLesCours() {
        List<Cours> cours = new ArrayList<>();
        String sql = "SELECT c.*, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as enseignant_nom, " +
                     "CONCAT(b.name, ' - ', r.room_number) as salle_nom " +
                     "FROM courses c " +
                     "LEFT JOIN users u ON c.enseignant_id = u.id " +
                     "LEFT JOIN rooms r ON c.salle_id = r.id " +
                     "LEFT JOIN buildings b ON r.building_id = b.id " +
                     "ORDER BY c.jour_semaine, c.heure_debut";
        
        try (Statement stmt = ConnexionBD.getConnexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setEnseignantId(rs.getInt("enseignant_id"));
                c.setNomEnseignant(rs.getString("enseignant_nom"));
                c.setClasse(rs.getString("classe"));
                c.setGroupe(rs.getString("groupe"));
                c.setJourSemaine(rs.getString("jour_semaine"));
                c.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
                c.setDuree(rs.getInt("duree"));
                c.setSalleId(rs.getInt("salle_id"));
                c.setNomSalle(rs.getString("salle_nom"));
                cours.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cours;
    }
    
    public boolean ajouterCours(Cours cours) {
        String sql = "INSERT INTO courses (nom, enseignant_id, classe, groupe, jour_semaine, heure_debut, duree, salle_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cours.getNom());
            stmt.setInt(2, cours.getEnseignantId());
            stmt.setString(3, cours.getClasse());
            stmt.setString(4, cours.getGroupe());
            stmt.setString(5, cours.getJourSemaine());
            stmt.setTime(6, Time.valueOf(cours.getHeureDebut()));
            stmt.setInt(7, cours.getDuree());
            stmt.setInt(8, cours.getSalleId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    cours.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean modifierCours(Cours cours) {
        String sql = "UPDATE courses SET nom = ?, enseignant_id = ?, classe = ?, groupe = ?, " +
                     "jour_semaine = ?, heure_debut = ?, duree = ?, salle_id = ? WHERE id = ?";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setString(1, cours.getNom());
            stmt.setInt(2, cours.getEnseignantId());
            stmt.setString(3, cours.getClasse());
            stmt.setString(4, cours.getGroupe());
            stmt.setString(5, cours.getJourSemaine());
            stmt.setTime(6, Time.valueOf(cours.getHeureDebut()));
            stmt.setInt(7, cours.getDuree());
            stmt.setInt(8, cours.getSalleId());
            stmt.setInt(9, cours.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean supprimerCours(int coursId) {
        String sql = "DELETE FROM courses WHERE id = ?";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setInt(1, coursId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
 // Dans CoursDAO.java, ajoutez :
    public List<Cours> getCoursParClasse(String classe) {
        List<Cours> cours = new ArrayList<>();
        String sql = "SELECT c.*, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as enseignant_nom, " +
                     "CONCAT(b.name, ' - ', r.room_number) as salle_nom " +
                     "FROM courses c " +
                     "LEFT JOIN users u ON c.enseignant_id = u.id " +
                     "LEFT JOIN rooms r ON c.salle_id = r.id " +
                     "LEFT JOIN buildings b ON r.building_id = b.id " +
                     "WHERE c.classe = ? " +
                     "ORDER BY c.jour_semaine, c.heure_debut";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            stmt.setString(1, classe);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setEnseignantId(rs.getInt("enseignant_id"));
                c.setNomEnseignant(rs.getString("enseignant_nom"));
                c.setClasse(rs.getString("classe"));
                c.setGroupe(rs.getString("groupe"));
                c.setJourSemaine(rs.getString("jour_semaine"));
                c.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
                c.setDuree(rs.getInt("duree"));
                c.setSalleId(rs.getInt("salle_id"));
                c.setNomSalle(rs.getString("salle_nom"));
                cours.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cours;
    }
 // Vérifier si un cours est en conflit avec d'autres
    public List<Cours> getCoursEnConflit(Cours nouveauCours) {
        List<Cours> conflits = new ArrayList<>();
        String sql = "SELECT c.*, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as enseignant_nom, " +
                     "CONCAT(b.name, ' - ', r.room_number) as salle_nom " +
                     "FROM courses c " +
                     "LEFT JOIN users u ON c.enseignant_id = u.id " +
                     "LEFT JOIN rooms r ON c.salle_id = r.id " +
                     "LEFT JOIN buildings b ON r.building_id = b.id " +
                     "WHERE c.id != ? " +
                     "AND c.salle_id = ? " +
                     "AND c.jour_semaine = ? " +
                     "AND ((c.heure_debut < ? AND ADDTIME(c.heure_debut, SEC_TO_TIME(c.duree*60)) > ?) " +
                     "OR (c.heure_debut < ? AND ADDTIME(c.heure_debut, SEC_TO_TIME(c.duree*60)) > ?))";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            LocalTime heureDebut = nouveauCours.getHeureDebut();
            LocalTime heureFin = nouveauCours.getHeureFin();
            
            stmt.setInt(1, nouveauCours.getId());
            stmt.setInt(2, nouveauCours.getSalleId());
            stmt.setString(3, nouveauCours.getJourSemaine());
            stmt.setTime(4, Time.valueOf(heureFin));
            stmt.setTime(5, Time.valueOf(heureDebut));
            stmt.setTime(6, Time.valueOf(heureFin));
            stmt.setTime(7, Time.valueOf(heureDebut));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setEnseignantId(rs.getInt("enseignant_id"));
                c.setNomEnseignant(rs.getString("enseignant_nom"));
                c.setClasse(rs.getString("classe"));
                c.setGroupe(rs.getString("groupe"));
                c.setJourSemaine(rs.getString("jour_semaine"));
                c.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
                c.setDuree(rs.getInt("duree"));
                c.setSalleId(rs.getInt("salle_id"));
                c.setNomSalle(rs.getString("salle_nom"));
                conflits.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conflits;
    }

    // Vérifier les conflits pour un enseignant (même heure sur deux cours différents)
    public List<Cours> getConflitsEnseignant(Cours nouveauCours) {
        List<Cours> conflits = new ArrayList<>();
        String sql = "SELECT c.*, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as enseignant_nom, " +
                     "CONCAT(b.name, ' - ', r.room_number) as salle_nom " +
                     "FROM courses c " +
                     "LEFT JOIN users u ON c.enseignant_id = u.id " +
                     "LEFT JOIN rooms r ON c.salle_id = r.id " +
                     "LEFT JOIN buildings b ON r.building_id = b.id " +
                     "WHERE c.id != ? " +
                     "AND c.enseignant_id = ? " +
                     "AND c.jour_semaine = ? " +
                     "AND ((c.heure_debut < ? AND ADDTIME(c.heure_debut, SEC_TO_TIME(c.duree*60)) > ?) " +
                     "OR (c.heure_debut < ? AND ADDTIME(c.heure_debut, SEC_TO_TIME(c.duree*60)) > ?))";
        
        try (PreparedStatement stmt = ConnexionBD.getConnexion().prepareStatement(sql)) {
            LocalTime heureDebut = nouveauCours.getHeureDebut();
            LocalTime heureFin = nouveauCours.getHeureFin();
            
            stmt.setInt(1, nouveauCours.getId());
            stmt.setInt(2, nouveauCours.getEnseignantId());
            stmt.setString(3, nouveauCours.getJourSemaine());
            stmt.setTime(4, Time.valueOf(heureFin));
            stmt.setTime(5, Time.valueOf(heureDebut));
            stmt.setTime(6, Time.valueOf(heureFin));
            stmt.setTime(7, Time.valueOf(heureDebut));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setEnseignantId(rs.getInt("enseignant_id"));
                c.setNomEnseignant(rs.getString("enseignant_nom"));
                c.setClasse(rs.getString("classe"));
                c.setGroupe(rs.getString("groupe"));
                c.setJourSemaine(rs.getString("jour_semaine"));
                c.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
                c.setDuree(rs.getInt("duree"));
                c.setSalleId(rs.getInt("salle_id"));
                c.setNomSalle(rs.getString("salle_nom"));
                conflits.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conflits;
    }

    // Récupérer tous les conflits existants dans l'emploi du temps
    public List<Cours[]> getTousLesConflits() {
        List<Cours[]> conflitsList = new ArrayList<>();
        List<Cours> tousLesCours = getTousLesCours();
        
        for (int i = 0; i < tousLesCours.size(); i++) {
            for (int j = i + 1; j < tousLesCours.size(); j++) {
                Cours c1 = tousLesCours.get(i);
                Cours c2 = tousLesCours.get(j);
                
                // Même salle et même jour
                if (c1.getSalleId() == c2.getSalleId() && 
                    c1.getJourSemaine().equals(c2.getJourSemaine())) {
                    
                    LocalTime debut1 = c1.getHeureDebut();
                    LocalTime fin1 = c1.getHeureFin();
                    LocalTime debut2 = c2.getHeureDebut();
                    LocalTime fin2 = c2.getHeureFin();
                    
                    if (debut1.isBefore(fin2) && debut2.isBefore(fin1)) {
                        conflitsList.add(new Cours[]{c1, c2});
                    }
                }
                // Même enseignant et même jour
                else if (c1.getEnseignantId() == c2.getEnseignantId() && 
                         c1.getJourSemaine().equals(c2.getJourSemaine())) {
                    
                    LocalTime debut1 = c1.getHeureDebut();
                    LocalTime fin1 = c1.getHeureFin();
                    LocalTime debut2 = c2.getHeureDebut();
                    LocalTime fin2 = c2.getHeureFin();
                    
                    if (debut1.isBefore(fin2) && debut2.isBefore(fin1)) {
                        conflitsList.add(new Cours[]{c1, c2});
                    }
                }
            }
        }
        
        return conflitsList;
    }
}