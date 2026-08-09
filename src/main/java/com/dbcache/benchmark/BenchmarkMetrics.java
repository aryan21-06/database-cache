package com.dbcache.benchmark;

public class BenchmarkMetrics {
    
    private int totalRequests;
    private int dbQueries;
    private long cacheHits;
    private long cacheMisses;
    private double hitRate;
    private long totalTimeMs;
    private double avgLatencyMs;
    
    public BenchmarkMetrics(int totalRequests, int dbQueries, long cacheHits, long cacheMisses,
                           double hitRate, long totalTimeMs, double avgLatencyMs) {
        // TODO: Initialize benchmark metrics
        this.totalRequests = totalRequests;
        this.dbQueries = dbQueries;
        this.cacheHits = cacheHits;
        this.cacheMisses = cacheMisses;
        this.hitRate = hitRate;
        this.totalTimeMs = totalTimeMs;
        this.avgLatencyMs = avgLatencyMs;
    }
    
    public int getTotalRequests() {
        return totalRequests;
    }
    
    public int getDbQueries() {
        return dbQueries;
    }
    
    public long getCacheHits() {
        return cacheHits;
    }
    
    public long getCacheMisses() {
        return cacheMisses;
    }
    
    public double getHitRate() {
        return hitRate;
    }
    
    public long getTotalTimeMs() {
        return totalTimeMs;
    }
    
    public double getAvgLatencyMs() {
        return avgLatencyMs;
    }
    
    public double calculateSpeedup(BenchmarkMetrics other) {
        // TODO: Calculate speedup compared to other metrics
        return (double) other.totalTimeMs / this.totalTimeMs;
    }
    
    public double calculateDbLoadReduction(BenchmarkMetrics other) {
        // TODO: Calculate database load reduction
        return 1.0 - ((double) this.dbQueries / other.dbQueries);
    }
}
