package edu.westminstercollege.cmpt328.memory;

/**
 * Interface representing a value stored in {@link Memory}. Normally an instance of this interface by using one of the
 * <code>allocate</code> methods of {@link MemorySystem}.
 *
 * @see MemorySystem
 */
public interface MemoryValue {

    /**
     * Returns the size (number of bytes) that this value occupies in memory.
     */
    int getMemorySize();

    /**
     * Returns the starting memory address of this value.
     */
    int getAddress();
}
