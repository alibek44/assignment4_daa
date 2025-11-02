package graph.dagsp;

import common.DirectedGraph;
import common.Edge;
import utils.Metrics;
import graph.topo.TopologicalSortKahn;

import java.util.*;

public class DagShortestPaths {
    private final DirectedGraph g;
    private final int source;
    private final int[] dist;
    private final int[] prev;

    public DagShortestPaths(DirectedGraph g, int source) {
        this.g = g; this.source = source;
        this.dist = new int[g.n()];
        this.prev = new int[g.n()];
        // STEP 0: initialize DP table
        // WHY: use a large "INF" so any real path improves it; /4 avoids overflow on additions
        Arrays.fill(dist, Integer.MAX_VALUE/4);
        Arrays.fill(prev, -1);
    }

    public void run(Metrics metrics) {
        // STEP 1: compute topological order (required for DAG SSSP)
        // WHY: when processing u, all predecessors already finalized → safe relaxations
        List<Integer> topo = TopologicalSortKahn.order(g, new Metrics()); // separate metrics for topo phase

        metrics.start();
        // STEP 2: seed source distance
        dist[source] = 0;

        // STEP 3: relax edges in topo order (classic DAG SSSP)
        for (int u : topo) {
            if (dist[u] == Integer.MAX_VALUE/4) continue; // EDGE: unreachable so far → skip
            for (Edge e : g.neighbors(u)) {
                int v = e.v;
                int nd = dist[u] + e.w;   // candidate distance via u
                if (nd < dist[v]) {
                    dist[v] = nd;
                    prev[v] = u;
                    metrics.relaxations++; // METRICS: count successful relax
                }
            }
        }
        metrics.stop();
    }

    public int distTo(int v) { return dist[v]; } // STEP: O(1) query

    public List<Integer> pathTo(int v) {
        // EDGE: unreachable → empty path
        if (dist[v] >= Integer.MAX_VALUE/4) return Collections.emptyList();
        // STEP: reconstruct path by following prev[] back to source
        LinkedList<Integer> path = new LinkedList<>();
        for (int x = v; x != -1; x = prev[x]) path.addFirst(x); // WHY: prev[] is a back-pointer chain
        return path;
    }
}