package com.dbcache.benchmark;

public class BenchmarkConfig {
    
    private int totalRequests;
    private int uniqueQueries;
    private int cacheCapacity;
    private long ttlSeconds;
    
    public BenchmarkConfig(int totalRequests, int uniqueQueries, int cacheCapacity, long ttlSeconds) {
        // TODO: Initialize benchmark configuration
        this.totalRequests = totalRequests;
        this.uniqueQueries = uniqueQueries;
        this.cacheCapacity = cacheCapacity;
        this.ttlSeconds = ttlSeconds;
    }
    
    public int getTotalRequests() {
        return totalRequests;
    }
    
    public int getUniqueQueries() {
        return uniqueQueries;
    }
    
    public int getCacheCapacity() {
        return cacheCapacity;
    }
    
    public long getTtlSeconds() {
        return ttlSeconds;
    }
}
