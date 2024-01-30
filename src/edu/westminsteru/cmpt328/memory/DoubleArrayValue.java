package edu.westminsteru.cmpt328.memory;

import java.util.Iterator;

/**
 * An interface representing an array of doubles stored in memory.
 * Since this interface extends {@link java.lang.Iterable}, you can iterate over one with a for-each loop just like a
 * native array.
 *
 * @see MemorySystem#allocateDoubleArray(int)
 */
public interface DoubleArrayValue extends MemoryValue, Iterable<Double> {

    int getLength();

    default int getMemorySize() {
        return getLength() * Bits.DOUBLE_SIZE;
    }

    /** Returns the i'th element of this array (a single double) */
    double get(int i);
    /** Sets the value of the i'th element of this array */
    void set(int i, double value);

    default Iterator<Double> iterator() {
        return new Iterator<Double>() {

            private int i;

            @Override
            public boolean hasNext() {
                return i < getLength();
            }

            @Override
            public Double next() {
                return get(i++);
            }
        };
    }

    /** Convenience method to get the i'th element of this array; equivalent to <code>get(i.get())</code> */
    default double get(IntValue i) {
        return get(i.get());
    }

    /** Convenience method to set the i'th element of this array; equivalent to <code>set(i.get(), value)</code> */
    default void set(IntValue i, double value) {
        set(i.get(), value);
    }

    /** Convenience method to set the i'th element of this array; equivalent to <code>set(i.get(), value.get())</code> */
    default void set(IntValue i, DoubleValue value) {
        set(i.get(), value.get());
    }

    /** Convenience method to set the i'th element of this array; equivalent to <code>set(i, value.get())</code> */
    default void set(int i, DoubleValue value) {
        set(i, value.get());
    }

    /*
    default DoubleArrayValue subArray(int a, int b) {
        if (b < a)
            throw new IllegalArgumentException("b must be at least a");
        return new DoubleArrayValue() {
            @Override
            public int getLength() {
                return b - a;
            }

            @Override
            public double get(int i) {
                return DoubleArrayValue.this.get(a + i);
            }

            @Override
            public void set(int i, double value) {
                DoubleArrayValue.this.set(a + i, value);
            }

            @Override
            public int getAddress() {
                return DoubleArrayValue.this.getAddress() + a * Bits.DOUBLE_SIZE;
            }
        };
    }

    default DoubleArrayValue subArray(IntValue a, IntValue b) {
        return subArray(a.get(), b.get());
    }
    */
}
