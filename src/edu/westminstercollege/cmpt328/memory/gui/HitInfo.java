package edu.westminstercollege.cmpt328.memory.gui;

import java.util.Objects;

public final class HitInfo {

    public static final int CANNOT_MISS = -1;

    public final long hits, accesses;

    public HitInfo(long hits, long accesses) {
        if (hits < 0 && hits != CANNOT_MISS)
            throw new IllegalArgumentException(String.format("hits must be nonnegative or CANNOT_MISS, not %d", hits));
        if (accesses < 0)
            throw new IllegalArgumentException(String.format("accesses must be nonnegative, not %d", accesses));
        this.hits = hits;
        this.accesses = accesses;
    }

    public HitInfo(long accesses) {
        this(CANNOT_MISS, accesses);
    }

    @Override
    public String toString() {
        return String.format("%,d/%,d", hits, accesses);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof HitInfo))
            return false;
        HitInfo hi = (HitInfo)other;
        return hits == hi.hits && accesses == hi.accesses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hits, accesses);
    }
}
