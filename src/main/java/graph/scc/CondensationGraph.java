package graph.scc;

import common.DirectedGraph;
import common.Edge;

import java.util.*;

public class CondensationGraph {

    // Helper pair to represent edges between SCC components
    public static class Pair {
        public final int u, v;
        public Pair(int u, int v) { this.u = u; this.v = v; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            Pair p = (Pair) o;
            return u == p.u && v == p.v;
        }
        @Override public int hashCode() { return Objects.hash(u, v); }
    }

    // STEP 1: Build condensation DAG using minimum edge weights between SCCs
    public static DirectedGraph buildMinEdgeWeights(DirectedGraph g, int[] compOf, int compCount) {
        Map<Pair, Integer> minW = new HashMap<>();

        // STEP 2: iterate all edges in the original graph
        for (Edge e : g.getEdges()) {
            int cu = compOf[e.u], cv = compOf[e.v];
            if (cu != cv) { // only edges between different SCCs
                Pair p = new Pair(cu, cv);
                // WHY: keep smallest weight for each SCC→SCC connection
                minW.put(p, Math.min(minW.getOrDefault(p, Integer.MAX_VALUE), e.w));
            }
        }

        // STEP 3: create new directed acyclic graph (DAG) of components
        DirectedGraph dag = new DirectedGraph(compCount);
        for (Map.Entry<Pair, Integer> en : minW.entrySet()) {
            dag.addEdge(en.getKey().u, en.getKey().v, en.getValue());
        }
        return dag;
    }

    // STEP 4: Build condensation DAG using maximum edge weights between SCCs
    public static DirectedGraph buildMaxEdgeWeights(DirectedGraph g, int[] compOf, int compCount) {
        Map<Pair, Integer> maxW = new HashMap<>();

        // STEP 5: iterate all edges, select the largest weight for each SCC→SCC link
        for (Edge e : g.getEdges()) {
            int cu = compOf[e.u], cv = compOf[e.v];
            if (cu != cv) {
                Pair p = new Pair(cu, cv);
                // WHY: for longest path computations (critical path)
                maxW.put(p, Math.max(maxW.getOrDefault(p, Integer.MIN_VALUE), e.w));
            }
        }

        // STEP 6: build the DAG with max-weighted edges
        DirectedGraph dag = new DirectedGraph(compCount);
        for (Map.Entry<Pair, Integer> en : maxW.entrySet()) {
            dag.addEdge(en.getKey().u, en.getKey().v, en.getValue());
        }
        return dag;
    }
}