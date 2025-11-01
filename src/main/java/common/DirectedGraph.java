package common;

import java.util.*;

public class DirectedGraph {
    private final int n;
    private final List<List<Edge>> adj;
    private final List<Edge> edges;

    public DirectedGraph(int n) {
        this.n = n;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        this.edges = new ArrayList<>();
    }

    public int n() { return n; }

    public void addEdge(int u, int v, int w) {
        Edge e = new Edge(u, v, w);
        adj.get(u).add(e);
        edges.add(e);
    }

    public List<Edge> neighbors(int u) { return adj.get(u); }

    public List<Edge> getEdges() { return edges; }

    public static DirectedGraph fromEdges(int n, List<Edge> edges) {
        DirectedGraph g = new DirectedGraph(n);
        for (Edge e : edges) g.addEdge(e.u, e.v, e.w);
        return g;
    }
}
