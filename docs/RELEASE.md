Piko Fold v3 (3.11.0), basé sur Piko 3.9.0. X compatible : **12.19.1-release.0 en APKM original**.

Actualiser la source dans Morphe : https://morphe.software/add-source?github=senor-roboto/piko-fold
Repatcher l’APKM original avec **Fold landscape 4:3 layout** et les autres patchs Piko souhaités.

- **Post à gauche, réponses à droite**, avec défilement indépendant. Le champ de réponse se place sous les réponses. Le post ne reste plus dupliqué dans le fil précédent.
- Le même adaptateur de données X fournit le post et ses réponses : vues, boutons, médias, identifiants et actions restent natifs. Les positions des réponses restent identiques pour le contrôleur X.
- **Messages classiques** : liste des conversations à gauche et conversation à droite, y compris depuis l’onglet messages de l’accueil, sur les systèmes compatibles Activity Embedding. Un post ouvert depuis l’accueil occupe désormais sa propre fenêtre.
- **XChat intégré** : remplacement du zoom de navigation par le fondu déjà fourni par X. Option réversible, uniquement en mode Fold. Le XChat Compose ne bénéficie pas encore de la disposition liste/conversation.
- Rail compact et stable conservé ; largeur de lecture et marges adaptées pour accueil, profils, recherche, favoris et messages classiques.

Colonnes uniquement dans une fenêtre **4:3 en paysage d’au moins 768 dp**. Les colonnes post/réponses ne nécessitent pas le composant OEM. Si le post natif n’est pas reconnu, la liste d’origine reste disponible. Retour à la disposition native en portrait, petite fenêtre ou lorsque le mode est désactivé.

Réglages en français : **Piko → Fold paysage 4:3**. Relancer X après avoir modifié le mode complet ou les deux panneaux des messages.

La compilation et les tests doivent réussir avant publication. Le patchage, la reconstruction, la signature et les références au RecyclerView réel de X sont vérifiés sur l’APKM original. La validation avec un compte réel, le défilement des longues conversations et les médias restent à effectuer sur téléphone ; les tests de données et de restauration utilisent l’ABI de X et des adaptateurs de test.

Code sous GPLv3. Auteur amont : crimera et les contributeurs Piko. NOTICE conservé.

