package com.dbcache.benchmark;

public class BenchmarkResults {
    
    private BenchmarkMetrics noCacheMetrics;
    private BenchmarkMetrics withCacheMetrics;
    
    public BenchmarkResults(BenchmarkMetrics noCacheMetrics, BenchmarkMetrics withCacheMetrics) {
        // TODO: Initialize benchmark results
        this.noCacheMetrics = noCacheMetrics;
        this.withCacheMetrics = withCacheMetrics;
    }
    
    public BenchmarkMetrics getNoCacheMetrics() {
        return noCacheMetrics;
    }
    
    public BenchmarkMetrics getWithCacheMetrics() {
        return withCacheMetrics;
    }
}
