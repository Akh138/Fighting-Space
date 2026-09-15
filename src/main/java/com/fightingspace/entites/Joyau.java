package com.fightingspace.entites;

import java.awt.*;
import javax.swing.ImageIcon;

/**
 * Ma classe Joyau : ce sont les récompenses qui apparaissent
 * quand le Cristal est détruit ou qu'un joueur explose.
 */
public class Joyau {

    // --- 1. MES VARIABLES DE BASE ---
    public int x, y;                        // Ma position au sol
    public int width = 45, height = 45;     // Ma taille (un peu plus petit qu'un vaisseau)
    public boolean estActif = true;         // Si faux, je disparais de l'écran
    private Image sprite;

    // Variables pour mes effets visuels
    private double animTimer = Math.random() * 10; // Pour que chaque joyau scintille de façon décalée
    private int offsetY = 0;

    // --- MON CONSTRUCTEUR ---
    public Joyau(int x, int y) {
        // Je place le joyau là où l'objet a explosé
        this.x = x;
        this.y = y;

        try {
            this.sprite = new ImageIcon(getClass().getResource("/sprites/joyau.png")).getImage();
        } catch (Exception e) {
            System.out.println("Erreur : Je n'ai pas trouvé l'image joyau.png");
        }
    }

    // --- MA LOGIQUE ET MON AFFICHAGE (DRAW) ---
    public void draw(Graphics g) {
        if (sprite != null && estActif) {
            // Je transforme le Graphics en Graphics2D pour gérer la transparence (scintillement)
            Graphics2D g2d = (Graphics2D) g.create();

            // Pour que le dessin soit lisse
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // ÉTAPE A : Mon animation de flottement
            animTimer += 0.07;
            offsetY = (int) (Math.sin(animTimer) * 6); // Je monte et descends de 6 pixels

            // ÉTAPE B : Mon effet de scintillement (Twinkle)
            // Je fais varier l'opacité entre 0.4 (presque invisible) et 1.0 (bien brillant)
            float opacite = (float) (Math.sin(animTimer * 1.5) * 0.3 + 0.7);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacite));

            // Je me dessine avec mon décalage de flottement
            g2d.drawImage(sprite, x, y + offsetY, width, height, null);

            g2d.dispose(); // Je libère la mémoire du pinceau
        }
    }

    // --- MA HITBOX ---
    // Elle sert à savoir si un joueur passe sur moi pour me ramasser
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}