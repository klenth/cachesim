package edu.westminstercollege.cmpt328.memory;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A class representing a CPU cache of memory. A cache is itself a {@link Memory}, meaning that it can hold
 * {@link MemoryValue}s, but it always obtains these from an underlying source memory (which may be a Cache itself).
 *
 * To make a Cache, first create the memory that it will sit upon (a {@link MainMemory} or another Cache). Then call
 * {@link Cache#builder()} to create a {@link Cache.Builder} object which is used to configure the cache.
 */
public class Cache implements Memory {

    private static final int ALL_LINES = 0;
    private static int CACHE_COUNT = 0;

    private final int cacheNumber;
    private final String name;
    private final Memory source;
    private final int accessTime;
    private final CacheLine[] lines;
    private final int linesPerSet;
    private final ReplacementAlgorithm replacementAlgorithm;
    private long hits, misses;
    private Random random = new Random();

    private final int offsetBits, setBits;

    /**
     * A class that is used to configure and create a {@link Cache}. A builder is obtained from {@link Cache#builder()} or
     * {@link Cache#cloneBuilder()}; methods of the Builder are used to specify parameter of the {@link Cache} to create.
     *
     * <p>Example:</p>
     * <pre><code>Cache c = Cache.builder()
     *     .name("Cache")
     *     .drawingFrom(someMemory)
     *     .lineCount(256)
     *     .accessTime(10)
     *     .directMapping()
     *     .build();</code></pre>
     *
     * <p>When using a builder, it is mandatory to call at least the following methods (but order does not matter):</p>
     * <ul>
     *     <li>{@link #drawingFrom(Memory)} &mdash; the memory this cache sits atop</li>
     *     <li>{@link #accessTime(int)} &mdash; access time for this cache in cycles</li>
     *     <li>{@link #lineCount(int)} &mdash; number of lines (each of size {@link Bits#LINE_SIZE})</li>
     *     <li>One of {@link #directMapping()}, {@link #fullyAssociative(ReplacementAlgorithm)}, or
     *         {@link #setAssociative(int, ReplacementAlgorithm)} &mdash; how cache lines are mapped</li>
     *     <li>{@link #build()} &mdash; called at the end to create the {@link Cache} object as configured</li>
     * </ul>
     */
    public static final class Builder {

        private Memory source = null;
        private int accessTime = -1;
        private int lineCount = -1;
        private int linesPerSet = -1;
        private ReplacementAlgorithm replacementAlgorithm;
        private String name;

        private Builder() {}

        private Builder(Cache source) {
            this.accessTime = accessTime;
            this.lineCount = source.getLineCount();
            this.linesPerSet = source.linesPerSet;
            this.replacementAlgorithm = source.replacementAlgorithm;
        }

        /** Specifies which {@link Memory} the created {@link Cache} will draw from; cannot be null */
        public Builder drawingFrom(Memory source) {
            if (source == null)
                throw new IllegalArgumentException("Source cannot be null");
            this.source = source;
            return this;
        }

        /** Specifies the access time of the created {@link Cache} in cycles; cannot be negative */
        public Builder accessTime(int accessTime) {
            if (accessTime < 0)
                throw new IllegalArgumentException("Access time cannot be negative");
            this.accessTime = accessTime;
            return this;
        }

        /** Specifies the number of lines (each of size {@link Bits#LINE_SIZE}); must be a power of 2 greater than 1 */
        public Builder lineCount(int lineCount) {
            if (!Bits.isPowerOf2(lineCount))
                throw new IllegalArgumentException("Line count must be a power of 2");
            this.lineCount = lineCount;
            return this;
        }

        /** Specifies that the created {@link Cache} should use direct mapping */
        public Builder directMapping() {
            this.linesPerSet = 1;
            this.replacementAlgorithm = null;
            return this;
        }

        /** Specifies that the created {@link Cache} should use fully-associative mapping; a {@link ReplacementAlgorithm}
         * must also be specified */
        public Builder fullyAssociative(ReplacementAlgorithm replacementAlgorithm) {
            if (replacementAlgorithm == null)
                throw new IllegalArgumentException("Replacement algorithm cannot be null");
            this.linesPerSet = ALL_LINES;
            this.replacementAlgorithm = replacementAlgorithm;
            return this;
        }

        /** Specifies that the created {@link Cache} should use set-associative mapping; a {@link ReplacementAlgorithm}
         * must also be specified */
        public Builder setAssociative(int linesPerSet, ReplacementAlgorithm replacementAlgorithm) {
            if (!Bits.isPowerOf2(linesPerSet))
                throw new IllegalArgumentException("Lines per set must be a power of 2");
            if (replacementAlgorithm == null)
                throw new IllegalArgumentException("Replacement algorithm cannot be null");
            this.linesPerSet = linesPerSet;
            this.replacementAlgorithm = replacementAlgorithm;
            return this;
        }

        /** Specifies the name of the created {@link Cache} (optional) */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** Returns the {@link Cache} as configured by calls to the other methods. Throws {@link java.lang.IllegalStateException}
         * if any configuration is incomplete or incorrect
         */
        public Cache build() {
            if (source == null)
                throw new IllegalStateException("Source has not been specified");
            if (accessTime < 0)
                throw new IllegalStateException("Access time has not been specified");
            if (lineCount < 0)
                throw new IllegalStateException("Line count has not been specified");
            if (linesPerSet < 0)
                throw new IllegalStateException("Must specify direct mapping, fully associative, or set associative");
            if (linesPerSet != 1 && replacementAlgorithm == null)
                throw new IllegalStateException("Replacement algorithm must be specified for fully/set associative caches");
            return new Cache(this);
        }
    }

    private Cache(Builder b) {
        this.cacheNumber = CACHE_COUNT++;
        this.source = b.source;
        this.accessTime = b.accessTime;
        this.lines = new CacheLine[b.lineCount];
        for (int i = 0; i < lines.length; ++i)
            this.lines[i] = CacheLine.unmapped();
        this.linesPerSet = (b.linesPerSet == ALL_LINES) ? this.lines.length : b.linesPerSet;
        this.replacementAlgorithm = b.replacementAlgorithm;
        this.name = b.name;
        offsetBits = Bits.log2(getLineSize());
        setBits = Bits.log2(getSetCount());
    }

    /** Returns the {@link Memory} that this Cache draws from */
    public Memory getSource() {
        return source;
    }

    @Override
    public int getAccessTime() {
        return accessTime;
    }

    /**
     * Returns a {@link Builder} object that can be used to configure and build a new Cache.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a {@link Builder} object that can be used to configure and build a new cache from the configuration of
     * this Cache. The returned {@link Builder} will come pre-configured with the access time, line count, mapping
     * function, and replacement algorithm of this Cache but will not copy the name or {@link Memory} source.
     */
    public Builder cloneBuilder() {
        return new Builder(this);
    }

    /** Returns the size of this Cache, i.e. line count * line size */
    @Override
    public int getSize() {
        return getLineCount() * getLineSize();
    }

    @Override
    public String getName() {
        return (name == null)
                ? String.format("Cache #%d", cacheNumber)
                : name;
    }

    /** Returns the number of lines in this cache */
    public int getLineCount() {
        return lines.length;
    }

    /** Returns the number of lines per set in this Cache:
     * <ul>
     *     <li>if this Cache uses direct mapping, returns 1;</li>
     *     <li>if this Cache uses fully-associative mapping, returns {@link #getLineCount()};</li>
     *     <li>if this Cache uses set-associative mapping, returns the lines per set as configured.</li>
     * </ul>
     * @return
     */
    public int getLinesPerSet() {
        return linesPerSet;
    }

    /** Returns the number of sets in this Cache, i.e. line count / lines per set */
    public int getSetCount() {
        return (getLineCount() / getLinesPerSet());
    }

    /** Returns the size of a single line (i.e. {@link Bits#LINE_SIZE} */
    public int getLineSize() {
        return Bits.LINE_SIZE;
    }

    /** Returns the number of bits in the offset field of an address in this cache */
    public int getOffsetBits() {
        return offsetBits;
    }

    /** Returns true if this cache uses direct mapping */
    public boolean isDirect() {
        return linesPerSet == 1;
    }

    /** Returns true if this cache uses fully-associative mapping */
    public boolean isFullyAssociative() {
        return linesPerSet == lines.length;
    }

    /** Returns true if this cache uses set-associative mapping */
    public boolean isSetAssociative() {
        return !isDirect() && !isFullyAssociative();
    }

    @Override
    public ByteValue getByte(final int address) {
        return new ByteValue() {
            @Override
            public int get() {
                CacheAddress addr = access(address, 1);
                return lines[addr.line].getData().getByteAt(addr.offset);
            }

            @Override
            public void set(int value) {
                CacheAddress addr = access(address, 1);
                lines[addr.line].getData().setByteAt(addr.offset, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public IntValue getInt(final int address) {
        return new IntValue() {
            @Override
            public int get() {
                CacheAddress addr = access(address, Bits.INT_SIZE);
                return lines[addr.line].getData().getIntAt(addr.offset);
            }

            @Override
            public void set(int value) {
                CacheAddress addr = access(address, Bits.INT_SIZE);
                lines[addr.line].getData().setIntAt(addr.offset, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public DoubleValue getDouble(final int address) {
        return new DoubleValue() {
            @Override
            public double get() {
                CacheAddress addr = access(address, Bits.DOUBLE_SIZE);
                return lines[addr.line].getData().getDoubleAt(addr.offset);
            }

            @Override
            public void set(double value) {
                CacheAddress addr = access(address, Bits.DOUBLE_SIZE);
                lines[addr.line].getData().setDoubleAt(addr.offset, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public ByteArrayValue getByteArray(final int address, final int length) {
        return new ByteArrayValue() {
            @Override
            public int getLength() {
                return length;
            }

            @Override
            public int get(int i) {
                CacheAddress addr = access(address + i, 1);
                return lines[addr.line].getData().getByteAt(addr.offset);
            }

            @Override
            public void set(int i, int value) {
                CacheAddress addr = access(address + i, 1);
                lines[addr.line].getData().setByteAt(addr.offset, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public IntArrayValue getIntArray(final int address, final int length) {
        return new IntArrayValue() {
            @Override
            public int getLength() {
                return length;
            }

            @Override
            public int get(int i) {
                CacheAddress addr = access(address + i * Bits.INT_SIZE, Bits.INT_SIZE);
                return lines[addr.line].getData().getIntAt(addr.offset);
            }

            @Override
            public void set(int i, int value) {
                CacheAddress addr = access(address + i * Bits.INT_SIZE, Bits.INT_SIZE);
                lines[addr.line].getData().setIntAt(addr.offset, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public DoubleArrayValue getDoubleArray(final int address, final int length) {
        return new DoubleArrayValue() {
            @Override
            public int getLength() {
                return length;
            }

            @Override
            public double get(int i) {
                CacheAddress addr = access(address + i * Bits.DOUBLE_SIZE, Bits.DOUBLE_SIZE);
                return lines[addr.line].getData().getDoubleAt(addr.offset);
            }

            @Override
            public void set(int i, double value) {
                CacheAddress addr = access(address + i * Bits.DOUBLE_SIZE, Bits.DOUBLE_SIZE);
                lines[addr.line].getData().setDoubleAt(addr.offset, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    @Override
    public PointerValue getPointer(final int address) {
        return new PointerValue() {
            @Override
            public int get() {
                CacheAddress addr = access(address, Bits.POINTER_SIZE);
                return lines[addr.line].getData().getPointerAt(addr.offset);
            }

            @Override
            public void set(int value) {
                CacheAddress addr = access(address, Bits.POINTER_SIZE);
                lines[addr.line].getData().setPointerAt(addr.offset, value);
            }

            @Override
            public int getAddress() {
                return address;
            }
        };
    }

    private int offset(int address) {
        return address & Bits.ones(offsetBits);
    }

    private int setNumber(int address) {
        return (address >> offsetBits) & Bits.ones(setBits);
    }

    private int tag(int address) {
        return address >> (offsetBits + setBits);
    }

    private int addressOfLine(int lineNumber) {
        CacheLine line = lines[lineNumber];
        if (!line.isMapped())
            throw new IllegalStateException("Line not mapped");
        int tag = lines[lineNumber].getTag();
        return (tag << (offsetBits + setBits))
                | ((lineNumber / linesPerSet) << offsetBits);
    }

    private CacheAddress access(int address, int bytes) {
        // Offset within line
        int lineOffset = offset(address);
        if (lineOffset + bytes > getLineSize())
            throw new IllegalStateException("Value spanning blocks");

        // Check whether one of our existing lines contains this address
        int setNumber = setNumber(address);
        int firstLineOfSet = getLinesPerSet() * setNumber;
        int addrTag = tag(address);
        for (int i = firstLineOfSet; i < firstLineOfSet + getLinesPerSet(); ++i) {
            if (lines[i].isMapped() && lines[i].getTag() == addrTag) {
                // Found it!
                ++hits;
                lineAccessed(setNumber, firstLineOfSet, i);
                return new CacheAddress(i, lineOffset);
            }
        }

        // We're going to have to load it - ask the mapping which line it should go into
        //System.out.printf("\n%s miss (address=0x%06x, offset=0x%02x, set=%d, tag=0x%x)\n",
        //        name, address, lineOffset, setNumber, addrTag);
        //print();
        //System.out.flush();
        ++misses;
        int line = evictLine(setNumber, firstLineOfSet);

        fetchLine(setNumber, firstLineOfSet, line, addrTag, address & ~Bits.ones(offsetBits));
        lineAccessed(setNumber, firstLineOfSet, line);

        //System.out.printf("\n%s resolved miss (address=0x%06x)\n", name, address);
        //print();
        //System.out.flush();

        return new CacheAddress(line, lineOffset);
    }

    private List<CacheLine> set(int setNumber) {
        return Arrays.asList(lines)
                .subList(setNumber * linesPerSet, (setNumber + 1) * linesPerSet);
    }

    private void lineAccessed(int setNumber, int firstLineOfSet, int lineAccessed) {
        if (!isDirect())
            replacementAlgorithm.lineAccessed(set(setNumber), lineAccessed - firstLineOfSet);
    }

    // Must return a line number in [firstLineOfSet, firstLineOfSet + getLinesPerSet())
    private int evictLine(int setNumber, int firstLineOfSet) {
        // 1) Choose line to evict
        // Are there any unmapped lines?
        for (int i = firstLineOfSet; i < firstLineOfSet + getLinesPerSet(); ++i)
            if (!lines[i].isMapped())
                return i;

        int lineToEvict = -1;
        if (isDirect())
            // No choice
            lineToEvict = firstLineOfSet;
        else
            lineToEvict = firstLineOfSet + replacementAlgorithm.chooseLineToEvict(set(setNumber));

        // 2) Writeback if needed
        if (lines[lineToEvict].isDirty()) {
            int blockNumber = (lines[lineToEvict].getTag() << setBits) | setNumber;
            // DEBUG
            int testAddr = blockNumber * Bits.BLOCK_SIZE;
            if (tag(testAddr) != lines[lineToEvict].getTag()
                    || setNumber(testAddr) != setNumber)
                System.out.println("Miscalculated block number!");
            source.writeback(lines[lineToEvict].getData(), blockNumber);
            //if (!source.writeback(lines[lineToEvict].getData(), blockNumber))
            //    System.out.println("Cache miss on writeback!");
            lines[lineToEvict].clean();
        }

        // 3) Evict it
        lines[lineToEvict].evict();

        // Return line number
        return lineToEvict;
    }

    private void fetchLine(int setNumber, int firstLineOfSet, int lineNumber, int tag, int baseAddress) {
        lines[lineNumber] = CacheLine.map(tag);
        source.fetch(lines[lineNumber].getData(), baseAddress >> offsetBits);

        if (!isDirect())
            replacementAlgorithm.lineLoaded(set(setNumber), lineNumber - firstLineOfSet);
    }

    /** Returns the total number of hits that this Cache has recorded. The {@link #reset()} method resets this counter. */
    public long getHitCount() {
        return hits;
    }

    /** Returns the total number of misses that this Cache has recorded. The {@link #reset()} method resets this counter. */
    public long getMissCount() {
        return misses;
    }

    /** Returns the total number of accesses (hits + misses) that this Cache has recorded. The {@link #reset()} method resets this counter. */
    @Override
    public long getAccessCount() {
        return hits + misses;
    }

    /** Returns the <em>total access time of all requests that have passed through this cache</em>, including the misses,
     * all the way down to main memory.
     */
    @Override
    public long getTotalAccessTime() {
        return getThisLevelAccessTime() + source.getTotalAccessTime();
    }

    /**
     * Resets this cache to its initial state. After the reset,
     * <ul>
     *     <li>hit, miss, and access count will be 0;</li>
     *     <li>every line of the Cache will be vacant.</li>
     * </ul>
     * <strong>This method immediately resets the cache. It does <em>not</em> write-back any dirty lines!</strong>
     */
    @Override
    public void reset() {
        hits = misses = 0;
        for (int i = 0; i < lines.length; ++i)
            lines[i] = CacheLine.unmapped();
    }

    @Override
    public boolean writeback(ByteStore data, int block) {
        long oldMisses = misses, oldHits = hits;
        CacheAddress addr = access(block * Bits.BLOCK_SIZE, Bits.BLOCK_SIZE);
        System.arraycopy(data.data, 0, lines[addr.line].getData().data, 0, Bits.BLOCK_SIZE);
        lines[addr.line].dirty();
        return misses == oldMisses && hits == oldHits + 1;
    }

    @Override
    public void fetch(ByteStore data, int block) {
        CacheAddress addr = access(block * Bits.BLOCK_SIZE, Bits.BLOCK_SIZE);
        System.arraycopy(lines[addr.line].getData().data, 0, data.data, 0, Bits.BLOCK_SIZE);
    }

    /** In contrast to {@link #getTotalAccessTime()}, this method returns the total amount of access time at this level
     * of cache only (i.e., number of hits * access time). */
    public long getThisLevelAccessTime() {
        return hits * accessTime;
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; ++i)
            sb.append(s);
        return sb.toString();
    }

    private String dataToString(ByteStore bytes, int off, int len, int bytesPerGroup) {
        StringBuilder sb = new StringBuilder(3 * len);
        for (int i = 0; i < len; ) {
            for (int k = 0; k < bytesPerGroup && i < len; ++k, ++i)
                sb.append(String.format("%02x", bytes.getByteAt(off + i)));
            if (i % bytesPerGroup == 0)
                sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Prints out the current status of the Cache in a nice table to the given {@link java.io.PrintWriter}. The columns of the table are
     * <ul>
     *     <li><code>#</code> &mdash; the line number (0 to {@link #getLineCount()} - 1);</li>
     *     <li><code>Tag</code> &mdash; the line's current tag if present;</li>
     *     <li><code>Data</code> &mdash; the current data contents of the line if present;</li>
     *     <li><code>F</code> &mdash; flags for the cache line; <code>P</code> for present, <code>V</code> for vacant,
     *         <code>D</code> for dirty (has been modified since being fetched);</li>
     *     <li><code>Meta</code> &mdash; the line's "meta" field, used and updated by the {@link ReplacementAlgorithm};
     *         normally the line with the smallest meta value in its set is replaced first.</li>
     * </ul>
     * <p>Single-stroke lines (<code>──────</code>) separate cache lines in the same set while double-stroke lines
     *    (<code>══════</code>) separate cache lines in different sets.</p>
     */
    public void print(PrintWriter out) {
        // 24-bit mem addrs -> 6 hex digits
        // 64-byte blocks -> 128 hex digits
        //    (4 rows of 32 in groups of 8 -> 36 per row)
        final int BYTES_PER_ROW = 16;
        final int LINE_NUMBER_COL_WIDTH = 8,
                  TAG_COL_WIDTH = 12,
                  DATA_COL_WIDTH = 40,
                  FLAGS_COL_WIDTH = 4,
                  META_COL_WIDTH = 8,
                  TABLE_WIDTH = LINE_NUMBER_COL_WIDTH + TAG_COL_WIDTH + DATA_COL_WIDTH + FLAGS_COL_WIDTH + META_COL_WIDTH;
        final String lineDivider = repeat("─", TABLE_WIDTH),
                     setDivider = repeat("═", TABLE_WIDTH);
        final String rowFmt = String.format("%%-%ds%%-%ds%%-%ds%%-%ds%%-%ds\n",
            LINE_NUMBER_COL_WIDTH, TAG_COL_WIDTH, DATA_COL_WIDTH, FLAGS_COL_WIDTH, META_COL_WIDTH);
        final String tagFmt = "0x%0" + (int)Math.ceil((Bits.ADDRESS_SIZE - offsetBits - setBits) / 4.0) + "x";
        out.printf("%-" + LINE_NUMBER_COL_WIDTH + "s%-" + TAG_COL_WIDTH + "s%-" + DATA_COL_WIDTH + "s%-" + FLAGS_COL_WIDTH + "s%-" + META_COL_WIDTH + "s\n",
                "#", "Tag", "Data", "F", "Meta");
        for (int i = 0; i < lines.length; ++i) {
            if (i % getLinesPerSet() == 0)
                out.println(setDivider);
            else
                out.println(lineDivider);

            CacheLine line = lines[i];
            String lineNumber = "" + i;
            if (line.isMapped()) {
                String tag = String.format(tagFmt, line.getTag());
                String data = dataToString(line.getData(), 0, BYTES_PER_ROW, 4);
                String flags = "" + (line.isPresent() ? "P" : "V") + (line.isDirty() ? "D" : "");
                String meta = "" + line.getMeta();
                out.printf(rowFmt, lineNumber, tag, data, flags, meta);
                for (int off = BYTES_PER_ROW; off < getLineSize(); off += BYTES_PER_ROW) {
                    data = dataToString(line.getData(), off, BYTES_PER_ROW, 4);
                    out.printf(rowFmt, "", "", data, "", "");
                }
            } else {
                out.printf(rowFmt, lineNumber, "—", "", "", "");
            }
        }
        out.println(setDivider);
        out.flush();
    }

    /** Calls {@link #print(PrintWriter)}, printing to {@link java.lang.System#out}. */
    public void print() {
        print(new PrintWriter(new OutputStreamWriter(System.out)));
    }
}
