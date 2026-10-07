# Direction produit de la v3

La lecture d’un post devient une tâche dédiée : **le post et ses actions à gauche,
les réponses à droite**. Le fil précédent disparaît de cet écran. Le partage 46:54
donne davantage de place aux réponses, sans montrer le même post deux fois.
Chaque colonne conserve son propre défilement ; le champ de réponse reste à droite.

La v2 s’appuyait sur la juxtaposition d’Activities. Cela ne convient pas pour séparer
le post et les réponses, qui appartiennent au même écran. La v3 garde le RecyclerView
des réponses, ses contrôleurs et les positions de ses éléments. Un adaptateur de
projection masque les éléments jusqu’au post par des cellules sans hauteur. Un second
RecyclerView natif affiche uniquement le post depuis la même source de données.
Les créations, liaisons, identifiants et callbacks de cycle de vie des vues sont délégués
à X. La vue du post n’est pas déplacée hors de son ViewHolder.

Les changements de données sont regroupés après la passe de layout. Le post est
retrouvé par son identifiant stable, y compris si des ancêtres sont ajoutés. Si cet
identifiant disparaît, la disposition native reprend. En portrait ou hors du mode,
le RecyclerView d’origine retrouve son adaptateur, sa place et ses paramètres.
Cette approche exige encore un essai réel des listes, du chargement et des médias.
Les stubs RecyclerView servent uniquement à compiler et tester l’ABI de la version
cible : aucune nouvelle bibliothèque AndroidX n’est injectée dans X.

La juxtaposition d’Activities reste pertinente pour la **messagerie classique** :
la liste et la conversation sont deux écrans distincts. Les règles concernent
RootDMActivity → DMActivity, et MainActivity → DMActivity uniquement lorsque son
onglet courant est une boîte de réception. Elles excluent les posts et profils.

Le **XChat intégré** est un écran Compose avec une pile Decompose. Son rendu n’utilise
pas DMActivity. La v3 ne lui impose pas les règles de la messagerie classique.
Elle remplace le composite natif fondu + zoom (facteurs 1,15 et 0,95) par le fondu
fourni par X. Le moteur conserve la pile, les callbacks et le cycle de vie de la
navigation. L’option est limitée à l’onglet messages dans une fenêtre Fold cible.
Une disposition liste/conversation de XChat devra gérer cette pile et ses composants
avant de pouvoir être annoncée comme compatible.

Références primaires :

- [RecyclerView.Adapter et adaptateurs imbriqués](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter)
- [Activity Embedding](https://developer.android.com/develop/ui/views/layout/activity-embedding)
- [Animators Decompose](https://arkivanov.github.io/Decompose/extensions/compose/)

