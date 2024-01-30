package edu.westminsteru.cmpt328.memory;

/**
 * An interface representing a single int stored in memory.
 * native array.
 *
 * @see MemorySystem#allocateInt()
 */
public interface IntValue extends MemoryValue {

    /** Returns the value of this int */
    int get();
    /** Sets the value of this int */
    void set(int value);

    @Override
    default int getMemorySize() {
        return Bits.INT_SIZE;
    }

    /** Convenience method to increment this int. Equivalent to <code>set(get() + 1)</code> */
    default void increment() {
        set(get() + 1);
    }

    /** Convenience method to decrement this int. Equivalent to <code>set(get() - 1)</code> */
    default void decrement() {
        set(get() - 1);
    }

    /** Convenience method to set the value of this int. Equivalent to <code>set(other.get())</code> */
    default void set(IntValue other) {
        set(other.get());
    }
}
