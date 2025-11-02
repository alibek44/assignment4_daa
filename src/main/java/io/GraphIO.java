package io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import common.DirectedGraph;
import common.Edge;

import java.io.File;
import java.util.*;

public class GraphIO {

    // DTO (Data Transfer Object) for JSON → Java mapping
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GraphDTO {
        @JsonProperty public boolean directed;
        @JsonProperty public int n;
        @JsonProperty public List<EdgeDTO> edges;
        @JsonProperty public Integer source;
        @JsonProperty("weight_model") public String weightModel;
    }

    // Inner DTO for edges inside JSON
    public static class EdgeDTO {
        @JsonProperty public int u;
        @JsonProperty public int v;
        @JsonProperty public int w;
    }

    // Object to hold parsed data after loading
    public static class Loaded {
        public final DirectedGraph graph;
        public final int source;
        public final String weightModel;
        public Loaded(DirectedGraph g, int s, String wm) { this.graph = g; this.source = s; this.weightModel = wm; }
    }

    public static Loaded load(String path) throws Exception {
        // STEP 1: read and parse JSON using Jackson
        ObjectMapper mapper = new ObjectMapper();
        GraphDTO dto = mapper.readValue(new File(path), GraphDTO.class);

        // STEP 2: ensure graph is directed (as required by assignment)
        if (!dto.directed) throw new IllegalArgumentException("Input must be directed");

        // STEP 3: convert EdgeDTO list → real Edge list
        List<Edge> edges = new ArrayList<>();
        for (EdgeDTO e : dto.edges) edges.add(new Edge(e.u, e.v, e.w));

        // STEP 4: build DirectedGraph object from edge list
        DirectedGraph g = DirectedGraph.fromEdges(dto.n, edges);

        // STEP 5: set defaults for optional fields
        int src = dto.source != null ? dto.source : 0;
        String wm = dto.weightModel != null ? dto.weightModel : "edge";

        // STEP 6: return packaged graph and metadata
        return new Loaded(g, src, wm);
    }
}