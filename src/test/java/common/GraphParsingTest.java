package common;

import io.GraphIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GraphParsingTest {

    @Test
    public void loadSimpleDataset() throws Exception {
        GraphIO.Loaded loaded = GraphIO.load("data/small_1.json");
        assertNotNull(loaded);
        assertTrue(loaded.graph.n() >= 6);
        assertEquals("edge", loaded.weightModel);
        assertTrue(loaded.graph.getEdges().size() > 0);
    }
}
