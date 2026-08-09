package com.dbcache.ui.panel;

import com.dbcache.benchmark.BenchmarkConfig;
import com.dbcache.benchmark.BenchmarkMetrics;
import com.dbcache.benchmark.BenchmarkResults;
import com.dbcache.benchmark.BenchmarkRunner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BenchmarkPanel extends JPanel {
    
    private final BenchmarkRunner benchmarkRunner;
    
    private JSpinner totalRequestsSpinner;
    private JSpinner uniqueQueriesSpinner;
    private JSpinner cacheCapacitySpinner;
    private JSpinner ttlSpinner;
    private JButton runButton;
    private JTable resultsTable;
    private JLabel speedupLabel;
    private JLabel dbLoadReductionLabel;
    
    public BenchmarkPanel(BenchmarkRunner benchmarkRunner) {
        this.benchmarkRunner = benchmarkRunner;
        
        // TODO: Initialize UI components
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // TODO: Build the panel layout
        // TODO: Add configuration form
        // TODO: Add results table
        // TODO: Add action listeners
    }
    
    private void runBenchmark() {
        // TODO: Get configuration from spinners
        // TODO: Create BenchmarkConfig
        // TODO: Call benchmarkRunner.runBenchmark(config)
        // TODO: Update results table
        // TODO: Update summary labels
    }
    
    private void updateResultsTable(BenchmarkResults results) {
        // TODO: Update results comparison table
        BenchmarkMetrics noCache = results.getNoCacheMetrics();
        BenchmarkMetrics withCache = results.getWithCacheMetrics();
        
        // TODO: Populate table with metrics
    }
    
    private void updateSummaryLabels(BenchmarkResults results) {
        // TODO: Calculate and display speedup and DB load reduction
        BenchmarkMetrics noCache = results.getNoCacheMetrics();
        BenchmarkMetrics withCache = results.getWithCacheMetrics();
        
        double speedup = (double) noCache.getTotalTimeMs() / withCache.getTotalTimeMs();
        double dbLoadReduction = 1.0 - ((double) withCache.getDbQueries() / noCache.getDbQueries());
        
        speedupLabel.setText(String.format("Speedup: %.2fx", speedup));
        dbLoadReductionLabel.setText(String.format("DB Load Reduction: %.1f%%", dbLoadReduction * 100));
    }
    
    private String formatTime(long ms) {
        if (ms < 1000) {
            return ms + "ms";
        } else {
            return String.format("%.1fs", ms / 1000.0);
        }
    }
    
    private String formatLatency(double ms) {
        return String.format("%.2fms", ms);
    }
}
