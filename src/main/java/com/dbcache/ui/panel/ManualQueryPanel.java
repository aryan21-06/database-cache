package com.dbcache.ui.panel;

import com.dbcache.handler.RequestHandler;
import com.dbcache.model.QueryResponse;
import com.dbcache.database.QueryResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManualQueryPanel extends JPanel {
    
    private final RequestHandler requestHandler;
    
    private JTextArea sqlTextArea;
    private JButton executeButton;
    private JButton clearButton;
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JLabel dbLabel;
    private JTable resultsTable;
    
    public ManualQueryPanel(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        
        // TODO: Initialize UI components
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // TODO: Build the panel layout
        // TODO: Add components
        // TODO: Add action listeners
    }
    
    private void executeQuery() {
        // TODO: Get SQL from text area
        // TODO: Call requestHandler.executeQuery(sql)
        // TODO: Update status labels
        // TODO: Update results table
    }
    
    private void updateResultsTable(QueryResult result) {
        // TODO: Update JTable with query result data
        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);
        
        // TODO: Set column headers
        // TODO: Add rows
    }
    
    private void updateStatus(QueryResponse response) {
        // TODO: Update status, time, and database labels
        if (response.isCacheHit()) {
            statusLabel.setText("Status: CACHE HIT");
            statusLabel.setForeground(new Color(0, 128, 0));
        } else {
            statusLabel.setText("Status: CACHE MISS");
            statusLabel.setForeground(new Color(255, 140, 0));
        }
        
        timeLabel.setText("Time: " + response.getResponseTimeMs() + "ms");
        dbLabel.setText("DB: " + (response.isDatabaseAccessed() ? "Yes" : "No"));
    }
}
