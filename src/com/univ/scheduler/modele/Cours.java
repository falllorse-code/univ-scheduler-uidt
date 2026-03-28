package com.univ.scheduler.modele;

import java.time.LocalTime;

public class Cours {
    private int id;
    private String nom;
    private int enseignantId;
    private String nomEnseignant;
    private String classe;
    private String groupe;
    private String jourSemaine; // LUNDI, MARDI, etc.
    private LocalTime heureDebut;
    private int duree; // en minutes
    private int salleId;
    private String nomSalle;
    
    // Constructeurs
    public Cours() {}
    
    public Cours(int id, String nom, int enseignantId, String classe, String groupe,
                 String jourSemaine, LocalTime heureDebut, int duree, int salleId) {
        this.id = id;
        this.nom = nom;
        this.enseignantId = enseignantId;
        this.classe = classe;
        this.groupe = groupe;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.duree = duree;
        this.salleId = salleId;
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    
    public String getNomEnseignant() { return nomEnseignant; }
    public void setNomEnseignant(String nomEnseignant) { this.nomEnseignant = nomEnseignant; }
    
    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }
    
    public String getGroupe() { return groupe; }
    public void setGroupe(String groupe) { this.groupe = groupe; }
    
    public String getJourSemaine() { return jourSemaine; }
    public void setJourSemaine(String jourSemaine) { this.jourSemaine = jourSemaine; }
    
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    
    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }
    
    public int getSalleId() { return salleId; }
    public void setSalleId(int salleId) { this.salleId = salleId; }
    
    public String getNomSalle() { return nomSalle; }
    public void setNomSalle(String nomSalle) { this.nomSalle = nomSalle; }
    
    public LocalTime getHeureFin() {
        return heureDebut.plusMinutes(duree);
    }
    
    public String getPlageHoraire() {
        return heureDebut.toString() + " - " + getHeureFin().toString();
    }
    public String getTypeContenu() {
        if (nom == null) return null;
        if (nom.contains("TD")) return "TD";
        if (nom.contains("TP")) return "TP";
        if (nom.contains("AMPHI") || nom.contains("Amphi")) return "AMPHI";
        return null;
    }
}