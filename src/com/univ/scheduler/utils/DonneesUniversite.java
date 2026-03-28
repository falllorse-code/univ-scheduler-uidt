package com.univ.scheduler.utils;

import com.univ.scheduler.modele.Salle;
import com.univ.scheduler.modele.Batiment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonneesUniversite {
    
    private static DonneesUniversite instance;
    private Map<String, Batiment> batiments;
    private Map<String, List<Salle>> sallesParBatiment;
    private List<Salle> toutesLesSalles;
    
    private DonneesUniversite() {
        batiments = new HashMap<>();
        sallesParBatiment = new HashMap<>();
        toutesLesSalles = new ArrayList<>();
        initialiserDonnees();
    }
    
    public static DonneesUniversite getInstance() {
        if (instance == null) {
            instance = new DonneesUniversite();
        }
        return instance;
    }
    
    private void initialiserDonnees() {
        // ===== BÂTIMENT A =====
        Batiment batimentA = new Batiment(1, "Bâtiment A", "Campus Nord", 3);
        batiments.put("Bâtiment A", batimentA);
        
        List<Salle> sallesA = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Salle s = new Salle();
            s.setId(i);
            s.setBatimentId(1);
            s.setNomBatiment("Bâtiment A");
            s.setNumeroSalle("A" + (100 + i));
            s.setCapacite(25 + (i % 5) * 5);
            s.setType(i % 3 == 0 ? "TP" : "TD");
            s.setAVideoprojecteur(i % 2 == 0);
            s.setATableauBlanc(true);
            s.setAClimatisation(i > 5);
            sallesA.add(s);
            toutesLesSalles.add(s);
        }
        sallesParBatiment.put("Bâtiment A", sallesA);
        
        // ===== BÂTIMENT B =====
        Batiment batimentB = new Batiment(2, "Bâtiment B", "Campus Est", 2);
        batiments.put("Bâtiment B", batimentB);
        
        List<Salle> sallesB = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            Salle s = new Salle();
            s.setId(10 + i);
            s.setBatimentId(2);
            s.setNomBatiment("Bâtiment B");
            s.setNumeroSalle("B" + (200 + i));
            s.setCapacite(30 + (i % 4) * 5);
            s.setType(i % 2 == 0 ? "TP" : "TD");
            s.setAVideoprojecteur(i % 3 != 0);
            s.setATableauBlanc(true);
            s.setAClimatisation(i % 2 == 0);
            sallesB.add(s);
            toutesLesSalles.add(s);
        }
        sallesParBatiment.put("Bâtiment B", sallesB);
        
        // ===== BÂTIMENT C =====
        Batiment batimentC = new Batiment(3, "Bâtiment C", "Campus Ouest", 2);
        batiments.put("Bâtiment C", batimentC);
        
        List<Salle> sallesC = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Salle s = new Salle();
            s.setId(18 + i);
            s.setBatimentId(3);
            s.setNomBatiment("Bâtiment C");
            s.setNumeroSalle("C" + (300 + i));
            s.setCapacite(20 + i * 5);
            s.setType(i % 2 == 0 ? "TP" : "TD");
            s.setAVideoprojecteur(i > 2);
            s.setATableauBlanc(true);
            s.setAClimatisation(i == 3);
            sallesC.add(s);
            toutesLesSalles.add(s);
        }
        sallesParBatiment.put("Bâtiment C", sallesC);
        
        // ===== AMPHITHÉÂTRES =====
        Batiment amphis = new Batiment(4, "Amphithéâtres", "Centre", 1);
        batiments.put("Amphithéâtres", amphis);
        
        List<Salle> listeAmphis = new ArrayList<>();
        Salle amphi1 = new Salle();
        amphi1.setId(23);
        amphi1.setBatimentId(4);
        amphi1.setNomBatiment("Amphithéâtres");
        amphi1.setNumeroSalle("Amphi 1");
        amphi1.setCapacite(250);
        amphi1.setType("AMPHI");
        amphi1.setAVideoprojecteur(true);
        amphi1.setATableauBlanc(true);
        amphi1.setAClimatisation(true);
        listeAmphis.add(amphi1);
        toutesLesSalles.add(amphi1);
        
        Salle amphi2 = new Salle();
        amphi2.setId(24);
        amphi2.setBatimentId(4);
        amphi2.setNomBatiment("Amphithéâtres");
        amphi2.setNumeroSalle("Amphi 2");
        amphi2.setCapacite(180);
        amphi2.setType("AMPHI");
        amphi2.setAVideoprojecteur(true);
        amphi2.setATableauBlanc(true);
        amphi2.setAClimatisation(true);
        listeAmphis.add(amphi2);
        toutesLesSalles.add(amphi2);
        
        sallesParBatiment.put("Amphithéâtres", listeAmphis);
        
        // ===== BIBLIOTHÈQUE =====
        Batiment bibliotheque = new Batiment(5, "Bibliothèque", "Campus Sud", 2);
        batiments.put("Bibliothèque", bibliotheque);
        sallesParBatiment.put("Bibliothèque", new ArrayList<>());
        
        // ===== RESTAURANTS =====
        Batiment restaurants = new Batiment(6, "Restaurants", "Campus", 1);
        batiments.put("Restaurants", restaurants);
        sallesParBatiment.put("Restaurants", new ArrayList<>());
    }
    
    public List<Salle> getToutesLesSalles() {
        return toutesLesSalles;
    }
    
    public Map<String, List<Salle>> getSallesParBatiment() {
        return sallesParBatiment;
    }
    
    public Map<String, Batiment> getBatiments() {
        return batiments;
    }
    
    public List<String> getNomsBatiments() {
        return new ArrayList<>(batiments.keySet());
    }
    
    public List<Salle> getSallesParBatiment(String nomBatiment) {
        return sallesParBatiment.getOrDefault(nomBatiment, new ArrayList<>());
    }
    
    public List<String> getNomsSalles(String nomBatiment) {
        List<String> noms = new ArrayList<>();
        for (Salle s : getSallesParBatiment(nomBatiment)) {
            noms.add(s.getNumeroSalle() + " (Cap: " + s.getCapacite() + ")");
        }
        return noms;
    }
    
    public Salle getSalleParNom(String nomComplet) {
        for (Salle s : toutesLesSalles) {
            if ((s.getNomBatiment() + " - " + s.getNumeroSalle()).equals(nomComplet)) {
                return s;
            }
        }
        return null;
    }
}