package com.fightingSchool.niveaux;

import com.fightingSchool.entites.Cristal;
import com.fightingSchool.entites.Fighter;
import com.fightingSchool.entites.Roche;
import java.util.List;

/**
 * Mon Gestionnaire de Niveaux (Level Manager).
 * C'est ici que je dessine la carte ! Mon rôle unique est de placer
 * les roches, le cristal et de positionner les joueurs au départ.
 */
public class LevelManager {

    // --- NIVEAU 1 : LA FORTERESSE TOURNANTE ---
    public Cristal genererNiveau1(List<Roche> listeRoches, Fighter p1, Fighter p2) {
        // Je vide la carte avant de commencer
        listeRoches.clear();

        // 1. LES ROCHES (3 anneaux concentriques)
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
    public Cristal genererNiveau2(List<Roche> listeRoches, Fighter p1, Fighter p2) {
        listeRoches.clear();

        // 1. LE CRISTAL (Décalé à droite pour faire une zone d'objectif)
        Cristal cristalBoss = new Cristal(750, 520 / 2 - 60);

        // 2. LE MUR SÉPARATEUR (Empêche les joueurs de se tirer dessus direct)
        for (int i = 0; i < 5; i++) {
            listeRoches.add(new Roche(100 + (i * 75), 230, 0, 0));
        }

        // 3. LA FORTERESSE DU CRISTAL (Roches Fixes pour obliger l'attaque frontale)
        for (int i = -1; i < 8; i++) {
            listeRoches.add(new Roche(850, i * 75, 0, 0)); // Le dos
        }
        for (int i = 0; i < 6; i++) {
            listeRoches.add(new Roche(450 + (i * 70), -15, 0, 0)); // Le plafond
            listeRoches.add(new Roche(450 + (i * 70), 450, 0, 0)); // Le plancher
        }

        // Le double bouclier en demi-cercle devant le cristal
        int cX = 780 + 60;
        int cY = 520 / 2;
        for (double angle = -Math.PI/2 + 0.3; angle <= Math.PI/2 - 0.3; angle += 0.4) {
            listeRoches.add(new Roche((int) (cX - Math.cos(angle) * 130) - 42, (int) (cY + Math.sin(angle) * 130) - 42, 0, 0));
        }
        for (double angle = -Math.PI/2 + 0.1; angle <= Math.PI/2 - 0.1; angle += 0.3) {
            listeRoches.add(new Roche((int) (cX - Math.cos(angle) * 190) - 42, (int) (cY + Math.sin(angle) * 190) - 42, 0, 0));
        }

        // 4. LE TAPIS ROULANT (Roches mobiles qui tombent et montent)
        for (int i = -3; i < 9; i++) {
            listeRoches.add(new Roche(330, i * 85, 2, 3.0)); // Descend (Type 2, Vitesse 3)
            listeRoches.add(new Roche(415, i * 85, 2, -3.5)); // Monte (Type 2, Vitesse -3.5)
        }

        // 5. LES JOUEURS (Placés tous les deux à gauche pour faire la course)
        p1.x = 40; p1.y = 80; p1.angle = 90;
        p2.x = 40; p2.y = 350; p2.angle = 90;

        return cristalBoss;
    }

    // --- UTILITAIRE INTERNE ---
    // Méthode pour éviter de répéter le code des cercles au Niveau 1
    private void creerAnneauDeRoches(List<Roche> liste, double rayon, int nombre, double vitesse) {
        for (int i = 0; i < nombre; i++) {
            double angleInitial = i * (2 * Math.PI / nombre);
            liste.add(new Roche(rayon, angleInitial, vitesse));
        }
    }
}