package com.fightingspace.core;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import com.fightingspace.entites.Fighter;

/**
 * Mon Gestionnaire de Manettes avec détection automatique sur les 4 ports Windows.
 */
public class GamepadManager {

    private ControllerManager controllers;
    private boolean estActif = false;
    private boolean messageAffiche = false; // Pour ne pas spammer la console

    // Mémorisation des boutons pour les tirs
    private boolean p1PrevX = false, p1PrevY = false;
    private boolean p2PrevX = false, p2PrevY = false;

    public GamepadManager() {
        try {
            controllers = new ControllerManager();
            controllers.initSDLGamepad();
            estActif = true;
            System.out.println("Succès : Gestionnaire de manettes prêt ! En attente de connexion...");
        } catch (Exception e) {
            System.out.println("Erreur initialisation manette : " + e.getMessage());
            estActif = false;
        }
    }

    public void update(Fighter p1, Fighter p2) {
        if (!estActif || controllers == null) return;

        // Je rafraîchis l'état matériel
        controllers.update();

        // Je cherche la première manette connectée parmi les 4 ports possibles de Windows
        int indexPremiereManette = -1;
        int indexDeuxiemeManette = -1;

        for (int i = 0; i < 4; i++) {
            ControllerState state = controllers.getState(i);
            if (state.isConnected) {
                if (indexPremiereManette == -1) {
                    indexPremiereManette = i;
                } else if (indexDeuxiemeManette == -1) {
                    indexDeuxiemeManette = i;
                }
            }
        }

        // Si j'ai trouvé au moins une manette
        if (indexPremiereManette != -1) {
            if (!messageAffiche) {
                System.out.println("🎮 MANETTE DÉTECTÉE sur le port " + indexPremiereManette + " ! Elle contrôle le Joueur 2 (Orange).");
                messageAffiche = true;
            }

            // Si 2 manettes sont branchées : J1 et J2 jouent à la manette
            if (indexDeuxiemeManette != -1) {
                appliquerCommandes(indexPremiereManette, p1, true);
                appliquerCommandes(indexDeuxiemeManette, p2, false);
            }
            // Si 1 seule manette est branchée : elle contrôle le Joueur 2 (P1 reste au clavier WASD)
            else {
                appliquerCommandes(indexPremiereManette, p2, false);
            }
        }
    }

    private void appliquerCommandes(int indexManette, Fighter joueur, boolean estJoueur1) {
        ControllerState state = controllers.getState(indexManette);
        if (!state.isConnected || joueur.estEnAttenteDeRespawn) return;

        // 1. DIRECTION (Stick Gauche OU Croix Directionnelle)
        double zoneMorte = 0.35;
        joueur.left  = state.dpadLeft  || (state.leftStickX < -zoneMorte);
        joueur.right = state.dpadRight || (state.leftStickX > zoneMorte);
        // Note : sur manette Xbox, pousser le stick vers le haut donne un Y négatif ou positif selon SDL
        joueur.up    = state.dpadUp    || (state.leftStickY > zoneMorte);
        joueur.down  = state.dpadDown  || (state.leftStickY < -zoneMorte);

        // 2. TIR NORMAL (Bouton A)
        if (state.a) {
            joueur.fire(0);
        }

        // 3. TIR PUISSANT (Bouton X)
        boolean prevX = estJoueur1 ? p1PrevX : p2PrevX;
        if (state.x && !prevX) {
            joueur.specialFire(0);
        }

        // 4. TIR TÊTE CHERCHEUSE (Bouton Y)
        boolean prevY = estJoueur1 ? p1PrevY : p2PrevY;
        if (state.y && !prevY) {
            joueur.fireHoming();
        }

        // Mémorisation
        if (estJoueur1) {
            p1PrevX = state.x;
            p1PrevY = state.y;
        } else {
            p2PrevX = state.x;
            p2PrevY = state.y;
        }
    }

    public void liberer() {
        if (estActif && controllers != null) {
            controllers.quitSDLGamepad();
        }
    }
}