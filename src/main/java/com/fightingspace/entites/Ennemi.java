package com.fightingspace.entites;

import java.awt.*;
import javax.swing.ImageIcon;
import java.awt.geom.AffineTransform;

/**
 * Ma classe Ennemi : l'IA du Niveau 3.
 * Ils sont maintenant de la même taille que les joueurs, plus résistants,
 * et leurs ailes/réacteurs s'animent en boucle !
 */
public class Ennemi {

    // --- 1. PROPRIÉTÉS ---
    public double x, y;
    public int width = 150, height = 150; // Agrandis pour faire la taille des vaisseaux !
    public int hp = 80;                   // Plus résistants (ne meurent plus en un coup)
    public double speed = 2.0;            // Vitesse de déplacement
    public double angle = 0;              // Direction vers la cible
    public boolean estVivant = true;

    // --- 2. GESTION DE L'ANIMATION (SPRITE SHEET) ---
    private Image[] sprites = new Image[4]; // Mes 4 images (ennemi_0 à ennemi_3)
    private int frameActuelle = 0;          // Indique l'image affichée
    private int animationTimer = 0;         // Compteur pour cadencer l'animation

    // --- MON CONSTRUCTEUR ---
    public Ennemi(int startX, int startY) {
        this.x = startX;
        this.y = startY;

        // Je charge mes 4 images d'animation depuis le dossier sprites
        try {
            for (int i = 0; i < 4; i++) {
                sprites[i] = new ImageIcon(getClass().getResource("/sprites/ennemi_" + i + ".png")).getImage();
            }
        } catch (Exception e) {
            System.out.println("Erreur : Impossible de charger les frames ennemi_0 à 3 !");
        }
    }

    // --- 3. MON INTELLIGENCE ARTIFICIELLE (UPDATE) ---
    public void update(Fighter p1, Fighter p2) {
        if (!estVivant) return;

        // A. Je cherche quel joueur est le plus proche
        Fighter cible = ciblerCibleLaPlusProche(p1, p2);

        if (cible != null) {
            // B. Je calcule l'angle vers lui
            double diffX = (cible.x + cible.width / 2.0) - (this.x + this.width / 2.0);
            double diffY = (cible.y + cible.height / 2.0) - (this.y + this.height / 2.0);
            double angleVersCible = Math.atan2(diffY, diffX);

            this.angle = Math.toDegrees(angleVersCible) + 90;

            // C. Je fonce sur lui
            this.x += Math.cos(angleVersCible) * speed;
            this.y += Math.sin(angleVersCible) * speed;
        }

        // D. J'anime mes sprites en boucle (je change d'image toutes les 8 frames)
        animationTimer++;
        if (animationTimer >= 8) {
            frameActuelle = (frameActuelle + 1) % 4; // Passe de 0 à 1, 2, 3 puis revient à 0
            animationTimer = 0;
        }
    }

    // Méthode pour choisir la cible
    private Fighter ciblerCibleLaPlusProche(Fighter p1, Fighter p2) {
        boolean p1Actif = !p1.estEnAttenteDeRespawn && p1.isAlive();
        boolean p2Actif = !p2.estEnAttenteDeRespawn && p2.isAlive();

        if (!p1Actif && !p2Actif) return null;
        if (p1Actif && !p2Actif) return p1;
        if (!p1Actif && p2Actif) return p2;

        double distP1 = Math.sqrt(Math.pow(p1.x - x, 2) + Math.pow(p1.y - y, 2));
        double distP2 = Math.sqrt(Math.pow(p2.x - x, 2) + Math.pow(p2.y - y, 2));

        return (distP1 < distP2) ? p1 : p2;
    }

    // --- 4. MON AFFICHAGE (DRAW) ---
    public void draw(Graphics g) {
        if (!estVivant) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Je sélectionne la frame active de l'animation
        Image imgAffiche = sprites[frameActuelle];
        if (imgAffiche == null) {
            imgAffiche = sprites[0]; // Sécurité si une image charge mal
        }

        if (imgAffiche != null) {
            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            double scaleX = (double) width / imgAffiche.getWidth(null);
            double scaleY = (double) height / imgAffiche.getHeight(null);
            at.rotate(Math.toRadians(angle), width / 2.0, height / 2.0);
            at.scale(scaleX, scaleY);
            g2d.drawImage(imgAffiche, at, null);
        }

        g2d.dispose();
    }

    // --- 5. UTILITAIRES ET DÉGÂTS ---
    public Rectangle getBounds() {
        int marge = 15;
        return new Rectangle((int)x + marge, (int)y + marge, width - (marge*2), height - (marge*2));
    }

    public void receiveDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) estVivant = false;
    }

    public boolean isAlive() {
        return estVivant;
    }
}