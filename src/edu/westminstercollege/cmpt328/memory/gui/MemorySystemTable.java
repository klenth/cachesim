package edu.westminstercollege.cmpt328.memory.gui;

import edu.westminstercollege.cmpt328.memory.MemorySystem;
import edu.westminstercollege.cmpt328.memory.ReplacementAlgorithm;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.text.TableView;
import java.awt.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class MemorySystemTable extends JTable {

    private class Renderer<T> extends DefaultTableCellRenderer {
        private Function<T, Object> valueMap;

        Renderer(Function<T, Object> valueMap) {
            this.valueMap = valueMap;
            setHorizontalAlignment(CENTER);
        }

        Renderer() {
            this(Object::toString);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void setValue(Object value) {
            if (value == null)
                super.setValue("");
            else
                super.setValue(valueMap.apply((T)value));
        }
    }

    private class SpinnerEditor<T> extends AbstractCellEditor implements TableCellEditor {

        private JSpinner spinner;
        private ToIntFunction<T> valueMap;

        SpinnerEditor(int min, int max, ToIntFunction<T> valueMap) {
            this.valueMap = valueMap;
            spinner = new JSpinner(new SpinnerNumberModel(min, min, max, 1));
            var field = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
            field.setHorizontalAlignment(JTextField.CENTER);
            field.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

            spinner.setValue(valueMap.applyAsInt((T)value));
            spinner.setFont(table.getFont());
            return spinner;
        }
    }

    private class ReplacementEditor extends AbstractCellEditor implements TableCellEditor {

        private JComboBox<ReplacementAlgorithm> box = new JComboBox<>(ReplacementAlgorithm.values());

        {
            box.setRenderer(new DefaultListCellRenderer() {

                {
                    setHorizontalAlignment(CENTER);
                }

                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value == ReplacementAlgorithm.RANDOM)
                        value = "Random";
                    else if (value != null)
                        value = value.toString();
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            box.setSelectedItem(value);
            box.setFont(table.getFont());
            return box;
        }

        @Override
        public Object getCellEditorValue() {
            return box.getSelectedItem();
        }
    }

    {
        setDefaultRenderer(MemorySystemTableModel.MemoryAccessTime.class,
                new Renderer<MemorySystemTableModel.MemoryAccessTime>(t -> t.getAccessTime() + " cycles"));
        setDefaultRenderer(MemorySystemTableModel.CacheSize.class,
                new Renderer<MemorySystemTableModel.CacheSize>(s -> {
                    if (s.getLines() > 0)
                        return String.format("<html><body><center>%d lines<br/><span style='font-size: 90%%;'>(%s)</span></center></body></html>",
                                s.getLines(), s.getSize());
                    else
                        return String.format("<html><body>%s</body></html>", s.getSize());
                }));
        setDefaultRenderer(MemorySystemTableModel.CacheWays.class,
                new Renderer<MemorySystemTableModel.CacheWays>(Object::toString));
        setDefaultRenderer(ReplacementAlgorithm.class,
                new Renderer<ReplacementAlgorithm>(r ->
                        (r == null) ? ""
                        : (r == ReplacementAlgorithm.RANDOM) ? "Random"
                        : r.toString()));

        setDefaultEditor(MemorySystemTableModel.MemoryAccessTime.class,
                new SpinnerEditor<MemorySystemTableModel.MemoryAccessTime>(0, 1_000_000, t -> t.getAccessTime()));
        setDefaultEditor(MemorySystemTableModel.CacheSize.class,
                new SpinnerEditor<MemorySystemTableModel.CacheSize>(1, 1_048_576, s -> s.getLines()));
        setDefaultEditor(MemorySystemTableModel.CacheWays.class,
                new SpinnerEditor<MemorySystemTableModel.CacheWays>(1, 64, w -> w.getWays()));
        setDefaultEditor(ReplacementAlgorithm.class,
                new ReplacementEditor());

        setShowGrid(false);
        setShowHorizontalLines(true);
        setRowHeight(5 * getRowHeight() / 2);
    }

    MemorySystemTable() {
        this(new MemorySystemTableModel());
    }

    MemorySystemTable(MemorySystemTableModel model) {
        super(model);
    }

    @Override
    public MemorySystemTableModel getModel() {
        return (MemorySystemTableModel)super.getModel();
    }

    @Override
    public void setModel(TableModel model) {
        if (model instanceof MemorySystemTableModel)
            super.setModel(model);
        else
            throw new IllegalArgumentException("Model must be an instance of MemorySystemTableModel");
    }
}
