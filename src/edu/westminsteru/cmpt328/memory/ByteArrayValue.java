package edu.westminsteru.cmpt328.memory;

import java.util.Iterator;

/**
 * An interface representing an array of bytes stored in memory. This interface uses int values to represent individual
 * bytes rather than the Java byte type because Java bytes are signed, which makes them less convenient to work with.
 * Since this interface extends {@link java.lang.Iterable}, you can iterate over one with a for-each loop just like a
 * native array.
 *
 * @see MemorySystem#allocateByteArray(int)
 */
public interface ByteArrayValue extends MemoryValue, Iterable<Integer> {

    int getLength();

    default int getMemorySize() {
        return getLength();
    }


    /** Returns the i'th element of this byte array (a single byte) */
    int get(int i);
    /** Sets the value of the i'th element of this byte array (a single byte) */
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

    /** Returns the i'th element of this byte array (a single byte). This convenience method is equivalent to
     * <code>get(i.get())</code>. */
    default int get(IntValue i) {
        return get(i.get());
    }

    /** Sets the i'th element of this byte array (a single byte). This convenience method is equivalent to
     * <code>set(i.get(), value)</code> */
    default void set(IntValue i, int value) {
        set(i.get(), value);
    }

    /** Sets the i'th element of this byte array (a single byte). This convenience method is equivalent to
     * <code>set(i.get(), value.get())</code> */
    default void set(IntValue i, IntValue value) {
        set(i.get(), value.get());
    }

    /** Sets the i'th element of this byte array (a single byte). This convenience method is equivalent to
     * <code>set(i, value.get())</code> */
    default void set(int i, IntValue value) {
        set(i, value.get());
    }
}
