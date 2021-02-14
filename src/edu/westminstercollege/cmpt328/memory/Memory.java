package edu.westminstercollege.cmpt328.memory;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Interface describing a memory, a device that can store values. {@link MainMemory} and {@link Cache} are the main
 * classes implementing this interface that you might care about. Ordinarily you will not use the <code>getInt()</code>,
 * <code>getDouble()</code>, etc. methods; instead use the <code>allocate</code> family of methods in {@link MemorySystem}.
 */
public interface Memory {

    /** Returns the size in bytes of this Memory */
    int getSize();
    /** Returns the access time in cycles of this Memory */
    int getAccessTime();
    /** Returns the name of this memory */
    String getName();
    /** Returns the number of accesses to this memory; this counter is reset by calling {@link #reset()} */
    long getAccessCount();

    /** Writes a block of memory back to this Memory. This method is called by {@link Cache}s when evicting a dirty line. */
    boolean writeback(ByteStore data, int block);
    /** Pulls a block of data from this Memory. This method is called by {@link Cache}s when fetching a line into cache. */
    void fetch(ByteStore data, int block);

    /**
     * Resets this memory, putting it back in its initial state. Specifically, after reset a memory will
     * <ul>
     *     <li>be filled with zeros;</li>
     *     <li>have zero access count;</li>
     *     <li>if a cache, will be vacant; but</li>
     *     <li>size, access time, and name are retained.</li>
     * </ul>
     */
    void reset();

    /** Returns the ByteValue at a given address of this memory. Normally you would use {@link MemorySystem#allocateByte()} instead. */
    ByteValue getByte(int address);
    /** Returns the IntValue at a given address of this memory. Normally you would use {@link MemorySystem#allocateInt()} instead. */
    IntValue getInt(int address);
    /** Returns the DoubleValue at a given address of this memory. Normally you would use {@link MemorySystem#allocateDouble()} instead. */
    DoubleValue getDouble(int address);
    /** Returns the IntArrayValue at a given address of this memory. Normally you would use {@link MemorySystem#allocateIntArray(int)} instead. */
    IntArrayValue getIntArray(int address, int length);
    /** Returns the DoubleArrayValue at a given address of this memory. Normally you would use {@link MemorySystem#allocateDoubleArray(int)} instead. */
    DoubleArrayValue getDoubleArray(int address, int length);
    /** Returns the ByteArrayValue at a given address of this memory. Normally you would use {@link MemorySystem#allocateByteArray(int)} instead. */
    ByteArrayValue getByteArray(int address, int length);
    /** Returns the Pointer at a given address of memory. Normally you would use {@link MemorySystem#allocatePointer()} instead. */
    PointerValue getPointer(int address);

    /** Returns the total access time of this Memory. For a simple memory, this is simply access time * accessCount. */
    default long getTotalAccessTime() {
        return getAccessTime() * getAccessCount();
    }

}
