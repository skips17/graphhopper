# Documentation des tests

## 1. Contexte

Les tests présentés ici portent sur la classe Path du module core de GraphHopper. 

L’objectif est de vérifier la méthode forEveryEdge de Path. Pour cela, deux classes sont simulées avec Mockito : Graph et EdgeIteratorState
Ces mocks permettent de contrôler le comportement du graphe artificiellement, de simuler la structure d’un chemin, et de tester la réaction de Path face aux erreurs sans dépendre d’un graphe réel.
Graph permet de récupérer une arete en fonction d'un identifiatn d'arete (edgeId) et d'un noeud de base (baseNode)
## 2. Définition des mocks

Dans les deux tests, on crée d’abord un graphe simulé et une ou plusieurs arêtes simulées.

```java
Graph graphe = mock(Graph.class);

EdgeIteratorState a1 = mock(EdgeIteratorState.class);
EdgeIteratorState a2 = mock(EdgeIteratorState.class);
```

Raisons du choix :

- Graph est mocké pour contrôler la réponse de getEdgeIteratorState(edgeId, baseNode) sans avoir à construire un vrai graphe en mémoire.
- Path se sert de EdgeIteratorState pour connaitre le noeud suivant(getBaseNode()) et l'identifiant de l'arete(getEdge()). C'est mocké pour simuler un chemin,fixer les valeurs retournées par getBaseNode et getEdge, ce qui permet de simuler un chemin d’arêtes sans dépendre de la structure interne de GraphHopper.


## 3. Test 1 : testParcoursDeuxAretes

### Intention du test

Vérifier que la méthode forEveryEdge de Path parcourt bien toutes les arêtes ajoutées au chemin, dans le bon ordre, et qu’elle appelle le visiteur avec les EdgeIteratorState attendus.

On simule un chemin de deux arêtes 5 puis 7, et on s’assure que le visiteur voit la séquence [5, 7].

### Mise en place des mocks

On configure le graphe et les arêtes simulées pour représenter un chemin 0 → 1 → 2 :

```java
when(graphe.getEdgeIteratorState(5, 0)).thenReturn(a1);
when(a1.getBaseNode()).thenReturn(1);
when(a1.getEdge()).thenReturn(5);
when(graphe.getEdgeIteratorState(5, 1)).thenReturn(a1);

when(graphe.getEdgeIteratorState(7, 1)).thenReturn(a2);
when(a2.getBaseNode()).thenReturn(2);
when(a2.getEdge()).thenReturn(7);
when(graphe.getEdgeIteratorState(7, 2)).thenReturn(a2);
```

Justification :

- L’appel p.addEdge(5) demande ensuite à Path de récupérer l’arête 5 à partir des nœuds (0 puis 1). D’où les appels getEdgeIteratorState(5, 0) et getEdgeIteratorState(5, 1).
- On fait la meme chose pour l’arête 7 entre les nœuds 1 et 2.
- Les valeurs de baseNode (1 puis 2) simulent la progression du chemin.

### Construction du chemin et visite des arêtes

```java
Path p = new Path(graphe).setFromNode(0).setEndNode(2);
p.addEdge(5);
p.addEdge(7);

List<Integer> resultat = new ArrayList<>();

p.forEveryEdge(new Path.EdgeVisitor() {
    @Override
    public void next(EdgeIteratorState edge, int index, int prevId) {
        resultat.add(edge.getEdge());
    }

    @Override
    public void finish() {}
});
```

- Path est construit en lui passant le graphe simulé, puis on indique le nœud de départ (0) et le nœud d’arrivée (2).
- On ajoute les deux arêtes 5 et 7 au chemin.
- Dans le visiteur, à chaque appel de next, on récupère l’identifiant de l’arête via edge.getEdge() et on l’ajoute dans la liste resultat.

### Oracle

```java
assertEquals(List.of(5, 7), resultat);
```

Résultat attendu :

- La liste resultat doit contenir l’ordre qui doit être [5, 7], ce qui prouve que Path parcourt bien les arêtes dans l’ordre où elles ont été ajoutées et qu’il utilise correctement le graphe pour récupérer les EdgeIteratorState.


## 4. Test 2 : testExceptionSiAreteManquante

### Nom du test

testExceptionSiAreteManquante

### Intention du test

Vérifier que la méthode forEveryEdge de Path réagit correctement lorsqu’une arête attendue n’existe pas dans le graphe.

Dans ce cas, on s’attend à ce que Path lève une IllegalStateException avec un message indiquant que l’arête est vide.

### Mise en place du mock

On  simule un graphe sans arête valide :

```java
Graph graphe = mock(Graph.class);

when(graphe.getEdgeIteratorState(11, 0)).thenReturn(null);
```
### Construction du chemin et appel de forEveryEdge

```java
Path p = new Path(graphe).setFromNode(0).setEndNode(1);
p.addEdge(11);

IllegalStateException ex = assertThrows(
        IllegalStateException.class,
        () -> p.forEveryEdge(new Path.EdgeVisitor() {
            @Override
            public void next(EdgeIteratorState edge, int index, int prevId) {}

            @Override
            public void finish() {}
        })
);
```

- On construit un chemin de 0 à 1 comportant une seule arête : 11.
- Lors de l’exécution de forEveryEdge, Path essaye de récupérer l’arête 11.
- Comme le mock renvoie null, la méthode doit lever une IllegalStateException.

### Oracle

```java
assertTrue(ex.getMessage().contains("Edge 11 was empty"));
```

Résultat attendu :

