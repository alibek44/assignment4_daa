# Assignment 4 – Smart City / Smart Campus Scheduling

**Topics:** SCC (Tarjan) + Condensation DAG, Topological Ordering (Kahn), Shortest & Longest paths on a DAG.  
**Choice:** We use **edge weights** as the weight model.

## Project layout
```
assignment4_smart_city/
  ├─ data/                 # 9 datasets (small/medium/large)
  ├─ out/                  # metrics.csv gets appended here at runtime
  ├─ src/
  │  ├─ main/java/
  │  │  ├─ common/        # Edge, DirectedGraph, helpers
  │  │  ├─ graph/
  │  │  │  ├─ scc/        # TarjanSCC + CondensationGraph
  │  │  │  ├─ topo/       # TopologicalSortKahn
  │  │  │  └─ dagsp/      # DagShortestPaths, DagLongestPath
  │  │  └─ main/          # Main.java entry
  │  └─ test/java/
  │     ├─ graph/scc/TarjanSCCTest.java
  │     └─ common/GraphParsingTest.java
  └─ pom.xml
```

## Build
```bash
mvn -q -e -DskipTests=false test
mvn -q -DskipTests package
```

## Run
Choose a dataset and (optionally) a source vertex (defaults to the JSON `source` or 0).

```bash
java -jar target/assignment4-smart-city-1.0.0.jar --data data/small_1.json
java -jar target/assignment4-smart-city-1.0.0.jar --data data/medium_2.json --source 1
```

**What the program does for a dataset:**
1. Runs **Tarjan SCC**, prints components and sizes.
2. Builds **Condensation DAG** twice:
   - min-aggregated edge weights (for shortest paths),
   - max-aggregated edge weights (for critical/longest path).
3. Runs **Topological sort (Kahn)** on condensation DAG.
4. Runs **DAG shortest paths** from the source component on min-aggregated DAG.
5. Runs **DAG longest path** (critical path) from the source component on max-aggregated DAG.
6. Appends phase-level **metrics** into `out/metrics.csv`:
   - `time_ms, dfsCalls, edgesVisited, topoPushes, topoPops, relaxations`.

## Dataset format
We use **edge weights** throughout.
```json
{
  "directed": true,
  "n": 8,
  "edges": [{ "u": 0, "v": 1, "w": 3 }, ...],
  "source": 0,
  "weight_model": "edge"
}
```

## Report pointers (what to include)
- **Data summary:** for each of 9 datasets – (n, |E|, density, cyclic or DAG).
- **Results tables** (per phase): metrics + time vs `n` (or |E|). Pull from `out/metrics.csv` after several runs.
- **Analysis:** 
  - SCC: how DFS traversals scale; impact of cycles (large SCCs) on condensation size.
  - Topo: queue behavior (push/pop counts) vs density.
  - DAG-SP: relaxations vs path length / degree.
- **Conclusions / Recommendations:**
  - Use SCC to compress cycles → plan on DAG.
  - For DAG shortest/longest, topo-order DP is linear in `O(V+E)` and robust to nonnegative weights.
  - Critical path via longest-path DP (topo) is straightforward when no cycles remain.

## Tests
We include small deterministic tests:
- `TarjanSCCTest` (SCC correctness on a toy graph).
- `GraphParsingTest` (JSON load sanity).

> You can add extra tests (Topo, DAG-SP) if your rubric requires them.
# assignment4_daa
