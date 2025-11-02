package graph.topo;

import common.DirectedGraph;
import common.Edge;
import utils.Metrics;

import java.util.*;

public class TopologicalSortKahn {

    public static List<Integer> order(DirectedGraph g, Metrics metrics) {
        int n = g.n();
        int[] indeg = new int[n];

        // STEP 1: compute indegree for each vertex
        // WHY: indegree = number of incoming edges; 0 means no prerequisites
        for (Edge e : g.getEdges()) indeg[e.v]++;

        // STEP 2: enqueue all vertices with indegree 0
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (indeg[i] == 0) {
                q.add(i);
                metrics.topoPushes++; // METRICS: count initial queue pushes
            }
        }

        List<Integer> ord = new ArrayList<>(n);
        metrics.start();

        // STEP 3: process queue until empty
        while (!q.isEmpty()) {
            int u = q.remove();
            metrics.topoPops++; // METRICS: count nodes popped (processed)
            ord.add(u);

            // STEP 4: "remove" u by decreasing indegree of its neighbors
            for (Edge e : g.neighbors(u)) {
                int v = e.v;
                indeg[v]--;
                // WHEN indegree becomes 0 → all dependencies processed
                if (indeg[v] == 0) {
                    q.add(v);
                    metrics.topoPushes++; // METRICS: count new pushes
                }
            }
        }

        metrics.stop();

        // STEP 5: verify DAG (if not all vertices processed → cycle exists)
        if (ord.size() != n) throw new IllegalStateException("Graph is not a DAG");

        // STEP 6: return valid topological order
        return ord;
    }
}