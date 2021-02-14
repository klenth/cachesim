package edu.westminstercollege.cmpt328.memory;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * A class representing the main memory (i.e. RAM) of a computer. This class implements {@link Memory}, so it is possible
 * to obtain mapped memory values (such as {@link IntValue}) from it, but normally one does this using one of the
 * <code>allocate</code> methods of {@link MemorySystem} instead.
 *
 * The default size of a {@link MainMemory} is {@link Bits#NUM_ADDRESSES} bytes, the largest possible in this simulated
 * system (currently 16 MiB).
 */
public class MainMemory implements Memory {

    private static int MAIN_MEMORY_COUNT = 0;

    private final int accessTime;
    private final ByteStore data;
    private int mainMemoryNumber;
    private String name;
    private long accessCount;

    /**
     * Creates a MainMemory with the given name, size in bytes, and access time in cycles
     */
    public MainMemory(String name, int size, int accessTime) {
        this(size, accessTime);
        this.name = name;
    }

    /**
     * Creates a MainMemory with the given size in bytes and access time in cycles
     */
    public MainMemory(int size, int accessTime) {
        mainMemoryNumber = MAIN_MEMORY_COUNT++;
        if (size > Bits.MAX_ADDRESS + 1)
            throw new IllegalArgumentException("Size too large (cannot be more than " + (Bits.MAX_ADDRESS + 1));
        data = new ByteStore(size);
        this.accessTime = accessTime;
    }

    /**
     * Creates a MainMemory with the given name and access time in cycles. The default size is used (full capacity).
     */
    public MainMemory(String name, int accessTime) {
        this(name, Bits.NUM_ADDRESSES, accessTime);
    }

    /**
     * Creates a MainMemory with the given access time in cycles. The default size is used (full capacity).
     */
    public MainMemory(int accessTime) {
        this(Bits.NUM_ADDRESSES, accessTime);
    }

    @Override
    public String getName() {
        return (name == null)
                ? String.format("Main memory #%d", mainMemoryNumber)
                : name;
    }

    private void checkAddressRange(int base, int bytes) {
        checkAddress(base);
        checkAddress(base + bytes - 1);
    }

    private void checkAddress(int address) {
        if (address < 0 || address >= data.getSize())
            throw new IllegalArgumentException(String.format("Bad memory address %d in memory of size %d", address, data.getSize()));
    }

    @Override
    public int getAccessTime() {
        return accessTime;
    }

    @Override
    public int getSize() {
        return data.getSize();
    }

    @Override
    public void reset() {
        data.clear();
        accessCount = 0;
    }

    @Override
    public ByteValue getByte(final int address) {
        checkAddress(address);

        return new ByteValue() {
            @Override
            public int get() {
                ++accessCount;
                return data.getByteAt(address);
            }

            @Override
            public void set(int value) {
                ++accessCount;
                data.setByteAt(address, value);
            }

            @Override
            public int getMemorySize() {
                return 1;
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public IntValue getInt(final int address) {
        checkAddressRange(address, Bits.INT_SIZE);

        return new IntValue() {
            @Override
            public int get() {
                ++accessCount;
                return data.getIntAt(address);
            }

            @Override
            public int getMemorySize() {
                return Bits.INT_SIZE;
            }

            @Override
            public void set(int value) {
                ++accessCount;
                data.setIntAt(address, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public DoubleValue getDouble(final int address) {
        checkAddressRange(address, Bits.DOUBLE_SIZE);

        return new DoubleValue() {

            @Override
            public double get() {
                ++accessCount;
                return data.getDoubleAt(address);
            }

            @Override
            public int getMemorySize() {
                return Bits.DOUBLE_SIZE;
            }

            @Override
            public void set(double value) {
                ++accessCount;
                data.setDoubleAt(address, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public ByteArrayValue getByteArray(final int address, final int length) {
        checkAddressRange(address, length);

        return new ByteArrayValue() {
            @Override
            public int getLength() {
                return length;
            }

            @Override
            public int get(int i) {
                ++accessCount;
                return data.getByteAt(address + i);
            }

            @Override
            public void set(int i, int value) {
                ++accessCount;
                data.setByteAt(address + i, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public IntArrayValue getIntArray(final int address, final int length) {
        checkAddressRange(address, length * Bits.INT_SIZE);

        return new IntArrayValue() {

            @Override
            public int getLength() {
                return length;
            }

            @Override
            public int get(int i) {
                ++accessCount;
                return data.getIntAt(address + i * Bits.INT_SIZE);
            }

            @Override
            public void set(int i, int value) {
                ++accessCount;
                data.setIntAt(address + i * Bits.INT_SIZE, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public DoubleArrayValue getDoubleArray(final int address, final int length) {
        checkAddressRange(address, length * Bits.DOUBLE_SIZE);

        return new DoubleArrayValue() {

            @Override
            public int getLength() {
                return length;
            }

            @Override
            public double get(int i) {
                ++accessCount;
                return data.getDoubleAt(address + i * Bits.DOUBLE_SIZE);
            }

            @Override
            public void set(int i, double value) {
                ++accessCount;
                data.setDoubleAt(address + i * Bits.DOUBLE_SIZE, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public PointerValue getPointer(int address) {

        checkAddressRange(address, Bits.POINTER_SIZE);
        return new PointerValue() {
            @Override
            public int get() {
                ++accessCount;
                return data.getPointerAt(address);
            }

            @Override
            public int getMemorySize() {
                return Bits.POINTER_SIZE;
            }

            @Override
            public void set(int value) {
                ++accessCount;
                data.setPointerAt(address, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public long getAccessCount() {
        return accessCount;
    }

    @Override
    public boolean writeback(ByteStore data, int block) {
        ++accessCount;
        System.arraycopy(data.data, 0, this.data.data, block * Bits.BLOCK_SIZE, Bits.BLOCK_SIZE);
        return true;
    }

    @Override
    public void fetch(ByteStore data, int block) {
        ++accessCount;
        System.arraycopy(this.data.data, block * Bits.BLOCK_SIZE, data.data, 0, Bits.BLOCK_SIZE);
    }

    /** Prints the complete contents of memory to {@link java.lang.System#out}.
     * This can produce a lot of output when the memory is large! */
    public void printContents() {
        printContents(new PrintWriter(new OutputStreamWriter(System.out)));
    }

    /** Prints the complete contents of memory to the given {@link java.io.PrintWriter}.
     * This can produce a lot of output when the memory is large! */
    public void printContents(PrintWriter w) {
        final int bytesPerLine = 16;
        for (int i = 0; i < getSize(); ++i) {
            if (i % bytesPerLine == 0)
                w.printf("%06x  │", i);
            w.printf("  %02x", getByte(i).get());
            if ((i + 1) % bytesPerLine == 0)
                w.println();
        }
        w.println();
        w.flush();
    }
}