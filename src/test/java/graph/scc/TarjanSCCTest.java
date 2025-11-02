package graph.scc;

import common.DirectedGraph;
import common.Edge;
import org.junit.jupiter.api.Test;
import utils.Metrics;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TarjanSCCTest {

    @Test
    public void smallGraphWithTwoSCCs() {
        // 0->1->2->0 (cycle), 3->4 (chain), 4->5->4 (cycle)
        DirectedGraph g = new DirectedGraph(6);
        g.addEdge(0,1,1);
        g.addEdge(1,2,1);
        g.addEdge(2,0,1);
        g.addEdge(3,4,1);
        g.addEdge(4,5,1);
        g.addEdge(5,4,1);

        TarjanSCC t = new TarjanSCC();
        TarjanSCC.Result r = t.compute(g, new Metrics());
        assertEquals(3, r.components.size()); // {0,1,2}, {4,5}, {3}

        // Check comp mapping consistency
        int c0 = r.compOf[0], c1 = r.compOf[1], c2 = r.compOf[2];
        assertEquals(c0, c1);
        assertEquals(c1, c2);

        int c4 = r.compOf[4], c5 = r.compOf[5];
        assertEquals(c4, c5);
        assertNotEquals(c0, c4);
    }
}
