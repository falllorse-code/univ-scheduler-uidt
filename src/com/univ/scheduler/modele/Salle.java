package com.univ.scheduler.modele;

public class Salle {
    private int id;
    private int batimentId;
    private String numeroSalle;
    private int capacite;
    private String type; // TD, TP, AMPHI
    private boolean aVideoprojecteur;
    private boolean aTableauBlanc;
    private boolean aClimatisation;
    private String nomBatiment;
    
    // Constructeurs
    public Salle() {}
    
    public Salle(int id, int batimentId, String numeroSalle, int capacite, String type,
                 boolean aVideoprojecteur, boolean aTableauBlanc, boolean aClimatisation) {
        this.id = id;
        this.batimentId = batimentId;
        this.numeroSalle = numeroSalle;
        this.capacite = capacite;
        this.type = type;
        this.aVideoprojecteur = aVideoprojecteur;
        this.aTableauBlanc = aTableauBlanc;
        this.aClimatisation = aClimatisation;
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getBatimentId() { return batimentId; }
    public void setBatimentId(int batimentId) { this.batimentId = batimentId; }
    
    public String getNumeroSalle() { return numeroSalle; }
    public void setNumeroSalle(String numeroSalle) { this.numeroSalle = numeroSalle; }
    
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isAVideoprojecteur() { return aVideoprojecteur; }
    public void setAVideoprojecteur(boolean aVideoprojecteur) { this.aVideoprojecteur = aVideoprojecteur; }
    
    public boolean isATableauBlanc() { return aTableauBlanc; }
    public void setATableauBlanc(boolean aTableauBlanc) { this.aTableauBlanc = aTableauBlanc; }
    
    public boolean isAClimatisation() { return aClimatisation; }
    public void setAClimatisation(boolean aClimatisation) { this.aClimatisation = aClimatisation; }
    
    public String getNomBatiment() { return nomBatiment; }
    public void setNomBatiment(String nomBatiment) { this.nomBatiment = nomBatiment; }
    
    public String getNomCompletSalle() {
        return (nomBatiment != null ? nomBatiment + " - " : "") + numeroSalle;
    }
}