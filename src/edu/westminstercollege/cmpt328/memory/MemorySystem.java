package edu.westminstercollege.cmpt328.memory;

import edu.westminstercollege.cmpt328.memory.gui.MemoryStatisticsView;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * A class to simulate a memory manager in a program (e.g. what the "new" operator does in Java - allocates memory on
 * the stack of the heap). As a first-order approximation of how real programs work, this class creates a "heap" area
 * (starting at address 0 of the underlying memory) that grows towards increasing addresses and a "stack" area (starting
 * at the maximum address) that grows towards decreasing addresses. Single values &mdash; bytes, ints, doubles &mdash;
 * are allocated on the stack (simulating local variables) while arrays are allocated on the heap (simulating
 * dynamically allocated arrays as are typical in most languages). If the stack and heap areas intersect, then there
 * is no more memory available and a {@link MemoryExhaustedException} is thrown.
 *
 * This class further simulates actual memory systems by imposing an alignment (by default the size of a double).
 * Allocating single values smaller than the alignment (e.g. individual bytes) will thus "waste" memory, but arrays are
 * allocated contiguously regardless of the size of their elements.
 */
public class MemorySystem {

    /** A preconfigured {@link MemorySystem} representative of the Intel Core i7 series of processors. */
    public static class CoreI7 extends MemorySystem {

        /** RAM: full capacity ({@link Bits#NUM_ADDRESSES} bytes); 225-cycle access time */
        public final MainMemory RAM = new MainMemory("RAM", Bits.NUM_ADDRESSES, 225);

        /** L3 cache: 42-cycle access time, 16-way set associative with LRU, 8 MiB in size */
        public final Cache L3 = Cache.builder()
                .accessTime(42)
                .drawingFrom(RAM)
                .setAssociative(16, ReplacementAlgorithm.LRU)
                .lineCount(1 << 17)
                .name("L3")
                .build();
        /** L2 cache: 12-cycle access time, 4-way set associative with LRU, 256 kiB in size */
        public final Cache L2 = Cache.builder()
                .accessTime(12)
                .drawingFrom(L3)
                .setAssociative(4, ReplacementAlgorithm.LRU)
                .lineCount(1 << 12)
                .name("L2")
                .build();
        /** L1 cache: 4-cycle access time, 8-way set associative with LRU, 32 kiB in size */
        public final Cache L1 = Cache.builder()
                .accessTime(4)
                .drawingFrom(L2)
                .setAssociative(8, ReplacementAlgorithm.LRU)
                .lineCount(1 << 9)
                .name("L1")
                .build();

        public CoreI7() {
            super();
            setTop(L1);
        }
    }

    /** A preconfigured {@link MemorySystem} representative of the Intel Core i7 series of processors but much smaller
     * so that it is easier to see what is going on in each {@link Cache}. Each Cache is 1/32 the size of that in
     * {@link MemorySystem.CoreI7}.
     */
    public static class MicroCoreI7 extends MemorySystem {

        /** RAM: full capacity ({@link Bits#NUM_ADDRESSES} bytes); 225-cycle access time */
        public final MainMemory RAM = new MainMemory("RAM", 225);
        /** L3 cache: 42-cycle access time, 16-way set associative with LRU, 256 kiB in size */
        public final Cache L3 = Cache.builder()
                .accessTime(42)
                .drawingFrom(RAM)
                .setAssociative(16, ReplacementAlgorithm.LRU)
                .lineCount(1 << 12) // 4096 lines / 262,144 B / 32,768 doubles
                .name("L3")
                .build();
        /** L2 cache: 12-cycle access time, 4-way set associative with LRU, 8 kiB in size */
        public final Cache L2 = Cache.builder()
                .accessTime(12)
                .drawingFrom(L3)
                .setAssociative(4, ReplacementAlgorithm.LRU)
                .lineCount(1 << 7) // 128 lines / 8192 B / 1024 doubles
                .name("L2")
                .build();
        /** L1 cache: 4-cycle access time, 8-way set associative with LRU, 1 kiB in size */
        public final Cache L1 = Cache.builder()
                .accessTime(4)
                .drawingFrom(L2)
                .setAssociative(8, ReplacementAlgorithm.LRU)
                .lineCount(1 << 4) // 16 lines / 1024 B / 128 doubles
                .name("L1")
                .build();

