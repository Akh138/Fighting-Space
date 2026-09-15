package com.fightingspace;

import com.fightingspace.entites.Fighter;
import com.fightingspace.entites.Vaisseau;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Ma classe de test pour les Combattants (Fighter).
 * Je teste que le bouclier protège bien la vie,
 * et que le calcul d'attaque/défense fonctionne sans bug.
 */
public class FighterTest {

    @Test
    public void testAbsorptionDuBouclier() {
        // Je crée un vaisseau avec 100 PV
        Vaisseau v = new Vaisseau(100, 50, 15, 8, 20);
        Fighter f = new Fighter(v, 0, 0, "vaisseau_bleu", 100);

        // 1. J'active le bouclier (50 Shield HP)
        f.activerBouclier();
        assertEquals(50, f.shieldHp, "Le bouclier doit démarrer à 50 PV");

        // 2. Je lui inflige 20 dégâts
        f.receiveDamage(20);

        // RÈGLE : C'est le bouclier qui doit baisser, PAS la vie !
        assertEquals(30, f.shieldHp, "Le bouclier doit passer de 50 à 30");
        assertEquals(100, f.model.getHp(), "La vie doit rester intacte à 100 PV tant que le bouclier est actif");

        // 3. Je lui inflige 40 dégâts (le bouclier casse !)
        f.receiveDamage(40);
        assertEquals(0, f.shieldHp, "Le bouclier doit être complètement brisé (0 PV)");
    }

    @Test
    public void testCalculDesDegatsAttaqueDefense() {
        // Joueur 1 : 15 en Attaque
        Vaisseau v1 = new Vaisseau(100, 50, 15, 8, 20);
        Fighter p1 = new Fighter(v1, 0, 0, "vaisseau_bleu", 100);

        // Joueur 2 : 8 en Défense
        Vaisseau v2 = new Vaisseau(100, 50, 15, 8, 20);
        Fighter p2 = new Fighter(v2, 0, 0, "vaisseau_orange", 100);

        // RÈGLE : Dégâts = Attaque (15) - Défense (8) = 7 dégâts
        int degatsInfliges = p1.calcDamageTo(p2);
        assertEquals(7, degatsInfliges, "Les dégâts calculés doivent être de 7");
    }
}