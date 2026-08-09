package com.dbcache.ui.component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ResultsTable extends JTable {
    
    private DefaultTableModel tableModel;
    
    public ResultsTable() {
        // TODO: Initialize table with default model
        tableModel = new DefaultTableModel();
        setModel(tableModel);
        
        // TODO: Configure table appearance
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setRowHeight(25);
        setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
    }
    
    public void updateData(List<String> columns, List<List<Object>> rows) {
        // TODO: Clear existing data
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        
        // TODO: Set column headers
        for (String column : columns) {
            tableModel.addColumn(column);
        }
        
        // TODO: Add rows
        for (List<Object> row : rows) {
            tableModel.addRow(row.toArray());
        }
    }
    
    public void clearData() {
        // TODO: Clear all data
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
    }
}
