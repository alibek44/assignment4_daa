package graph.dagsp;

import common.DirectedGraph;
import common.Edge;
import graph.topo.TopologicalSortKahn;
import utils.Metrics;

import java.util.*;

public class DagLongestPath {
    private final DirectedGraph g;
    private final int source;
    private final int[] dist;
    private final int[] prev;

    public DagLongestPath(DirectedGraph g, int source) {
        this.g = g; this.source = source;
        this.dist = new int[g.n()];
        this.prev = new int[g.n()];
        // STEP 0: initialize DP table
        // WHY: seed with very negative so any real path wins; /4 avoids overflow on addition
        Arrays.fill(dist, Integer.MIN_VALUE/4);
        Arrays.fill(prev, -1);
    }

    public void run(Metrics metrics) {
        // STEP 1: compute topological order (DAG requirement for longest-path DP)
        // WHY: when processing u, all predecessors are already finalized
        List<Integer> topo = TopologicalSortKahn.order(g, new Metrics()); // separate metrics for topo

        metrics.start();
        // STEP 2: seed source component
        dist[source] = 0;

        // STEP 3: relax edges in topo order using max-DP
        for (int u : topo) {
            if (dist[u] == Integer.MIN_VALUE/4) continue; // EDGE: unreachable state → skip
            for (Edge e : g.neighbors(u)) {
                int v = e.v;
                int nd = dist[u] + e.w;     // candidate longest via u
                if (nd > dist[v]) {
                    dist[v] = nd;
                    prev[v] = u;
                    metrics.relaxations++;  // METRICS: count successful relax
                }
            }
        }
        metrics.stop();
    }

    public int longestValue() {
        // STEP 4: scan dist[] for global maximum
        int best = Integer.MIN_VALUE/4;
        for (int x : dist) best = Math.max(best, x);
        return best;
    }

    public int argmax() {
        // STEP 5: index of the vertex with maximum dist (or -1 if none reachable)
        int best = Integer.MIN_VALUE/4, id = -1;
        for (int i = 0; i < dist.length; i++)
            if (dist[i] > best) { best = dist[i]; id = i; }
        return id;
    }

    public List<Integer> criticalPath() {
        // STEP 6: reconstruct path from argmax back to source via prev[]
        int t = argmax();
        if (t == -1 || dist[t] == Integer.MIN_VALUE/4) return Collections.emptyList(); // EDGE: no reachable nodes
        LinkedList<Integer> path = new LinkedList<>();
        for (int x = t; x != -1; x = prev[x]) path.addFirst(x); // WHY: prev[] is a back-pointer chain
        return path;
    }
}