package com.fightingspace.core;

import com.fightingspace.entites.*;
import com.fightingspace.ui.HudManager;
import com.fightingspace.niveaux.LevelManager;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Mon GamePanel : C'est le cœur et le chef d'orchestre de mon jeu.
 * Je gère l'écran titre, la boucle principale (60 FPS), les entrées clavier,
 * les listes d'objets, et je fais le lien entre tous mes managers.
 */
public class GamePanel extends JPanel implements KeyListener {

    // --- 1. MES PROPRIÉTÉS DU JEU ---
    private Fighter p1;
    private Fighter p2;
    private Cristal cristal;
    private Timer timer;

    // --- MON ÉCRAN TITRE ---
    private boolean enEcranTitre = true; // Je commence sur l'écran d'accueil
    private Image imgFondTitre;
    private Image imgLogoTitre;
    private double logoFloatTimer = 0;   // Pour faire flotter mon logo chromé
    private int clignoteTimer = 0;       // Pour faire clignoter le texte "PRESS START"

    // Mes Managers pour garder un code propre et structuré
    private HudManager hudManager;
    private LevelManager levelManager;
    private GamepadManager gamepadManager;

    // Mes listes d'objets présents dans l'arène à l'instant T
    private List<Point> etoiles = new ArrayList<>();
    private List<DecorPlanete> listeDecors = new ArrayList<>();
    private List<Joyau> listeJoyaux = new ArrayList<>();
    private List<Roche> listeRoches = new ArrayList<>();
    private List<Item> listeItems = new ArrayList<>();
    private List<Ennemi> listeEnnemis = new ArrayList<>();

    // Paramètres de progression et de vie
    private final int P1_MAX_HP = 100;
    private final int P2_MAX_HP = 100;
    private int niveauActuel = 1;
    private boolean gameOver = false;

