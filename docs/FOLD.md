# Piko Fold : v3 paysage 4:3 (3.11.0)

Fork de [Piko](https://github.com/crimera/piko), patch **Fold landscape 4:3 layout**.
Version cible : **X 12.19.1-release.0 en APKM original**.

## Installer avec Morphe

1. Ajouter ou actualiser [la source Piko Fold](https://morphe.software/add-source?github=senor-roboto/piko-fold).
2. Repartir de l’APKM original X 12.19.1-release.0.
3. Activer **Fold landscape 4:3 layout**, puis les autres patchs Piko souhaités.
4. Patcher et installer. Ouvrir **Piko → Fold paysage 4:3**.
5. Relancer X après avoir modifié le mode complet ou la juxtaposition des messages.

Morphe conserve sa clé de signature pour les mises à jour. Une installation officielle
peut devoir être désinstallée si sa signature diffère de celle de l’application patchée.

## Écrans et comportement

| Écran | Adaptation |
| --- | --- |
| Accueil | Rail natif compact de 64 dp, sélection arrondie, badges, position stable sous les barres système |
| Post détaillé | Post à gauche (46 %), réponses à droite (54 %), défilements indépendants, champ de réponse à droite |
| Messages classiques | Liste à gauche, conversation à droite via Activity Embedding, y compris depuis l’onglet messages de l’accueil |
| XChat intégré | Fondu natif sans le zoom de la pile ; disposition Compose native |
| Profils, recherche, favoris, réglages | Colonne de lecture centrée, largeur réglable et marges de listes adaptées |
| Médias, caméra, connexion, composition | Écrans natifs en pleine fenêtre |

Les colonnes nécessitent une largeur d’au moins **768 dp** dans le format cible.
Le mode cible exige au moins 600 × 480 dp, une largeur supérieure à la hauteur,
et un ratio entre 1,25 et 1,45 pour absorber les barres système autour de 4:3.
Le portrait, les autres ratios et les petites fenêtres reprennent leur disposition native.
La hauteur réduite par le clavier ne désactive pas le mode sur Android 11+.

Le post et ses réponses sont rendus par les vues natives de X et le même adaptateur
de données. Le post n’est pas une capture d’écran : ses boutons, liens et médias
utilisent les liaisons natives. Le RecyclerView des réponses conserve ses identifiants,
son contrôleur et les positions originales de ses éléments. Les ancêtres et le post
deviennent des cellules sans hauteur dans cette colonne, pour éviter les doublons.
Le post est identifié par son tag natif exact et suivi par son identifiant stable.
Si le post n’est pas reconnu ou disparaît des données, l’affichage d’origine sert de repli.
Aucune nouvelle bibliothèque RecyclerView ou Compose n’est ajoutée à l’APK.

Les messages classiques utilisent la bibliothèque système optionnelle
`androidx.window.extensions`, déjà déclarée par X. Le partage initial est 50:50.
Un séparateur système permet éventuellement de le modifier selon les API OEM.
Les règles sont limitées à RootDMActivity → DMActivity, ou MainActivity → DMActivity
avec une boîte de réception sélectionnée. Le support dépend du système et est indiqué
dans les réglages. Les colonnes de post ne dépendent pas de ce composant.

Le nouveau XChat intégré utilise une pile Compose/Decompose dans MainActivity,
sans DMActivity. Il ne bénéficie pas encore de la juxtaposition liste/conversation.
Son option de mouvements réduits remplace le composite fondu + zoom de X par le fondu
déjà livré avec l’application, tout en conservant le moteur de navigation.
X Lite reste exclu par la dépendance Piko correspondante.

## Réglages

- **Activer la disposition Fold** : toutes les adaptations, paysage 4:3 uniquement.
- **Navigation latérale** : rail compact avec les onglets natifs configurés par X/Piko.
- **Messages à deux panneaux** : juxtaposition des écrans de messagerie classique.
- **Post et réponses côte à côte** : disposition du détail de post, indépendante du support OEM.
- **Réduire les mouvements de XChat** : fondu sans zoom, uniquement dans le tchat et le format cible.
- **Largeur de lecture** : colonne unique, 640 dp par défaut, réglable de 480 à 840 dp.

## Validation

Le workflow compile le bundle et exécute les contrôles de géométrie et les tests Android.
Ces derniers couvrent le rail, le rendu de ses icônes, la restauration des vues,
la projection des données, les identifiants après insertion, les actions natives des
réponses et la restitution du RecyclerView original. Les nouveaux tests de listes
utilisent des adaptateurs de test et l’ABI de la version cible, pas l’intégralité de X.
Le patchage, la reconstruction et la signature sont également vérifiés sur l’APKM réel.
Les références RecyclerView de l’extension sont comparées aux méthodes et champs du DEX cible.

Il reste à essayer sur téléphone : ouvrir un post court puis long, une citation, un fil,
faire défiler les réponses et charger la suite, aimer/enregistrer/répondre, ouvrir les médias,
afficher le clavier, changer le tri, passer en portrait et revenir, désactiver les colonnes.
Tester également le tchat, les messages classiques, le thème clair/sombre et une petite fenêtre.
Les tests automatiques ne confirment pas le rendu et les gestes OEM sur un Fold réel.

## Développement

`./gradlew buildAndroid :patches:generatePatchesList` ;
`./gradlew :extensions:twitter:testDebugUnitTest`.
Les dépendances Morphe nécessitent GitHub Packages ; le workflow utilise son jeton éphémère.
Aucun APK X, compte, capture personnelle ou clé de signature n’est publié dans ce dépôt.
Code GPLv3, NOTICE et README amont conservés.
