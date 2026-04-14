# 🎓 UNIV-SCHEDULER - UIDT

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17-orange.svg)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-green.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Description

**UNIV-SCHEDULER** est une application de gestion des salles et des emplois du temps développée pour **l'Université Iba Der Thiam de Thiès (UIDT)**.

### 🏛️ Filières prises en charge (UFR SET)

| Filière | Niveaux | Cours |
|---------|---------|-------|
| **Informatique** | L1, L2, L3 | Algorithmique, Programmation, Réseaux, IA... |
| **LMI** | LMI1, LMI2, LMI3 | Mathématiques-Informatique |
| **LPC** | LPC1, LPC2, LPC3 | Physique-Chimie |
| **LSEE** | LSEE1, LSEE2, LSEE3 | Sciences de l'Éducation |

---

## ✨ Fonctionnalités

### 👥 Gestion des utilisateurs
- ✅ Authentification sécurisée
- ✅ 4 rôles : **Admin**, **Manager**, **Enseignant**, **Étudiant**
- ✅ Gestion des comptes (activation/désactivation)

### 🏢 Gestion des infrastructures
- ✅ Gestion des bâtiments et salles
- ✅ Équipements (vidéoprojecteur, tableau blanc, climatisation)
- ✅ Recherche avancée par critères

### 📚 Gestion des cours
- ✅ Création/modification/suppression de cours
- ✅ Détection automatique des conflits
- ✅ Vue calendrier (jour/semaine/mois)
- ✅ Emploi du temps personnalisé par classe

### 🗺️ Carte interactive
- ✅ Visualisation des bâtiments du campus
- ✅ Statuts des salles en temps réel (libre/occupée)
- ✅ Mise à jour automatique toutes les 30 secondes

### 📊 Statistiques en temps réel
- ✅ Tableau de bord avec indicateurs clés
- ✅ Graphiques d'occupation
- ✅ Export PDF

### 📧 Notifications
- ✅ Alertes de conflit
- ✅ Notifications par email
- ✅ Rappels de réservation

---

## 🛠️ Technologies utilisées

| Technologie | Version | Utilisation |
|-------------|---------|-------------|
| **Java** | 17 | Langage principal |
| **JavaFX** | 17 | Interface graphique |
| **MySQL** | 8.0 | Base de données |
| **Maven** | 3.8+ | Gestion des dépendances |
| **JavaMail** | 1.6.2 | Envoi d'emails |

---

## 📦 Installation

### Prérequis
- JDK 17 ou supérieur
- MySQL 8.0
- Maven 3.8+

### 1. Cloner le dépôt
```bash
git clone https://github.com/falllorse-code/univ-scheduler-uidt.git
cd univ-scheduler-uidt
```

### 2. Configurer la base de données
```bash
# Créer la base de données
mysql -u root -p
CREATE DATABASE univ_scheduler;
USE univ_scheduler;
SOURCE script.sql;
```

### 3. Configurer l'application
```bash
# Copier le fichier de configuration exemple
cp config.properties.example config.properties

# Éditer avec vos identifiants
# Modifier le fichier config.properties
```

### 4. Compiler et exécuter
```bash
# Compiler avec Maven
mvn clean compile

# Générer le JAR
mvn package

# Exécuter l'application
java -jar target/univ-scheduler-3.0.jar
```

---

## 🔑 Comptes de test

| Rôle | Username | Mot de passe |
|------|----------|--------------|
| **Admin** | `admin` | `admin123` |
| **Manager** | `mdiouf` | `password123` |
| **Enseignant** | `mdiagne` | `password123` |
| **Étudiant INFO** | `mdiop1` | `etudiant123` |

---

## 📁 Structure du projet

```
univ-scheduler-uidt/
├── src/main/java/com/univ/scheduler/
│   ├── Main.java                 # Point d'entrée
│   ├── dao/                      # Accès aux données
│   ├── modele/                   # Classes métier
│   ├── utils/                    # Utilitaires
│   └── vue/                      # Interfaces JavaFX
├── script.sql                    # Script de création BD
├── config.properties.example     # Exemple de configuration
├── pom.xml                       # Configuration Maven
└── README.md                     # Ce fichier
```

---

## 👥 Équipe

- **Étudiant** : [Votre Nom] - Licence 2 Informatique, UFR SET, UIDT

---

## 📅 Version

- **Version** : 3.0
- **Date** : Mars 2026

---

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## 📧 Contact

- **GitHub** : [falllorse-code](https://github.com/falllorse-code)