    // --- 2. MON CONSTRUCTEUR (LANCEMENT DU JEU) ---
    public GamePanel() {
        setPreferredSize(new Dimension(900, 520));
        setFocusable(true);
        addKeyListener(this);

        // Je crée les statistiques de base de mes deux vaisseaux
        Vaisseau v1 = new Vaisseau(P1_MAX_HP, 50, 15, 8, 20);
        Vaisseau v2 = new Vaisseau(P2_MAX_HP, 50, 15, 6, 30);

        p1 = new Fighter(v1, 0, 0, "vaisseau_bleu", P1_MAX_HP);
        p2 = new Fighter(v2, 0, 0, "vaisseau_orange", P2_MAX_HP);

        p1.opponent = p2;
        p2.opponent = p1;

        // J'initialise mes gestionnaires (Interface, Niveaux, Manette)
        hudManager = new HudManager();
        levelManager = new LevelManager();
        gamepadManager = new GamepadManager();

        // JE CHARGE LES IMAGES DE MON ÉCRAN TITRE
        try {
            imgFondTitre = new ImageIcon(getClass().getResource("/sprites/fond_titre.png")).getImage();
            imgLogoTitre = new ImageIcon(getClass().getResource("/sprites/logo_titre.png")).getImage();
        } catch (Exception e) {
            System.out.println("Erreur chargement images écran titre");
        }

        // Je génère l'arrière-plan spatial (étoiles et planètes)
        genererEspace();

        // Je prépare le Niveau 1 via mon LevelManager
        cristal = levelManager.genererNiveau1(listeRoches, listeEnnemis, p1, p2);

        // --- MA BOUCLE DE JEU (60 FPS) ---
        timer = new Timer(16, e -> {
            // A. SI JE SUIS SUR L'ÉCRAN TITRE : J'ANIME LE MENU ET J'ATTENDS
            if (enEcranTitre) {
                logoFloatTimer += 0.05; // Animation de flottement du logo
                clignoteTimer = (clignoteTimer + 1) % 60; // Boucle de clignotement

                // Si le joueur appuie sur le bouton A de sa manette, on lance la partie !
                if (gamepadManager != null) {
                    gamepadManager.update(p1, p2);
                    if (!p1.projectiles.isEmpty() || !p2.projectiles.isEmpty()) {
                        p1.projectiles.clear();
                        p2.projectiles.clear();
                        enEcranTitre = false; // Lancement du jeu à la manette !
                        SoundManager.getInstance().jouerSon("/sons/item.wav");
                    }
                }

                repaint();
                return; // Je ne calcule pas les collisions du combat pendant le menu
            }

            // B. SI LE COMBAT TOURNE NORMALEMENT
            if (!gameOver) {
                // Lecture de la manette
                if (gamepadManager != null) {
                    gamepadManager.update(p1, p2);
                }

                // Sauvegarde des positions pour la physique solide des roches
                int oldP1X = p1.x; int oldP1Y = p1.y;
                int oldP2X = p2.x; int oldP2Y = p2.y;

                if (!p1.estEnAttenteDeRespawn) p1.update(getWidth(), getHeight());
                if (!p2.estEnAttenteDeRespawn) p2.update(getWidth(), getHeight());

                gererObstaclesSolides(oldP1X, oldP1Y, oldP2X, oldP2Y);

                if (cristal != null) cristal.update();

                // Mise à jour des roches
                for (Roche r : listeRoches) {
                    r.update(900 / 2, 520 / 2, getHeight());
                }

                // Mise à jour des monstres (bloqués par les roches sans dégâts)
                for (Ennemi ennemi : listeEnnemis) {
                    double oldEX = ennemi.x;
                    double oldEY = ennemi.y;

                    ennemi.update(p1, p2);

                    for (Roche r : listeRoches) {
                        if (ennemi.getBounds().intersects(r.getBounds())) {
                            ennemi.x = oldEX;
                            ennemi.y = oldEY;
                            break;
                        }
                    }
                }

                for (DecorPlanete dp : listeDecors) dp.update();

                checkCollisions();
                checkEtatPartie();
            }
            repaint();
        });

        // Je lance la musique en boucle et démarre le jeu !
        SoundManager.getInstance().jouerMusique("/sons/musique_fond.wav");
        timer.start();

        requestFocusInWindow();
    }

    // --- 3. GÉNÉRATION DU DÉCOR DE FOND ---
    private void genererEspace() {
        for (int i = 0; i < 150; i++) {
            etoiles.add(new Point((int) (Math.random() * 900), (int) (Math.random() * 520)));
        }
        listeDecors.add(new DecorPlanete(700, 50, 200, "/sprites/planete_saturne.png", 0.05));
        listeDecors.add(new DecorPlanete(100, 350, 100, "/sprites/planete_lune.png", 0.1));
        listeDecors.add(new DecorPlanete(50, 80, 60, "/sprites/planete_terre.png", 0.03));
    }

