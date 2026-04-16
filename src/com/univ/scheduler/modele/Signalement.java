package com.univ.scheduler.modele;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Signalement {
    private int id;
    private String enseignant;
    private String email;
    private String salle;
    private String description;
    private LocalDateTime dateSignalement;
    private String statut; // "en_attente", "traite"
    
    public Signalement() {}
    
    public Signalement(String enseignant, String email, String salle, String description) {
        this.enseignant = enseignant;
        this.email = email;
        this.salle = salle;
        this.description = description;
        this.dateSignalement = LocalDateTime.now();
        this.statut = "en_attente";
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getEnseignant() { return enseignant; }
    public void setEnseignant(String enseignant) { this.enseignant = enseignant; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getDateSignalement() { return dateSignalement; }
    public void setDateSignalement(LocalDateTime dateSignalement) { this.dateSignalement = dateSignalement; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public String getDateFormatee() {
        return dateSignalement.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}