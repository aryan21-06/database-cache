package com.dbcache.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dbcache.database.QueryResult;

public class LRUCache implements CacheEngine {
    
    private final Map<String, CacheEntry> cache;
    private int capacity;
    private long ttlMillis;
    
    private long hits = 0;
    private long misses = 0;
    private long evictions = 0;
    
    public LRUCache(int capacity, long ttlSeconds) {
        this.capacity = capacity;
        this.ttlMillis = ttlSeconds * 1000;
        
        // TODO: Initialize LinkedHashMap with access-order
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            boolean shouldRemove = size() > LRUCache.this.capacity;
            if (shouldRemove) {
                evictions++;
            }
            return shouldRemove;
        }
    };
 }
    
    @Override
    public QueryResult get(String sql) {
        // TODO: Implement cache lookup with TTL check
    	CacheEntry cacheEntry = cache.get(normalizeQuery(sql));
    	if (cacheEntry!=null) {
    		if(System.currentTimeMillis()-cacheEntry.getCreatedAt()<ttlMillis){
                hits++;
                cacheEntry.recordAccess();
                return cacheEntry.getResult();
            }else{
                cache.remove(normalizeQuery(sql));
                misses++;
                evictions++;
                return null;
            }
    	}else{
            misses++;
            return null;
        }
    }
    
    @Override
    public void put(String sql, QueryResult result) {
        // TODO: Implement cache insertion
    	CacheEntry cacheEntry = new CacheEntry(result);
    	cache.put(normalizeQuery(sql), cacheEntry);
    }
    
    @Override
    public void clear() {
        // TODO: Clear cache and reset stats
        cache.clear();
        hits = 0;
        misses = 0;
        evictions = 0;
    }
    
    @Override
    public CacheStats getStats() {
        long total = hits + misses;
        double hitRate = (total == 0) ? 0.0 : (double) hits / total;
        
        return new CacheStats(
            cache.size(),
            capacity,
            hits,
            misses,
            evictions,
            hitRate
        );
}
    
    @Override
    public void configure(CacheConfig config) {
        this.capacity = config.getCapacity();
        this.ttlMillis = config.getTtlSeconds() * 1000;
        clear();
}
    
    private String normalizeQuery(String sql) {
        // TODO: Normalize SQL query for cache key
        return sql.toLowerCase().trim().replaceAll(";\\s*$", "").replaceAll("\\s+", " ");
    }
}