    // --- 4. GESTION DES COLLISIONS ---
    private void checkCollisions() {
        // --- COLLISIONS POUR LE JOUEUR 1 ---
        if (!p1.estEnAttenteDeRespawn) {
            for (Projectile proj : p1.projectiles) {
                if (!proj.actif) continue;

                if (proj.getBounds().intersects(p2.getBounds())) {
                    p2.receiveDamage(p1.calcDamageTo(p2)); p2.triggerExplosion((int) proj.x, (int) proj.y, 10); proj.actif = false;
                } else if (cristal != null && proj.getBounds().intersects(cristal.getBounds())) {
                    cristal.receiveDamage(10); p1.triggerExplosion((int) proj.x, (int) proj.y, 10); proj.actif = false;
                } else {
                    boolean touche = false;
                    for (Roche r : listeRoches) {
                        if (proj.getBounds().intersects(r.getBounds())) {
                            r.receiveDamage(10); p1.triggerExplosion((int) proj.x, (int) proj.y, 5); proj.actif = false;
                            if (!r.isAlive()) tenterLacherItem(r.x, r.y);
                            touche = true;
                            break;
                        }
                    }
                    if (!touche) {
                        for (Ennemi e : listeEnnemis) {
                            if (e.isAlive() && proj.getBounds().intersects(e.getBounds())) {
                                e.receiveDamage(15);
                                p1.triggerExplosion((int) proj.x, (int) proj.y, 8);
                                proj.actif = false;
                                if (!e.isAlive()) genererButin((int)e.x, (int)e.y, 3);
                                break;
                            }
                        }
                    }
                }
            }
            for (Joyau j : listeJoyaux) { if (j.estActif && p1.getBounds().intersects(j.getBounds())) { p1.scoreJoyaux++; j.estActif = false; } }
            for (Item it : listeItems) { if (it.estActif && p1.getBounds().intersects(it.getBounds())) { appliquerEffetItem(p1, it); it.estActif = false; } }
        }

        // --- COLLISIONS POUR LE JOUEUR 2 ---
        if (!p2.estEnAttenteDeRespawn) {
            for (Projectile proj : p2.projectiles) {
                if (!proj.actif) continue;

                if (proj.getBounds().intersects(p1.getBounds())) {
                    p1.receiveDamage(p2.calcDamageTo(p1)); p1.triggerExplosion((int) proj.x, (int) proj.y, 10); proj.actif = false;
                } else if (cristal != null && proj.getBounds().intersects(cristal.getBounds())) {
                    cristal.receiveDamage(10); p2.triggerExplosion((int) proj.x, (int) proj.y, 10); proj.actif = false;
                } else {
                    boolean touche = false;
                    for (Roche r : listeRoches) {
                        if (proj.getBounds().intersects(r.getBounds())) {
                            r.receiveDamage(10); p2.triggerExplosion((int) proj.x, (int) proj.y, 5); proj.actif = false;
                            if (!r.isAlive()) tenterLacherItem(r.x, r.y);
                            touche = true;
                            break;
                        }
                    }
                    if (!touche) {
                        for (Ennemi e : listeEnnemis) {
                            if (e.isAlive() && proj.getBounds().intersects(e.getBounds())) {
                                e.receiveDamage(15);
                                p2.triggerExplosion((int) proj.x, (int) proj.y, 8);
                                proj.actif = false;
                                if (!e.isAlive()) genererButin((int)e.x, (int)e.y, 3);
                                break;
                            }
                        }
                    }
                }
            }
            for (Joyau j : listeJoyaux) { if (j.estActif && p2.getBounds().intersects(j.getBounds())) { p2.scoreJoyaux++; j.estActif = false; } }
            for (Item it : listeItems) { if (it.estActif && p2.getBounds().intersects(it.getBounds())) { appliquerEffetItem(p2, it); it.estActif = false; } }
        }

        // Contact corps à corps Joueur vs Monstre
        for (Ennemi e : listeEnnemis) {
            if (!e.isAlive()) continue;
            if (!p1.estEnAttenteDeRespawn && p1.getBounds().intersects(e.getBounds())) {
                p1.receiveDamage(10); p1.triggerExplosion(p1.x, p1.y, 10); e.receiveDamage(10);
            }
            if (!p2.estEnAttenteDeRespawn && p2.getBounds().intersects(e.getBounds())) {
                p2.receiveDamage(10); p2.triggerExplosion(p2.x, p2.y, 10); e.receiveDamage(10);
            }
        }

        listeJoyaux.removeIf(j -> !j.estActif);
        listeRoches.removeIf(r -> !r.isAlive());
        listeItems.removeIf(it -> !it.estActif);
        listeEnnemis.removeIf(e -> !e.isAlive());
    }

    private void gererObstaclesSolides(int o1X, int o1Y, int o2X, int o2Y) {
        for (Roche r : listeRoches) {
            if (p1.getBounds().intersects(r.getBounds())) { p1.x = o1X; p1.y = o1Y; p1.toucherObstacle(); }
            if (p2.getBounds().intersects(r.getBounds())) { p2.x = o2X; p2.y = o2Y; p2.toucherObstacle(); }
        }
    }

