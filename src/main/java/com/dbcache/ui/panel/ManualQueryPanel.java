package com.dbcache.ui.panel;

import com.dbcache.handler.RequestHandler;
import com.dbcache.model.QueryResponse;
import com.dbcache.database.QueryResult;
import com.dbcache.ui.AquaTheme;
import com.dbcache.ui.component.ResultsTable;

import javax.swing.*;
import java.awt.*;

public class ManualQueryPanel extends JPanel {

    private final RequestHandler requestHandler;

    private JTextArea    sqlArea;
    private JButton      executeBtn;
    private JButton      clearBtn;
    private JLabel       statusBadge;
    private JLabel       timeLabel;
    private JLabel       dbLabel;
    private ResultsTable resultsTable;

    public ManualQueryPanel(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        setLayout(new BorderLayout(10, 10));
        setBackground(AquaTheme.WHITE_BLUE);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        initComponents();
    }

    private void initComponents() {

        JPanel inputCard = new JPanel(new BorderLayout(6, 6));
        inputCard.setBackground(AquaTheme.WHITE_BLUE);
        inputCard.setBorder(AquaTheme.sectionBorder("SQL Query Input"));

        sqlArea = new JTextArea(5, 60);
        sqlArea.setFont(AquaTheme.mono(13));
        sqlArea.setBackground(AquaTheme.WHITE_BLUE);
        sqlArea.setForeground(AquaTheme.DEEP_NAVY);
        sqlArea.setCaretColor(AquaTheme.SKY_BLUE);
        sqlArea.setLineWrap(true);
        sqlArea.setWrapStyleWord(true);
        sqlArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AquaTheme.BABY_BLUE, 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JScrollPane sqlScroll = new JScrollPane(sqlArea);
        sqlScroll.setBorder(BorderFactory.createEmptyBorder());
        sqlScroll.setBackground(AquaTheme.WHITE_BLUE);
        inputCard.add(sqlScroll, BorderLayout.CENTER);

        executeBtn = AquaTheme.makeButton("Execute", true);
        clearBtn   = AquaTheme.makeButton("Clear",   false);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        btnRow.setBackground(AquaTheme.WHITE_BLUE);
        btnRow.add(executeBtn);
        btnRow.add(clearBtn);
        inputCard.add(btnRow, BorderLayout.SOUTH);

        statusBadge = new JLabel("Status: --", SwingConstants.CENTER);
        statusBadge.setFont(AquaTheme.ui(Font.BOLD, 13));
        statusBadge.setForeground(AquaTheme.OCEAN_DARK);
        statusBadge.setBackground(AquaTheme.BABY_BLUE);
        statusBadge.setOpaque(true);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        timeLabel = new JLabel("Time: --");
        timeLabel.setFont(AquaTheme.ui(Font.PLAIN, 12));
        timeLabel.setForeground(AquaTheme.DEEP_NAVY);

        dbLabel = new JLabel("DB: --");
        dbLabel.setFont(AquaTheme.ui(Font.PLAIN, 12));
        dbLabel.setForeground(AquaTheme.DEEP_NAVY);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        statusRow.setBackground(AquaTheme.MIST);
        statusRow.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, AquaTheme.BABY_BLUE),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        statusRow.add(statusBadge);
        statusRow.add(makeSep());
        statusRow.add(timeLabel);
        statusRow.add(makeSep());
        statusRow.add(dbLabel);

        JPanel resultsCard = new JPanel(new BorderLayout(6, 6));
        resultsCard.setBackground(AquaTheme.WHITE_BLUE);
        resultsCard.setBorder(AquaTheme.sectionBorder("Results"));

        resultsTable = new ResultsTable();
        resultsCard.add(resultsTable, BorderLayout.CENTER);

        JPanel topSection = new JPanel(new BorderLayout(6, 6));
        topSection.setBackground(AquaTheme.WHITE_BLUE);
        topSection.add(inputCard,  BorderLayout.NORTH);
        topSection.add(statusRow,  BorderLayout.SOUTH);

        add(topSection,  BorderLayout.NORTH);
        add(resultsCard, BorderLayout.CENTER);

        executeBtn.addActionListener(e -> executeQuery());
        clearBtn.addActionListener(e   -> clearAll());
    }

    private void executeQuery() {
        String sql = sqlArea.getText().trim();
        if (sql.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a SQL query first!",
                "Empty Query", JOptionPane.WARNING_MESSAGE);
            return;
        }

        executeBtn.setEnabled(false);
        try {
            QueryResponse resp = requestHandler.executeQuery(sql);

            if (resp.isSuccessful()) {
                QueryResult result = resp.getResult();
                resultsTable.setData(result.getColumns(), result.getRows());

                boolean hit = resp.isCacheHit();
                if (hit) {
                    statusBadge.setText("  CACHE HIT  (o‿o)  ");
                    statusBadge.setBackground(AquaTheme.HIT_TEAL);
                    statusBadge.setForeground(Color.WHITE);
                } else {
                    statusBadge.setText("  CACHE MISS  (╥_╥)  ");
                    statusBadge.setBackground(AquaTheme.MISS_AMBER);
                    statusBadge.setForeground(Color.WHITE);
                }

                timeLabel.setText("Time: " + resp.getResponseTimeMs() + "ms");
                dbLabel.setText("DB: " + (resp.isDatabaseAccessed() ? "Yes" : "No"));

            } else {
                statusBadge.setText("  FAILED  (>_<)  ");
                statusBadge.setBackground(AquaTheme.ERR_RED);
                statusBadge.setForeground(Color.WHITE);
                timeLabel.setText("Time: --");
                dbLabel.setText("DB: --");

                AquaTheme.showErrorToast(
                    SwingUtilities.getWindowAncestor(this),
                    resp.getErrorMessage());
            }

        } catch (Exception ex) {
            System.err.println("[ManualQueryPanel] Query error: " + ex.getMessage());
            ex.printStackTrace();

            statusBadge.setText("  ERROR  (>_<)  ");
            statusBadge.setBackground(AquaTheme.ERR_RED);
            statusBadge.setForeground(Color.WHITE);

            AquaTheme.showErrorToast(
                SwingUtilities.getWindowAncestor(this),
                ex.getMessage());
        } finally {
            executeBtn.setEnabled(true);
        }
    }

    private void clearAll() {
        sqlArea.setText("");
        resultsTable.clear();
        statusBadge.setText("Status: --");
        statusBadge.setBackground(AquaTheme.BABY_BLUE);
        statusBadge.setForeground(AquaTheme.OCEAN_DARK);
        timeLabel.setText("Time: --");
        dbLabel.setText("DB: --");
    }

    private JSeparator makeSep() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 22));
        sep.setForeground(AquaTheme.BABY_BLUE);
        return sep;
    }
}
