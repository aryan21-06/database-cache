package com.dbcache.cache;

import com.dbcache.database.QueryResult;

public interface CacheEngine {
    
    QueryResult get(String sql);
    
    void put(String sql, QueryResult result);
    
    void clear();
    
    CacheStats getStats();
    
    void configure(CacheConfig config);
}
