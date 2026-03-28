package com.univ.scheduler.modele;

public class Batiment {
    private int id;
    private String nom;
    private String adresse;
    private int nombreEtages;
    
    public Batiment() {}
    
    public Batiment(int id, String nom, String adresse, int nombreEtages) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.nombreEtages = nombreEtages;
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public int getNombreEtages() { return nombreEtages; }
    public void setNombreEtages(int nombreEtages) { this.nombreEtages = nombreEtages; }
}