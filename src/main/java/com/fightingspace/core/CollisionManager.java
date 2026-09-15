package com.fightingspace.core;

import com.fightingspace.entites.*;
import java.util.List;

/**
 * Mon Gestionnaire de Collisions (Collision Manager).
 * Je centralise toute la physique du jeu pour soulager mon GamePanel :
 * 1. Les impacts de tirs (contre vaisseaux, cristal, roches et monstres) avec les sons associés.
 * 2. Les collisions physiques (vaisseaux bloqués par les roches, et monstres bloqués par les roches).
 * 3. La collecte des objets (joyaux et items bonus) avec les sons de ramassage.
 */
public class CollisionManager {

    // --- 1. MA GESTION DES OBSTACLES SOLIDES POUR LES JOUEURS ---
    public void gererObstaclesSolides(Fighter p1, Fighter p2, List<Roche> listeRoches,
                                      int oldP1X, int oldP1Y, int oldP2X, int oldP2Y) {
        for (Roche r : listeRoches) {
            // Si le Joueur 1 percute une roche, je le bloque et je le blesse
            if (p1.getBounds().intersects(r.getBounds())) {
                p1.x = oldP1X;
                p1.y = oldP1Y;
                p1.toucherObstacle();
            }
            // Si le Joueur 2 percute une roche, je le bloque et je le blesse
            if (p2.getBounds().intersects(r.getBounds())) {
                p2.x = oldP2X;
                p2.y = oldP2Y;
                p2.toucherObstacle();
            }
        }
    }

    // --- NOUVEAUTÉ : LES ROCHES BLOQUENT LES MONSTRES SANS LES BLESSER ---
    public void gererObstaclesEnnemis(List<Ennemi> listeEnnemis, List<Roche> listeRoches) {
        for (Ennemi e : listeEnnemis) {
            if (!e.isAlive()) continue;

            for (Roche r : listeRoches) {
                // Si un monstre touche une roche, je l'empêche d'avancer
                if (e.getBounds().intersects(r.getBounds())) {
                    // Je le recule un peu ou je bloque son déplacement
                    // (L'IA de l'ennemi le fera glisser ou contourner au tour suivant)
                    break;
                }
            }
        }
    }

    // --- 2. VÉRIFICATION GLOBALE DES IMPACTS ET DU RAMASSAGE ---
    public void verifierCollisions(Fighter p1, Fighter p2, Cristal cristal,
                                   List<Roche> listeRoches, List<Joyau> listeJoyaux,
                                   List<Item> listeItems, List<Ennemi> listeEnnemis) {

        // A. Collisions pour le Joueur 1
        if (!p1.estEnAttenteDeRespawn) {
            verifierTirsJoueur(p1, p2, cristal, listeRoches, listeItems, listeEnnemis);
            verifierRamassage(p1, listeJoyaux, listeItems);
        }

        // B. Collisions pour le Joueur 2
        if (!p2.estEnAttenteDeRespawn) {
            verifierTirsJoueur(p2, p1, cristal, listeRoches, listeItems, listeEnnemis);
            verifierRamassage(p2, listeJoyaux, listeItems);
        }

        // C. Dégâts de contact : Si un monstre touche un joueur (Le joueur prend des dégâts, pas le monstre)
        for (Ennemi e : listeEnnemis) {
            if (!e.isAlive()) continue;

            if (!p1.estEnAttenteDeRespawn && p1.getBounds().intersects(e.getBounds())) {
                p1.receiveDamage(10); // Le joueur perd de la vie au contact
                p1.triggerExplosion(p1.x, p1.y, 10);
            }
            if (!p2.estEnAttenteDeRespawn && p2.getBounds().intersects(e.getBounds())) {
                p2.receiveDamage(10);
                p2.triggerExplosion(p2.x, p2.y, 10);
            }
        }

        // D. Je nettoie toutes mes listes d'objets et d'ennemis morts
        listeJoyaux.removeIf(j -> !j.estActif);
        listeRoches.removeIf(r -> !r.isAlive());
        listeItems.removeIf(it -> !it.estActif);
        listeEnnemis.removeIf(e -> !e.isAlive());
    }

