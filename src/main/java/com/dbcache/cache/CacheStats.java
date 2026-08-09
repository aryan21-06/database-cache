package com.dbcache.cache;

public class CacheStats {
    
    private int size;
    private int capacity;
    private long hits;
    private long misses;
    private long evictions;
    private double hitRate;
    
    public CacheStats(int size, int capacity, long hits, long misses, long evictions, double hitRate) {
        // TODO: Initialize cache statistics
        this.size = size;
        this.capacity = capacity;
        this.hits = hits;
        this.misses = misses;
        this.evictions = evictions;
        this.hitRate = hitRate;
    }
    
    public int getSize() {
        return size;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public long getHits() {
        return hits;
    }
    
    public long getMisses() {
        return misses;
    }
    
    public long getEvictions() {
        return evictions;
    }
    
    public double getHitRate() {
        return hitRate;
    }
}