    private void tenterLacherItem(int x, int y) {
        if (Math.random() < 0.4) listeItems.add(new Item(x, y, (int) (Math.random() * 3)));
    }

    private void appliquerEffetItem(Fighter f, Item it) {
        if (it.type == 0) f.model.receiveDamage(-30);
        else if (it.type == 1) f.model.restoreEnergy(50);
        else if (it.type == 2) f.activerBouclier();
    }

    // --- 5. ÉTAT DE LA PARTIE ET NIVEAUX ---
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
        for (int i = 0; i < quantite; i++) listeJoyaux.add(new Joyau(startX + (int) (Math.random() * 80 - 40), startY + (int) (Math.random() * 80 - 40)));
    }

    private void passerAuNiveauSuivant() {
        niveauActuel++;
        listeItems.clear();

        if (niveauActuel == 2) {
            cristal = levelManager.genererNiveau2(listeRoches, listeEnnemis, p1, p2);
        } else if (niveauActuel == 3) {
            cristal = levelManager.genererNiveau3(listeRoches, listeEnnemis, p1, p2);
        } else {
            niveauActuel = 1;
            cristal = levelManager.genererNiveau1(listeRoches, listeEnnemis, p1, p2);
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

        listeJoyaux.clear(); listeRoches.clear(); listeItems.clear(); listeEnnemis.clear();
        cristal = levelManager.genererNiveau1(listeRoches, listeEnnemis, p1, p2);

        // Quand la partie est finie, je reviens à l'écran titre !
        enEcranTitre = true;

        gameOver = false; timer.start();
    }

    // --- 6. AFFICHAGE GRAPHIQUE ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // A. SI JE SUIS SUR L'ÉCRAN TITRE
        if (enEcranTitre) {
            // 1. Fond de bataille spatiale
            if (imgFondTitre != null) {
                g2d.drawImage(imgFondTitre, 0, 0, getWidth(), getHeight(), null);
            }

            // 2. Logo chromé flottant (Format XXL)
            if (imgLogoTitre != null) {
                int logoLargeur = 820; // Il prend presque toute la largeur de l'écran (900px) !
                int logoHauteur = 270; // Bien haut et massif

                int flottementY = 5 + (int) (Math.sin(logoFloatTimer) * 8); // Calé tout en haut
                int logoX = getWidth() / 2 - (logoLargeur / 2); // Toujours parfaitement centré

                g2d.drawImage(imgLogoTitre, logoX, flottementY, logoLargeur, logoHauteur, null);
            }

            // 3. Texte rétro clignotant
            if (clignoteTimer < 35) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
                String texte = "PRESS SPACE OR BUTTON A TO PLAY";
                int texteLargeur = g2d.getFontMetrics().stringWidth(texte);
                g2d.drawString(texte, getWidth() / 2 - (texteLargeur / 2), 485);
            }
            return; // Je ne dessine pas les objets de combat
        }

        // B. AFFICHAGE NORMAL DU JEU EN COMBAT
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

        for (Ennemi e : listeEnnemis) e.draw(g);

        if (cristal != null) cristal.draw(g);
        p1.draw(g); p2.draw(g);

        if (hudManager != null) hudManager.draw(g2d, p1, p2, niveauActuel, getWidth());
    }

    // --- 7. CONTRÔLES ---
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        // Si je suis sur l'écran d'accueil : ESPACE ou ENTRÉE lance la partie !
        if (enEcranTitre) {
            if (k == KeyEvent.VK_SPACE || k == KeyEvent.VK_ENTER) {
                enEcranTitre = false; // Je lance le combat
                SoundManager.getInstance().jouerSon("/sons/item.wav"); // Petit son de validation
            }
            return;
        }

        // Contrôles normaux en jeu
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