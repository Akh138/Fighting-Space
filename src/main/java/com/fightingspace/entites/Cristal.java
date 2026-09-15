package com.fightingspace.entites;

import java.awt.*;
import javax.swing.ImageIcon;

/**
 * Ma classe Cristal : c'est l'objectif central du niveau.
 * Il a sa propre vie, ses propres images de dégâts et une petite animation.
 */
public class Cristal {

    // --- 1. MES VARIABLES DE BASE ---
    public int x, y;                        // Ma position sur la carte
    public int width = 120, height = 120;   // Ma taille d'affichage
    public int hp = 400;                    // Mes points de vie (je suis assez costaud)
    private int maxHp = 400;                // Ma vie maximum pour calculer mes paliers

    // --- 2. GESTION DU VISUEL (DAMAGE STATES) ---
    private Image[] sprites = new Image[3]; // Mon tableau pour stocker mes 3 états (Neuf, Fissuré, Abîmé)
    private Image currentSprite;            // L'image que je suis en train d'afficher

    // --- 3. ANIMATION DE FLOTTEMENT ---
    private double animTimer = 0;           // Un compteur qui tourne en continu
    private int offsetY = 0;                // Le décalage en pixels (haut/bas) pour l'effet flottant

    // --- MON CONSTRUCTEUR ---
    public Cristal(int x, int y) {
        this.x = x;
        this.y = y;

        // Je charge mes images depuis le dossier resources
        try {
            sprites[0] = new ImageIcon(getClass().getResource("/sprites/cristal.png")).getImage();
            sprites[1] = new ImageIcon(getClass().getResource("/sprites/cristal_1.png")).getImage();
            sprites[2] = new ImageIcon(getClass().getResource("/sprites/cristal_2.png")).getImage();
            currentSprite = sprites[0]; // Par défaut, je suis tout neuf
        } catch (Exception e) {
            System.out.println("Oups, j'ai eu un problème pour charger les images du cristal !");
        }
    }

    // --- MA LOGIQUE (UPDATE) ---
    public void update() {
        // ÉTAPE A : Je change d'image selon ma vie restante
        float vieRestante = (float) hp / maxHp;

        if (vieRestante > 0.66) {
            currentSprite = sprites[0]; // Je suis à plus de 66% de vie
        } else if (vieRestante > 0.33) {
            currentSprite = sprites[1]; // Je suis entre 33% et 66% de vie (premières fissures)
        } else {
            currentSprite = sprites[2]; // Je suis presque détruit
        }

        // ÉTAPE B : Mon animation de flottement "magique"
        // J'utilise une fonction Sinus pour faire varier mon offsetY de façon fluide
        animTimer += 0.05;
        offsetY = (int) (Math.sin(animTimer) * 12); // Je monte et je descends de 12 pixels
    }

    // --- MON AFFICHAGE (DRAW) ---
    public void draw(Graphics g) {
        if (currentSprite != null) {
            // Je me dessine en ajoutant le décalage de mon animation sur l'axe Y
            g.drawImage(currentSprite, x, y + offsetY, width, height, null);
        }

        // ÉTAPE C : Je dessine ma barre de vie juste au-dessus de moi
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y + offsetY - 20, width, 8); // Le fond de ma barre

        g.setColor(new Color(180, 0, 255)); // Une couleur Magenta pour ma vie
        int barWidth = (int)((hp / (float)maxHp) * width);
        g.fillRect(x, y + offsetY - 20, barWidth, 8); // La jauge de vie
    }

    // --- MES MÉTHODES UTILITAIRES ---

    // Pour que les missiles sachent s'ils m'ont touché
    public Rectangle getBounds() {
        // Ma hitbox suit aussi mon animation de flottement pour être précise !
        return new Rectangle(x, y + offsetY, width, height);
    }

    // Pour diminuer mes HP quand on me tire dessus
    public void receiveDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    // Pour vérifier si je suis toujours là ou si j'ai explosé
    public boolean isAlive() {
        return hp > 0;
    }
}