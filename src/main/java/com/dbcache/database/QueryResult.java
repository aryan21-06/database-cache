package com.dbcache.database;

import java.util.List;

public class QueryResult {
    
    private final List<String> columns;
    private final List<List<Object>> rows;
    
    public QueryResult(List<String> columns, List<List<Object>> rows) {
        // TODO: Initialize query result
        this.columns = columns;
        this.rows = rows;
    }
    
    public List<String> getColumns() {
        return columns;
    }
    
    public List<List<Object>> getRows() {
        return rows;
    }
    
    public int getRowCount() {
        return rows.size();
    }
    
    public int getColumnCount() {
        return columns.size();
    }
}
