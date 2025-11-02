package graph.scc;

import common.DirectedGraph;
import common.Edge;
import utils.Metrics;

import java.util.*;

public class TarjanSCC {

    public static class Result {
        public final List<List<Integer>> components;
        public final int[] compOf; // component id for each vertex
        public Result(List<List<Integer>> comps, int[] compOf) {
            this.components = comps; this.compOf = compOf;
        }
    }

    private int index;
    private int[] idx;
    private int[] low;
    private boolean[] onStack;
    private Deque<Integer> stack;
    private List<List<Integer>> comps;
    private int[] compOf;
    private Metrics metrics;
    private DirectedGraph g;

    public Result compute(DirectedGraph g, Metrics metrics) {
        this.g = g;
        this.metrics = metrics;
        int n = g.n();

        // STEP 0: initialize all helper arrays and structures
        index = 0;
        idx = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new ArrayDeque<>();
        comps = new ArrayList<>();
        compOf = new int[n];
        Arrays.fill(idx, -1); // -1 means unvisited

        metrics.start();

        // STEP 1: launch DFS for every unvisited vertex
        for (int v = 0; v < n; v++) {
            if (idx[v] == -1) dfs(v);
        }

        metrics.stop();
        return new Result(comps, compOf);
    }

    private void dfs(int v) {
        metrics.dfsCalls++; // METRICS: count DFS calls

        // STEP 2: assign discovery index and low-link value
        idx[v] = low[v] = index++;
        stack.push(v);
        onStack[v] = true;

        // STEP 3: explore neighbors
        for (Edge e : g.neighbors(v)) {
            metrics.edgesVisited++; // METRICS: count edge visits
            int w = e.v;
            if (idx[w] == -1) {
                // TREE edge: DFS deeper
                dfs(w);
                low[v] = Math.min(low[v], low[w]); // WHY: propagate low-link from child
            } else if (onStack[w]) {
                // BACK edge: update low-link with discovery index of ancestor
                low[v] = Math.min(low[v], idx[w]);
            }
        }

        // STEP 4: root of SCC → pop stack until v
        if (low[v] == idx[v]) {
            List<Integer> comp = new ArrayList<>();
            while (true) {
                int w = stack.pop();
                onStack[w] = false;
                comp.add(w);
                compOf[w] = comps.size(); // assign SCC id
                if (w == v) break; // stop when we reach root
            }
            comps.add(comp); // store found component
        }
    }
}