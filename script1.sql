-- ============================================
-- SCRIPT COMPLET UNIV-SCHEDULER UIDT
-- ============================================

-- Supprimer les tables existantes
DROP TABLE IF EXISTS signalements;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS buildings;
DROP TABLE IF EXISTS users;

-- ============================================
-- 1. TABLE buildings
-- ============================================
CREATE TABLE buildings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    floors INT
);

-- ============================================
-- 2. TABLE rooms
-- ============================================
CREATE TABLE rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    building_id INT,
    room_number VARCHAR(20) NOT NULL,
    capacity INT,
    type VARCHAR(20),
    has_projector BOOLEAN DEFAULT false,
    has_whiteboard BOOLEAN DEFAULT false,
    has_air_conditioning BOOLEAN DEFAULT false,
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE
);

-- ============================================
-- 3. TABLE users
-- ============================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    actif BOOLEAN DEFAULT true,
    classe VARCHAR(50),
    filiere VARCHAR(50)
);

-- ============================================
-- 4. TABLE courses
-- ============================================
CREATE TABLE courses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    enseignant_id INT,
    classe VARCHAR(50),
    groupe VARCHAR(20),
    jour_semaine VARCHAR(20),
    heure_debut TIME,
    duree INT,
    salle_id INT,
    FOREIGN KEY (enseignant_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (salle_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- ============================================
-- 5. TABLE reservations
-- ============================================
CREATE TABLE reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT NOT NULL,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    reservation_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) DEFAULT 'confirmée',
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================
-- 6. TABLE signalements
-- ============================================
CREATE TABLE signalements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    enseignant VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    salle VARCHAR(50),
    description TEXT NOT NULL,
    date_signalement DATETIME NOT NULL,
    statut VARCHAR(20) DEFAULT 'en_attente'
);

-- ============================================
-- 7. INSERTION DES BÂTIMENTS
-- ============================================
INSERT INTO buildings (name, address, floors) VALUES
('Bâtiment A', 'Campus Nord', 3),
('Bâtiment B', 'Campus Est', 2),
('Bâtiment C', 'Campus Ouest', 2),
('Amphithéâtres', 'Centre', 1),
('Bibliothèque', 'Campus Sud', 2),
('Restaurants', 'Campus', 1),
('Bâtiment D', 'Campus Nord', 2),
('Bâtiment E', 'Campus Sud', 2);

-- ============================================
-- 8. INSERTION DES SALLES
-- ============================================
INSERT INTO rooms (building_id, room_number, capacity, type, has_projector, has_whiteboard, has_air_conditioning) VALUES
(1, 'Salle 1', 30, 'TD', true, true, false),
(1, 'Salle 2', 30, 'TD', true, true, false),
(1, 'Salle 3', 35, 'TD', true, true, false),
(1, 'Salle 4', 35, 'TD', true, true, false),
(1, 'Salle 5', 40, 'TD', true, true, true),
(1, 'Salle 6', 40, 'TD', true, true, true),
(1, 'Salle 7', 45, 'TP', true, true, true),
(1, 'Salle 8', 45, 'TP', true, true, true),
(1, 'Salle 9', 50, 'TP', true, true, true),
(1, 'Salle 10', 50, 'TP', true, true, true),
(2, 'Salle 1', 30, 'TD', true, true, false),
(2, 'Salle 2', 30, 'TD', true, true, false),
(2, 'Salle 3', 35, 'TD', true, true, false),
(2, 'Salle 4', 35, 'TD', true, true, false),
(2, 'Salle 5', 40, 'TP', true, true, true),
(2, 'Salle 6', 45, 'TP', true, true, true),
(2, 'Salle 7', 50, 'TP', true, true, true),
(2, 'Salle 8', 55, 'TP', true, true, true),
(3, 'Salle 1', 25, 'TD', true, true, false),
(3, 'Salle 2', 30, 'TD', true, true, false),
(3, 'Salle 3', 35, 'TP', true, true, true),
(3, 'Salle 4', 40, 'TP', true, true, true),
(3, 'Salle 5', 45, 'TP', true, true, true),
(7, 'Salle 1', 35, 'TD', true, true, true),
(7, 'Salle 2', 35, 'TD', true, true, true),
(7, 'Salle 3', 40, 'TD', true, true, true),
(7, 'Salle 4', 40, 'TD', true, true, true),
(7, 'Salle 5', 45, 'TP', true, true, true),
(7, 'Salle 6', 45, 'TP', true, true, true),
(7, 'Salle 7', 50, 'TP', true, true, true),
(7, 'Salle 8', 50, 'TP', true, true, true),
(8, 'Salle 1', 30, 'TD', true, true, true),
(8, 'Salle 2', 35, 'TD', true, true, true),
(8, 'Salle 3', 40, 'TP', true, true, true),
(8, 'Salle 4', 45, 'TP', true, true, true),
(8, 'Salle 5', 50, 'TP', true, true, true),
(4, 'Amphi 1', 250, 'AMPHI', true, true, true),
(4, 'Amphi 2', 180, 'AMPHI', true, true, true),
(4, 'Amphi 3', 300, 'AMPHI', true, true, true),
(4, 'Amphi 4', 200, 'AMPHI', true, true, true);

