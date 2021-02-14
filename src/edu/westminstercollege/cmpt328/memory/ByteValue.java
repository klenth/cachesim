package edu.westminstercollege.cmpt328.memory;

/**
 * An interface representing a single byte stored in memory. This interface uses int values to represent individual
 * bytes rather than the Java byte type because Java bytes are signed, which makes them less convenient to work with.
 *
 * @see MemorySystem#allocateByte
 */
public interface ByteValue extends MemoryValue {

    /** Returns the value of this byte as an int */
    int get();
    /** Sets the value of this byte as an int */
    void set(int value);

    @Override
    default int getMemorySize() {
        return 1;
    }

    /** Convenience method to set the value of this byte. Equivalent to <code>set(other.get())</code> */
    default void set(ByteValue other) {
        set(other.get());
    }
}
