package edu.westminsteru.cmpt328.memory.gui;

import java.util.Objects;

public final class TotalAccessTime {

    public final long cycles;
    public final long maxCycles;

    public TotalAccessTime(long cycles, long maxCycles) {
        this.cycles = cycles;
        this.maxCycles = maxCycles;
    }

    @Override
    public String toString() {
        return String.format("%,d cycles (max: %,d)", cycles, maxCycles);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TotalAccessTime))
            return false;
        TotalAccessTime tat = (TotalAccessTime)other;
        return cycles == tat.cycles && maxCycles == tat.maxCycles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cycles, maxCycles);
    }
}