-- ============================================
-- 9. INSERTION DES UTILISATEURS
-- ============================================
INSERT INTO users (username, password, email, role, first_name, last_name, actif, classe) VALUES
('admin', 'admin123', 'admin@univ-sn.sn', 'admin', 'Admin', 'System', true, NULL),
('mdiouf', 'password123', 'mamadou.diouf@univ-sn.sn', 'manager', 'Mamadou', 'Diouf', true, NULL),
('afall', 'password123', 'aissata.fall@univ-sn.sn', 'manager', 'Aissata', 'Fall', true, NULL),
('mdiagne', 'password123', 'mouhamadou.diagne@univ-sn.sn', 'enseignant', 'Mouhamadou', 'Diagne', true, NULL),
('ssow', 'password123', 'saly.sow@univ-sn.sn', 'enseignant', 'Saly', 'Sow', true, NULL),
('amba', 'password123', 'abdourahmane.ba@univ-sn.sn', 'enseignant', 'Abdourahmane', 'Ba', true, NULL),
('fgueye', 'password123', 'fatou.gueye@univ-sn.sn', 'enseignant', 'Fatou', 'Gueye', true, NULL),
('athiam', 'password123', 'abdou.thiam@univ-sn.sn', 'enseignant', 'Abdou', 'Thiam', true, NULL),
('mdiop', 'password123', 'mbaye.diop@univ-sn.sn', 'enseignant', 'Mbaye', 'Diop', true, NULL),
('lsy', 'password123', 'lamine.sy@univ-sn.sn', 'enseignant', 'Lamine', 'Sy', true, NULL),
('bndiaye', 'password123', 'birame.ndiaye@univ-sn.sn', 'enseignant', 'Birame', 'Ndiaye', true, NULL),
('ckane', 'password123', 'cheikh.kane@univ-sn.sn', 'enseignant', 'Cheikh', 'Kane', true, NULL),
('mdiop1', 'etudiant123', 'mamadou.diop1@etu.univ-sn.sn', 'etudiant', 'Mamadou', 'Diop', true, 'L1 INFO'),
('mdiop2', 'etudiant123', 'mamadou.diop2@etu.univ-sn.sn', 'etudiant', 'Mamadou', 'Diop', true, 'L2 INFO'),
('mdiop3', 'etudiant123', 'mamadou.diop3@etu.univ-sn.sn', 'etudiant', 'Mamadou', 'Diop', true, 'L3 INFO');

-- ============================================
-- 10. INSERTION DES COURS
-- ============================================
INSERT INTO courses (nom, enseignant_id, classe, groupe, jour_semaine, heure_debut, duree, salle_id) VALUES
('Mathématiques', 8, 'L1 INFO', 'CM', 'LUNDI', '08:00:00', 90, 1),
('Algorithmique', 6, 'L1 INFO', 'CM', 'LUNDI', '10:00:00', 90, 2),
('Programmation Java', 4, 'L2 INFO', 'CM', 'LUNDI', '08:00:00', 90, 11),
('Réseaux', 5, 'L2 INFO', 'CM', 'MARDI', '08:00:00', 90, 13),
('Intelligence Artificielle', 5, 'L3 INFO', 'CM', 'LUNDI', '08:00:00', 90, 21),
('Analyse 1', 8, 'LMI1', 'CM', 'LUNDI', '08:00:00', 90, 31),
('Mécanique 1', 10, 'LPC1', 'CM', 'LUNDI', '08:00:00', 90, 41),
('Psychologie', 11, 'LSEE1', 'CM', 'LUNDI', '08:00:00', 90, 51);

-- ============================================
-- 11. VÉRIFICATION FINALE
-- ============================================
SELECT '=== INSTALLATION TERMINÉE ===' as '';
SELECT COUNT(*) as total_buildings FROM buildings;
SELECT COUNT(*) as total_rooms FROM rooms;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_courses FROM courses;