        public MicroCoreI7() {
            super();
            setTop(L1);
        }
    }

    private Memory top;
    private Memory bottom;

    private int heapPtr, stackPtr;
    private final int alignment = Bits.DOUBLE_SIZE;
    private final int alignedByteSize = alignment,
            alignedIntSize = alignedSize(Bits.INT_SIZE),
            alignedDoubleSize = alignedSize(Bits.DOUBLE_SIZE),
            alignedPointerSize = alignedSize(Bits.POINTER_SIZE);

    private static MemoryStatisticsView view = null;

    private static MemorySystem def = null;

    /**
     * Creates a new memory system whose top level of memory (ordinarily the highest level of cache) is given.
     * @param top the highest memory level (e.g. the L1 cache).
     */
    public MemorySystem(Memory top) {
        setTop(top);
    }

    /**
     * Creates a new memory system as in {@link #MemorySystem(Memory)} and also makes it the default if <code>makeDefault</code>
     * is <code>true</code>.
     */
    public MemorySystem(Memory top, boolean makeDefault) {
        setTop(top);
        if (makeDefault)
            setDefault(this);
    }

    MemorySystem() {

    }

    void setTop(Memory top) {
        this.top = top;
        bottom = top;
        while (bottom instanceof Cache)
            bottom = ((Cache)bottom).getSource();
        heapPtr = 0;
        stackPtr = bottom.getSize();
    }

    /**
     * Sets the default memory system. Calls to {@link #getDefault()} will return the one given here.
     * @param system
     */
    public static void setDefault(MemorySystem system) {
        def = system;
    }

    /**
     * Returns the default memory system. Initially there isn't one; the default can be specified by calling {@link #setDefault(MemorySystem)}
     */
    public static MemorySystem getDefault() {
        if (def == null)
            throw new IllegalStateException("No memory system has been designated default");
        else
            return def;
    }

    /**
     * Allocates a single byte value in the "stack" area of memory. When this value is accessed the memory block it
     * belongs to will automatically be fetched into cache if needed.
     * @throws MemoryExhaustedException
     */
    public ByteValue allocateByte() throws MemoryExhaustedException {
        stackPtr -= alignedByteSize;
        checkMemoryExhausted();
        return top.getByte(stackPtr);
    }

    /**
     * Convenience method to allocate one byte of memory and also initialize it with a value. Equivalent to calling
     * {@link #allocateByte()} followed by {@link ByteValue#set(int)}.
     */
    public ByteValue allocateByte(int initValue) throws MemoryExhaustedException {
        ByteValue val = allocateByte();
        val.set(initValue);
        return val;
    }

    /**
     * Allocates a single int value in the "stack" area of memory. When this value is accessed the memory block it
     * belongs to will automatically be fetched into cache if needed.
     * @throws MemoryExhaustedException
     */
    public IntValue allocateInt() throws MemoryExhaustedException {
        stackPtr -= alignedIntSize;
        checkMemoryExhausted();
        return top.getInt(stackPtr);
    }

    /**
     * Convenience method to allocate one int of memory and also initialize it with a value. Equivalent to calling
     * {@link #allocateInt()} followed by {@link IntValue#set(int)}.
     */
    public IntValue allocateInt(int initValue) throws MemoryExhaustedException {
        IntValue val = allocateInt();
        val.set(initValue);
        return val;
    }

    /**
     * Allocates a single double value in the "stack" area of memory. When this value is accessed the memory block it
     * belongs to will automatically be fetched into cache if needed.
     * @throws MemoryExhaustedException
     */
    public DoubleValue allocateDouble() throws MemoryExhaustedException {
        stackPtr -= alignedDoubleSize;
        checkMemoryExhausted();
        return top.getDouble(stackPtr);
    }

    /**
     * Convenience method to allocate one double of memory and also initialize it with a value. Equivalent to calling
     * {@link #allocateDouble()} followed by {@link DoubleValue#set(double)}.
     */
    public DoubleValue allocateDouble(double initValue) throws MemoryExhaustedException {
        DoubleValue val = allocateDouble();
        val.set(initValue);
        return val;
    }

