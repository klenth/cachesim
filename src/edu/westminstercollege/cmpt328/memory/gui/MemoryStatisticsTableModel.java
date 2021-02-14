package edu.westminstercollege.cmpt328.memory.gui;

import edu.westminstercollege.cmpt328.memory.Cache;
import edu.westminstercollege.cmpt328.memory.Memory;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MemoryStatisticsTableModel extends AbstractTableModel {

    public enum Column {
        Name, Hits, HitRatio, TotalAccessTime;
    }

    private List<Memory> memories;
    private List<Column> columns;

    public MemoryStatisticsTableModel(Memory top) {
        memories = new ArrayList<>();
        setTopMemory(top);
        setColumns(Arrays.asList(Column.values()));
    }

    public MemoryStatisticsTableModel() {
        this(null);
    }

    public void setTopMemory(Memory top) {
        if (top != getTopMemory()) {
            memories = new ArrayList<>();
            collectMemories(top, memories);
            fireTableDataChanged();
        }
    }

    public Memory getTopMemory() {
        return memories.isEmpty() ? null : memories.get(0);
    }

    public void setColumns(List<Column> columns) {
        if (columns == null)
            throw new IllegalArgumentException("columns list cannot be null");
        this.columns = new ArrayList<>(columns);
        fireTableStructureChanged();
    }

    private void collectMemories(Memory top, List<Memory> memories) {
        if (top != null) {
            memories.add(top);
            if (top instanceof Cache)
                collectMemories(((Cache)top).getSource(), memories);
        }
    }

    @Override
    public int getRowCount() {
        return memories.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columns.get(columnIndex)) {
        case Name:
            return String.class;
        case Hits:
            return HitInfo.class;
        case HitRatio:
            return Double.class;
        case TotalAccessTime:
            return TotalAccessTime.class;
        default:
            throw new IllegalArgumentException("Bad column index: " + columnIndex);
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Memory m = memories.get(rowIndex);
        Column c = columns.get(columnIndex);
        if (m instanceof Cache)
            return getCacheValue((Cache)m, c);
        else
            return getMemoryValue(m, c);
    }

    private Object getCacheValue(Cache cache, Column col) {
        switch (col) {
        case Name:
            return cache.getName();
        case Hits:
            return new HitInfo(cache.getHitCount(), cache.getAccessCount());
        case HitRatio:
            return ((double)cache.getHitCount()) / cache.getAccessCount();
        case TotalAccessTime:
            return new TotalAccessTime(cache.getThisLevelAccessTime(), getMaxTotalAccessTime());
        default:
            throw new IllegalArgumentException("Unknown column: " + col);
        }
    }

    private Object getMemoryValue(Memory memory, Column col) {
        switch (col) {
        case Name:
            return memory.getName();
        case Hits:
            return new HitInfo(memory.getAccessCount());
        case HitRatio:
            return Double.NaN;
        case TotalAccessTime:
            return new TotalAccessTime(memory.getTotalAccessTime(), getMaxTotalAccessTime());
        default:
            throw new IllegalArgumentException("Unknown column: " + col);
        }
    }

    private long getMaxTotalAccessTime() {
        return memories.stream()
                .mapToLong(Memory::getTotalAccessTime)
                .max()
                .orElseGet(() -> 0L);
    }

    @Override
    public String getColumnName(int column) {
        switch (columns.get(column)) {
        case Name:
            return "Memory";
        case Hits:
            return "Hits / accesses";
        case HitRatio:
            return "Hit ratio";
        case TotalAccessTime:
            return "Total access time (cycles)";
        default:
            throw new IllegalArgumentException("Bad column: " + column);
        }
    }
}
