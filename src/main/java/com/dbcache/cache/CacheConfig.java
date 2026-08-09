package com.dbcache.cache;

public class CacheConfig {
    
    private int capacity;
    private long ttlSeconds;
    private EvictionPolicy policy;
    
    public CacheConfig(int capacity, long ttlSeconds, EvictionPolicy policy) {
        // TODO: Initialize cache configuration
        this.capacity = capacity;
        this.ttlSeconds = ttlSeconds;
        this.policy = policy;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public long getTtlSeconds() {
        return ttlSeconds;
    }
    
    public EvictionPolicy getPolicy() {
        return policy;
    }
}