    /**
     * Allocates an array of bytes in the "heap" area of memory. When a value from this array is accessed the memory
     * block it belongs to will automatically be fetched into cache if needed.
     * @param length the number of elements in the array to allocate
     * @throws MemoryExhaustedException
     */
    public ByteArrayValue allocateByteArray(int length) throws MemoryExhaustedException {
        return alignHeap(top.getByteArray(heapPtr, length));
    }

    /**
     * Allocates an array of ints in the "heap" area of memory. When a value from this array is accessed the memory
     * block it belongs to will automatically be fetched into cache if needed.
     * @param length the number of elements in the array to allocate
     * @throws MemoryExhaustedException
     */
    public IntArrayValue allocateIntArray(int length) throws MemoryExhaustedException {
        return alignHeap(top.getIntArray(heapPtr, length));
    }

    /**
     * Allocates an array of doubles in the "heap" area of memory. When a value from this array is accessed the memory
     * block it belongs to will automatically be fetched into cache if needed.
     * @param length the number of elements in the array to allocate
     * @throws MemoryExhaustedException
     */
    public DoubleArrayValue allocateDoubleArray(int length) throws MemoryExhaustedException {
        return alignHeap(top.getDoubleArray(heapPtr, length));
    }

    /**
     * Allocates a pointer in the "stack" area of memory. When this value is accessed the memory block it
     * belongs to will automatically be fetched into cache if needed.
     * @throws MemoryExhaustedException
     */
    public PointerValue allocatePointer() throws MemoryExhaustedException {
        stackPtr -= alignedPointerSize;
        checkMemoryExhausted();
        return top.getPointer(stackPtr);
    }

    public StackFrame allocateStackFrame(int bytes) throws MemoryExhaustedException {
        bytes = alignedSize(bytes);
        stackPtr -= bytes;
        checkMemoryExhausted();
        StackFrame frame = new StackFrame(stackPtr, bytes);
        return frame;
    }

    public void deallocateStackFrame(StackFrame frame) {
        stackPtr += frame.getMemorySize();
    }

    public ByteValue getByteAt(int address) {
        return top.getByte(address);
    }

    public IntValue getIntAt(int address) {
        return top.getInt(address);
    }

    public DoubleValue getDoubleAt(int address) {
        return top.getDouble(address);
    }

    public ByteArrayValue getByteArrayAt(int address, int length) {
        return top.getByteArray(address, length);
    }

    public IntArrayValue getIntArrayAt(int address, int length) {
        return top.getIntArray(address, length);
    }

    public DoubleArrayValue getDoubleArrayAt(int address, int length) {
        return top.getDoubleArray(address, length);
    }

    public PointerValue getPointerAt(int address) {
        return top.getPointer(address);
    }

    /**
     * Returns the total access time across the entire system in cycles.
     */
    public long getTotalAccessTime() {
        return top.getTotalAccessTime();
    }

    /**
     * Resets this memory system, calling {@link Memory#reset()} on every memory it uses. This also resets the stack and
     * heap pointers, meaning any allocations already done will no longer be valid.
     */
    public void resetAll() {
        resetMemories();
        heapPtr = 0;
        stackPtr = bottom.getSize();
    }

    /**
     * Resets each memory, calling {@link Memory#reset()} on every memory it uses. This method does <em>not</em> reset
     * allocations so any allocations already done will still be valid.
     */
    public void resetMemories() {
        Memory m = top;
        while (m != null) {
            m.reset();
            m = (m instanceof Cache) ? ((Cache)m).getSource() : null;
        }
    }

