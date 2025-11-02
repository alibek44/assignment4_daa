package utils;

public class Metrics {
    public long startTime;
    public long endTime;

    // Counters
    public int dfsCalls = 0;
    public int edgesVisited = 0;
    public int topoPushes = 0;
    public int topoPops = 0;
    public int relaxations = 0;


    public void start() { startTime = System.nanoTime(); }
    public void stop()  { endTime = System.nanoTime(); }

    public long getElapsedMillis() { return (endTime - startTime) / 1_000_000; }

    public long getElapsedMicros() { return (endTime - startTime) / 1_000; }
    public double getElapsedMillisDouble() { return (endTime - startTime) / 1_000_000.0; }

    public void reset() {
        dfsCalls = 0;
        edgesVisited = 0;
        topoPushes = 0;
        topoPops = 0;
        relaxations = 0;
        startTime = 0;
        endTime = 0;
    }

    @Override
    public String toString() {
        return "time_ms=" + String.format("%.3f", getElapsedMillisDouble()) +
                ", dfsCalls=" + dfsCalls +
                ", edgesVisited=" + edgesVisited +
                ", topoPushes=" + topoPushes +
                ", topoPops=" + topoPops +
                ", relaxations=" + relaxations;
    }
}