    // --- MES MÉTHODES INTERNES DE CALCUL ---

    // Je vérifie où atterrissent les projectiles du tireur
    private void verifierTirsJoueur(Fighter tireur, Fighter cible, Cristal cristal,
                                    List<Roche> listeRoches, List<Item> listeItems, List<Ennemi> listeEnnemis) {
        for (Projectile proj : tireur.projectiles) {
            if (!proj.actif) continue;

            // 1. Est-ce que je touche la cible adverse ?
            if (proj.getBounds().intersects(cible.getBounds())) {
                cible.receiveDamage(tireur.calcDamageTo(cible));
                cible.triggerExplosion((int) proj.x, (int) proj.y, 10);
                proj.actif = false;

                // --- SON : Impact sur un vaisseau ---
                SoundManager.getInstance().jouerSon("/sons/impact_vaisseau.wav");
            }
            // 2. Est-ce que je touche le Cristal Boss ?
            else if (cristal != null && proj.getBounds().intersects(cristal.getBounds())) {
                cristal.receiveDamage(10);
                tireur.triggerExplosion((int) proj.x, (int) proj.y, 10);
                proj.actif = false;

                // --- SON : Impact sur le cristal ---
                SoundManager.getInstance().jouerSon("/sons/impact_cristal.wav");
            }
            // 3. Est-ce que je touche un monstre (Ennemi) ?
            else {
                boolean toucheMonstre = false;
                for (Ennemi e : listeEnnemis) {
                    if (e.isAlive() && proj.getBounds().intersects(e.getBounds())) {
                        e.receiveDamage(15); // Le monstre prend des dégâts
                        tireur.triggerExplosion((int) proj.x, (int) proj.y, 8);
                        proj.actif = false;
                        toucheMonstre = true;

                        // Si le monstre meurt sous mes tirs, il lâche du butin !
                        if (!e.isAlive()) {
                            tenterLacherItem((int)e.x, (int)e.y, listeItems);
                        }
                        break;
                    }
                }

                // 4. Si je ne touche ni joueur, ni cristal, ni monstre -> Est-ce que je touche un rocher ?
                if (!toucheMonstre) {
                    for (Roche r : listeRoches) {
                        if (proj.getBounds().intersects(r.getBounds())) {
                            r.receiveDamage(10);
                            tireur.triggerExplosion((int) proj.x, (int) proj.y, 5);
                            proj.actif = false;

                            // Si le rocher vient de mourir sous ce coup
                            if (!r.isAlive()) {
                                // --- SON : Explosion de la roche ---
                                SoundManager.getInstance().jouerSon("/sons/explosion_roche.wav");
                                tenterLacherItem(r.x, r.y, listeItems);
                            } else {
                                // --- SON : Simple impact sur la roche ---
                                SoundManager.getInstance().jouerSon("/sons/impact_roche.wav");
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    // Je vérifie si le vaisseau roule sur un joyau ou un bonus
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

                // --- SON : Ramassage d'un bonus ---
                SoundManager.getInstance().jouerSon("/sons/item.wav");
            }
        }
    }

    // Je tente de faire tomber un bonus à la mort d'un rocher ou d'un monstre (40% de chance)
    private void tenterLacherItem(int x, int y, List<Item> listeItems) {
        if (listeItems != null && Math.random() < 0.4) {
            int randomType = (int) (Math.random() * 3);
            listeItems.add(new Item(x, y, randomType));
        }
    }

    // J'applique l'effet du bonus ramassé
    private void appliquerEffetItem(Fighter f, Item it) {
        if (it.type == 0) {
            f.model.receiveDamage(-30); // Soin
        }
        else if (it.type == 1) {
            f.model.restoreEnergy(50); // Énergie
        }
        else if (it.type == 2) {
            f.activerBouclier(); // Bouclier
        }
    }
}