package com.fightingSchool.core;

import javax.swing.*;

public class GameFrame extends JFrame {

    public GameFrame(){
        setTitle(" Fighting School - Space Battle Démo ");


        // Quand l'utilisateur ferme la fenêtre : quitter l'application
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Interdire le redimensionnement pour garder le layout simple
        setResizable(false);

        // Créer et ajouter notre panel de jeu (GamePanel contient la logique et le rendu)
        GamePanel panel = new GamePanel();
        add(panel);

        // Demande la fenêtre de prendre la taille minimale nécessaire (c'est basé preferredSize du panel)
        pack();

        // Centre la fenêtre sur l'écran
        setLocationRelativeTo(null);

        //Rendre la fenêtre visible
        setVisible(true);





        panel.requestFocusInWindow();





    }
}
