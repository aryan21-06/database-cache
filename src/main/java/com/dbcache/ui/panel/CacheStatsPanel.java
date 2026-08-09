package com.dbcache.ui.panel;

import com.dbcache.cache.CacheStats;
import com.dbcache.handler.RequestHandler;

import javax.swing.*;
import java.awt.*;

public class CacheStatsPanel extends JPanel {
    
    private final RequestHandler requestHandler;
    
    private JLabel sizeLabel;
    private JLabel hitsLabel;
    private JLabel missesLabel;
    private JLabel hitRateLabel;
    private JLabel evictionsLabel;
    private JButton clearButton;
    private JButton refreshButton;
    
    public CacheStatsPanel(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        
        // TODO: Initialize UI components
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // TODO: Build the panel layout
        // TODO: Add stats labels
        // TODO: Add buttons
        // TODO: Add action listeners
        
        refreshStats();
    }
    
    private void refreshStats() {
        // TODO: Get cache stats and update labels
        CacheStats stats = requestHandler.getCacheStats();
        
        // TODO: Update all labels with stats data
    }
    
    private void clearCache() {
        // TODO: Show confirmation dialog
        // TODO: Clear cache
        // TODO: Refresh stats
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear the cache?",
            "Confirm",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            requestHandler.clearCache();
            refreshStats();
        }
    }
}
