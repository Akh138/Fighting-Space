package com.fightingSchool.entites;

import java.awt.*;
import javax.swing.ImageIcon;

/**
 * Ma classe DecorPlanete : elle gère les planètes qui défilent en arrière-plan.
 * Je l'ai sortie de GamePanel pour que mon code soit mieux rangé.
 */
public class DecorPlanete {
    // --- MES VARIABLES ---
    private double dx, dy;
    private double speed;
    private int size;
    private Image img;

    // --- MON CONSTRUCTEUR ---
    public DecorPlanete(int x, int y, int size, String path, double speed) {
        this.dx = x;
        this.dy = y;
        this.size = size;
        this.speed = speed;
        try {
            this.img = new ImageIcon(getClass().getResource(path)).getImage();
        } catch (Exception e) {
            System.out.println("Erreur image décor : " + path);
        }
    }

    // --- MA LOGIQUE DE DÉPLACEMENT ---
    public void update() {
        dx -= speed; // Je dérive lentement vers la gauche
        if (dx + size < 0) {
            dx = 950; // Si je sors de l'écran, je réapparais à droite
        }
    }

    // --- MON AFFICHAGE ---
    public void draw(Graphics2D g2d) {
        if (img != null) {
            // Je mets un peu de transparence pour l'effet d'éloignement
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.drawImage(img, (int) dx, (int) dy, size, size, null);
            // Je remets la transparence normale pour le reste du jeu
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
}