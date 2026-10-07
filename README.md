# Piko Fold

Fork de [Piko](https://github.com/crimera/piko) avec une adaptation de X pour un grand écran **4:3 en paysage uniquement**.

[Ajouter Piko Fold à Morphe](https://morphe.software/add-source?github=senor-roboto/piko-fold) · [Releases](https://github.com/senor-roboto/piko-fold/releases) · [Guide complet et limites](docs/FOLD.md)

Utiliser **X 12.19.1-release.0 en APKM original** et sélectionner le patch **Fold landscape 4:3 layout**. Les autres patchs Piko sont inclus dans cette même source.

La v2 (3.10.0) apporte un rail compact sous la barre système, des boutons centrés et un contenu qui garde la même place lors des changements d’onglet. Sur les systèmes compatibles, ouvrir un post conserve le fil à gauche et affiche la conversation à droite dans deux véritables écrans X. Un séparateur redimensionnable et le passage en plein écran par glissement sont utilisés lorsque le système les fournit.

Les deux panneaux nécessitent une fenêtre 4:3 en paysage d’au moins 768 dp et la prise en charge native Activity Embedding. Les réglages affichent la disponibilité et la largeur courante. Sinon, la colonne de lecture réglable reste active. Le portrait, les petites fenêtres et les autres ratios gardent la disposition d’origine.

Réglages : **Piko → Fold paysage 4:3**. Largeur de la colonne unique : 640 dp par défaut. Relancer X après avoir changé les commutateurs du mode ou des deux panneaux.

La compilation et les tests Android sont exécutés par [GitHub Actions](https://github.com/senor-roboto/piko-fold/actions/workflows/fold.yml). Les essais sur appareil restent nécessaires pour valider les écrans avec un compte réel.

Les patches Piko d’origine, leur licence GPLv3 et leur [NOTICE](NOTICE) sont conservés. Auteur amont : **crimera et les contributeurs Piko**. Voir le [README amont conservé](docs/UPSTREAM-README.md) pour son catalogue et son historique.
