package edu.westminstercollege.cmpt328.memory.gui;

import edu.westminstercollege.cmpt328.memory.Bits;
import edu.westminstercollege.cmpt328.memory.ReplacementAlgorithm;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class MemorySystemTableModel extends AbstractTableModel {

    static final class Cache {

        private int accessTime;
        private int lines;
        private int ways;
        private ReplacementAlgorithm replacement;

        Cache(int accessTime, int lines, int ways, ReplacementAlgorithm replacement) {
            setAccessTime(accessTime);
            setLines(lines);
            setWays(ways);
            setReplacement(replacement);
        }

        int getAccessTime() {
            return accessTime;
        }

        void setAccessTime(int accessTime) {
            if (accessTime < 0)
                throw new IllegalArgumentException("accessTime cannot be negative");
            this.accessTime = accessTime;
        }

        int getLines() {
            return lines;
        }

        void setLines(int lines) {
            if (lines < 1)
                throw new IllegalArgumentException("lines must be at least 1");
            this.lines = lines;
        }

        int getWays() {
            return ways;
        }

        void setWays(int ways) {
            if (ways < 1)
                throw new IllegalArgumentException("ways must be at least 1");
            this.ways = ways;
        }

        public ReplacementAlgorithm getReplacement() {
            return replacement;
        }

        public void setReplacement(ReplacementAlgorithm replacement) {
            Objects.requireNonNull(replacement, "replacement cannot be null");
            this.replacement = replacement;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Cache other)
                    && other.accessTime == accessTime
                    && other.lines == lines
                    && other.ways == ways
                    && other.replacement == replacement;
        }

        @Override
        public int hashCode() {
            return Objects.hash(accessTime, lines, ways, replacement);
        }
    }

    static class MemoryAccessTime {
        private final int accessTime;

        private MemoryAccessTime(int accessTime) {
            this.accessTime = accessTime;
        }

        int getAccessTime() {
            return accessTime;
        }

        @Override
        public String toString() {
            return (accessTime == 1) ? "1 cycle" : String.format("%d cycles", accessTime);
        }
    }

    static class CacheSize {
        private final int lines;

        private CacheSize(int lines) {
            this.lines = lines;
        }

        int getLines() {
            return lines;
        }

        boolean isRam() {
            return lines == -1;
        }

        MemorySize getSize() {
            if (isRam())
                return MemorySize.of(64, Unit.Mebibyte);
            else
                return MemorySize.of(lines * 64, Unit.Byte);
        }

        @Override
        public String toString() {
            return (isRam()) ? getSize().toString() : String.format("%d lines (%s)", lines, getSize());
        }
    }

    static class CacheWays {
        private final int ways;

        private CacheWays(int ways) {
            this.ways = ways;
        }

        int getWays() {
            return ways;
        }

        @Override
        public String toString() {
            if (ways < 1)
                return "—";
            else if (ways == 1)
                return "1-way (direct)";
            else
                return String.format("%d-way", ways);
        }
    }

    private List<Cache> caches = new ArrayList<>(3);
    private int ramAccessTime = 200;
    private static final CacheSize ramSize = new CacheSize(-1);
    private static final String[] COLUMN_NAMES = {
        "Memory", "Access time", "Size", "Associativity", "Replacement"
    };

    public void setConfiguration(MemorySystemConfiguration configuration) {
        this.ramAccessTime = configuration.getRamAccessTime();
        caches = new ArrayList<>(configuration.getCaches().size());
        for (var c : configuration.getCaches()) {
            var newCache = new Cache(
                    c.getAccessTime(),
                    c.getLineCount(),
                    c.getWays(),
                    c.getReplacement()
            );
            caches.add(newCache);
        }

        fireTableDataChanged();
    }

    public MemorySystemConfiguration getConfiguration() {
        MemorySystemConfiguration.CacheConfiguration[] configCaches = new MemorySystemConfiguration.CacheConfiguration[caches.size()];
        for (int i = 0; i < configCaches.length; ++i) {
            var cache = caches.get(i);
            configCaches[i] = new MemorySystemConfiguration.CacheConfiguration(
                    cache.getLines(),
                    cache.getAccessTime(),
                    cache.getWays(),
                    cache.getReplacement()
            );
        }

        return new MemorySystemConfiguration(Bits.NUM_ADDRESSES, ramAccessTime, configCaches);
    }

    public int getRamAccessTime() {
        return ramAccessTime;
    }

    public List<Cache> getCaches() {
        return Collections.unmodifiableList(caches);
    }

    public void removeCacheAt(int index) {
        caches.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public void insertCacheAt(int index) {
        if (caches.isEmpty())
            insertCacheAt(index, new Cache(20, 32, 1, ReplacementAlgorithm.LRU));
        else if (index == 0) {
            var top = caches.get(0);
            insertCacheAt(0, new Cache(
                    Math.max(1, top.getAccessTime() / 2),
                    Math.max(1, top.getLines() / 4),
                    top.getWays(),
                    top.getReplacement()));
        } else if (index == caches.size()) {
            var above = caches.get(index - 1);
            insertCacheAt(index, new Cache(
                    (int)Math.round(Math.sqrt(above.getAccessTime() * ramAccessTime)),
                    above.getLines() * 4,
                    above.getWays(),
                    above.getReplacement()
            ));
        } else {
            var above = caches.get(index - 1);
            var below = caches.get(index);
            insertCacheAt(index, new Cache(
                    (above.getAccessTime() + below.getAccessTime()) / 2,
                    (above.getLines() + below.getLines()) / 2,
                    above.getWays(),
                    above.getReplacement()
            ));
        }
    }

    public void insertCacheAt(int index, Cache cache) {
        caches.add(index, cache);
        fireTableRowsInserted(index, index);
    }

    @Override
    public int getRowCount() {
        return 1 + caches.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < caches.size()) {
            var c = caches.get(rowIndex);
            return switch (columnIndex) {
                case 0  -> String.format("L%d", rowIndex + 1);
                case 1  -> new MemoryAccessTime(c.getAccessTime());
                case 2  -> new CacheSize(c.getLines());
                case 3  -> new CacheWays(c.getWays());
                case 4  -> (c.ways == 1) ? null : c.getReplacement();
                default -> throw new IllegalArgumentException("Invalid column index: " + columnIndex);
            };
        } else if (rowIndex == caches.size()) {
            return switch (columnIndex) {
                case 0  -> "RAM";
                case 1  -> new MemoryAccessTime(ramAccessTime);
                case 2  -> ramSize;
                case 3  -> null;
                case 4  -> null;
                default -> throw new IllegalArgumentException("Invalid column index: " + columnIndex);
            };
        } else
            throw new IllegalArgumentException("Invalid row index: " + rowIndex);
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0  -> String.class;
            case 1  -> MemoryAccessTime.class;
            case 2  -> CacheSize.class;
            case 3  -> CacheWays.class;
            case 4  -> ReplacementAlgorithm.class;
            default -> throw new IllegalArgumentException("Invalid column index: " + columnIndex);
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (rowIndex == caches.size())
            return columnIndex == 1;
        else {
            if (columnIndex > 0 && columnIndex < 4)
                return true;
            else if (columnIndex == 4)
                return caches.get(rowIndex).getWays() > 1;
            return false;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex == caches.size()) {
            if (columnIndex == 1) {
                ramAccessTime = ((Number)aValue).intValue();
                fireTableRowsUpdated(rowIndex, rowIndex);
            } else
                throw new IllegalArgumentException(String.format("Cannot update row %d, column %d", rowIndex, columnIndex));
        } else {
            var c = caches.get(rowIndex);
            switch (columnIndex) {
                case 1  -> c.setAccessTime(((Number)aValue).intValue());
                case 2  -> c.setLines(((Number)aValue).intValue());
                case 3  -> c.setWays(((Number)aValue).intValue());
                case 4  -> c.setReplacement((ReplacementAlgorithm)aValue);
                default -> throw new IllegalArgumentException(String.format("Cannot update row %d, column %d", rowIndex, columnIndex));
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

}
