package main;

import common.DirectedGraph;
import graph.dagsp.DagLongestPath;
import graph.dagsp.DagShortestPaths;
import graph.scc.CondensationGraph;
import graph.scc.TarjanSCC;
import graph.topo.TopologicalSortKahn;
import io.GraphIO;
import utils.Metrics;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final String CSV_PATH = "out/metrics.csv";
    private static final String NA = "NA";

    private static void initCsv(boolean fresh) throws IOException {
        Files.createDirectories(Paths.get("out"));
        Path p = Paths.get(CSV_PATH);
        if (fresh && Files.exists(p)) Files.delete(p);
        if (!Files.exists(p)) {
            try (PrintWriter pw = new PrintWriter(new FileOutputStream(CSV_PATH, true))) {
                pw.println(String.join(",",
                        "dataset","phase","time_ms","dfsCalls","edgesVisited","topoPushes","topoPops","relaxations",
                        "scc_count","scc_sizes","scc_components",
                        "topo_components","derived_task_order",
                        "sssp_source_comp","sssp_distances","sssp_target_comp","sssp_distance","sssp_path_components",
                        "critical_path_components","critical_length"
                ));
            }
        }
    }
    private static String quote(String s){ return "\"" + s.replace("\"","\"\"") + "\""; }
    private static String joinInts(Collection<Integer> xs){
        return xs.stream().map(String::valueOf).collect(Collectors.joining(" "));
    }
    private static String qJoinInts(Collection<Integer> xs){ return quote(joinInts(xs)); }
    private static String qCompSizes(List<List<Integer>> comps){
        return quote(comps.stream().map(c -> String.valueOf(c.size())).collect(Collectors.joining(" ")));
    }
    private static String qComponentsPretty(List<List<Integer>> comps){
        List<String> parts = new ArrayList<>();
        for(int i=0;i<comps.size();i++){
            parts.add("C"+i+":["+joinInts(comps.get(i))+"]");
        }
        return quote(String.join("; ", parts));
    }
    private static String qDistancesVector(int n, DagShortestPaths s){
        final int INF = Integer.MAX_VALUE/4;
        List<String> parts = new ArrayList<>();
        for(int v=0; v<n; v++){
            int d = s.distTo(v);
            parts.add("C"+v+"="+(d<INF? String.valueOf(d) : "INF"));
        }
        return quote(String.join("; ", parts));
    }
    private static void writePhase(String dataset, String phase, Metrics m, Map<String,String> extra){
        String dfs=NA, edges=NA, pushes=NA, pops=NA, relax=NA;
        switch (phase){
            case "SCC_Tarjan": dfs=String.valueOf(m.dfsCalls); edges=String.valueOf(m.edgesVisited); break;
            case "Topo_Kahn":  pushes=String.valueOf(m.topoPushes); pops=String.valueOf(m.topoPops); break;
            case "DAG_SSSP":
            case "DAG_Longest": relax=String.valueOf(m.relaxations); break;
        }
        String[] cols = new String[]{
                extra.getOrDefault("scc_count",NA),
                extra.getOrDefault("scc_sizes",NA),
                extra.getOrDefault("scc_components",NA),
                extra.getOrDefault("topo_components",NA),
                extra.getOrDefault("derived_task_order",NA),
                extra.getOrDefault("sssp_source_comp",NA),
                extra.getOrDefault("sssp_distances",NA),
                extra.getOrDefault("sssp_target_comp",NA),
                extra.getOrDefault("sssp_distance",NA),
                extra.getOrDefault("sssp_path_components",NA),
                extra.getOrDefault("critical_path_components",NA),
                extra.getOrDefault("critical_length",NA)
        };
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(CSV_PATH, true))) {
            pw.printf("%s,%s,%.3f,%s,%s,%s,%s,%s",
                    dataset, phase, m.getElapsedMillisDouble(),
                    dfs, edges, pushes, pops, relax);
            for (String c : cols) pw.print("," + c);
            pw.println();
        } catch (Exception e){
            System.err.println("Failed to write " + CSV_PATH + ": " + e.getMessage());
        }
    }
    private static void runOne(String datasetPath) throws Exception {
        System.out.println("\n================= DATASET: " + datasetPath + " =================");

        GraphIO.Loaded loaded = GraphIO.load(datasetPath);
        DirectedGraph g = loaded.graph;
        int source = loaded.source;
        System.out.println("Loaded: n="+g.n()+", edges="+g.getEdges().size()
                +", source="+source+", weight_model="+loaded.weightModel);

        // 1) SCC (Tarjan)
        Metrics mscc = new Metrics();
        TarjanSCC scc = new TarjanSCC();
        TarjanSCC.Result res = scc.compute(g, mscc);

        System.out.println("-- SCC Components ("+res.components.size()+") --");
        for (int i=0;i<res.components.size();i++)
            System.out.printf("C%d size=%d : %s%n", i, res.components.get(i).size(), res.components.get(i));

        Map<String,String> sccFields = new HashMap<>();
        sccFields.put("scc_count", String.valueOf(res.components.size()));
        sccFields.put("scc_sizes", qCompSizes(res.components));
        sccFields.put("scc_components", qComponentsPretty(res.components));
        writePhase(datasetPath, "SCC_Tarjan", mscc, sccFields);

        // 2) Condensation DAG
        DirectedGraph dagMin = CondensationGraph.buildMinEdgeWeights(g, res.compOf, res.components.size());
        DirectedGraph dagMax = CondensationGraph.buildMaxEdgeWeights(g, res.compOf, res.components.size());
        System.out.println("Condensation DAG (min): n="+dagMin.n()+", edges="+dagMin.getEdges().size());
        System.out.println("Condensation DAG (max): n="+dagMax.n()+", edges="+dagMax.getEdges().size());

        // 3) Topological sort + derived order
        Metrics mtopo = new Metrics();
        List<Integer> topo = TopologicalSortKahn.order(dagMin, mtopo);
        System.out.println("-- Topological order of components --");
        System.out.println(topo);

        System.out.println("-- Derived order of original tasks (grouped by component topo) --");
        for (int c : topo) System.out.printf("C%d: %s%n", c, res.components.get(c));
        List<Integer> derived = new ArrayList<>();
        for (int c : topo) derived.addAll(res.components.get(c));
        System.out.println("Derived flat order of tasks: " + derived);

        Map<String,String> topoFields = new HashMap<>();
        topoFields.put("topo_components", qJoinInts(topo));
        topoFields.put("derived_task_order", qJoinInts(derived));
        writePhase(datasetPath, "Topo_Kahn", mtopo, topoFields);

        // Map source to SCC id
        int compSource = res.compOf[Math.max(0, Math.min(source, g.n()-1))];

        // 4) DAG SSSP
        Metrics msssp = new Metrics();
        DagShortestPaths sssp = new DagShortestPaths(dagMin, compSource);
        sssp.run(msssp);

        final int INF = Integer.MAX_VALUE/4;
        System.out.println("-- Shortest distances from component "+compSource+" --");
        for (int v=0; v<dagMin.n(); v++){
            int d = sssp.distTo(v);
            System.out.printf("to C%d = %s%n", v, (d<INF? String.valueOf(d) : "INF"));
        }

        int bestDist=-1, target=-1;
        for (int v=0; v<dagMin.n(); v++){
            int d = sssp.distTo(v);
            if (d<INF && d>bestDist){ bestDist=d; target=v; }
        }
        List<Integer> path = Collections.emptyList();
        if (target!=-1){
            path = sssp.pathTo(target);
            System.out.printf("-- One optimal shortest path (to farthest reachable) --%n");
            System.out.printf("to C%d (dist=%d): %s%n", target, bestDist, path);
        } else {
            System.out.println("-- One optimal shortest path --");
            System.out.println("No reachable target from the source component.");
        }

        Map<String,String> ssspFields = new HashMap<>();
        ssspFields.put("sssp_source_comp", String.valueOf(compSource));
        ssspFields.put("sssp_distances", qDistancesVector(dagMin.n(), sssp));
        ssspFields.put("sssp_target_comp", (target!=-1? String.valueOf(target): NA));
        ssspFields.put("sssp_distance", (target!=-1? String.valueOf(bestDist): NA));
        ssspFields.put("sssp_path_components", qJoinInts(path));
        writePhase(datasetPath, "DAG_SSSP", msssp, ssspFields);

        // 5) DAG Longest (critical path)
        Metrics mlong = new Metrics();
        DagLongestPath lp = new DagLongestPath(dagMax, compSource);
        lp.run(mlong);
        List<Integer> crit = lp.criticalPath();
        int clen = lp.longestValue();
        System.out.println("-- Critical path on condensation DAG --");
        System.out.println(crit);
        System.out.println("Critical length = " + clen);

        Map<String,String> longestFields = new HashMap<>();
        longestFields.put("critical_path_components", qJoinInts(crit));
        longestFields.put("critical_length", String.valueOf(clen));
        writePhase(datasetPath, "DAG_Longest", mlong, longestFields);

        System.out.println("Metrics appended to " + CSV_PATH);
    }

    // ---------- discover datasets ----------
    private static List<Path> findAllJsonUnderData() throws IOException {
        Path dir = Paths.get("data");
        if (!Files.isDirectory(dir)) return Collections.emptyList();
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(p -> p.toString().endsWith(".json"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
        }
    }

    // ---------- entry ----------
    public static void main(String[] args) throws Exception {
        String dataPath = null;
        boolean runAll = false, fresh = false;

        for (int i=0;i<args.length;i++){
            if ("--data".equals(args[i]) && i+1<args.length) dataPath = args[++i];
            else if ("--all".equals(args[i])) runAll = true;
            else if ("--fresh".equals(args[i])) fresh = true;
        }
        initCsv(fresh);
        if (dataPath != null){ runOne(dataPath); return; }
        runAll = runAll || args.length==0;
        if (runAll){
            List<Path> files = findAllJsonUnderData();
            if (files.isEmpty()){ System.err.println("No JSON files in ./data"); return; }
            System.out.println("Found " + files.size() + " JSON file(s) under ./data. Running all...");
            for (Path p : files){
                try { runOne(p.toString()); }
                catch (Throwable t){ System.err.println("Failed on dataset "+p+": "+t.getMessage()); }
            }
            System.out.println("\nAll done. See " + CSV_PATH);
        } else {
            System.out.println("Usage:");
            System.out.println("  --data <path>   # single dataset");
            System.out.println("  --all           # run all data/*.json");
            System.out.println("  (no args)       # same as --all (IntelliJ Run)");
        }
    }
}