    /**
     * Prints a table of statistical information on the use of this memory system, including the hit ratio and total
     * access time at each level of memory.
     */
    public void printStatistics(PrintWriter out) {
        // First, iterate over all the memories, finding what the largest per-level access time is (so that we know how
        // wide to make the nice chart)
        long maxAccessTime = 1;
        int maxNameLength = 0;
        Memory m = top;
        while (m != null) {
            maxNameLength = Math.max(maxNameLength, m.getName().length());
            long thisLevelAccessTime;
            if (m instanceof Cache) {
                Cache c = (Cache)m;
                thisLevelAccessTime = c.getThisLevelAccessTime();
                m = c.getSource();
            } else {
                thisLevelAccessTime = m.getTotalAccessTime();
                m = null;
            }

            maxAccessTime = Math.max(maxAccessTime, thisLevelAccessTime);
        }

        // Now make the table!
        final int totalTableWidth = 120;
        final int nameColWidth = Math.max("Memory".length(), maxNameLength) + 1;
        final int hitsAccessesColWidth = 2 * 8 + 1;
        final int hitRatioColWidth = 8;
        final int accessTimeWidth = 8 + 1;
        final int accessTimeBarWidth = totalTableWidth - nameColWidth - hitsAccessesColWidth - hitRatioColWidth
                - accessTimeWidth - 4 * 2;
        final int totalAccessTimeColWidth = accessTimeWidth + accessTimeBarWidth;
        final double charsPerAccessTime = ((double)accessTimeBarWidth) / maxAccessTime;

        final String rowFormat = "%-" + nameColWidth + "s│ %-" + hitsAccessesColWidth + "s│ %-" + hitRatioColWidth + "s│ %-"
                + totalAccessTimeColWidth + "s\n";
        out.printf(rowFormat,
                "Memory", "Hits/accesses", "Hit %", "Total access time (cycles)");
        out.printf("%s┼%s┼%s┼%s\n",
                repeat("─", nameColWidth),
                repeat("─", hitsAccessesColWidth + 1),
                repeat("─", hitRatioColWidth + 1),
                repeat("─", totalAccessTimeColWidth + 1));

        m = top;
        while (m != null) {
            String name = m.getName();

            if (m instanceof Cache) {
                Cache c = (Cache)m;
                String hitsAccesses = String.format("%s/%s",
                        scientificNotation(c.getHitCount()),
                        scientificNotation(c.getAccessCount()));
                String hitRatio = String.format("%.2f%%", 100.0 * c.getHitCount() / c.getAccessCount());
                String accessTime = String.format("%-8s %s",
                        scientificNotation(c.getThisLevelAccessTime()),
                        repeat("█", (int)Math.round(charsPerAccessTime * c.getThisLevelAccessTime())));
                out.printf(rowFormat,
                        name, hitsAccesses, hitRatio, accessTime);

                m = c.getSource();
            } else {
                String accesses = String.format("—/%s", scientificNotation(m.getAccessCount()));
                String accessTime = String.format("%-8s %s",
                        scientificNotation(m.getTotalAccessTime()),
                        repeat("█", (int)Math.round(charsPerAccessTime * m.getTotalAccessTime())));
                out.printf(rowFormat,
                        name, accesses, "—", accessTime);
                m = null;
            }
        }

        out.flush();
    }

    /**
     * Prints statistics (as in {@link #printStatistics(PrintWriter)} to {@link java.lang.System#out}.
     */
    public void printStatistics() {
        PrintWriter out = new PrintWriter(System.out);
        printStatistics(out);
        out.flush();
    }

    /**
     * Displays a window showing statistical information on the use of this memory system, including the hit ratio and
     * total access time at each level of memory.
     */
    public void viewStatistics() {
        if (view == null)
            view = new MemoryStatisticsView();
        view.showWindow(top);
    }

    private String scientificNotation(long n) {
        if (n < 0)
            return "-" + scientificNotation(-n);
        if (n < 1000000)
            return NumberFormat.getNumberInstance().format(n);
        else {
            int pow = (int)Math.floor(Math.log10(n));
            double order = Math.pow(10.0, pow);
            return String.format("%-3.1f×10%s", n / order, intToSuperscript(pow));
        }
    }

    private String intToSuperscript(int x) {
        String s = "" + x;
        String out = "";
        for (int i = 0; i < s.length(); ++i)
            out += superscript(s.charAt(i));
        return out;
    }

    private char superscript(char digit) {
        switch (digit) {
            case '1': return '¹';
            case '2': return '²';
            case '3': return '³';
            default: return (char)('⁰' + (digit - '0'));
        }
    }

    private String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; ++i)
            sb.append(s);
        return sb.toString();
    }

    private void checkMemoryExhausted() {
        if (stackPtr < heapPtr)
            throw new IllegalStateException("Memory of " + bottom.getName() + " exhausted!");
    }

    private <T extends MemoryValue> T alignHeap(T v) {
        heapPtr += alignedSize(v.getMemorySize());
        checkMemoryExhausted();
        return v;
    }

    private int alignedSize(int bytes) {
        return (int)Math.ceil((float)bytes / alignment) * alignment;
    }
}
