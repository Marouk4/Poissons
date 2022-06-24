# Simulateur de bancs de poisson

## Informations

Ce programme simule un banc de poisson grâce à 3 simples règles.

### 1 - Séparation

Les poissons vont chercher à ne pas se rentrer dedans.

### 2 - Alignement

Les poissons vont chercher à s'aligner avec les autres

### 3 - Cohésion

Les poissons vont chercher à se diriger vers un groupe.

## Utilisation du programme (QWERTY)

- Echap : Quitter
- Retour arrière : Pause
- Espace : Activer les 3 règles ou les désactiver
- S : Activer la règle de séparation
- A : Activer la règle d'alignement
- C : Activer la règle de cohésion
- H : Afficher le HUD
- P : Afficher les paramètres
- I : Afficher les informations de chaques règles
- T : Cibler d'un poisson
- R : Afficher le radar
- D : Activer le mode densité
- Entrée : Activer/Désactiver ou augmenter une propriété
- Flèche du haut : Sélectionner la propriété au dessus
- Flèche du bas : Sélectionner la propriété en dessous
- Plus clavier numérique : Augmenter une propriété numérique
- Moins clavier numérique : Diminuer une propriété numérique
- 0 clavier numérique : Réinitialiser les propriétés numériques
- Clic gauche souris : Ajouter 10 poissons à l'endroit du curseur 

## Sources :

- Utilisation de [LWJGL](https://www.lwjgl.org/) pour librairie graphique
- Inspiré de la [vidéo de Sebastian Lague](https://www.youtube.com/watch?v=bqtqltqcQhw) (j'ai imité les règles principales et le design)
- Texte avec [STB](https://github.com/LWJGL/lwjgl3/tree/master/modules/samples/src/test/java/org/lwjgl/demo/stb)