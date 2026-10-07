# Piko Fold : première version paysage 4:3

Ce fork de [Piko](https://github.com/crimera/piko) ajoute le patch facultatif
**Fold landscape 4:3 layout** pour X **12.19.1-release.0**. Les autres patchs Piko
restent disponibles. Le code et le fichier NOTICE de Piko sont conservés.

## Installer avec Morphe

1. Ajouter [Piko Fold à Morphe](https://morphe.software/add-source?github=senor-roboto/piko-fold).
2. Utiliser l’APKM original X 12.19.1-release.0, sans le convertir en APK.
3. Activer **Fold landscape 4:3 layout**, puis les autres patchs Piko souhaités.
   Ses dépendances incluent les réglages Piko et le blocage de la redirection vers X Lite.
4. Appliquer les patchs et installer. Utiliser cette source pour les patchs Piko.
5. Ouvrir X sur l’écran interne en paysage. Dans les réglages Piko, ouvrir
   **Fold landscape 4:3** pour désactiver le mode, le rail ou choisir la largeur de lecture.

La signature change quand on patche une application : une installation X officielle
peut devoir être désinstallée avant d’installer la version patchée. Morphe conserve
sa clé de signature pour les mises à jour suivantes.

## Comportement de la v1

- **Paysage 4:3 uniquement** : largeur d’au moins 600 dp, hauteur d’au moins 480 dp,
  largeur supérieure à la hauteur, ratio entre 1,25 et 1,45. Cette petite tolérance
  prend en compte les barres système et les variations autour de 4:3.
- **Navigation latérale native** : le rail reproduit les vrais onglets configurés
  par X/Piko et transmet clics et appuis longs aux contrôles d’origine. Icônes,
  sélection et badges proviennent des vues natives. Les docks audio restent en bas.
  Si la structure des onglets n’est pas reconnue, la barre d’origine est conservée.
- **Lecture** : la colonne est centrée, limitée à 640 dp par défaut (réglable entre
  480 et 840 dp). Elle concerne accueil, profils, recherche, détails de posts,
  favoris, messages classiques et réglages X.
- **Marges tablette** : les marges natives de 120 dp de chaque côté des listes
  classiques sont désactivées dans le mode cible pour éviter une double marge.
- **Portrait et autres ratios** : le wrapper reprend les dimensions d’origine,
  la navigation du bas est restaurée. Il ne force aucune orientation.
- **Clavier et redimensionnement** : les dimensions de la fenêtre, plutôt que la
  hauteur momentanément réduite par le clavier, déterminent l’activation sur Android 11+.
- **Médias, caméra, connexion et composition** : les activités correspondantes
  gardent leur interface native. X Lite et son XChat ne sont pas adaptés par ce patch.

La v1 conserve la navigation habituelle entre liste et détail : elle n’ajoute pas
encore deux fils simultanés ni un panneau de détail indépendant. Elle intervient
sur les vues de l’application installée, sans page web embarquée ni données simulées.

Les réglages de largeur et de rail s’appliquent au retour à l’écran. Après avoir
activé/désactivé le mode complet, relancer X pour réévaluer aussi les marges natives
des écrans déjà ouverts.

## Validation et essai sur téléphone

Les tests automatiques vérifient la géométrie, la conservation des vues et de leurs
identifiants, les clics natifs du rail, le rétablissement de la barre du bas,
la désactivation et les limites de largeur. Les tests Android couvrent les API 28 et 35.
Le workflow compile le bundle Morphe et publie les résultats des tests.

Pour l’essai sur Fold : accueil → recherche → notifications → messages → profil →
post détaillé → favoris, puis ouvrir une image/vidéo, afficher le clavier,
passer en portrait et revenir en paysage, tester une petite fenêtre et les thèmes clair/sombre.
La validation physique reste distincte des tests automatiques.

## Développement

Le workflow `.github/workflows/fold.yml` construit la branche de travail. Sur `main`,
il publie une release `.mpp`, la liste des patchs et les métadonnées Morphe.
Le jeton éphémère GitHub Actions sert à lire les dépendances Morphe sur GitHub Packages.
Aucun APK X, compte utilisateur ou clé privée n’est publié dans le dépôt.

Build : `./gradlew buildAndroid`. Tests : `./gradlew :extensions:twitter:testDebugUnitTest`.
Les dépendances Morphe nécessitent une authentification GitHub Packages pour une compilation locale.

Le nom du modèle du téléphone ne conditionne pas le patch : la fenêtre réelle
détermine le comportement, y compris en mode multi-fenêtre.
