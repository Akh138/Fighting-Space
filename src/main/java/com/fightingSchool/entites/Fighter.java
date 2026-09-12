package com.fightingSchool.entites;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

public class Fighter {
    // --- 1. MES PROPRIÉTÉS DE BASE ---
    public Vaisseau model;
    public int x, y;
    public int width = 110, height = 110;
    public int speed = 5;
    public double angle = 0;
    private int maxHp;

    // --- 2. MA GESTION DU BOUCLIER ET DES RÈGLES ---
    public int shieldHp = 0;           // Mes points de bouclier actuels
    public int maxShieldHp = 50;       // Ma capacité max de bouclier
    private Image shieldSprite;        // Mon image de protection bleue (shield_fx.png)
    private int cooldownDegatsContact = 0; // Mon temps d'invincibilité après un choc
    public int scoreJoyaux = 0;        // Mon compteur de diamants
    public boolean estEnAttenteDeRespawn = false; // Mon état si je suis mort

    // --- 3. GESTION DE MON VISUEL (DAMAGE STATES) ---
    private Image[] sprites = new Image[4]; // Mon tableau pour mes 4 états de santé
    private Image currentSprite;            // L'image que j'affiche à l'instant T

    // --- 4. SYSTÈME D'EFFETS SPÉCIAUX ---
    private List<FlameParticle> engineFlames = new ArrayList<>();   // Mes flammes de moteur
    private List<ExplosionEffect> explosionEffects = new ArrayList<>(); // Mes explosions subies

    // --- 5. MES RÉFÉRENCES ET MON ARMEMENT ---
    public Fighter opponent;            // Je stocke ici qui est mon ennemi
    public Color projectileColor;       // La couleur de mes tirs
    public boolean left, right, up, down; // Mes directions (WASD ou Flèches)
    public List<Projectile> projectiles = new ArrayList<>(); // Ma liste de tirs à l'écran
    private int shootCooldown = 0;      // Mon temps d'attente entre deux tirs
    private final int SHOOT_COOLDOWN_MAX = 18;

    // --- MON CONSTRUCTEUR ---
    public Fighter(Vaisseau model, int startX, int startY, String baseName, int maxHp) {
        this.model = model;
        this.maxHp = maxHp;
        this.x = startX;
        this.y = startY;
        this.projectileColor = Color.WHITE;

        // Je charge mes 4 versions de vaisseau selon ma santé
        try {
            for (int i = 0; i < 4; i++) {
                String suffix = (i == 0) ? "" : "_" + i;
                sprites[i] = new ImageIcon(getClass().getResource("/sprites/" + baseName + suffix + ".png")).getImage();
            }
            currentSprite = sprites[0];

            // Je charge aussi l'effet visuel de mon bouclier
            this.shieldSprite = new ImageIcon(getClass().getResource("/sprites/shield_fx.png")).getImage();

        } catch (Exception e) {
            System.out.println("Erreur : Je n'ai pas pu charger les images pour " + baseName);
        }
    }

