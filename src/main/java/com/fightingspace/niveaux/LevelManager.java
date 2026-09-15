package com.fightingspace.niveaux;

import com.fightingspace.entites.Cristal;
import com.fightingspace.entites.Fighter;
import com.fightingspace.entites.Roche;
import com.fightingspace.entites.Ennemi; // J'importe les monstres pour le niveau 3
import java.util.List;

/**
 * Mon Gestionnaire de Niveaux (Level Manager).
 * C'est ici que je dessine les cartes. Je place les roches, le cristal,
 * les monstres et je positionne les joueurs au départ de chaque niveau.
 */
public class LevelManager {

    // --- NIVEAU 1 : LA FORTERESSE TOURNANTE ---
    public Cristal genererNiveau1(List<Roche> listeRoches, List<Ennemi> listeEnnemis, Fighter p1, Fighter p2) {
        // Je nettoie la carte
        listeRoches.clear();
        listeEnnemis.clear(); // Pas de monstres au niveau 1

        // 1. LES ROCHES (3 anneaux concentriques tournants)
        creerAnneauDeRoches(listeRoches, 90, 12, 0.005);
        creerAnneauDeRoches(listeRoches, 150, 18, -0.008);
        creerAnneauDeRoches(listeRoches, 210, 24, 0.003);

        // 2. LES JOUEURS (Placés en face à face aux extrémités)
        p1.x = 40;
        p1.y = 520 / 2 - (p1.height / 2);
        p1.angle = 90;

        p2.x = 900 - 40 - p2.width;
        p2.y = 520 / 2 - (p2.height / 2);
        p2.angle = -90;

        // 3. LE CRISTAL (Au centre parfait)
        return new Cristal(900 / 2 - 60, 520 / 2 - 60);
    }

    // --- NIVEAU 2 : LE COULOIR DE LA MORT ---
    public Cristal genererNiveau2(List<Roche> listeRoches, List<Ennemi> listeEnnemis, Fighter p1, Fighter p2) {
        listeRoches.clear();
        listeEnnemis.clear(); // Pas de monstres au niveau 2 non plus

        // 1. LE CRISTAL (Décalé à droite)
        Cristal cristalBoss = new Cristal(750, 520 / 2 - 60);

        // 2. LE MUR SÉPARATEUR
        for (int i = 0; i < 5; i++) {
            listeRoches.add(new Roche(100 + (i * 75), 230, 0, 0));
        }

        // 3. LA FORTERESSE DU CRISTAL (Roches Fixes)
        for (int i = -1; i < 8; i++) {
            listeRoches.add(new Roche(850, i * 75, 0, 0));
        }
        for (int i = 0; i < 6; i++) {
            listeRoches.add(new Roche(450 + (i * 70), -15, 0, 0));
            listeRoches.add(new Roche(450 + (i * 70), 450, 0, 0));
        }

        int cX = 780 + 60;
        int cY = 520 / 2;
        for (double angle = -Math.PI/2 + 0.3; angle <= Math.PI/2 - 0.3; angle += 0.4) {
            listeRoches.add(new Roche((int) (cX - Math.cos(angle) * 130) - 42, (int) (cY + Math.sin(angle) * 130) - 42, 0, 0));
        }
        for (double angle = -Math.PI/2 + 0.1; angle <= Math.PI/2 - 0.1; angle += 0.3) {
            listeRoches.add(new Roche((int) (cX - Math.cos(angle) * 190) - 42, (int) (cY + Math.sin(angle) * 190) - 42, 0, 0));
        }

        // 4. LE TAPIS ROULANT (Roches mobiles)
        for (int i = -3; i < 9; i++) {
            listeRoches.add(new Roche(330, i * 85, 2, 3.0));
            listeRoches.add(new Roche(415, i * 85, 2, -3.5));
        }

        // 5. LES JOUEURS (Tous les deux à gauche)
        p1.x = 40; p1.y = 80; p1.angle = 90;
        p2.x = 40; p2.y = 350; p2.angle = 90;

        return cristalBoss;
    }

    // --- NIVEAU 3 : LE LABYRINTHE ET LES MONSTRES (Ton dessin) ---
    public Cristal genererNiveau3(List<Roche> listeRoches, List<Ennemi> listeEnnemis, Fighter p1, Fighter p2) {
        listeRoches.clear();
        listeEnnemis.clear();

        // 1. LE CRISTAL (À droite, entouré d'un cercle complet de roches fixes)
        int cristalX = 750;
        int cristalY = 520 / 2 - 60;
        Cristal cristalBoss = new Cristal(cristalX, cristalY);

        // Cercle de protection complet autour du cristal (Le losange sur ton dessin)
        int cristalCenterX = cristalX + 60;
        int cristalCenterY = cristalY + 60;
        for (double angle = 0; angle < 2 * Math.PI; angle += 0.6) {
            int posX = (int) (cristalCenterX + Math.cos(angle) * 100) - 42;
            int posY = (int) (cristalCenterY + Math.sin(angle) * 100) - 42;
            listeRoches.add(new Roche(posX, posY, 0, 0)); // Type 0 = Fixe
        }

        // 2. LE MUR SÉPARATEUR DU DÉPART (Les roches avec un trait au milieu à gauche)
        for (int i = 0; i < 5; i++) {
            listeRoches.add(new Roche(150 + (i * 65), 240, 0, 0)); // Fixes
        }

        // 3. LES COLONNES MOBILES (Le tapis roulant du milieu)
        // Colonne de gauche qui descend
        for (int i = -2; i < 8; i++) {
            listeRoches.add(new Roche(340, i * 85, 2, 2.5)); // Type 2 = Vertical, Vitesse positive = descend
        }
        // Colonne de droite qui monte
        for (int i = -2; i < 8; i++) {
            listeRoches.add(new Roche(520, i * 85, 2, -2.5)); // Vitesse négative = monte
        }

        // 4. LES MONSTRES (Les étoiles sur ton dessin)
        // Je les place entre les deux colonnes de roches mobiles
        listeEnnemis.add(new Ennemi(430, 150));
        listeEnnemis.add(new Ennemi(430, 350));
        listeEnnemis.add(new Ennemi(650, 420)); // Un monstre près de la base du cristal

        // 5. LES JOUEURS (Les rectangles de départ à gauche, l'un en haut, l'autre en bas)
        p1.x = 40; p1.y = 100; p1.angle = 90; // J1 en haut à gauche
        p2.x = 40; p2.y = 360; p2.angle = 90; // J2 en bas à gauche

        return cristalBoss;
    }

    // --- UTILITAIRE INTERNE ---
    private void creerAnneauDeRoches(List<Roche> liste, double rayon, int nombre, double vitesse) {
        for (int i = 0; i < nombre; i++) {
            double angleInitial = i * (2 * Math.PI / nombre);
            liste.add(new Roche(rayon, angleInitial, vitesse));
        }
    }
}