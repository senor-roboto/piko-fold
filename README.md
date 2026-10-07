# Piko Fold

Fork de [Piko](https://github.com/crimera/piko) avec une première adaptation réelle de X pour un grand écran **4:3 en paysage uniquement**.

[Ajouter Piko Fold à Morphe](https://morphe.software/add-source?github=senor-roboto/piko-fold) · [Releases](https://github.com/senor-roboto/piko-fold/releases) · [Guide complet et limites](docs/FOLD.md)

Utiliser **X 12.19.1-release.0 en APKM original** et sélectionner le patch **Fold landscape 4:3 layout**. Les autres patchs Piko sont inclus dans cette même source.

La v1 apporte un rail qui utilise les onglets natifs, une colonne de lecture réglable et la suppression des marges tablette excessives dans le mode cible. Elle couvre accueil, profils, recherche, posts détaillés, favoris et messages classiques. Le portrait, les petites fenêtres et les autres ratios gardent la disposition d’origine. La v1 conserve la navigation habituelle liste/détail ; les deux panneaux indépendants sont une évolution future.

Réglages : **Piko → Fold landscape 4:3**. Largeur par défaut : 640 dp. Relancer X après avoir changé le commutateur principal pour réévaluer les marges des écrans déjà ouverts.

La compilation et les tests Android sont exécutés par [GitHub Actions](https://github.com/senor-roboto/piko-fold/actions/workflows/fold.yml). Les essais sur appareil restent nécessaires pour valider les écrans avec un compte réel.

Les patches Piko d’origine, leur licence GPLv3 et leur [NOTICE](NOTICE) sont conservés. Auteur amont : **crimera et les contributeurs Piko**. Voir le [README amont conservé](docs/UPSTREAM-README.md) pour son catalogue et son historique.
