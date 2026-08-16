package com.dbcache.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.dbcache.benchmark.BenchmarkConfig;
import com.dbcache.benchmark.BenchmarkMetrics;
import com.dbcache.benchmark.BenchmarkResults;
import com.dbcache.benchmark.BenchmarkRunner;
import com.dbcache.ui.AquaTheme;

public class BenchmarkPanel extends JPanel {

    private final BenchmarkRunner benchmarkRunner;

    private JSpinner totalRequestsSpinner;
    private JSpinner uniqueQueriesSpinner;
    private JSpinner cacheCapacitySpinner;
    private JSpinner ttlSpinner;

    private JButton           runBtn;
    private JTable            resultsTable;
    private DefaultTableModel tableModel;
    private JLabel            speedupLabel;
    private JLabel            dbLoadLabel;
    private JLabel            kaomoji;

    public BenchmarkPanel(BenchmarkRunner benchmarkRunner) {
        this.benchmarkRunner = benchmarkRunner;
        setLayout(new BorderLayout(12, 12));
        setBackground(AquaTheme.WHITE_BLUE);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        initComponents();
    }

    private void initComponents() {

        JPanel configCard = new JPanel(new GridBagLayout());
        configCard.setBackground(AquaTheme.MIST);
        configCard.setBorder(AquaTheme.sectionBorder("Benchmark Config"));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(7, 10, 7, 10);

        GridBagConstraints sc = new GridBagConstraints();
        sc.fill    = GridBagConstraints.HORIZONTAL;
        sc.weightx = 1.0;
        sc.insets  = new Insets(7, 0, 7, 16);

        totalRequestsSpinner = styledSpinner(new SpinnerNumberModel(10000, 100, 1000000, 100));
        uniqueQueriesSpinner = styledSpinner(new SpinnerNumberModel(1000,   10,  100000,  10));
        cacheCapacitySpinner = styledSpinner(new SpinnerNumberModel(100,    10,   50000,  10));
        ttlSpinner           = styledSpinner(new SpinnerNumberModel(60,      1,   3600,   1));

        String[]   labels = {"Total Requests:", "Unique Queries:", "Cache Capacity:", "TTL (seconds):"};
        JSpinner[] spins  = {totalRequestsSpinner, uniqueQueriesSpinner, cacheCapacitySpinner, ttlSpinner};

        for (int i = 0; i < labels.length; i++) {
            lc.gridx = 0; lc.gridy = i;
            sc.gridx = 1; sc.gridy = i;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(AquaTheme.ui(Font.PLAIN, 13));
            lbl.setForeground(AquaTheme.DEEP_NAVY);
            configCard.add(lbl, lc);
            configCard.add(spins[i], sc);
        }

        runBtn = AquaTheme.makeButton("Run Benchmark!  \\(^o^)/", true);
        runBtn.setPreferredSize(new Dimension(240, 38));

        JPanel runRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        runRow.setBackground(AquaTheme.WHITE_BLUE);
        runRow.add(runBtn);

        JPanel topPanel = new JPanel(new BorderLayout(6, 6));
        topPanel.setBackground(AquaTheme.WHITE_BLUE);
        topPanel.add(configCard, BorderLayout.CENTER);
        topPanel.add(runRow,     BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JPanel resultsCard = new JPanel(new BorderLayout(8, 8));
        resultsCard.setBackground(AquaTheme.WHITE_BLUE);
        resultsCard.setBorder(AquaTheme.sectionBorder("Results"));

        String[] cols = {"Metric", "No Cache", "With Cache"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        populatePlaceholder();

        resultsTable = new JTable(tableModel);
        resultsTable.setFont(AquaTheme.mono(12));
        resultsTable.setRowHeight(26);
        resultsTable.setBackground(AquaTheme.WHITE_BLUE);
        resultsTable.setForeground(AquaTheme.DEEP_NAVY);
        resultsTable.setGridColor(AquaTheme.BABY_BLUE);
        resultsTable.getTableHeader().setFont(AquaTheme.ui(Font.BOLD, 12));
        resultsTable.getTableHeader().setBackground(AquaTheme.OCEAN_DARK);
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setSelectionBackground(AquaTheme.CORNFLOWER);
        resultsTable.setSelectionForeground(Color.WHITE);

        resultsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                c.setBackground(sel ? AquaTheme.CORNFLOWER : new Color(0xD1F2EB));
                c.setForeground(new Color(0x0E6655));
                return c;
            }
        });

        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setBorder(BorderFactory.createLineBorder(AquaTheme.BABY_BLUE, 1, true));
        resultsCard.add(scroll, BorderLayout.CENTER);

        speedupLabel = summaryLabel("Speedup: --",           AquaTheme.CORNFLOWER);
        dbLoadLabel  = summaryLabel("DB Load Reduction: --", AquaTheme.GOLD);

