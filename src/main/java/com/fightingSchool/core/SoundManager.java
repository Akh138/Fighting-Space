package com.fightingSchool.core;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Mon Gestionnaire Audio (Sound Manager) en Singleton.
 * N'importe quel fichier du jeu peut m'appeler pour jouer un son en 1 ligne !
 */
public class SoundManager {

    // Mon instance unique (Pattern Singleton)
    private static SoundManager instance;
    private Clip musiqueClip;

    // Méthode pour m'appeler de n'importe où : SoundManager.getInstance()
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public SoundManager() {
        // Constructeur
    }

    // --- 1. JOUER LA MUSIQUE EN BOUCLE ---
    public void jouerMusique(String chemin) {
        new Thread(() -> {
            try {
                arreterMusique();

                InputStream is = getClass().getResourceAsStream(chemin);
                if (is == null) {
                    System.out.println("Musique introuvable : " + chemin);
                    return;
                }

                AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                musiqueClip = AudioSystem.getClip();
                musiqueClip.open(audioIn);
                musiqueClip.loop(Clip.LOOP_CONTINUOUSLY); // Boucle infinie
                musiqueClip.start();

            } catch (Exception e) {
                System.out.println("Erreur musique : " + e.getMessage());
            }
        }).start();
    }

    public void arreterMusique() {
        if (musiqueClip != null && musiqueClip.isOpen()) {
            musiqueClip.stop();
            musiqueClip.close();
        }
    }

    // --- 2. JOUER UN BRUITAGE (SFX) ---
    public void jouerSon(String chemin) {
        new Thread(() -> {
            try {
                InputStream is = getClass().getResourceAsStream(chemin);
                if (is == null) return;

                AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();

                // On ferme le son une fois terminé pour libérer la mémoire
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                // Silencieux
            }
        }).start();
    }
}