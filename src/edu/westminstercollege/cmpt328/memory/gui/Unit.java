package edu.westminstercollege.cmpt328.memory.gui;

public enum Unit {
    Byte("B", 0),
    Kibibyte("kiB", 10),
    Mebibyte("MiB", 20),
    Gibibyte("GiB", 30),
    Tebibyte("TiB", 40),
    Pebibyte("PiB", 50),
    Exbibyte("EiB", 60),
    Zebibyte("ZiB", 70),
    Yobibyte("YiB", 80);

    private final String abbreviation;
    private final int log2;

    Unit(String abbreviation, int log2) {
        this.abbreviation = abbreviation;
        this.log2 = log2;
    }

    public int getLog2() {
        return log2;
    }

    public Unit larger() {
        if (this == Yobibyte)
            return null;
        return Unit.values()[this.ordinal() + 1];
    }

    public Unit smaller() {
        if (this == Byte)
            return null;
        return Unit.values()[this.ordinal() - 1];
    }

    public static Unit forLog2(int log2) {
        if (log2 < 0)
            throw new IllegalArgumentException("log2 cannot be negative");
        if (log2 >= 80)
            return Yobibyte;
        else
            return values()[log2 / 10];
    }

    @Override
    public String toString() {
        return abbreviation;
    }
}
