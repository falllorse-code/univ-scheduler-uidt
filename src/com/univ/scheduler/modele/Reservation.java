package com.univ.scheduler.modele;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {
    private int id;
    private int salleId;
    private String nomSalle;
    private int utilisateurId;
    private String nomUtilisateur;
    private String titre;
    private String description;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String statut; // "confirmée", "annulée", "terminée"
    
    // Constructeurs
    public Reservation() {}
    
    public Reservation(int id, int salleId, int utilisateurId, String titre, 
                       LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        this.id = id;
        this.salleId = salleId;
        this.utilisateurId = utilisateurId;
        this.titre = titre;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.statut = "confirmée";
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getSalleId() { return salleId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }
    
    public String getNomSalle() { return nomSalle; }
    public void setNomSalle(String nomSalle) { this.nomSalle = nomSalle; }
    
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    
    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }
    
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public String getPlageHoraire() {
        return heureDebut.toString() + " - " + heureFin.toString();
    }
}