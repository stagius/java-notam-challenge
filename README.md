# java-notam-challenge

## L'objectif

Dans le cadre de nos offres de stages nous souhaitons évaluer les candidats sur des aspects humains mais aussi des aspects techniques. Pour la première partie rien ne vaut une rencontre IRL. Mais pour la partie technique rien ne vaut l'analyse d'un bout de code produit pour résoudre un problème précis et mettant en valeur le niveau de créativité, de logique et de rigueur du candidat à un stage dans nos équipes.

C'est pourquoi nous avons imaginé ce "code challenge" qui nous permettra d'apprécier au mieux votre motivation, votre patte de développeur et votre sens pratique.

Le code produit sera évalué sur les points suivants (par ordre d'importance) :
* Respect des spécifications données
* Clarté du code
* Robustesse du code (gestion des exceptions et logging associé)
* Performance du code
* Élégance des solutions retenues

Bien entendu votre code fera l'objet de discussion lors de notre prochains entretiens IRL.

## Le contexte métier

Les NOTAM (Notice To Air Men) sont des messages produits par les services de la navigation aérienne d'un état à destination des utilisateurs de son espace aérien.

En France, ces messages sont publiés sur le site officiel suivant : http://notamweb.aviation-civile.gouv.fr/

Le site officiel est plutôt ancien et ne propose aucune API de récupération des données. Pour récupérer les NOTAMs il est donc nécessaire de "crawleré le site pour en extraire les données.

Un formulaire permet de récupérer les NOTAM par FIR (Flight Information Region, il y en a 5 en France : LFBB, LFEE, LFFF, LFMM et LFRR) en précisant certain critères (durée, IFR/VFR, Flight Level min et max, FIR concernées, ...). Le but est d'utiliser ce formulaire pour récupérer un ensemble de NOTAM de manière programmatique.

La compréhension des notions aéronautiques utilisées dans les NOTAM n'est pas nécessaire pour la réalisation du code.

Il n'y a pas de piège et l'ensemble des recettes nécessaires à la réalisation de challenge sont disponibles sur Google. A vous de savoir trouver, comprendre et adapter en comprenant ce que vous faite.

## Le challenge

Le programme doit récupérer et afficher les NOTAM des **12 prochaines heures** pour la **FIR LFBB** depuis NOTAM-Web et afficher le code du NOTAM et le **champ Q** dans la console puis se stopper proprement le programme.

Chaque NOTAM ne devra être affichée qu’une seul fois par exécution.

Comme le site NOTAM-Web est relativement instable et retourne aléatoirement une erreur quand les requêtes ont faites en mode `COMPLET` vous devrez réaliser `1+n` requête(s). 
La première requête permet de lister les NOTAMs disponibles et les requêtes suivantes permettent de récupérer le détail de chaque NOTAM.

### Lister les NOTAMs

Pour récupérer la liste des NOTAM vous devrez réaliser une requêtes HTTP POST de la forme suivante :

```
POST http://notamweb.aviation-civile.gouv.fr/Script/IHM/Bul_FIR.php?FIR_Langue=FR
bResultat true
ModeAffichage RESUME
FIR_Date_DATE 2020/09/29 // maintenant
FIR_Date_HEURE 14:00 // maintenant
FIR_Langue FR
FIR_Duree 12 // 12 heures
FIR_CM_REGLE 1 // IFR/VFR
FIR_CM_GPS 2 // non
FIR_CM_INFO_COMP 2 // oui
FIR_NivMin 0
FIR_Niv_Max 20
FIR_Tab_Fir[0] LFBB 
```

Vous pouvez tester la requête avec l'éxécutable `curl` :

```bash
$ curl -F 'bResultat=true' -F 'ModeAffichage=RESUME' -F 'FIR_Date_DATE=2020/10/05' -F 'FIR_Date_HEURE=19:00' -F 'FIR_Duree=12' -F 'FIR_CM_REGLE=1' -F 'FIR_CM_GPS=2' -F 'FOR_CM_INFO_COMP=2' -F 'FIR_NivMin=0' -F 'FIR_NivMax=5' -F 'FIR_Tab_Fir[0]=LFBB'  -X POST http://notamweb.aviation-civile.gouv.fr/Script/IHM/Bul_FIR.php?FIR_Langue=FR

> <HTML><HEAD><TITLE>(R&eacute;sultat)BULLETIN FIR (Fran&ccedil;ais)</TITLE>...
```

Aide sur CURL : https://gist.github.com/subfuzion/08c5d85437d5d4f00e58

**Attention, une requête retournant trop de NOTAM pourra échouer si le nombre de NOTAM demandé est trop long. Vous devez donc être capable de prendre en compte cet échec et d’adapter de recommencer la requête jusqu’à ce que cela fonctionne.**

### Parser le résultat

Une fois la requête exécutée vous obtenez le résultat sous la forme d’une page HTML qu’il va falloir parser pour extraire une liste manipulable des identifiants de NOTAM à récupérer individuellement à l’étape suivante. 
Plusieurs stratégies sont possibles pour parser la page mais nous vous recommandons de faire simple et de vous contenter de manipulation de chaîne et/ou d’expressions régulières.

### Récupérer le détail d’une NOTAM

Pour chaque identifiant de NOTAM récupéré précédemment vous devez récupérer le détail avec la requête suivante :

```
@todo
```

De même que pour la liste vous devrez parser le résultat pour obtenir le champ **Q**.

## Le livrable

Vous devrez implémenter le programme en **Java 11** et nous fournir le code sous la forme d’un dépôt Github clonable qui sera un fork de ce dépôt : https://github.com/innovatm/java-notam-challenge

Votre code sera récupéré et exécuté dans l’environnement cible :

* Linux Ubuntu ou MacOSX
* Java 11 installé et JAVA_HOME correctement renseignée

Le code et tous les fichiers produits doivent être en anglais (noms des variables et fonctions, documentation, ...).

Pour tester votre code nous exécuterons les commandes suivantes :

```bash
$ git clone https://github.com/{VOTRE_USERNAME_GITHUB}/java-notam-challenge
$ cd java-notam-challenge
$ javac src/main/java/*.java
$ java -cp src/main/java/ NotamCrawler
```

Vous verrez que le dépôt actuel, une fois cloner propose déjà un squellete de code éxécutable qui affiche la sortie suivante, cela vous permet de tester votre environnement avant de démarrer :

```bash
> Ready to implement!
```

A vous d’implémenter votre programme à partir du fichier `src/main/java/NotamCrawler.java`.

## Bonus

Si vous en avez la motivation vous pouvez aller une peu plus loin en réalisant un ou plusieurs objectifs bonus :

* Ajouter une option à la ligne de commande pour choisir entre la récupération de la liste des NOTAMs décrite ci-avant ou la récupération et l’affichage d’une NOTAM précise par son identifiant passé en argument de la ligne de commande
* Enregistrer pour chaque NOTAM son code et tous ses champs (Q, A, B, D, E, F, G) dans un fichier CSV

Bon courage ! En effet en tant que futur développeur professionnel ce cas pratique est très représentatif des problèmes à résoudre que vous rencontrerez dans votre futur travail. Votre capacité à rechercher, analyser et appliquer les solutions trouvées sur Internet seront la clé.