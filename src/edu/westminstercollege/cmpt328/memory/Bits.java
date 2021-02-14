package edu.westminstercollege.cmpt328.memory;

/**
 * Some constants and routines having to do with low-level representation of quantities and binary operations
 */
public final class Bits {

    public static final int INT_SIZE = 4,
                     DOUBLE_SIZE = 8,
                     POINTER_SIZE = 4;   // Must be at least ADDRESS_SIZE / 8

    /** Number of bits in a memory address */
    public static final int ADDRESS_SIZE = 24;
    /** Number of possible memory addresses (i.e. maximum memory capacity in bytes) */
    public static final int NUM_ADDRESSES = 1 << ADDRESS_SIZE;
    /** Largest possible memory address */
    public static final int MAX_ADDRESS = (1 << ADDRESS_SIZE) - 1;

    /** Number of bytes in a memory block (same as LINE_SIZE) */
    public static final int BLOCK_SIZE = 64;
    /** Number of bytes in a cache line (same as BLOCK_SIZE) */
    public static final int LINE_SIZE = BLOCK_SIZE;

    private Bits() {
        throw new RuntimeException();
    }

    /** Returns true if the argument is a power of 2 greater than 1 */
    public static boolean isPowerOf2(long x) {
        return x > 0 && (x & (x - 1)) == 0;
    }

    /** Computes floor(log₂(x)) by fast integer arithmetic */
    public static int log2(long n) {
        int count;
        for (count = 0; n > 1; n >>= 1, ++count)
            ;
        return count;
    }

    /** Returns an int whose bottom count bits are 1 and the rest are zeros */
    public static int ones(int count) {
        return (1 << count) - 1;
    }
}