        kaomoji = new JLabel("\\(^o^)/");
        kaomoji.setFont(AquaTheme.ui(Font.BOLD, 18));
        kaomoji.setForeground(AquaTheme.GOLD);
        kaomoji.setVisible(false);

        JPanel summaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
        summaryRow.setBackground(AquaTheme.MIST);
        summaryRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AquaTheme.BABY_BLUE));
        summaryRow.add(speedupLabel);
        summaryRow.add(dbLoadLabel);
        summaryRow.add(kaomoji);
        resultsCard.add(summaryRow, BorderLayout.SOUTH);

        add(resultsCard, BorderLayout.CENTER);

        runBtn.addActionListener(e -> runBenchmark());
    }

    private void runBenchmark() {
        int  totalRequests = (Integer) totalRequestsSpinner.getValue();
        int  uniqueQueries = (Integer) uniqueQueriesSpinner.getValue();
        int  cacheCapacity = (Integer) cacheCapacitySpinner.getValue();
        long ttlSeconds    = (Integer) ttlSpinner.getValue();

        runBtn.setEnabled(false);
        kaomoji.setVisible(false);

        JDialog loadingDialog = AquaTheme.showLoadingDialog(
            SwingUtilities.getWindowAncestor(this),
            "Running Benchmark..."
        );

        SwingWorker<BenchmarkResults, Void> worker = new SwingWorker<>() {
            @Override
            protected BenchmarkResults doInBackground() throws Exception {
                BenchmarkConfig config = new BenchmarkConfig(
                    totalRequests, uniqueQueries, cacheCapacity, ttlSeconds);
                return benchmarkRunner.runBenchmark(config);
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    BenchmarkResults results = get();
                    BenchmarkMetrics nc = results.getNoCacheMetrics();
                    BenchmarkMetrics wc = results.getWithCacheMetrics();

                    tableModel.setRowCount(0);
                    tableModel.addRow(new Object[]{"Requests",    nc.getTotalRequests(),                wc.getTotalRequests()});
                    tableModel.addRow(new Object[]{"DB Queries",  nc.getDbQueries(),                    wc.getDbQueries()});
                    tableModel.addRow(new Object[]{"Cache Hits",  "--",                                 wc.getCacheHits()});
                    tableModel.addRow(new Object[]{"Cache Misses","--",                                 wc.getCacheMisses()});
                    tableModel.addRow(new Object[]{"Hit Rate",    "--",                                 String.format("%.1f%%", wc.getHitRate() * 100)});
                    tableModel.addRow(new Object[]{"Total Time",  formatTime(nc.getTotalTimeMs()),       formatTime(wc.getTotalTimeMs())});
                    tableModel.addRow(new Object[]{"Avg Latency", formatLatency(nc.getAvgLatencyMs()),  formatLatency(wc.getAvgLatencyMs())});

                    double speedup     = (wc.getTotalTimeMs() > 0) ? (double) nc.getTotalTimeMs() / wc.getTotalTimeMs() : 0;
                    double dbReduction = (nc.getDbQueries()   > 0) ? 1.0 - ((double) wc.getDbQueries() / nc.getDbQueries()) : 0;

                    speedupLabel.setText(String.format("Speedup: %.2fx", speedup));
                    dbLoadLabel.setText(String.format("DB Load Reduction: %.1f%%", dbReduction * 100));
                    kaomoji.setVisible(true);

                    AquaTheme.showSuccessToast(
                        SwingUtilities.getWindowAncestor(BenchmarkPanel.this),
                        "Benchmark completed successfully!"
                    );

                } catch (Exception ex) {
                    System.err.println("[BenchmarkPanel] Benchmark error: " + ex.getMessage());
                    ex.printStackTrace();
                    AquaTheme.showErrorToast(
                        SwingUtilities.getWindowAncestor(BenchmarkPanel.this),
                        "Benchmark failed: " + ex.getMessage());
                } finally {
                    runBtn.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void populatePlaceholder() {
        for (String m : new String[]{"Requests","DB Queries","Cache Hits","Cache Misses","Hit Rate","Total Time","Avg Latency"})
            tableModel.addRow(new Object[]{m, "--", "--"});
    }

    private String formatTime(long ms) {
        return (ms < 1000) ? ms + "ms" : String.format("%.1fs", ms / 1000.0);
    }

    private String formatLatency(double ms) { return String.format("%.2fms", ms); }

    private JSpinner styledSpinner(SpinnerModel model) {
        JSpinner s = new JSpinner(model);
        s.setFont(AquaTheme.ui(Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setBackground(AquaTheme.WHITE_BLUE);
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setForeground(AquaTheme.DEEP_NAVY);
        return s;
    }

    private JLabel summaryLabel(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AquaTheme.ui(Font.BOLD, 14));
        lbl.setForeground(color);
        return lbl;
    }
}
