# Piko Fold

Fork de [Piko](https://github.com/crimera/piko) avec une adaptation de X pour un grand écran **4:3 en paysage uniquement**.

[Ajouter Piko Fold à Morphe](https://morphe.software/add-source?github=senor-roboto/piko-fold) · [Releases](https://github.com/senor-roboto/piko-fold/releases) · [Guide complet et limites](docs/FOLD.md)

Utiliser **X 12.19.1-release.0 en APKM original** et sélectionner le patch **Fold landscape 4:3 layout**. Les autres patchs Piko sont inclus dans cette même source.

La v3 (3.11.0) conserve le rail corrigé et ouvre le **post à gauche, ses réponses à droite**, avec défilement indépendant et champ de réponse à droite. Le fil précédent ne reste plus affiché. Cette disposition utilise les vues natives et le même modèle de données X, sans Activity Embedding.

Les colonnes de post et la juxtaposition des messages classiques nécessitent au moins 768 dp en paysage 4:3. Les **messages classiques** conservent la liste à gauche et ouvrent la conversation à droite lorsque le système prend en charge Activity Embedding. Le XChat intégré utilise un **fondu natif sans zoom** ; sa disposition Compose reste native. La colonne de lecture réglable sert de repli. Le portrait, les petites fenêtres et les autres ratios gardent la disposition d’origine.

Réglages : **Piko → Fold paysage 4:3**, avec des interrupteurs séparés pour le post/réponses, les messages et les mouvements de XChat. Largeur de la colonne unique : 640 dp par défaut. Relancer X après avoir changé le mode complet ou la juxtaposition des messages.

La compilation et les tests Android sont exécutés par [GitHub Actions](https://github.com/senor-roboto/piko-fold/actions/workflows/fold.yml). Les essais sur appareil restent nécessaires pour valider les écrans avec un compte réel.

Les patches Piko d’origine, leur licence GPLv3 et leur [NOTICE](NOTICE) sont conservés. Auteur amont : **crimera et les contributeurs Piko**. Voir le [README amont conservé](docs/UPSTREAM-README.md) pour son catalogue et son historique.
