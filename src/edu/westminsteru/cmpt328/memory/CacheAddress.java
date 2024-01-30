package edu.westminsteru.cmpt328.memory;

import java.util.Objects;

class CacheAddress {

    public final int line, offset;

    public CacheAddress(int line, int offset) {
        this.line = line;
        this.offset = offset;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CacheAddress))
            return false;
        CacheAddress ca = (CacheAddress)o;

        return line == ca.line
                && offset == ca.offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, offset);
    }

    @Override
    public String toString() {
        return String.format("line %d, offset %d", line, offset);
    }
}
