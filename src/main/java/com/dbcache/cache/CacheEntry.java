package com.dbcache.cache;

import com.dbcache.database.QueryResult;

public class CacheEntry {
    
    private final QueryResult result;
    private final long createdAt;
    private long lastAccessedAt;
    private int accessCount;
    
    public CacheEntry(QueryResult result) {
        // TODO: Initialize cache entry
        this.result = result;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessedAt = createdAt;
        this.accessCount = 1;
    }
    
    public void recordAccess() {
        // TODO: Update access metadata
        this.lastAccessedAt = System.currentTimeMillis();
        this.accessCount++;
    }
    
    public QueryResult getResult() {
        return result;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public int getAccessCount() {
        return accessCount;
    }
}
