package com.dbcache.ui.component;

import com.dbcache.ui.AquaTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ResultsTable extends JScrollPane {

    private final JTable           table;
    private final DefaultTableModel model;

    public ResultsTable() {
        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setFont(AquaTheme.mono(12));
        table.setRowHeight(25);
        table.setBackground(AquaTheme.WHITE_BLUE);
        table.setForeground(AquaTheme.DEEP_NAVY);
        table.setGridColor(AquaTheme.BABY_BLUE);
        table.setSelectionBackground(AquaTheme.CORNFLOWER);
        table.setSelectionForeground(AquaTheme.DEEP_NAVY);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);

        table.getTableHeader().setFont(AquaTheme.ui(Font.BOLD, 12));
        table.getTableHeader().setBackground(AquaTheme.OCEAN_DARK);
        table.getTableHeader().setForeground(Color.WHITE);

        setViewportView(table);
        setBackground(AquaTheme.WHITE_BLUE);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public void setData(List<String> columns, List<List<Object>> rows) {
        model.setRowCount(0);
        model.setColumnCount(0);
        model.setColumnIdentifiers(columns.toArray());
        for (List<Object> row : rows) model.addRow(row.toArray());
    }

    public void clear() {
        model.setRowCount(0);
        model.setColumnCount(0);
    }

    public JTable getTable() { return table; }
}
