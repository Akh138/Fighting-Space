package com.fightingSchool.entites;

import java.awt.*;
import javax.swing.ImageIcon;

public class Roche {

    // --- 1. VARIABLES DE BASE ---
    public int x, y;
    public int width = 85, height = 85;
    public int hp = 40;
    private int maxHp = 40;

    // --- 2. VARIABLES DE MOUVEMENT (Chaque roche a les siennes !) ---
    public int typeMouvement; // 0=Fixe, 1=Cercle, 2=Vertical (Ligne)

    // Variables pour le mouvement Circulaire (Type 1 - Niveau 1)
    private double rayon;
    private double angleOrbite;
    private double vitesseRotation;

    // Variables pour le mouvement Vertical (Type 2 - Niveau 2)
    private double vitesseVerticale;

    // --- 3. VISUEL (OPTIMISÉ AVEC STATIC) ---
    // STATIC : Les images sont partagées par toutes les roches pour éviter les 162 chargements
    private static Image[] sprites = null;
    private Image currentSprite; // L'image individuelle affichée selon les dégâts

    /**
     * CONSTRUCTEUR 1 : Pour les Roches Fixes ou Verticales (Niveau 2)
     */
    public Roche(int startX, int startY, int typeMouvement, double vitesse) {
        this.x = startX;
        this.y = startY;
        this.typeMouvement = typeMouvement;
        this.vitesseVerticale = vitesse;
        chargerImages();
    }

    /**
     * CONSTRUCTEUR 2 : Pour les Roches en Orbite (Niveau 1)
     */
    public Roche(double rayon, double angleInitial, double vitesseRotation) {
        this.typeMouvement = 1;
        this.rayon = rayon;
        this.angleOrbite = angleInitial;
        this.vitesseRotation = vitesseRotation;
        chargerImages();
    }

    // Je ne charge les 3 images sur le disque QUE la première fois !
    private void chargerImages() {
        if (sprites == null) {
            try {
                sprites = new Image[3];
                sprites[0] = new ImageIcon(getClass().getResource("/sprites/roche.png")).getImage();
                sprites[1] = new ImageIcon(getClass().getResource("/sprites/roche_1.png")).getImage();
                sprites[2] = new ImageIcon(getClass().getResource("/sprites/roche_2.png")).getImage();
            } catch (Exception e) {
                System.out.println("Erreur images roche");
            }
        }
        if (sprites != null) {
            currentSprite = sprites[0];
        }
    }

    // --- MA LOGIQUE DE MISE À JOUR (Strictement inchangée !) ---
    public void update(int centerX, int centerY, int panelHeight) {
        // SELON MON TYPE, JE BOUGE DIFFÉREMMENT :

        if (typeMouvement == 1) {
            // Mouvement en Cercle (Niveau 1)
            angleOrbite += vitesseRotation;
            this.x = (int) (centerX + Math.cos(angleOrbite) * rayon) - (width / 2);
            this.y = (int) (centerY + Math.sin(angleOrbite) * rayon) - (height / 2);
        }
        else if (typeMouvement == 2) {
            // Mouvement Vertical (Niveau 2)
            this.y += vitesseVerticale;

            // Effet "Pac-Man" : Si je sors en bas, je reviens en haut (et vice versa)
            if (this.y > panelHeight + 50) {
                this.y = -height - 50;
            } else if (this.y + height < -50) {
                this.y = panelHeight + 50;
            }
        }
        // (Si typeMouvement == 0, je reste fixe)

        // Mise à jour visuelle (Dégâts)
        float vieRestante = (float) hp / maxHp;
        if (sprites != null) {
            if (vieRestante > 0.75) currentSprite = sprites[0];
            else if (vieRestante > 0.40) currentSprite = sprites[1];
            else currentSprite = sprites[2];
        }
    }

    public void draw(Graphics g) {
        if (currentSprite != null) g.drawImage(currentSprite, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        int marge = 15;
        return new Rectangle(x + marge, y + marge, width - (marge * 2), height - (marge * 2));
    }

    public void receiveDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    public boolean isAlive() { return hp > 0; }
}