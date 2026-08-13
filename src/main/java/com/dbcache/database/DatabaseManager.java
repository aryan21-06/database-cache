package com.dbcache.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    
    private final Connection connection;
    
    public DatabaseManager(String url, String username, String password) throws SQLException {
        // TODO: Initialize database connection
        this.connection = DriverManager.getConnection(url, username, password);
    }
    
    public QueryResult executeQuery(String sql) throws SQLException {
        // TODO: Execute SQL query and convert ResultSet to QueryResult
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            return convertResultSet(rs);
        }
    }
    
    private QueryResult convertResultSet(ResultSet rs) throws SQLException {
        // TODO: Convert JDBC ResultSet to QueryResult
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        List<String> columns = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();
        
        // TODO: Extract column names
        for(int i=1;i<=columnCount;i++){
            columns.add(metaData.getColumnName(i));
        }
        // TODO: Extract rows
            while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            rows.add(row);
        }
        return new QueryResult(columns, rows);

    }
    
    public void close() throws SQLException {
        // TODO: Close database connection
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
