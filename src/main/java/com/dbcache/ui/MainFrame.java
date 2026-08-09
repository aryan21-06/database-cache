package com.dbcache.ui;

import com.dbcache.benchmark.BenchmarkRunner;
import com.dbcache.handler.RequestHandler;
import com.dbcache.ui.panel.ManualQueryPanel;
import com.dbcache.ui.panel.BenchmarkPanel;
import com.dbcache.ui.panel.CacheStatsPanel;

import javax.swing.*;

public class MainFrame extends JFrame {
    
    private final RequestHandler requestHandler;
    private final BenchmarkRunner benchmarkRunner;
    
    public MainFrame(RequestHandler requestHandler, BenchmarkRunner benchmarkRunner) {
        this.requestHandler = requestHandler;
        this.benchmarkRunner = benchmarkRunner;
        
        setTitle("Database Query Cache System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // TODO: Create tabbed pane with three tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manual Query", new ManualQueryPanel(requestHandler));
        tabbedPane.addTab("Benchmark", new BenchmarkPanel(benchmarkRunner));
        tabbedPane.addTab("Cache Stats", new CacheStatsPanel(requestHandler));
        
        add(tabbedPane);
    }
}
