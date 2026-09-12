package com.fightingSchool.core;

import com.fightingSchool.entites.*;
import com.fightingSchool.ui.HudManager;
import com.fightingSchool.niveaux.LevelManager;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel implements KeyListener {

    // --- 1. MES PROPRIÉTÉS DU JEU ---
    private Fighter p1;
    private Fighter p2;
    private Cristal cristal;
    private Timer timer;

    // Mes Managers (Interface, Niveaux et Collisions)
    private HudManager hudManager;
    private LevelManager levelManager;
    private CollisionManager collisionManager; // <-- NOUVEAU MANAGER !

    // Listes pour les éléments visuels et de gameplay
    private List<Point> etoiles = new ArrayList<>();
    private List<DecorPlanete> listeDecors = new ArrayList<>();
    private List<Joyau> listeJoyaux = new ArrayList<>();
    private List<Roche> listeRoches = new ArrayList<>();
    private List<Item> listeItems = new ArrayList<>();

    // Paramètres de progression
    private final int P1_MAX_HP = 100;
    private final int P2_MAX_HP = 100;
    private int niveauActuel = 1;
    private boolean gameOver = false;

    // --- 2. MON CONSTRUCTEUR ---
    public GamePanel() {
        setPreferredSize(new Dimension(900, 520));
        setFocusable(true);
        addKeyListener(this);

        Vaisseau v1 = new Vaisseau(P1_MAX_HP, 50, 15, 8, 20);
        Vaisseau v2 = new Vaisseau(P2_MAX_HP, 50, 15, 6, 30);

        p1 = new Fighter(v1, 0, 0, "vaisseau_bleu", P1_MAX_HP);
        p2 = new Fighter(v2, 0, 0, "vaisseau_orange", P2_MAX_HP);

        p1.opponent = p2;
        p2.opponent = p1;

        // J'initialise tous mes Managers
        hudManager = new HudManager();
        levelManager = new LevelManager();
        collisionManager = new CollisionManager(); // <-- INITIALISATION

        // Je génère l'espace profond
        genererEspace();

        // Je génère le niveau 1 via mon LevelManager
        cristal = levelManager.genererNiveau1(listeRoches, p1, p2);

        // MA BOUCLE DE JEU (60 FPS)
        timer = new Timer(16, e -> {
            if (!gameOver) {
                // Sauvegarde des positions avant mouvement
                int oldP1X = p1.x; int oldP1Y = p1.y;
                int oldP2X = p2.x; int oldP2Y = p2.y;

                if (!p1.estEnAttenteDeRespawn) p1.update(getWidth(), getHeight());
                if (!p2.estEnAttenteDeRespawn) p2.update(getWidth(), getHeight());

                // J'utilise mon CollisionManager pour bloquer les vaisseaux contre les rochers
                collisionManager.gererObstaclesSolides(p1, p2, listeRoches, oldP1X, oldP1Y, oldP2X, oldP2Y);

                if (cristal != null) cristal.update();

                for (Roche r : listeRoches) {
                    r.update(900 / 2, 520 / 2, getHeight());
                }

                for (DecorPlanete dp : listeDecors) dp.update();

                // J'utilise mon CollisionManager pour tous les tirs, dégâts et ramassages !
                collisionManager.verifierCollisions(p1, p2, cristal, listeRoches, listeJoyaux, listeItems);

                checkEtatPartie();
            }
            repaint();
        });
        timer.start();

        requestFocusInWindow();
    }

    // --- 3. GÉNÉRATION DE L'ESPACE ---
    private void genererEspace() {
        for (int i = 0; i < 150; i++) {
            etoiles.add(new Point((int) (Math.random() * 900), (int) (Math.random() * 520)));
        }
        listeDecors.add(new DecorPlanete(700, 50, 200, "/sprites/planete_saturne.png", 0.05));
        listeDecors.add(new DecorPlanete(100, 350, 100, "/sprites/planete_lune.png", 0.1));
        listeDecors.add(new DecorPlanete(50, 80, 60, "/sprites/planete_terre.png", 0.03));
    }

    // --- 4. ÉTAT DE LA PARTIE ET TRANSITIONS DE NIVEAUX ---
    private void checkEtatPartie() {
        if (cristal != null && !cristal.isAlive()) {
            p1.triggerExplosion(cristal.x + 60, cristal.y + 60, 100);
            genererButin(cristal.x + 40, cristal.y + 40, 12);
            JOptionPane.showMessageDialog(this, "CRISTAL DÉTRUIT ! Niveau suivant...");
            passerAuNiveauSuivant();
        }
        if (p1.isAlive() == false && !p1.estEnAttenteDeRespawn) {
            genererButin(p1.x, p1.y, p1.scoreJoyaux); p1.scoreJoyaux = 0;
            p1.estEnAttenteDeRespawn = true; p1.triggerExplosion(p1.x, p1.y, 50);
        }
        if (p2.isAlive() == false && !p2.estEnAttenteDeRespawn) {
            genererButin(p2.x, p2.y, p2.scoreJoyaux); p2.scoreJoyaux = 0;
            p2.estEnAttenteDeRespawn = true; p2.triggerExplosion(p2.x, p2.y, 50);
        }
        if (p1.estEnAttenteDeRespawn && p2.estEnAttenteDeRespawn) {
            gameOver = true; timer.stop();
            JOptionPane.showMessageDialog(this, "GAME OVER - REESSAYEZ !");
            recommencerToutLeJeu();
        }
    }

    private void genererButin(int startX, int startY, int quantite) {
        for (int i = 0; i < quantite; i++) {
            listeJoyaux.add(new Joyau(startX + (int) (Math.random() * 80 - 40), startY + (int) (Math.random() * 80 - 40)));
        }
    }

    private void passerAuNiveauSuivant() {
        niveauActuel++;
        listeItems.clear();

        if (niveauActuel == 2) {
            cristal = levelManager.genererNiveau2(listeRoches, p1, p2);
        } else {
            cristal = levelManager.genererNiveau1(listeRoches, p1, p2);
        }

        if (p1.estEnAttenteDeRespawn) { p1.model = new Vaisseau(P1_MAX_HP / 2, 25, 15, 8, 20); p1.estEnAttenteDeRespawn = false; }
        if (p2.estEnAttenteDeRespawn) { p2.model = new Vaisseau(P2_MAX_HP / 2, 25, 15, 6, 30); p2.estEnAttenteDeRespawn = false; }

        p1.projectiles.clear(); p2.projectiles.clear();
    }

    private void recommencerToutLeJeu() {
        niveauActuel = 1;
        p1.model = new Vaisseau(P1_MAX_HP, 50, 15, 8, 20); p2.model = new Vaisseau(P2_MAX_HP, 50, 15, 6, 30);
        p1.scoreJoyaux = 0; p2.scoreJoyaux = 0;
        p1.shieldHp = 0; p2.shieldHp = 0;
        p1.estEnAttenteDeRespawn = false; p2.estEnAttenteDeRespawn = false;

        listeJoyaux.clear(); listeItems.clear();
        cristal = levelManager.genererNiveau1(listeRoches, p1, p2);

        gameOver = false; timer.start();
    }

    // --- 5. AFFICHAGE GRAPHIQUE ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(5, 5, 15));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        for (Point p : etoiles) {
            int size = (Math.random() > 0.95) ? 3 : 1;
            g2d.fillOval(p.x, p.y, size, size);
        }

        for (DecorPlanete dp : listeDecors) dp.draw(g2d);
        for (Joyau j : listeJoyaux) j.draw(g);
        for (Item it : listeItems) it.draw(g);
        for (Roche r : listeRoches) r.draw(g);

        if (cristal != null) cristal.draw(g);
        p1.draw(g); p2.draw(g);

        // Dessin du HUD
        if (hudManager != null) hudManager.draw(g2d, p1, p2, niveauActuel, getWidth());
    }

    // --- 6. CONTRÔLES ---
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (!p1.estEnAttenteDeRespawn) {
            if (k == KeyEvent.VK_W) p1.up = true; if (k == KeyEvent.VK_S) p1.down = true;
            if (k == KeyEvent.VK_A) p1.left = true; if (k == KeyEvent.VK_D) p1.right = true;
            if (k == KeyEvent.VK_SPACE) p1.fire(0); if (k == KeyEvent.VK_Q) p1.specialFire(0);
            if (k == KeyEvent.VK_E) p1.fireHoming();
        }
        if (!p2.estEnAttenteDeRespawn) {
            if (k == KeyEvent.VK_UP) p2.up = true; if (k == KeyEvent.VK_DOWN) p2.down = true;
            if (k == KeyEvent.VK_LEFT) p2.left = true; if (k == KeyEvent.VK_RIGHT) p2.right = true;
            if (k == KeyEvent.VK_ENTER) p2.fire(0); if (k == KeyEvent.VK_SHIFT) p2.specialFire(0);
            if (k == KeyEvent.VK_CONTROL) p2.fireHoming();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W) p1.up = false; if (k == KeyEvent.VK_S) p1.down = false;
        if (k == KeyEvent.VK_A) p1.left = false; if (k == KeyEvent.VK_D) p1.right = false;
        if (k == KeyEvent.VK_UP) p2.up = false; if (k == KeyEvent.VK_DOWN) p2.down = false;
        if (k == KeyEvent.VK_LEFT) p2.left = false; if (k == KeyEvent.VK_RIGHT) p2.right = false;
    }
    @Override public void keyTyped(KeyEvent e) {}
}