    // --- MA LOGIQUE DE MISE À JOUR (UPDATE) ---
    public void update(int panelWidth, int panelHeight) {
        // Si je suis mort en attendant le prochain niveau, j'arrête tout
        if (estEnAttenteDeRespawn) return;

        // 1. MA LOGIQUE DE DÉPLACEMENT ARCADE
        int moveX = 0, moveY = 0;
        if (left)  moveX -= speed;
        if (right) moveX += speed;
        if (up)    moveY -= speed;
        if (down)  moveY += speed;
        x += moveX; y += moveY;

        // Je m'oriente automatiquement vers ma direction de marche
        if (moveX != 0 || moveY != 0) {
            double angleRad = Math.atan2(moveY, moveX);
            this.angle = Math.toDegrees(angleRad) + 90;
        }

        // 2. JE CHANGE MON ASPECT SELON MES POINTS DE VIE
        float hpPercent = (float) model.getHp() / maxHp;
        if (hpPercent >= 0.75)      currentSprite = sprites[0];
        else if (hpPercent >= 0.50) currentSprite = sprites[1];
        else if (hpPercent >= 0.25) currentSprite = sprites[2];
        else                        currentSprite = sprites[3];

        // 3. MA GESTION DES FLAMMES DU RÉACTEUR
        double radRear = Math.toRadians(angle + 90);
        double ventX = (x + width / 2.0) + Math.cos(radRear) * (height / 2.5);
        double ventY = (y + height / 2.0) + Math.sin(radRear) * (height / 2.5);

        if (moveX != 0 || moveY != 0) {
            for(int i = 0; i < 3; i++) engineFlames.add(new FlameParticle(ventX, ventY, angle, Color.ORANGE));
        } else if (Math.random() > 0.8) {
            engineFlames.add(new FlameParticle(ventX, ventY, angle, Color.YELLOW));
        }

        // 4. JE NETTOIE MES EFFETS ET GÈRE MES TIMERS
        for (FlameParticle f : engineFlames) f.update();
        engineFlames.removeIf(f -> f.life <= 0);
        for (ExplosionEffect e : explosionEffects) e.update();
        explosionEffects.removeIf(e -> e.opacity <= 0.05f);

        // --- 5. EFFET PAC-MAN (Screen Wrapping Sécurisé) ---
        // Sécurité : Si la fenêtre n'est pas encore prête (largeur = 0), on ne téléporte pas !
        if (panelWidth > 0 && panelHeight > 0) {

            // Si le vaisseau sort à droite, il réapparaît à gauche
            if (x > panelWidth) {
                x = -width;
            }
            // S'il sort à gauche, il réapparaît à droite
            else if (x + width < 0) {
                x = panelWidth;
            }

            // Si le vaisseau sort en bas, il réapparaît en haut
            if (y > panelHeight) {
                y = -height;
            }
            // S'il sort en haut, il réapparaît en bas
            else if (y + height < 0) {
                y = panelHeight;
            }
        }


        // 6. JE METS À JOUR MES PROJECTILES
        for (Projectile p : projectiles) p.update(panelWidth, panelHeight);
        projectiles.removeIf(p -> !p.actif);

        if (cooldownDegatsContact > 0) cooldownDegatsContact--;
        if (shootCooldown > 0) shootCooldown--;
    }

    // --- MA LOGIQUE DE TIR (RÉTABLIE) ---

    // Mon tir normal (Missile 2)
    public void fire(int ignored) {
        if (shootCooldown > 0 || estEnAttenteDeRespawn) return;
        double rad = Math.toRadians(angle - 90);
        // Je crée un projectile qui part droit devant mon nez
        Projectile proj = new Projectile(x + width/2.0, y + height/2.0, Math.cos(rad)*10, Math.sin(rad)*10, angle, "/sprites/missile2.png", 5, null);
        projectiles.add(proj);
        shootCooldown = SHOOT_COOLDOWN_MAX;
    }

    // Mon tir puissant (Missile)
    public void specialFire(int ignored) {
        if (estEnAttenteDeRespawn) return;
        if (model.useEnergy(25) && shootCooldown <= 0) {
            double rad = Math.toRadians(angle - 90);
            Projectile proj = new Projectile(x + width/2.0, y + height/2.0, Math.cos(rad)*12, Math.sin(rad)*12, angle, "/sprites/missile.png", 15, null);
            proj.width = 30; proj.height = 40;
            projectiles.add(proj);
            shootCooldown = SHOOT_COOLDOWN_MAX * 2;
        }
    }

    // Mon tir à tête chercheuse (Missile Sp)
    public void fireHoming() {
        if (estEnAttenteDeRespawn) return;
        if (model.useEnergy(45) && shootCooldown <= 0) {
            double rad = Math.toRadians(angle - 90);
            Projectile proj = new Projectile(x + width/2.0, y + height/2.0, Math.cos(rad)*6, Math.sin(rad)*6, angle, "/sprites/missileSp.png", 25, opponent);
            proj.width = 35; proj.height = 45;
            projectiles.add(proj);
            shootCooldown = SHOOT_COOLDOWN_MAX * 3;
        }
    }

    // --- MON AFFICHAGE (DRAW) ---
    public void draw(Graphics g) {
        if (estEnAttenteDeRespawn) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Si je viens de prendre des dégâts, je clignote (invincibilité)
        if (cooldownDegatsContact > 0 && cooldownDegatsContact % 4 > 2) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }

        // JE DESSINE MON VAISSEAU
        if (currentSprite != null) {
            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            double scaleX = (double) width / currentSprite.getWidth(null);
            double scaleY = (double) height / currentSprite.getHeight(null);
            at.rotate(Math.toRadians(angle), width / 2.0, height / 2.0);
            at.scale(scaleX, scaleY);
            g2d.drawImage(currentSprite, at, null);
        }

