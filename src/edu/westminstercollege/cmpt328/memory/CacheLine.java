package edu.westminstercollege.cmpt328.memory;

import java.util.function.IntFunction;
import java.util.function.LongFunction;

class CacheLine {

    private class LineByteStore extends ByteStore {
        public LineByteStore(int size) {
            super(size);
        }

        @Override
        void setByteAt(int address, int value) {
            super.setByteAt(address, value);
            dirty = true;
        }

        @Override
        void setIntAt(int address, int value) {
            super.setIntAt(address, value);
            dirty = true;
        }

        @Override
        void setDoubleAt(int address, double value) {
            super.setDoubleAt(address, value);
            dirty = true;
        }
    }

    private final int tag;
    private final ByteStore data;
    private long meta;
    private boolean dirty;
    private boolean present;

    private CacheLine(int tag, boolean present, long meta) {
        this.tag = tag;
        data = present ? new LineByteStore(Bits.LINE_SIZE) : null;
        this.meta = meta;
        dirty = false;
        this.present = present;
    }

    static CacheLine unmapped() {
        return new CacheLine(-1, false, 0);
    }

    static CacheLine map(int tag) {
        return new CacheLine(tag, true, 0);
    }

    long getMeta() {
        return meta;
    }

    void setMeta(long meta) {
        this.meta = meta;
    }

    void updateMeta(LongFunction<Long> f) {
        this.meta = f.apply(meta);
    }

    void evict() {
        present = false;
    }

    boolean isMapped() {
        return tag != -1;
    }

    boolean isPresent() {
        return present;
    }

    boolean isDirty() {
        return dirty;
    }

    ByteStore getData() {
        if (!present)
            throw new IllegalStateException("Line has been evicted");
        return data;
    }

    int getTag() {
        return tag;
    }

    void clean() {
        dirty = false;
    }

    void dirty() {
        dirty = true;
    }
}
