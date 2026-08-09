package com.dbcache.handler;

import java.sql.SQLException;

import com.dbcache.cache.CacheConfig;
import com.dbcache.cache.CacheEngine;
import com.dbcache.cache.CacheStats;
import com.dbcache.cache.LRUCache;
import com.dbcache.database.DatabaseManager;
import com.dbcache.database.QueryResult;
import com.dbcache.model.QueryResponse;
import com.dbcache.model.QueryValidator;

/*
input - will be used as a method i.e. execute query, sql string
processing - validate, check cache- return , if cache miss - query
db, put in cache, return
*/

public class RequestHandler {
    
    private final CacheEngine cache;
    private final DatabaseManager dbManager;
    
    public RequestHandler(DatabaseManager dbManager, CacheConfig config) {
        // TODO: Initialize request handler with cache and database manager
        this.dbManager = dbManager;
        this.cache = new LRUCache(config.getCapacity(), config.getTtlSeconds());
    }
    
    public QueryResponse executeQuery(String sql) {
        // TODO: Implement query execution with caching
        long startTime = System.currentTimeMillis(); 
        // TODO: Validate query
        if(!QueryValidator.isValidSelectQuery(sql)){
            return QueryResponse.error(QueryValidator.getValidationError(sql));
        }
         QueryResult result=cache.get(sql);
        // TODO: Check cache
        if(result!=null){
            boolean cacheHit = true;
            boolean databaseAccessed = false;
            long responseTimeMs = System.currentTimeMillis() - startTime;
            return QueryResponse.success(result, cacheHit, responseTimeMs, databaseAccessed);
        }
        boolean cacheHit = false;
        boolean databaseAccessed = true;
        // TODO: If cache miss, query database
        try{
            result= dbManager.executeQuery(sql);
        }catch(SQLException se){
            return QueryResponse.error("Database Error"+""+se.getMessage());
        }
        // TODO: Store result in cache
        cache.put(sql, result);
        // TODO: Return response with metadata
        long responseTimeMs = System.currentTimeMillis() - startTime;
        return QueryResponse.success(result, cacheHit, responseTimeMs, databaseAccessed);
    }
    
    public CacheStats getCacheStats() {
        // TODO: Return cache statistics
        return cache.getStats();
    }
    
    public void clearCache() {
        // TODO: Clear cache
        cache.clear();
    }
    
    public void configureCache(CacheConfig config) {
        // TODO: Reconfigure cache
        cache.configure(config);
    }
}
