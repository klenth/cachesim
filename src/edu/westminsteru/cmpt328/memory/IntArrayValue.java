package edu.westminsteru.cmpt328.memory;

import java.util.Iterator;

/**
 * An interface representing an array of ints stored in memory.
 * Since this interface extends {@link java.lang.Iterable}, you can iterate over one with a for-each loop just like a
 * native array.
 *
 * @see MemorySystem#allocateIntArray(int)
 */
public interface IntArrayValue extends MemoryValue, Iterable<Integer> {

    int getLength();

    default int getMemorySize() {
        return getLength() * Bits.INT_SIZE;
    }

    /** Gets the i'th element of this array, a single int */
    int get(int i);
    /** Sets the i'th element of this array, a single int */
    void set(int i, int value);

    default Iterator<Integer> iterator() {
        return new Iterator<Integer>() {

            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < getLength();
            }

            @Override
            public Integer next() {
                return get(i++);
            }
        };
    }

    /** Convenience method to get the i'th element of this array. Equivalent to <code>get(i.get())</code> */
    default int get(IntValue i) {
        return get(i.get());
    }

    /** Convenience method to set the i'th element of this array. Equivalent to <code>set(i.get(), value)</code> */
    default void set(IntValue i, int value) {
        set(i.get(), value);
    }

    /** Convenience method to set the i'th element of this array. Equivalent to <code>set(i.get(), value.get())</code> */
    default void set(IntValue i, IntValue value) {
        set(i.get(), value.get());
    }

    /** Convenience method to set the i'th element of this array. Equivalent to <code>set(i, value.get())</code> */
    default void set(int i, IntValue value) {
        set(i, value.get());
    }

    /*
    default IntArrayValue subArray(int a, int b) {
        if (b < a)
            throw new IllegalArgumentException("b must be at least a");
        return new IntArrayValue() {
            @Override
            public int getLength() {
                return b - a;
            }

            @Override
            public int get(int i) {
                return IntArrayValue.this.get(a + i);
            }

            @Override
            public void set(int i, int value) {
                IntArrayValue.this.set(a + i, value);
            }

            @Override
            public int getAddress() {
                return IntArrayValue.this.getAddress() + a * Bits.INT_SIZE;
            }
        };
    }

    default IntArrayValue subArray(IntValue a, IntValue b) {
        return subArray(a.get(), b.get());
    }
    */
}
