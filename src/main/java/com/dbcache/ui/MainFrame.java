package com.dbcache.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.dbcache.benchmark.BenchmarkRunner;
import com.dbcache.handler.RequestHandler;
import com.dbcache.ui.panel.BenchmarkPanel;
import com.dbcache.ui.panel.CacheStatsPanel;
import com.dbcache.ui.panel.ManualQueryPanel;

public class MainFrame extends JFrame {

    private final RequestHandler  requestHandler;
    private final BenchmarkRunner benchmarkRunner;

    public MainFrame(RequestHandler requestHandler, BenchmarkRunner benchmarkRunner) {
        this.requestHandler  = requestHandler;
        this.benchmarkRunner = benchmarkRunner;

        AquaTheme.apply();

        setTitle("Database Cache System");
        setSize(1040, 740);
        setMinimumSize(new Dimension(860, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(AquaTheme.WHITE_BLUE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0,            AquaTheme.DEEP_NAVY,
                    getWidth(), 0,   AquaTheme.OCEAN_DARK);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(93, 173, 226, 30));
                g2.setStroke(new BasicStroke(1.0f));
                int[][] rings = {{20,28,32},{60,20,18},{160,30,24},{800,15,28},{900,25,20},{980,10,16}};
                for (int[] r : rings) {
                    g2.drawOval(r[0] - r[2]/2, r[1] - r[2]/2, r[2], r[2]);
                    g2.drawOval(r[0] - r[2]/4, r[1] - r[2]/4, r[2]/2, r[2]/2);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 56));
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel title = new JLabel("Database Cache System");
        title.setFont(AquaTheme.ui(Font.BOLD, 18));
        title.setForeground(AquaTheme.GOLD);

        JLabel subtitle = new JLabel("kawaii caching  \\(^o^)/");
        subtitle.setFont(AquaTheme.ui(Font.PLAIN, 11));
        subtitle.setForeground(AquaTheme.BABY_BLUE);
        subtitle.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(title,    BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(AquaTheme.ui(Font.BOLD, 13));
        tabs.setBackground(AquaTheme.WHITE_BLUE);

        tabs.addTab("  ~ Manual Query ~  ", new ManualQueryPanel(requestHandler));
        tabs.addTab("  ~ Benchmark ~  ",    new BenchmarkPanel(benchmarkRunner));
        tabs.addTab("  ~ Cache Stats ~  ",  new CacheStatsPanel(requestHandler));

        add(header, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
    }
}
