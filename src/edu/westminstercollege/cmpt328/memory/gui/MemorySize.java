package edu.westminstercollege.cmpt328.memory.gui;

import javassist.util.proxy.ProxyObjectInputStream;

import java.util.Optional;

public record MemorySize(int count, Unit unit) {

    public MemorySize {
        if (unit == null)
            throw new IllegalArgumentException("unit cannot be null");
        if (count < 0 || (count >= 1024 && unit != Unit.Yobibyte))
            throw new IllegalArgumentException("count must be nonnegative, and it must be less than 1024 (unless the unit is YiB)");
    }

    public static MemorySize of(final int count, final Unit unit) {
        if (unit == null)
            throw new IllegalArgumentException("unit cannot be null");
        if (count < 0)
            throw new IllegalArgumentException("count cannot be negative");

        if (count == 0)
            return new MemorySize(0, Unit.Byte);

        int log2, finalCount = count;
        for (log2 = 0; (finalCount & 1) == 0; finalCount >>= 1, ++log2)
            ;

        Unit finalUnit = Unit.forLog2(unit.getLog2() + log2);
        if (finalUnit == Unit.Yobibyte)
            finalCount <<= log2 + unit.getLog2() - Unit.Yobibyte.getLog2();
        else
            finalCount <<= log2 % 10;

        if (finalCount >= 1024 && finalUnit != Unit.Yobibyte)
            throw new IllegalArgumentException(String.format("%d %s not a valid memory size (would be %d %s which is more than 1023 %s)", count, unit, finalCount, finalUnit, finalUnit));

        return new MemorySize(finalCount, finalUnit);
    }

    @Override
    public String toString() {
        return String.format("%d %s", count, unit);
    }
}
