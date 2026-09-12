package com.fightingSchool.core;

import com.fightingSchool.entites.*;
import java.util.List;

/**
 * Mon Gestionnaire de Collisions (Collision Manager).
 * Je centralise toute la physique du jeu pour soulager mon GamePanel :
 * 1. Les impacts de tirs (contre vaisseaux, cristal, roches).
 * 2. Les collisions physiques (vaisseaux bloqués par les roches).
 * 3. La collecte des objets (joyaux et items bonus).
 */
public class CollisionManager {

    // --- 1. MA GESTION DES OBSTACLES SOLIDES ---
    public void gererObstaclesSolides(Fighter p1, Fighter p2, List<Roche> listeRoches,
                                      int oldP1X, int oldP1Y, int oldP2X, int oldP2Y) {
        for (Roche r : listeRoches) {
            // Si le Joueur 1 percute une roche
            if (p1.getBounds().intersects(r.getBounds())) {
                p1.x = oldP1X;
                p1.y = oldP1Y;
                p1.toucherObstacle();
            }
            // Si le Joueur 2 percute une roche
            if (p2.getBounds().intersects(r.getBounds())) {
                p2.x = oldP2X;
                p2.y = oldP2Y;
                p2.toucherObstacle();
            }
        }
    }

    // --- 2. VÉRIFICATION GLOBALE DES IMPACTS ET DU RAMASSAGE ---
    public void verifierCollisions(Fighter p1, Fighter p2, Cristal cristal,
                                   List<Roche> listeRoches, List<Joyau> listeJoyaux, List<Item> listeItems) {

        // A. Collisions pour le Joueur 1
        if (!p1.estEnAttenteDeRespawn) {
            verifierTirsJoueur(p1, p2, cristal, listeRoches, listeItems);
            verifierRamassage(p1, listeJoyaux, listeItems);
        }

        // B. Collisions pour le Joueur 2
        if (!p2.estEnAttenteDeRespawn) {
            verifierTirsJoueur(p2, p1, cristal, listeRoches, listeItems);
            verifierRamassage(p2, listeJoyaux, listeItems);
        }

        // C. Nettoyage des listes d'objets ramassés ou détruits
        listeJoyaux.removeIf(j -> !j.estActif);
        listeRoches.removeIf(r -> !r.isAlive());
        listeItems.removeIf(it -> !it.estActif);
    }

    // --- MES MÉTHODES INTERNES DE CALCUL ---

    // Vérifie où atterrissent les projectiles du tireur
    private void verifierTirsJoueur(Fighter tireur, Fighter cible, Cristal cristal,
                                    List<Roche> listeRoches, List<Item> listeItems) {
        for (Projectile proj : tireur.projectiles) {
            if (!proj.actif) continue;

            // Touche la cible adverse ?
            if (proj.getBounds().intersects(cible.getBounds())) {
                cible.receiveDamage(tireur.calcDamageTo(cible));
                cible.triggerExplosion((int) proj.x, (int) proj.y, 10);
                proj.actif = false;
            }
            // Touche le Cristal Boss ?
            else if (cristal != null && proj.getBounds().intersects(cristal.getBounds())) {
                cristal.receiveDamage(10);
                tireur.triggerExplosion((int) proj.x, (int) proj.y, 10);
                proj.actif = false;
            }
            // Touche un rocher ?
            else {
                for (Roche r : listeRoches) {
                    if (proj.getBounds().intersects(r.getBounds())) {
                        r.receiveDamage(10);
                        tireur.triggerExplosion((int) proj.x, (int) proj.y, 5);
                        proj.actif = false;
                        if (!r.isAlive()) {
                            tenterLacherItem(r.x, r.y, listeItems);
                        }
                        break;
                    }
                }
            }
        }
    }

    // Vérifie si le vaisseau roule sur un joyau ou un bonus
    private void verifierRamassage(Fighter f, List<Joyau> listeJoyaux, List<Item> listeItems) {
        // Collecte des Joyaux
        for (Joyau j : listeJoyaux) {
            if (j.estActif && f.getBounds().intersects(j.getBounds())) {
                f.scoreJoyaux++;
                j.estActif = false;
            }
        }
        // Collecte des Items (Vie, Énergie, Bouclier)
        for (Item it : listeItems) {
            if (it.estActif && f.getBounds().intersects(it.getBounds())) {
                appliquerEffetItem(f, it);
                it.estActif = false;
            }
        }
    }

    // Tente de faire tomber un bonus à la mort d'un rocher
    private void tenterLacherItem(int x, int y, List<Item> listeItems) {
        if (Math.random() < 0.4) {
            listeItems.add(new Item(x, y, (int) (Math.random() * 3)));
        }
    }

    // Applique l'effet du bonus ramassé
    private void appliquerEffetItem(Fighter f, Item it) {
        if (it.type == 0) f.model.receiveDamage(-30);
        else if (it.type == 1) f.model.restoreEnergy(50);
        else if (it.type == 2) f.activerBouclier();
    }
}