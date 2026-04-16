-- ============================================================
--  SCRIPT COMPLET E-TONTINE (Version corrigée)
-- ============================================================

DROP DATABASE IF EXISTS etontine;
CREATE DATABASE IF NOT EXISTS etontine
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE etontine;

-- Table des utilisateurs
CREATE TABLE utilisateurs (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nom_complet     VARCHAR(100)        NOT NULL,
    email           VARCHAR(150)        NOT NULL UNIQUE,
    mot_de_passe    VARCHAR(255)        NOT NULL,
    telephone       VARCHAR(20)         NOT NULL,
    role_global     ENUM('super_admin','utilisateur') NOT NULL DEFAULT 'utilisateur',
    est_bloque      TINYINT(1)          NOT NULL DEFAULT 0,
    jeton_invitation VARCHAR(64)        NULL,
    date_inscription DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion DATETIME         NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des tontines
CREATE TABLE tontines (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nom             VARCHAR(150)        NOT NULL,
    description     TEXT                NULL,
    montant_cotisation DECIMAL(12,2)    NOT NULL,
    frequence       ENUM('hebdomadaire','mensuelle','trimestrielle') NOT NULL DEFAULT 'mensuelle',
    echeance_jour_semaine TINYINT UNSIGNED NULL DEFAULT NULL,
    echeance_jour_mois TINYINT UNSIGNED NULL DEFAULT NULL,
    nombre_max_membres SMALLINT UNSIGNED NOT NULL DEFAULT 10,
    statut          ENUM('active','fermee','suspendue','brouillon') NOT NULL DEFAULT 'active',
    createur_id     INT UNSIGNED        NOT NULL,
    amende_par_jour DECIMAL(10,2)       DEFAULT 0,
    amende_delai_grace INT              DEFAULT 0,
    amendes_activees TINYINT            DEFAULT 0,
    date_creation   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fermeture  DATETIME            NULL,
    CONSTRAINT fk_tontine_createur FOREIGN KEY (createur_id) REFERENCES utilisateurs(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des membres des tontines
CREATE TABLE membres_tontine (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tontine_id      INT UNSIGNED        NOT NULL,
    utilisateur_id  INT UNSIGNED        NOT NULL,
    role            ENUM('admin','membre') NOT NULL DEFAULT 'membre',
    statut          ENUM('actif','retard','exclu') NOT NULL DEFAULT 'actif',
    ordre_tour      SMALLINT UNSIGNED   NULL,
    score_fiabilite DECIMAL(5,2)        NOT NULL DEFAULT 100.00,
    date_adhesion   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_membre_tontine (tontine_id, utilisateur_id),
    CONSTRAINT fk_mt_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE,
    CONSTRAINT fk_mt_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des cycles de cotisation
CREATE TABLE cycles_cotisation (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tontine_id      INT UNSIGNED        NOT NULL,
    numero_cycle    SMALLINT UNSIGNED   NOT NULL,
    date_debut      DATE                NOT NULL,
    date_limite     DATE                NOT NULL,
    date_distribution DATE              NULL,
    beneficiaire_id INT UNSIGNED        NULL,
    statut          ENUM('en_cours','clos','distribue') NOT NULL DEFAULT 'en_cours',
    cagnotte_collectee DECIMAL(12,2)    NOT NULL DEFAULT 0.00,
    cagnotte_theorique DECIMAL(12,2)    NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_cycle_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE,
    CONSTRAINT fk_cycle_beneficiaire FOREIGN KEY (beneficiaire_id) REFERENCES utilisateurs(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des paiements
CREATE TABLE paiements (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tontine_id      INT UNSIGNED        NOT NULL,
    cycle_id        INT UNSIGNED        NOT NULL,
    payeur_id       INT UNSIGNED        NOT NULL,
    beneficiaire_id INT UNSIGNED        NOT NULL,
    montant         DECIMAL(12,2)       NOT NULL,
    type_paiement   ENUM('normal','pour_autrui') NOT NULL DEFAULT 'normal',
    mode_paiement   ENUM('wave','orange_money','especes') NOT NULL DEFAULT 'wave',
    numero_telephone VARCHAR(20)        NULL,
    statut          ENUM('paye','en_attente','retard') NOT NULL DEFAULT 'paye',
    reference       VARCHAR(64)         NULL,
    note            TEXT                NULL,
    date_paiement   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_paiement_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE,
    CONSTRAINT fk_paiement_cycle FOREIGN KEY (cycle_id) REFERENCES cycles_cotisation(id) ON DELETE CASCADE,
    CONSTRAINT fk_paiement_payeur FOREIGN KEY (payeur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_paiement_beneficiaire FOREIGN KEY (beneficiaire_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des amendes
CREATE TABLE amendes (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tontine_id      INT UNSIGNED        NOT NULL,
    cycle_id        INT UNSIGNED        NOT NULL,
    membre_id       INT UNSIGNED        NOT NULL,
    montant         DECIMAL(10,2)       NOT NULL,
    type_calcul     ENUM('fixe','par_jour') DEFAULT 'fixe',
    jours_retard    INT                 DEFAULT 0,
    motif           TEXT                NULL,
    est_paye        TINYINT             DEFAULT 0,
    date_creation   DATETIME            NOT NULL,
    date_paiement   DATETIME            NULL,
    INDEX idx_tontine (tontine_id),
    INDEX idx_cycle (cycle_id),
    INDEX idx_membre (membre_id),
    CONSTRAINT fk_amende_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE,
    CONSTRAINT fk_amende_cycle FOREIGN KEY (cycle_id) REFERENCES cycles_cotisation(id) ON DELETE CASCADE,
    CONSTRAINT fk_amende_membre FOREIGN KEY (membre_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des distributions
CREATE TABLE distributions (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tontine_id      INT UNSIGNED        NOT NULL,
    cycle_id        INT UNSIGNED        NOT NULL,
    beneficiaire_id INT UNSIGNED        NOT NULL,
    montant_prevu   DECIMAL(12,2)       NOT NULL,
    montant_recu    DECIMAL(12,2)       NOT NULL DEFAULT 0.00,
    montant_restant DECIMAL(12,2)       NOT NULL DEFAULT 0.00,
    choix_beneficiaire ENUM('attendre','recevoir_maintenant') NOT NULL DEFAULT 'attendre',
    statut          ENUM('en_attente','partiel','complet') NOT NULL DEFAULT 'en_attente',
    date_demande    DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_distribution DATETIME          NULL,
    CONSTRAINT fk_distrib_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE,
    CONSTRAINT fk_distrib_cycle FOREIGN KEY (cycle_id) REFERENCES cycles_cotisation(id) ON DELETE CASCADE,
    CONSTRAINT fk_distrib_beneficiaire FOREIGN KEY (beneficiaire_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des paiements pour autrui
CREATE TABLE paiements_pour_autrui (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    paiement_id     INT UNSIGNED        NOT NULL,
    payeur_id       INT UNSIGNED        NOT NULL,
    beneficiaire_id INT UNSIGNED        NOT NULL,
    tontine_id      INT UNSIGNED        NOT NULL,
    montant         DECIMAL(12,2)       NOT NULL,
    note            VARCHAR(255)        NULL,
    date_enregistrement DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ppa_paiement FOREIGN KEY (paiement_id) REFERENCES paiements(id) ON DELETE CASCADE,
    CONSTRAINT fk_ppa_payeur FOREIGN KEY (payeur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_ppa_beneficiaire FOREIGN KEY (beneficiaire_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_ppa_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des demandes d'urgence
CREATE TABLE demandes_urgence (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tontine_id      INT UNSIGNED        NOT NULL,
    demandeur_id    INT UNSIGNED        NOT NULL,
    motif           TEXT                NOT NULL,
    statut          ENUM('en_attente','validee','refusee') NOT NULL DEFAULT 'en_attente',
    mode_traitement ENUM('immediat','prochain_cycle') NULL,
    decision_admin_id INT UNSIGNED      NULL,
    date_demande    DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_decision   DATETIME            NULL,
    commentaire_admin TEXT              NULL,
    CONSTRAINT fk_urgence_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE,
    CONSTRAINT fk_urgence_demandeur FOREIGN KEY (demandeur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_urgence_admin FOREIGN KEY (decision_admin_id) REFERENCES utilisateurs(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des pétitions
CREATE TABLE petitions (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tontine_id      INT UNSIGNED        NOT NULL,
    initiateur_id   INT UNSIGNED        NOT NULL,
    motif           TEXT                NOT NULL,
    statut          ENUM('en_cours','vote_ouvert','acceptee','rejetee') NOT NULL DEFAULT 'en_cours',
    nombre_signatures SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    seuil_signatures SMALLINT UNSIGNED  NOT NULL,
    date_creation   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_cloture    DATETIME            NULL,
    CONSTRAINT fk_petition_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE,
    CONSTRAINT fk_petition_initiateur FOREIGN KEY (initiateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des signatures de pétitions
CREATE TABLE signatures_petition (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    petition_id     INT UNSIGNED        NOT NULL,
    signataire_id   INT UNSIGNED        NOT NULL,
    date_signature  DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_signature (petition_id, signataire_id),
    CONSTRAINT fk_sig_petition FOREIGN KEY (petition_id) REFERENCES petitions(id) ON DELETE CASCADE,
    CONSTRAINT fk_sig_signataire FOREIGN KEY (signataire_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des votes
CREATE TABLE votes (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tontine_id      INT UNSIGNED        NOT NULL,
    petition_id     INT UNSIGNED        NOT NULL,
    statut          ENUM('ouvert','clos') NOT NULL DEFAULT 'ouvert',
    nombre_oui      SMALLINT UNSIGNED   NOT NULL DEFAULT 0,
    nombre_non      SMALLINT UNSIGNED   NOT NULL DEFAULT 0,
    seuil_validation DECIMAL(5,2)       NOT NULL DEFAULT 75.00,
    resultat        ENUM('admin_change','admin_maintenu') NULL,
    nouvel_admin_id INT UNSIGNED        NULL,
    date_ouverture  DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_cloture    DATETIME            NULL,
    CONSTRAINT fk_vote_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE,
    CONSTRAINT fk_vote_petition FOREIGN KEY (petition_id) REFERENCES petitions(id) ON DELETE CASCADE,
    CONSTRAINT fk_vote_nouvel_admin FOREIGN KEY (nouvel_admin_id) REFERENCES utilisateurs(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des votes des membres
CREATE TABLE votes_membres (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    vote_id         INT UNSIGNED        NOT NULL,
    votant_id       INT UNSIGNED        NOT NULL,
    choix           ENUM('oui','non')   NOT NULL,
    date_vote       DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_vote_membre (vote_id, votant_id),
    CONSTRAINT fk_vm_vote FOREIGN KEY (vote_id) REFERENCES votes(id) ON DELETE CASCADE,
    CONSTRAINT fk_vm_votant FOREIGN KEY (votant_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des candidatures admin
CREATE TABLE candidatures_admin (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    vote_id         INT UNSIGNED        NOT NULL,
    candidat_id     INT UNSIGNED        NOT NULL,
    message         TEXT                NULL,
    date_candidature DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_candidature (vote_id, candidat_id),
    CONSTRAINT fk_cand_vote FOREIGN KEY (vote_id) REFERENCES votes(id) ON DELETE CASCADE,
    CONSTRAINT fk_cand_candidat FOREIGN KEY (candidat_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des notifications
CREATE TABLE notifications (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    destinataire_id INT UNSIGNED        NOT NULL,
    tontine_id      INT UNSIGNED        NULL,
    type            ENUM(
                        'paiement_confirme',
                        'retard_cotisation',
                        'paiement_pour_autrui',
                        'urgence_validee',
                        'urgence_refusee',
                        'vote_ouvert',
                        'admin_change',
                        'invitation_tontine',
                        'cagnotte_disponible',
                        'cagnotte_complete',
                        'amende'
                    ) NOT NULL,
    titre           VARCHAR(200)        NOT NULL,
    message         TEXT                NOT NULL,
    lien            VARCHAR(255)        NULL,
    est_lu          TINYINT(1)          NOT NULL DEFAULT 0,
    date_creation   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_destinataire FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des invitations
CREATE TABLE invitations (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tontine_id      INT UNSIGNED        NOT NULL,
    invite_par_id   INT UNSIGNED        NOT NULL,
    email_invite    VARCHAR(150)        NULL,
    jeton           VARCHAR(64)         NOT NULL UNIQUE,
    statut          ENUM('en_attente','acceptee','expiree') NOT NULL DEFAULT 'en_attente',
    date_envoi      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_expiration DATETIME            NOT NULL,
    CONSTRAINT fk_inv_tontine FOREIGN KEY (tontine_id) REFERENCES tontines(id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_inviteur FOREIGN KEY (invite_par_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des journaux de paiement
CREATE TABLE journaux_paiement (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    paiement_id     INT UNSIGNED        NULL,
    utilisateur_id  INT UNSIGNED        NOT NULL,
    montant         DECIMAL(12,2)       NOT NULL,
    mode            ENUM('wave','orange_money','especes') NOT NULL,
    numero_telephone VARCHAR(20)        NULL,
    statut_simulation ENUM('succes','echec','en_cours') NOT NULL DEFAULT 'en_cours',
    reference_externe VARCHAR(100)      NULL,
    reponse_api     TEXT                NULL,
    date_tentative  DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_journal_paiement FOREIGN KEY (paiement_id) REFERENCES paiements(id) ON DELETE SET NULL,
    CONSTRAINT fk_journal_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des logs système
CREATE TABLE IF NOT EXISTS logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    utilisateur_id INT UNSIGNED NULL,
    details TEXT NULL,
    tontine_id INT UNSIGNED NULL,
    ip_adresse VARCHAR(45) NULL,
    date_creation DATETIME NOT NULL,
    INDEX idx_date (date_creation),
    INDEX idx_action (action),
    INDEX idx_utilisateur (utilisateur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes pour les performances
CREATE INDEX idx_paiements_cycle ON paiements(cycle_id);
CREATE INDEX idx_paiements_payeur ON paiements(payeur_id);
CREATE INDEX idx_paiements_date ON paiements(date_paiement);
CREATE INDEX idx_cycles_tontine ON cycles_cotisation(tontine_id, statut);
CREATE INDEX idx_membres_statut ON membres_tontine(statut);
CREATE INDEX idx_notifs_destinataire ON notifications(destinataire_id, est_lu);
CREATE INDEX idx_distributions_statut ON distributions(statut);
CREATE INDEX idx_urgences_statut ON demandes_urgence(statut);

