package com.dbcache.model;

import java.util.regex.Pattern;

public class QueryValidator {
    
    private static final Pattern SQL_COMMENT = Pattern.compile("(--[^\\n]*|/\\*.*?\\*/)", Pattern.DOTALL);
    private static final Pattern MULTIPLE_STATEMENTS = Pattern.compile(";\\s*\\S+");
    
    public static boolean isValidSelectQuery(String sql) {
        return getValidationError(sql) == null;
    }
    
    public static String getValidationError(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "Query cannot be empty";
        }
        
        String cleaned = removeComments(sql).trim();
        
        if (cleaned.isEmpty()) {
            return "Query cannot be empty";
        }
        
        if (MULTIPLE_STATEMENTS.matcher(cleaned).find()) {
            return "Multiple SQL statements are not allowed";
        }
        
        String normalized = cleaned.toLowerCase();
        String firstKeyword = extractFirstKeyword(normalized);
        
        if (!firstKeyword.equals("select") && !firstKeyword.equals("with")) {
            return getCommandSpecificError(firstKeyword);
        }
        
        return null;
    }
    
    private static String removeComments(String sql) {
        return SQL_COMMENT.matcher(sql).replaceAll(" ");
    }
    
    private static String extractFirstKeyword(String sql) {
        StringBuilder keyword = new StringBuilder();
        for (char c : sql.toCharArray()) {
            if (Character.isLetter(c)) {
                keyword.append(c);
            } else {
                break;
            }
        }
        return keyword.toString();
    }
    
    private static String getCommandSpecificError(String keyword) {
        if (keyword.isEmpty()) {
            return "Invalid SQL syntax";
        }
        
        return switch (keyword.toUpperCase()) {
            case "INSERT", "UPDATE", "DELETE", "MERGE", "REPLACE" -> 
                "DML command '" + keyword.toUpperCase() + "' is not allowed. Only read-only queries are permitted";
            case "CREATE", "ALTER", "DROP", "TRUNCATE", "RENAME" -> 
                "DDL command '" + keyword.toUpperCase() + "' is not allowed. Only read-only queries are permitted";
            case "COMMIT", "ROLLBACK", "GRANT", "REVOKE" -> 
                "Transaction/admin command '" + keyword.toUpperCase() + "' is not allowed";
            default -> 
                "Only SELECT queries are allowed. Got: " + keyword.toUpperCase();
        };
    }
}
