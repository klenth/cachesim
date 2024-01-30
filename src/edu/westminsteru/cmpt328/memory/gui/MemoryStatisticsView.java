package edu.westminsteru.cmpt328.memory.gui;

import edu.westminsteru.cmpt328.memory.Memory;

import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class MemoryStatisticsView extends JComponent {

    private JTable table;
    private JLabel totalAccessTimeLabel;
    private JComponent controlPanel = new JPanel();
    private JButton closeButton = new JButton("Close");

    private MemoryStatisticsTableModel tableModel = new MemoryStatisticsTableModel();
    private JFrame frame = null;

    private static Font MONO_FONT = null;

    static {
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MemoryStatisticsView() {
        table = new JTable(tableModel);

        setLayout(new BorderLayout());
        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        totalAccessTimeLabel = new JLabel();
        center.add(totalAccessTimeLabel, BorderLayout.SOUTH);
        totalAccessTimeLabel.setHorizontalAlignment(JLabel.TRAILING);

        add(center, BorderLayout.CENTER);

        controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        controlPanel.add(closeButton);
        add(controlPanel, BorderLayout.SOUTH);
        controlPanel.setVisible(false);

        closeButton.addActionListener(e -> {
            if (frame != null)
                frame.dispose();
        });

        table.setDefaultRenderer(String.class, new BasicCellRenderer());
        table.setDefaultRenderer(HitInfo.class, new HitInfoCellRenderer());
        table.setDefaultRenderer(Double.class, new HitRatioCellRenderer());
        table.setDefaultRenderer(TotalAccessTime.class, new TotalAccessTimeCellRenderer());
        table.setFont(getMonoFont());
        table.setShowGrid(false);

        table.getColumnModel().getColumn(0).setMinWidth(100);
        table.getColumnModel().getColumn(0).setMaxWidth(100);
        table.getColumnModel().getColumn(1).setMinWidth(180);
        table.getColumnModel().getColumn(2).setMinWidth(100);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setMinWidth(180);

        Font headerFont = table.getTableHeader().getFont();
        table.getTableHeader().setFont(headerFont.deriveFont(16f));
    }

    public void showWindow(Memory top) {
        tableModel.setTopMemory(top);
        totalAccessTimeLabel.setText(String.format("Total access time: %,d cycles", top.getTotalAccessTime()));
        Font f = totalAccessTimeLabel.getFont();
        totalAccessTimeLabel.setFont(f.deriveFont(Font.BOLD).deriveFont(f.getSize2D() * 1.5f));

        if (frame == null) {
            frame = new JFrame("Memory statistics");
            ((JComponent)frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            frame.setLayout(new BorderLayout());
            frame.add(this, BorderLayout.CENTER);

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        controlPanel.setVisible(true);
        frame.pack();
        Dimension size = frame.getSize();
        size.width = Math.max(size.width, 900);
        frame.setSize(size);
        frame.setVisible(true);
    }

    private String formatInteger(long value, int maxColumns) {
        String s = String.format("%,d", value);
        if (s.length() > maxColumns) {
            // Go into scientific notation!
            int power = (int)Math.log10(value);
            double mantissa = ((double)value) / Math.pow(10, power);
            String suffix = String.format("×10%s", exponent(power));
            int mantissaDigits = maxColumns - suffix.length() - 2;
            return String.format("%." + mantissaDigits + "f%s", mantissa, suffix);
        } else
            return s;
    }

    private String exponent(int digits) {
        switch (digits) {
        case 1:
            return "¹";
        case 2:
            return "²";
        case 3:
            return "³";
        case 0:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
        case 9:
            return Character.toString((char)('⁰' + digits));
        default:
            if (digits < 0)
                throw new IllegalArgumentException("Digits cannot be negative");
            return exponent(digits / 10) + exponent(digits % 10);
        }
    }

    private static class BasicCellRenderer<T> extends DefaultTableCellRenderer {

        {
            setFont(getMonoFont());
            setHorizontalAlignment(JLabel.CENTER);
        }

        protected String getText(T value) {
            return (value == null) ? "" : value.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(getText((T)value));
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setFont(getMonoFont());

            return this;
        }

        @Override
        protected void paintComponent(Graphics _g) {
            Graphics2D g = (Graphics2D)_g.create();
            //g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            super.paintComponent(g);
            g.dispose();
        }
    }

    private class HitInfoCellRenderer extends BasicCellRenderer<HitInfo> {

        private static final int COUNT_COLUMNS = 11;

        @Override
        public String getText(HitInfo hi) {
            if (hi.hits != HitInfo.CANNOT_MISS)
                return String.format("%" + COUNT_COLUMNS + "s/%-" + COUNT_COLUMNS + "s",
                    formatInteger(hi.hits, COUNT_COLUMNS),
                    formatInteger(hi.accesses, COUNT_COLUMNS)
                );
            else
                return String.format("%" + COUNT_COLUMNS + "s/%-" + COUNT_COLUMNS + "s",
                    "",
                    formatInteger(hi.accesses, COUNT_COLUMNS)
                );
        }
    }

    private class HitRatioCellRenderer extends BasicCellRenderer<Double> {
        @Override
        public String getText(Double ratio) {
            if (ratio.isNaN())
                return "";
            else
                return String.format("%6.2f%%", 100.0 * ratio);
        }
    }

    private class TotalAccessTimeCellRenderer extends BasicCellRenderer<TotalAccessTime> {

        {
            setHorizontalTextPosition(JLabel.LEFT);
        }

        private static final int TIME_COLUMNS = 11;
        private static final int MAX_BAR_WIDTH = 100;

        @Override
        protected String getText(TotalAccessTime value) {
            return String.format("%" + TIME_COLUMNS + "s", formatInteger(value.cycles, TIME_COLUMNS));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TotalAccessTime tat = (TotalAccessTime)value;
            int barWidth = (int)Math.round(((double)tat.cycles) / tat.maxCycles * MAX_BAR_WIDTH);
            setIcon(new BarIcon(barWidth, MAX_BAR_WIDTH));
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    private static class BarIcon implements Icon {

        private static final int HEIGHT = 14;

        private int width, maxWidth;

        BarIcon(int width, int maxWidth) {
            this.width = width;
            this.maxWidth = maxWidth;
        }

        @Override
        public void paintIcon(Component c, Graphics _g, int x, int y) {
            Graphics2D g = (Graphics2D)_g;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c.getBackground());
            g.fillRect(x, y, maxWidth, HEIGHT);
            g.setColor(Color.RED);
            g.fillRoundRect(x, y, width, HEIGHT, HEIGHT, HEIGHT);
        }

        @Override
        public int getIconWidth() {
            return maxWidth;
        }

        @Override
        public int getIconHeight() {
            return HEIGHT;
        }
    }

    private static Font getMonoFont() {
        if (MONO_FONT == null) {
            try {
                MONO_FONT = Font.createFont(Font.TRUETYPE_FONT,
                    MemoryStatisticsView.class.getResourceAsStream("/ttf/DejaVuSansMono.ttf")
                ).deriveFont(16f);
            } catch (IOException | FontFormatException ex) {
                ex.printStackTrace();
            }
        }

        return (MONO_FONT == null) ? new Font("Monospaced", Font.PLAIN, 16) : MONO_FONT;
    }
}
