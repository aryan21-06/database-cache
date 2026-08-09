package com.dbcache.model;

public class QueryValidator {
    
    public static boolean isValidSelectQuery(String sql) {
        // TODO: Validate that query is a SELECT statement
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        
        String normalized = sql.trim().toLowerCase();
        return normalized.startsWith("select");
    }
    
    public static String getValidationError(String sql) {
        // TODO: Return specific validation error message
        if (sql == null || sql.trim().isEmpty()) {
            return "Query cannot be empty";
        }
        
        String normalized = sql.trim().toLowerCase();
        if (!normalized.startsWith("select")) {
            return "Only SELECT queries are allowed";
        }
        
        return null;
    }
}