- Une IllegalStateException doit être renvoyée.
- Le message doit contenir la chaîne Edge 11 was empty.


# Documentation des modification de l'action


###  Compilation et tests Maven

```yaml
      - name: Build ${{ matrix.java-version }}
        id: build
        run: mvn -B clean install
```

**Choix et justification**
- La commande mvn -B clean install construit l’ensemble des modules de GraphHopper et exécute les tests unitaires.
- L’utilisation de install (et non seulement test) installe les artefacts dans le dépôt Maven local (~/.m2/repository), ce qui garantit que le module navigation peut résoudre les dépendances vers graphhopper-core et graphhopper-web-api lorsque PIT est lancé plus tard.
- L’option -B (batch mode) est recommandée pour les scripts CI.

**Validation**

<img src="graphhopper/mutation/buildsucces.png" />


---

### Étape Rickroll en cas d’échec

```yaml
      - name: Rickroll
        if: failure() && matrix.java-version == 24
        run: |
          echo ""plus de details sur l'erreur:https://www.youtube.com/watch?v=dQw4w9WgXcQ"
```

**Choix et justification**
- Si un ou plusieurs tests échouent un message se faisant passer pour un lien renvoyant vers plus de détails sur l'erreur est en réalite un lien youtube pointant vers Never Gonna Give You Up de Rick Astley. Un test a été modifié pour provoquer ce message d'erreur.

**Validation**

<img src="graphhopper/mutation/Rickroll.png" />

---

### Téléchargement du score de mutation précédent

```yaml
      - name: Télécharger le score précédent
        if: matrix.java-version == 24
        uses: dawidd6/action-download-artifact@v3
        continue-on-error: true
        with:
          name: fichierScore
          workflow: build.yml
```

**Choix et justification**
- On doit comparer le score de mutation actuel avec celui de la précédente exécution du workflow.
- L’action dawidd6/action-download-artifact permet de récupérer l’artefact fichierScore produit lors d’un run précédent.
- continue-on-error: true évite de faire échouer le job si aucun artefact n’existe encore. Dans ce cas, la comparaison traitera l’absence de fichier comme un score initial.

**Validation**

<img src="graphhopper/mutation/fichierScore.png" />


---

### Exécution des tests de mutation avec PIT

```yaml
      - name: Tests de mutation
        id: pitest
        if: matrix.java-version == 24
        run: |
          mvn org.pitest:pitest-maven:mutationCoverage -pl navigation | tee pitest.log

          nouveauScore=$(grep "mutations Killed" pitest.log | grep -o '[0-9]*%' | tr -d '%')
          if [ -z "$nouveauScore" ]; then
            echo "Aucun score trouvé."
            exit 1
          fi
          echo "Nouveau score: $nouveauScore%"
          echo "score=$nouveauScore" >> $GITHUB_OUTPUT
```

**Choix et justification**
- La commande mvn org.pitest:pitest-maven:mutationCoverage -pl navigation lance PIT sur le module navigation. 
- La sortie standard est dupliquée dans le fichier pitest.log grâce à tee. Ce fichier est ensuite analysé pour extraire la ligne contenant le pourcentage de mutations tuées.
- La commande

  bash
  nouveauScore=$(grep "mutations Killed" pitest.log | grep -o '[0-9]*%' | tr -d '%')
  

  récupère la valeur numérique du score de mutation.
La variable de sortie score est exposée via GITHUB_OUTPUT pour être utilisée dans l’étape de comparaison.

**Validation**

<img src="graphhopper/mutation/mutationValide.png" />


---

### Comparaison des scores

```yaml
      - name: Comparaison des scores
        id: compare
        if: matrix.java-version == 24
        run: |
          nouveauScore=${{ steps.pitest.outputs.score }}
          echo "Score: $nouveauScore%"
          if [ ! -f fichierScore.txt ]; then
            echo "Aucun score trouvé. $nouveauScore%"
          else
            ancienScore=$(cat fichierScore.txt)
            echo "Score précédent: $ancienScore%"
            if [ "$nouveauScore" -lt "$ancienScore" ]; then
              echo "Score baissé: $ancienScore% → $nouveauScore%"
              exit 1
            else
              echo " $ancienScore% → $nouveauScore%"
            fi
          fi
          echo "$nouveauScore" > fichierScore.txt
```

**Choix et justification**
- L’étape récupère la nouvelle valeur du score de mutation depuis steps.pitest.outputs.score.
- Si le fichier fichierScore.txt n’existe pas, le script affiche le nouveau score et l’enregistre comme référence.
- Si le fichier existe, on lit l’ancien score, on l’affiche, puis on compare les deux valeurs :
  - Si nouveauScore est inférieur à ancienScore, l’étape échoue avec exit 1. Cela marque le job comme en échec et signale une régression de la qualité des tests.
  - Sinon le score est stable ou amélioré et on met à jour fichierScore.txt avec la nouvelle valeur.

**Validation**

Cas avec score plus bas

<img src="graphhopper/mutation/casdeTestScorePlusBas.png" />

Cas avec score maintenu

<img src="graphhopper/mutation/casdeTestScoreMaintenu.png" />

---

### Enregistrement du nouveau score en tant qu’artefact

```yaml
      - name: enregistrement du score
        if: success() && matrix.java-version == 24
        uses: actions/upload-artifact@v4
        with:
          name: fichierScore
          path: fichierScore.txt
```

**Choix et justification**
On enregistre le score en utilisant un fichier pour le garder en mémoire afin de le comparer dans les runs suivantes avec le fichierScore.

**Validation**

<img src="graphhopper/mutation/testMutationValide.png" />


