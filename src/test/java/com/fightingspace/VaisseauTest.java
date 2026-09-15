package com.fightingspace;

import com.fightingspace.entites.Vaisseau;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Ma classe de Tests Unitaires automatisés.
 * Je teste la logique de mes vaisseaux pour m'assurer qu'aucun bug
 * mathématique ne survienne pendant les parties.
 */
public class VaisseauTest {

    @Test
    public void testGestionDesDegats() {
        // Je crée un vaisseau avec 100 PV
        Vaisseau v = new Vaisseau(100, 50, 15, 8, 20);

        // 1. Je lui inflige 30 dégâts
        v.receiveDamage(30);
        assertEquals(70, v.getHp(), "Les PV doivent passer à 70 après 30 dégâts");

        // 2. Je lui inflige un coup fatal (100 dégâts)
        v.receiveDamage(100);
        // Règle : la vie ne doit jamais être négative (ex: -30)
        assertEquals(0, v.getHp(), "Les PV ne doivent jamais descendre sous 0");
        assertFalse(v.isAlive(), "Le vaisseau doit être considéré comme mort à 0 PV");
    }

    @Test
    public void testConsommationEnergie() {
        // Vaisseau avec 50 d'énergie
        Vaisseau v = new Vaisseau(100, 50, 15, 8, 20);

        // 1. Premier tir spécial (coûte 30) -> Doit réussir
        boolean tir1Reussi = v.useEnergy(30);
        assertTrue(tir1Reussi, "Le tir doit réussir car j'ai 50 d'énergie");
        assertEquals(20, v.getEnergy(), "Il doit me rester 20 d'énergie");

        // 2. Deuxième tir spécial (coûte 30 alors qu'il n'en reste que 20) -> Doit être refusé
        boolean tir2Reussi = v.useEnergy(30);
        assertFalse(tir2Reussi, "Le tir doit être refusé par manque d'énergie");
        assertEquals(20, v.getEnergy(), "L'énergie ne doit pas descendre sous zéro");
    }

    @Test
    public void testRechargeEnergie() {
        Vaisseau v = new Vaisseau(100, 20, 15, 8, 20);

        // Je ramasse un bonus d'énergie (+50)
        v.restoreEnergy(50);
        assertEquals(70, v.getEnergy(), "L'énergie doit passer de 20 à 70");

        // Règle : l'énergie ne doit jamais dépasser 100%
        v.restoreEnergy(100);
        assertEquals(100, v.getEnergy(), "L'énergie maximale doit être bloquée à 100");
    }
}