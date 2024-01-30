package edu.westminsteru.cmpt328.memory;

/**
 * An interface representing a single double stored in memory.
 *
 * @see MemorySystem#allocateDouble()
 */
public interface DoubleValue extends MemoryValue {

    /** Gets the value of this double */
    double get();
    /** Sets the value of this double */
    void set(double value);

    @Override
    default int getMemorySize() {
        return Bits.DOUBLE_SIZE;
    }

    /** Convenience method to set the value of this double. Equivalent to <code>set(other.get())</code> */
    default void set(DoubleValue other) {
        set(other.get());
    }
}
