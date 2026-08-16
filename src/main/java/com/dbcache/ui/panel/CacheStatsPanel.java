package com.dbcache.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.dbcache.cache.CacheStats;
import com.dbcache.handler.RequestHandler;
import com.dbcache.ui.AquaTheme;

public class CacheStatsPanel extends JPanel {

    private final RequestHandler requestHandler;

    private JLabel sizeValue;
    private JLabel hitsValue;
    private JLabel missesValue;
    private JLabel hitRateValue;
    private JLabel evictionsValue;
    private JLabel hitRateKaomoji;

    private JButton refreshBtn;
    private JButton clearBtn;

    public CacheStatsPanel(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        setLayout(new BorderLayout(12, 12));
        setBackground(AquaTheme.WHITE_BLUE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        initComponents();
        refreshStats();
    }

    private void initComponents() {

        JLabel pageTitle = new JLabel("Cache Statistics", SwingConstants.CENTER);
        pageTitle.setFont(AquaTheme.ui(Font.BOLD, 18));
        pageTitle.setForeground(AquaTheme.SKY_BLUE);
        pageTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        add(pageTitle, BorderLayout.NORTH);

        sizeValue      = bigLabel("--");
        hitsValue      = bigLabel("--");
        missesValue    = bigLabel("--");
        hitRateValue   = bigLabel("--");
        evictionsValue = bigLabel("--");

        hitRateKaomoji = new JLabel("(o‿o)", SwingConstants.CENTER);
        hitRateKaomoji.setFont(AquaTheme.ui(Font.PLAIN, 14));
        hitRateKaomoji.setForeground(AquaTheme.OCEAN_MID);

        JPanel topRow = new JPanel(new GridLayout(1, 3, 12, 0));
        topRow.setBackground(AquaTheme.WHITE_BLUE);

        topRow.add(makeCard("Cache Size",   sizeValue,      "[  ]", AquaTheme.MIST,                        null));
        topRow.add(makeCard("Cache Hits",   hitsValue,      "[ v ]", new Color(0xD1F2EB),                  null));
        topRow.add(makeCard("Cache Misses", missesValue,    "[ x ]", new Color(0xFEF9E7),                  null));

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomRow.setBackground(AquaTheme.WHITE_BLUE);
        bottomRow.add(makeCard("Hit Rate",  hitRateValue,   "[ * ]", AquaTheme.BABY_BLUE,  hitRateKaomoji));
        bottomRow.add(makeCard("Evictions", evictionsValue, "[ ^ ]", AquaTheme.GOLD_LIGHT,               null));

        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(AquaTheme.WHITE_BLUE);
        cardsPanel.add(topRow);
        cardsPanel.add(Box.createVerticalStrut(12));
        cardsPanel.add(bottomRow);

        add(cardsPanel, BorderLayout.CENTER);

        refreshBtn = AquaTheme.makeButton("Refresh",      false);
        clearBtn   = AquaTheme.makeButton("Clear Cache",  true);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        btnRow.setBackground(AquaTheme.WHITE_BLUE);
        btnRow.add(refreshBtn);
        btnRow.add(clearBtn);
        add(btnRow, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshStats());
        clearBtn.addActionListener(e   -> clearCache());
    }

    private void refreshStats() {
        try {
            CacheStats stats = requestHandler.getCacheStats();

            sizeValue.setText(stats.getSize() + " / " + stats.getCapacity());
            hitsValue.setText(String.valueOf(stats.getHits()));
            missesValue.setText(String.valueOf(stats.getMisses()));

            double rate = stats.getHitRate() * 100;
            hitRateValue.setText(String.format("%.1f%%", rate));

            if      (rate >= 80) hitRateKaomoji.setText("\\(^o^)/  Amazing!");
            else if (rate >= 50) hitRateKaomoji.setText("(o‿o)  Not bad");
            else if (rate >= 20) hitRateKaomoji.setText("(╥_╥)  Try harder...");
            else                 hitRateKaomoji.setText("(>_<)  Rough...");

            evictionsValue.setText(String.valueOf(stats.getEvictions()));

        } catch (Exception ex) {
            System.err.println("[CacheStatsPanel] Failed to load stats: " + ex.getMessage());
            ex.printStackTrace();
            AquaTheme.showErrorToast(
                SwingUtilities.getWindowAncestor(this),
                "Failed to load cache stats: " + ex.getMessage());
        }
    }

    private void clearCache() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear the cache?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            requestHandler.clearCache();
            refreshStats();
            JOptionPane.showMessageDialog(this,
                "Cache cleared successfully!",
                "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            System.err.println("[CacheStatsPanel] Failed to clear cache: " + ex.getMessage());
            ex.printStackTrace();
            AquaTheme.showErrorToast(
                SwingUtilities.getWindowAncestor(this),
                "Failed to clear cache: " + ex.getMessage());
        }
    }

    private JPanel makeCard(String label, JLabel valueLabel,
                            String icon, Color bg, JLabel extraLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AquaTheme.BABY_BLUE, 1, true),
            BorderFactory.createEmptyBorder(14, 12, 14, 12)));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(AquaTheme.ui(Font.BOLD, 14));
        iconLbl.setForeground(AquaTheme.SKY_BLUE);
        iconLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel(label, SwingConstants.CENTER);
        nameLbl.setFont(AquaTheme.ui(Font.BOLD, 12));
        nameLbl.setForeground(AquaTheme.OCEAN_MID);
        nameLbl.setAlignmentX(CENTER_ALIGNMENT);

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setAlignmentX(CENTER_ALIGNMENT);

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        if (extraLabel != null) {
            extraLabel.setAlignmentX(CENTER_ALIGNMENT);
            card.add(Box.createVerticalStrut(4));
            card.add(extraLabel);
        }
        return card;
    }

    private JLabel bigLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(AquaTheme.ui(Font.BOLD, 26));
        lbl.setForeground(AquaTheme.DEEP_NAVY);
        return lbl;
    }
}
