package com.dbcache.model;

import com.dbcache.database.QueryResult;

public class QueryResponse {
    
    private QueryResult result;
    private boolean cacheHit;
    private long responseTimeMs;
    private boolean databaseAccessed;
    private String errorMessage;
    private boolean successful;
    
    private QueryResponse() {
        // Private constructor
    }
    
    public static QueryResponse success(QueryResult result, boolean cacheHit, long responseTimeMs, boolean databaseAccessed) {
        // TODO: Create success response
        QueryResponse response = new QueryResponse();
        response.result = result;
        response.cacheHit = cacheHit;
        response.responseTimeMs = responseTimeMs;
        response.databaseAccessed = databaseAccessed;
        response.successful = true;
        return response;
    }
    
    public static QueryResponse error(String errorMessage) {
        // TODO: Create error response
        QueryResponse response = new QueryResponse();
        response.errorMessage = errorMessage;
        response.successful = false;
        return response;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public QueryResult getResult() {
        return result;
    }
    
    public boolean isCacheHit() {
        return cacheHit;
    }
    
    public long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public boolean isDatabaseAccessed() {
        return databaseAccessed;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}
