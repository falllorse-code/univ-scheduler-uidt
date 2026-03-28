package com.univ.scheduler.modele;

public class Utilisateur {
    private int id;
    private String nomUtilisateur;
    private String motDePasse;
    private String email;
    private String role; // admin, manager, enseignant, etudiant
    private String prenom;
    private String nom;
    private boolean actif = true;
    private String classe; // Pour les étudiants (ex: "L1 INFO")
    private String filiere; // Pour les étudiants (ex: "Informatique")
    
    // Constructeurs
    public Utilisateur() {}
    
    public Utilisateur(int id, String nomUtilisateur, String email, String role, 
                       String prenom, String nom, boolean actif, String classe, String filiere) {
        this.id = id;
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.role = role;
        this.prenom = prenom;
        this.nom = nom;
        this.actif = actif;
        this.classe = classe;
        this.filiere = filiere;
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }
    
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    
    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }
    
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    
    public String getNomComplet() {
        return prenom + " " + nom;
    }
    
    public String getStatutTexte() {
        return actif ? "Actif" : "Inactif";
    }
    
    public String getStatutCouleur() {
        return actif ? "#2ecc71" : "#e74c3c";
    }
    
    @Override
    public String toString() {
        return getNomComplet() + " (" + role + ")";
    }
}