        // JE DESSINE MON BOUCLIER (Si j'en ai un actif)
        if (shieldHp > 0 && shieldSprite != null) {
            AffineTransform atShield = new AffineTransform();
            double sScale = 1.3; // Un peu plus grand que le vaisseau
            double sX = x - (width * (sScale - 1) / 2);
            double sY = y - (height * (sScale - 1) / 2);
            atShield.translate(sX, sY);
            double imgScaleX = (double) (width * sScale) / shieldSprite.getWidth(null);
            double imgScaleY = (double) (height * sScale) / shieldSprite.getHeight(null);
            atShield.scale(imgScaleX, imgScaleY);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.drawImage(shieldSprite, atShield, null);
        }

        // JE DESSINE MES EFFETS (Flammes et Explosions)
        for (FlameParticle f : engineFlames) f.draw(g2d);
        for (ExplosionEffect e : explosionEffects) e.draw(g2d);
        g2d.dispose();

        // JE DESSINE MES PROJECTILES
        for (Projectile p : projectiles) p.draw(g);
    }

    // --- MES MÉTHODES DE DÉGÂTS ---
    public void receiveDamage(int dmg) {
        if (shieldHp > 0) {
            shieldHp -= dmg;
            if (shieldHp < 0) shieldHp = 0;
        } else {
            model.receiveDamage(dmg);
        }
    }

    public void activerBouclier() {
        this.shieldHp = maxShieldHp;
    }

    public void toucherObstacle() {
        if (cooldownDegatsContact <= 0) {
            this.receiveDamage(5);
            this.triggerExplosion(x + width/2, y + height/2, 5);
            cooldownDegatsContact = 40;
        }
    }

    // --- MES UTILITAIRES ---

    // Ma hitbox resserrée pour mieux circuler
    public Rectangle getBounds() {
        if (estEnAttenteDeRespawn) return new Rectangle(-1000, -1000, 0, 0);
        int marge = 30;
        return new Rectangle(x + marge, y + marge, width - (marge * 2), height - (marge * 2));
    }

    public int calcDamageTo(Fighter cible) { return Math.max(1, this.model.getAttack() - cible.model.getDefense()); }
    public boolean isAlive() { return model.isAlive(); }

    public void triggerExplosion(int hitX, int hitY, int damagePower) {
        int count = Math.max(3, damagePower / 3);
        for (int i = 0; i < count; i++) {
            double offX = (Math.random() - 0.5) * 30;
            double offY = (Math.random() - 0.5) * 30;
            Color c = (i % 2 == 0) ? Color.ORANGE : Color.RED;
            if (i % 3 == 0) c = Color.YELLOW;
            explosionEffects.add(new ExplosionEffect(hitX + offX, hitY + offY, c, (float)damagePower));
        }
    }

    // --- MES CLASSES INTERNES D'EFFETS ---
    class FlameParticle {
        double px, py, vx, vy; int life = 15; Color col;
        FlameParticle(double x, double y, double angle, Color c) {
            this.px = x; this.py = y; this.col = c;
            double rad = Math.toRadians(angle + 90);
            this.vx = Math.cos(rad) * (Math.random() * 2);
            this.vy = Math.sin(rad) * (Math.random() * 2);
        }
        void update() { px += vx; py += vy; life--; }
        void draw(Graphics2D g2d) {
            int size = Math.max(2, life / 3);
            g2d.setColor(col); g2d.fillOval((int)px, (int)py, size, size);
        }
    }

    class ExplosionEffect {
        double px, py; float radius; float opacity = 1.0f; Color col; float growSpeed;
        ExplosionEffect(double x, double y, Color c, float dmg) {
            this.px = x; this.py = y; this.col = c; this.radius = 5;
            this.growSpeed = (dmg / 5.0f) + (float)Math.random() * 2;
        }
        void update() { radius += growSpeed; opacity -= 0.04f; if (opacity < 0) opacity = 0; }
        void draw(Graphics2D g2d) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2d.setColor(col); g2d.fillOval((int)(px - radius), (int)(py - radius), (int)(radius * 2), (int)(radius * 2));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    public Image getIcone() { return sprites[0]; }
}