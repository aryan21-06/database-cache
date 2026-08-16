package com.dbcache.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;

public final class AquaTheme {

    public static final Color DEEP_NAVY   = new Color(0x0D1B2A);
    public static final Color OCEAN_DARK  = new Color(0x1B3A5C);
    public static final Color OCEAN_MID   = new Color(0x1E6091);
    public static final Color SKY_BLUE    = new Color(0x2E86C1);
    public static final Color CORNFLOWER  = new Color(0x5DADE2);
    public static final Color BABY_BLUE   = new Color(0xAED6F1);
    public static final Color MIST        = new Color(0xD6EAF8);
    public static final Color WHITE_BLUE  = new Color(0xEBF5FB);
    public static final Color GOLD        = new Color(0xD4AC0D);
    public static final Color GOLD_LIGHT  = new Color(0xFCF3CF);
    public static final Color HIT_TEAL    = new Color(0x1ABC9C);
    public static final Color MISS_AMBER  = new Color(0xE67E22);
    public static final Color ERR_RED     = new Color(0xE74C3C);

    public static Font ui(int style, int size) { return new Font("SansSerif",  style,      size); }
    public static Font mono(int size)           { return new Font("Monospaced", Font.PLAIN,  size); }

    public static Border sectionBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(OCEAN_MID, 1, true),
            " " + title + " ",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            ui(Font.BOLD, 12),
            CORNFLOWER
        );
    }

    public static JButton makeButton(String text, boolean accent) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if      (getModel().isPressed())  g2.setColor(OCEAN_DARK);
                else if (getModel().isRollover()) g2.setColor(accent ? OCEAN_MID : BABY_BLUE);
                else                              g2.setColor(accent ? SKY_BLUE  : WHITE_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (!accent) {
                    g2.setColor(SKY_BLUE);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                }
                g2.setColor(accent ? Color.WHITE : SKY_BLUE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(ui(Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(160, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void showErrorToast(Window owner, String message) {
        JWindow toast = new JWindow(owner);
        toast.setBackground(new Color(0, 0, 0, 0));

        JPanel bg = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DEEP_NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(ERR_RED);
                g2.fillRoundRect(0, 0, getWidth(), 4, 6, 6);
                g2.dispose();
            }
        };
        bg.setOpaque(false);
        bg.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("X  Error");
        title.setFont(ui(Font.BOLD, 12));
        title.setForeground(ERR_RED);
        bg.add(title, BorderLayout.NORTH);

        String display = (message != null) ? message : "An unknown error occurred.";
        if (display.length() > 120) display = display.substring(0, 117) + "...";
        JLabel msg = new JLabel("<html><body style='width:260px'>" + display + "</body></html>");
        msg.setFont(ui(Font.PLAIN, 12));
        msg.setForeground(new Color(0xCCCCCC));
        bg.add(msg, BorderLayout.CENTER);

        toast.setContentPane(bg);
        toast.pack();

        if (owner != null) {
            Rectangle b = owner.getBounds();
            toast.setLocation(b.x + b.width  - toast.getWidth()  - 12,
                              b.y + b.height - toast.getHeight() - 12);
        }
        toast.setVisible(true);

        Timer t = new Timer(4000, (ActionEvent e) -> toast.dispose());
        t.setRepeats(false);
        t.start();
    }

    public static JDialog showLoadingDialog(Window owner, String title) {
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.MODELESS);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel bg = new JPanel(new BorderLayout(10, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MIST);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(GOLD);
                g2.fillRoundRect(0, 0, getWidth(), 4, 6, 6);
                g2.dispose();
            }
        };
        bg.setOpaque(false);
        bg.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ui(Font.BOLD, 14));
        titleLabel.setForeground(DEEP_NAVY);
        bg.add(titleLabel, BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(280, 24));
        progressBar.setBackground(WHITE_BLUE);
        progressBar.setForeground(SKY_BLUE);
        progressBar.setBorderPainted(false);
        bg.add(progressBar, BorderLayout.CENTER);

        JLabel kaomoji = new JLabel("(=^･ω･^=)︵ᴡᴀɪᴛ");
        kaomoji.setFont(ui(Font.PLAIN, 12));
        kaomoji.setForeground(GOLD);
        kaomoji.setHorizontalAlignment(SwingConstants.CENTER);
        bg.add(kaomoji, BorderLayout.SOUTH);

        dialog.setContentPane(bg);
        dialog.pack();

        if (owner != null) {
            dialog.setLocationRelativeTo(owner);
        }
        dialog.setVisible(true);
        return dialog;
    }

    public static void showSuccessToast(Window owner, String message) {
        JWindow toast = new JWindow(owner);
        toast.setBackground(new Color(0, 0, 0, 0));

        JPanel bg = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DEEP_NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(HIT_TEAL);
                g2.fillRoundRect(0, 0, getWidth(), 4, 6, 6);
                g2.dispose();
            }
        };
        bg.setOpaque(false);
        bg.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("✓ Success");
        title.setFont(ui(Font.BOLD, 12));
        title.setForeground(HIT_TEAL);
        bg.add(title, BorderLayout.NORTH);

        String display = (message != null) ? message : "Operation completed.";
        if (display.length() > 120) display = display.substring(0, 117) + "...";
        JLabel msg = new JLabel("<html><body style='width:260px'>" + display + "</body></html>");
        msg.setFont(ui(Font.PLAIN, 12));
        msg.setForeground(new Color(0xCCCCCC));
        bg.add(msg, BorderLayout.CENTER);

        toast.setContentPane(bg);
        toast.pack();

        if (owner != null) {
            Rectangle b = owner.getBounds();
            toast.setLocation(b.x + b.width  - toast.getWidth()  - 12,
                              b.y + b.height - toast.getHeight() - 12);
        }
        toast.setVisible(true);

        Timer t = new Timer(3000, (ActionEvent e) -> toast.dispose());
        t.setRepeats(false);
        t.start();
    }

    public static void apply() {
        UIManager.put("Panel.background",             WHITE_BLUE);
        UIManager.put("TabbedPane.background",        WHITE_BLUE);
        UIManager.put("TabbedPane.selected",          BABY_BLUE);
        UIManager.put("TabbedPane.contentAreaColor",  WHITE_BLUE);
        UIManager.put("TabbedPane.focus",             SKY_BLUE);
        UIManager.put("TabbedPane.font",              ui(Font.BOLD, 13));
        UIManager.put("Label.foreground",             DEEP_NAVY);
        UIManager.put("TextArea.background",          WHITE_BLUE);
        UIManager.put("TextArea.foreground",          DEEP_NAVY);
        UIManager.put("TextArea.caretForeground",     SKY_BLUE);
        UIManager.put("TextField.background",         WHITE_BLUE);
        UIManager.put("TextField.foreground",         DEEP_NAVY);
        UIManager.put("Table.background",             WHITE_BLUE);
        UIManager.put("Table.foreground",             DEEP_NAVY);
        UIManager.put("Table.gridColor",              BABY_BLUE);
        UIManager.put("Table.selectionBackground",    CORNFLOWER);
        UIManager.put("Table.selectionForeground",    Color.WHITE);
        UIManager.put("TableHeader.background",       OCEAN_DARK);
        UIManager.put("TableHeader.foreground",       Color.WHITE);
        UIManager.put("TableHeader.font",             ui(Font.BOLD, 12));
        UIManager.put("ScrollPane.background",        WHITE_BLUE);
        UIManager.put("ScrollBar.thumb",              CORNFLOWER);
        UIManager.put("ScrollBar.track",              MIST);
        UIManager.put("OptionPane.background",        WHITE_BLUE);
        UIManager.put("OptionPane.messageForeground", DEEP_NAVY);
        UIManager.put("Spinner.background",           WHITE_BLUE);
        UIManager.put("Spinner.foreground",           DEEP_NAVY);
        UIManager.put("ComboBox.background",          WHITE_BLUE);
        UIManager.put("TitledBorder.titleColor",      CORNFLOWER);
    }

    private AquaTheme() {}
}
