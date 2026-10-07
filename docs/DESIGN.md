# Direction produit de la v2

Les retours sur la v1 montrent deux problèmes : le rail démarre dans la zone système,
et l’ouverture d’un post conserve une présentation trop étendue pour la lecture.
La largeur du contenu peut aussi changer lorsque les contrôles natifs sont masqués
pendant la transition entre onglets.

Le rail devient une colonne étroite et stable. La capsule indique la sélection sans
reproduire une grande cellule rectangulaire de navigation horizontale. Le dessin natif
reste utilisé pour conserver les couleurs, personnalisations, badges et actions de X.
Le centrage se fait sur l’icône réelle, et non sur les marges de sa cellule d’origine.

Pour l’espace restant, le modèle retenu est un écran principal et un détail contextuel.
Il reprend l’idée utile du bureau : garder son contexte quand on consulte un contenu.
Deux fils simultanés ou une colonne permanente de suggestions consommeraient trop de place
sur le format 4:3 et n’apporteraient pas autant à la consultation d’une conversation.

Découper manuellement le post et ses réponses dans les RecyclerViews de X rendrait les
réponses groupées, les chargements et les médias fragiles. La v2 juxtapose plutôt les
véritables Activities avec le composant public du système : la navigation et les données
continuent d’appartenir à X. Le partage initial laisse 384 dp par écran au seuil de
768 dp, soit 320 dp pour le fil après son rail. En dessous, une seule colonne est retenue.

Le composant reste optionnel. Les règles, le manifeste et les API publiques sont vérifiés
dans le code et le patch appliqué à l’APKM ; leur rendu sur un Fold ne peut pas être déduit
des tests Robolectric. La disponibilité native et la largeur courante sont donc visibles
dans les réglages, et le mode à une colonne sert de repli.

Références primaires :

- [Activity Embedding Android](https://developer.android.com/develop/ui/views/layout/activity-embedding)
- [Propriétés de manifeste](https://developer.android.com/reference/androidx/window/WindowProperties)
- [API publique des règles OEM](https://github.com/androidx/androidx/blob/androidx-main/window/extensions/extensions/src/main/java/androidx/window/extensions/embedding/SplitPairRule.java)
- [API publique du séparateur](https://github.com/androidx/androidx/blob/androidx-main/window/extensions/extensions/src/main/java/androidx/window/extensions/embedding/DividerAttributes.java)
