package common;

import java.util.*;

public class GraphUtils {
    public static boolean isDag(int[] compOf) {
        return true;
    }

    public static int[] indegrees(DirectedGraph g) {
        int[] indeg = new int[g.n()];
        for (Edge e : g.getEdges()) indeg[e.v]++;
        return indeg;
    }
}
