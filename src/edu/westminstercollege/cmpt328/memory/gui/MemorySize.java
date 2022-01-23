package edu.westminstercollege.cmpt328.memory.gui;

import java.util.Optional;
import java.util.regex.*;

public record MemorySize(int count, Unit unit) implements Comparable<MemorySize> {

    private static final Pattern PATTERN = Pattern.compile(
      "^\s*([0-9]+)\s*([kMGTPEZY]?)i?B\s*", Pattern.CASE_INSENSITIVE
    );

    public MemorySize {
        if (unit == null)
            throw new IllegalArgumentException("unit cannot be null");
        /*if (count < 0 || (count >= 1024 && unit != Unit.Yobibyte))
            throw new IllegalArgumentException("count must be nonnegative, and it must be less than 1024 (unless the unit is YiB)");*/
        if (count < 0)
            throw new IllegalArgumentException("count must be nonnegative");
    }

    public double as(Unit asUnit) {
        if (unit == null)
            throw new IllegalArgumentException("unit cannot be null");
        double q = count;
        for ( ; asUnit.compareTo(this.unit) > 0; asUnit = asUnit.smaller())
            q /= 1024;
        for ( ; asUnit.compareTo(this.unit) < 0; asUnit = asUnit.larger())
            q *= 1024;
        return q;
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

        /*(if (finalCount >= 1024 && finalUnit != Unit.Yobibyte)
            throw new IllegalArgumentException(String.format("%d %s not a valid memory size (would be %d %s which is more than 1023 %s)", count, unit, finalCount, finalUnit, finalUnit));*/

        return new MemorySize(finalCount, finalUnit);
    }

    public static Optional<MemorySize> parse(String source) {
        Matcher m = PATTERN.matcher(source);
        if (m.matches()) {
            int count = Integer.parseInt(m.group(1));
            Unit unit = switch (m.group(2).toLowerCase()) {
                case ""     -> Unit.Byte;
                case "k"    -> Unit.Kibibyte;
                case "m"    -> Unit.Mebibyte;
                case "g"    -> Unit.Gibibyte;
                case "t"    -> Unit.Tebibyte;
                case "p"    -> Unit.Pebibyte;
                case "e"    -> Unit.Exbibyte;
                case "z"    -> Unit.Zebibyte;
                case "y"    -> Unit.Yobibyte;
                default     -> throw new IllegalArgumentException("[INTERNAL ERROR] unknown size prefix: " + m.group(2));
            };

            return Optional.of(MemorySize.of(count, unit));
        }

        return Optional.empty();
    }

    public static MemorySize min(MemorySize a, MemorySize b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    public static MemorySize max(MemorySize a, MemorySize b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static MemorySize geometricMean(MemorySize a, MemorySize b) {
        // Ensure that a is the smaller of the two
        if (a.compareTo(b) > 0)
            return geometricMean(b, a);
        else if (a.equals(b))
            return a;

        int count = a.count() * b.count();
        int unitDifference = b.unit.ordinal() - a.unit.ordinal();

        return MemorySize.of((int)Math.round(Math.sqrt(count * Math.pow(1024.0, unitDifference))), a.unit);
    }

    @Override
    public String toString() {
        return String.format("%d %s", count, unit);
    }

    @Override
    public int compareTo(MemorySize other) {
        if (unit != other.unit)
            return unit.compareTo(other.unit);
        return count - other.count;
    }
}
