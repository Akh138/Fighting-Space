package com.fightingspace.entites;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public class Projectile {
    public double x, y;
    public int width, height;
    public double speedX, speedY;
    public double angle;
    public boolean actif = true;
    public int damage;
    private Image sprite;

    // Pour la tête chercheuse
    private Fighter target;
    private int homingTimer = 300; // 5 secondes environ (60 fps * 5)

    // Particules pour la traînée du missile
    private List<Point> trail = new ArrayList<>();

    public Projectile(double startX, double startY, double speedX, double speedY, double angle, String imagePath, int damage, Fighter target) {
        this.x = startX;
        this.y = startY;
        this.speedX = speedX;
        this.speedY = speedY;
        this.angle = angle;
        this.damage = damage;
        this.target = target; // Si null, c'est un missile normal

        // Taille par défaut, on l'ajustera dans Fighter
        this.width = 20;
        this.height = 30;

        try {
            this.sprite = new ImageIcon(getClass().getResource(imagePath)).getImage();
        } catch (Exception e) {
            System.out.println("Erreur image : " + imagePath);
        }
    }

    public void update(int panelWidth, int panelHeight) {
        // --- LOGIQUE TÊTE CHERCHEUSE ---
        if (target != null && homingTimer > 0 && target.isAlive()) {
            double diffX = (target.x + target.width/2.0) - (x + width/2.0);
            double diffY = (target.y + target.height/2.0) - (y + height/2.0);
            double targetAngle = Math.atan2(diffY, diffX);

            // On ajuste doucement la vitesse vers la cible (homing effect)
            double magnitude = 7.0; // vitesse constante
            speedX += Math.cos(targetAngle) * 0.5;
            speedY += Math.sin(targetAngle) * 0.5;

            // On limite la vitesse max pour pas qu'il devienne fou
            double currentSpeed = Math.sqrt(speedX*speedX + speedY*speedY);
            speedX = (speedX / currentSpeed) * magnitude;
            speedY = (speedY / currentSpeed) * magnitude;

            // On met à jour l'angle pour que l'image pointe vers la cible
            angle = Math.toDegrees(targetAngle) + 90;
            homingTimer--;
        }

        x += speedX;
        y += speedY;

        // Ajouter des particules de traînée (seulement pour les gros missiles)
        if (damage > 10) {
            trail.add(new Point((int)(x + width/2.0), (int)(y + height/2.0)));
            if (trail.size() > 10) trail.remove(0);
        }

        if (x < -100 || x > panelWidth + 100 || y < -100 || y > panelHeight + 100) actif = false;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dessiner la traînée (particules)
        for (int i = 0; i < trail.size(); i++) {
            g2d.setColor(new Color(255, 150, 0, (i * 25))); // Dégradé de transparence
            g2d.fillOval(trail.get(i).x, trail.get(i).y, 5, 5);
        }

        if (sprite != null) {
            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            double scaleX = (double) width / sprite.getWidth(null);
            double scaleY = (double) height / sprite.getHeight(null);
            at.rotate(Math.toRadians(angle), width / 2.0, height / 2.0);
            at.scale(scaleX, scaleY);
            g2d.drawImage(sprite, at, null);
        }
        g2d.dispose();
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }
}