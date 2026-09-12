package com.fightingSchool.entites;

import java.awt.*;
import javax.swing.ImageIcon;

/**
 * Ma classe Item : ce sont les bonus qui tombent quand on détruit une roche.
 * Type 0 = Vie (Cœur), Type 1 = Energie (Éclair), Type 2 = Bouclier (Shield)
 */
public class Item {

    // --- 1. MES VARIABLES ---
    public int x, y;
    public int width = 40, height = 40;
    public int type; // 0: Vie, 1: NRJ, 2: Bouclier
    public boolean estActif = true;
    private Image sprite;

    // Pour mon animation de flottement
    private double animTimer = Math.random() * 10;

    public Item(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;

        // Je charge l'image correspondante
        String path = "";
        if (type == 0) path = "/sprites/item_vie.png";
        else if (type == 1) path = "/sprites/item_nrj.png";
        else path = "/sprites/item_bouclier.png";

        try {
            this.sprite = new ImageIcon(getClass().getResource(path)).getImage();
        } catch (Exception e) {
            System.out.println("Erreur image item : " + path);
        }
    }

    // --- 2. MON AFFICHAGE ---
    public void draw(Graphics g) {
        if (sprite != null && estActif) {
            // Petite animation de haut en bas
            animTimer += 0.1;
            int offset = (int)(Math.sin(animTimer) * 5);
            g.drawImage(sprite, x, y + offset, width, height, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}