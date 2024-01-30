package edu.westminsteru.cmpt328.memory;

/**
 * An interface representing a pointer (memory address) stored in memory. Such a pointer can hold an address of some
 * other kind of {@link MemoryValue} such as an {@link IntValue} or {@link DoubleValue}. Memory addresses are
 * represented as ints and use the same amount of memory as an {@code IntValue}.
 *
 * @see MemorySystem#allocatePointer
 */
public interface PointerValue extends MemoryValue {

    /** Returns the value of this pointer, i.e. the memory address that it refers to */
    int get();
    /** Sets the value of this pointer, i.e. the memory address that it refers to */
    void set(int value);

    @Override
    default int getMemorySize() {
        return Bits.POINTER_SIZE;
    }
}
