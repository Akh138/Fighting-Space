package com.fightingSchool.entites;

/**
 * Ma classe Vaisseau : c'est ici que je stocke toutes les statistiques
 * "brutes" de mon appareil (vie, énergie, force).
 */
public class Vaisseau {

    // --- 1. MES VARIABLES DE STATISTIQUES ---
    private int hp;           // Mes points de vie actuels
    private int energy;       // Ma jauge d'énergie pour les tirs spéciaux
    private int attack;       // Ma puissance de feu de base
    private int defense;      // Ma capacité à réduire les dégâts reçus
    private int specialShot;  // La puissance de mon tir spécial

    // --- MON CONSTRUCTEUR ---
    public Vaisseau(int hp, int energy, int attack, int defense, int specialShot){
        this.hp = hp;
        this.energy = energy;
        this.attack = attack;
        this.defense = defense;
        this.specialShot = specialShot;
    }

    // --- MES GETTERS (Pour que les autres fichiers puissent lire mes stats) ---
    public int getHp() { return hp; }
    public int getEnergy() { return energy; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpecialShot() { return specialShot; }


    // --- MES MÉTHODES DE LOGIQUE ---

    // Pour savoir si je suis toujours en état de voler
    public boolean isAlive() {
        return hp > 0;
    }

    // Ma méthode pour gérer les dégâts ET les soins
    public void receiveDamage(int damage) {
        hp -= damage;
        // Sécurité : ma vie ne peut pas descendre en dessous de 0
        if (hp < 0) hp = 0;
        // Sécurité : ma vie ne peut pas dépasser 100 (le max défini dans GamePanel)
        if (hp > 100) hp = 100;
    }

    // Pour consommer de l'énergie quand je lance un gros missile
    public boolean useEnergy(int amount) {
        if (energy >= amount) {
            energy -= amount; // Je dépense l'énergie
            return true;      // Je confirme que le tir peut partir
        }
        return false; // Pas assez d'énergie !
    }

    // Pour recharger mes batteries quand je ramasse un item
    public void restoreEnergy(int amount) {
        energy += amount;
        // Sécurité : mon énergie ne dépasse pas 100
        if (energy > 100) energy = 100;
    }
}