Piko Fold v2 (3.10.0), basé sur Piko 3.9.0. X compatible : **12.19.1-release.0 en APKM original**.

Mettre à jour cette source dans Morphe : https://morphe.software/add-source?github=senor-roboto/piko-fold

Utiliser l’APKM original **X 12.19.1-release.0**, puis sélectionner **Fold landscape 4:3 layout** et les autres patchs Piko souhaités.

- Rail compact de 64 dp, positionné sous la barre système. Boutons centrés, sélection arrondie et badges natifs.
- Largeur du fil stable quand X masque temporairement ses onglets ou réécrit la hauteur de sa barre pendant un changement d’écran.
- Deux véritables écrans X côte à côte : le fil ou le profil à gauche, le post et sa conversation à droite. Également prévu pour certains profils et messages classiques.
- Deux panneaux uniquement sur les systèmes proposant Activity Embedding, dans une fenêtre 4:3 en paysage d’au moins 768 dp. Partage 50:50 ; séparateur redimensionnable et glissement vers le plein écran lorsque les API OEM correspondantes sont disponibles.
- Retour à la colonne de lecture réglable sur les systèmes non compatibles. Le portrait et les autres ratios conservent leur disposition.
- Réglages en français : **Piko → Fold paysage 4:3**. La disponibilité native et la largeur courante y sont affichées. Relancer X après avoir modifié le mode ou les deux panneaux.

Caméra, médias, connexion, composition et réglages gardent leurs écrans natifs en pleine fenêtre. X Lite et son XChat restent exclus.

La compilation et les tests Android, dont un test de rendu des icônes et un test de transition des onglets, doivent réussir avant publication. Le patchage, la reconstruction et la signature de l’APKM original sont vérifiés avec Morphe Desktop. La juxtaposition et les gestes fournis par le constructeur nécessitent encore une validation sur appareil.

Code sous GPLv3, dérivé du travail de crimera et des contributeurs Piko. Le fichier NOTICE est conservé.
