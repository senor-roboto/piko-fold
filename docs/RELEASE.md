Première version de Piko Fold, basée sur Piko 3.9.0.

Ajouter cette source dans Morphe : https://morphe.software/add-source?github=senor-roboto/piko-fold

Utiliser l’APKM original **X 12.19.1-release.0**, puis sélectionner **Fold landscape 4:3 layout** et les autres patchs Piko souhaités.

- Rail latéral utilisant les vrais onglets de X, avec leurs icônes, badges et clics natifs.
- Colonne de lecture centrée, 640 dp par défaut, réglable de 480 à 840 dp.
- Suppression des marges natives tablette excessives dans les listes classiques.
- Activation uniquement en paysage proche de 4:3 sur une grande fenêtre. Retour à l’affichage d’origine en portrait et dans les petites fenêtres.
- Réglages dans **Piko → Fold landscape 4:3**. Relancer X après avoir modifié le commutateur principal.

La v1 adapte l’accueil, les profils, la recherche, les posts détaillés, les favoris et les messages classiques. Elle conserve la navigation habituelle liste/détail et ne fournit pas encore deux panneaux indépendants. Les activités de connexion, caméra, médias et composition restent natives. X Lite et son XChat ne sont pas couverts.

Le bundle est compilé et les tests Android doivent réussir avant publication. Les essais avec un compte réel sur Fold restent nécessaires. Le bundle n’est pas signé avec la clé GPG du projet Piko d’origine ; le fichier SHA256SUMS permet de contrôler son téléchargement.

Code sous GPLv3, dérivé du travail de crimera et des contributeurs Piko. Le fichier NOTICE est conservé.
