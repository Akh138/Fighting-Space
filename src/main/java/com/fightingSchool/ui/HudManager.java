package com.fightingSchool.ui;

import com.fightingSchool.entites.Fighter;
import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

/**
 * Mon Gestionnaire d'Interface (HUD).
 * Je gère le dessin des barres de vie, d'énergie et le texte des niveaux.
 */
public class HudManager {

    // Mes images pour le HUD miroir
    private Image imgHudGauche;
    private Image imgHudDroite;

    // Je stocke le max HP pour mes calculs de jauge (100)
    private final int MAX_HP = 100;

    public HudManager() {
        // --- CHARGEMENT DU HUD GAUCHE ET DROITE ---
        try {
            imgHudGauche = new ImageIcon(getClass().getResource("/sprites/hud_gauche.png")).getImage();
            imgHudDroite = new ImageIcon(getClass().getResource("/sprites/hud_droite.png")).getImage();
        } catch (Exception e) {
            System.out.println("Erreur : Impossible de charger les images du HUD.");
        }
    }

    // --- LA MÉTHODE PRINCIPALE APPELÉE PAR LE GAMEPANEL ---
    public void draw(Graphics2D g2d, Fighter p1, Fighter p2, int niveauActuel, int screenWidth) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform ecranNormal = g2d.getTransform();

        // Le réglage d'échelle (60%)
        double echelle = 0.60;

        // --- HUD JOUEUR 1 (À GAUCHE) ---
        if (!p1.estEnAttenteDeRespawn) {
            g2d.translate(0, 0);
            g2d.scale(echelle, echelle);
            dessinerBlocStat(g2d, 0, 0, "PILOTE : J1", p1, Color.GREEN, true);
            g2d.setTransform(ecranNormal);
        }

        // --- HUD JOUEUR 2 (À DROITE) ---
        if (!p2.estEnAttenteDeRespawn) {
            int largeurReduite = (int) (400 * echelle);
            g2d.translate(screenWidth - largeurReduite, 0);
            g2d.scale(echelle, echelle);
            dessinerBlocStat(g2d, 0, 0, "PILOTE : J2", p2, new Color(255, 100, 0), false);
            g2d.setTransform(ecranNormal);
        }

        // Affichage du niveau actuel au centre
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.drawString("NIVEAU " + niveauActuel, screenWidth / 2 - 45, 30);
    }

    // --- LA MÉTHODE DE DESSIN PRÉCISE (Avec tes réglages) ---
    private void dessinerBlocStat(Graphics2D g2d, int x, int y, String nom, Fighter f, Color couleurVie, boolean estJoueur1) {

        // 1. DESSIN DU CADRE
        Image imageAUtiliser = estJoueur1 ? imgHudGauche : imgHudDroite;
        if (imageAUtiliser != null) {
            g2d.drawImage(imageAUtiliser, x, y, 400, 110, null);
        }

        // --- RÉGLAGES DE PRÉCISION DU LOGO ---
        int logoX, barreX;
        int logoTaille = 92;
        int logoY = y + 10;

        int largeurBarre = 190;
        int hauteurBarreY = y + 44;

        if (estJoueur1) {
            // J1 : Le cercle est à GAUCHE.
            logoX = x + 43;   // Ton réglage !
            barreX = x + 135;
        } else {
            // J2 : Le cercle est à DROITE.
            logoX = x + 263;  // Ton réglage !
            barreX = x + 75;
        }

        // 2. DESSIN DU LOGO (Le vaisseau)
        Image logo = f.getIcone();
        if (logo != null) {
            g2d.drawImage(logo, logoX, logoY, logoTaille, logoTaille, null);
        }

        // 3. DESSIN DES TEXTES ET BARRES
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2d.drawString(nom, barreX, hauteurBarreY - 10);

        // Barre Vie
        int hpW = (int) ((f.model.getHp() / (float) MAX_HP) * largeurBarre);
        g2d.setColor(new Color(20, 20, 20));
        g2d.fillRect(barreX, hauteurBarreY, largeurBarre, 10);
        g2d.setColor(couleurVie);
        g2d.fillRect(barreX, hauteurBarreY, Math.max(0, hpW), 10);

        // Barre Energie
        int nrgW = (int) ((f.model.getEnergy() / 100f) * largeurBarre);
        g2d.setColor(new Color(20, 20, 20));
        g2d.fillRect(barreX, hauteurBarreY + 14, largeurBarre, 7);
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(barreX, hauteurBarreY + 14, nrgW, 7);

        // Barre Bouclier
        if (f.shieldHp > 0) {
            int shdW = (int) ((f.shieldHp / (float) f.maxShieldHp) * largeurBarre);
            g2d.setColor(new Color(0, 191, 255));
            g2d.fillRect(barreX, hauteurBarreY + 24, shdW, 4);
        }

        // Joyaux
        g2d.setColor(new Color(200, 50, 255));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.drawString("💎 : " + f.scoreJoyaux, barreX, hauteurBarreY + 42);
    }
}