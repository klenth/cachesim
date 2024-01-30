package edu.westminsteru.cmpt328.memory;

import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

/**
 * An enumeration describing common replacement algorithms for associative/set-associative caches. Each of these
 * algorithms use and update the "meta" field of cache lines (displayed by {@link Cache#print(PrintWriter)}) to decide
 * which line to evict.
 */
public enum ReplacementAlgorithm {

    /**
     * The LRU (least recently used) algorithm &mdash; the line that was accessed longest ago is evicted
     */
    LRU {
        @Override
        void lineAccessed(List<CacheLine> set, int accessedIndex) {
            CacheLine accessedLine = set.get(accessedIndex);
            if (accessedLine.getMeta() + 1 == set.size())
                // This line was already most recently accessed
                return;
            long oldMeta = accessedLine.getMeta();
            for (CacheLine line : set) {
                if (!line.isMapped())
                    continue;
                if (line == accessedLine)
                    line.setMeta(set.size() - 1);
                else if (line.getMeta() > oldMeta)
                    line.setMeta(line.getMeta() - 1);
            }
        }
    },

    /**
     * FIFO (first in, first out) algorithm &mdash; the line that was loaded from memory longest ago is evicted
     */
    FIFO {
        @Override
        void lineLoaded(List<CacheLine> set, int fetchedIndex) {
            CacheLine loaded = set.get(fetchedIndex);
            for (CacheLine line : set) {
                if (line == loaded)
                    line.setMeta(set.size() - 1);
                else if (line.isMapped())
                    line.setMeta(line.getMeta() - 1);
            }
        }
    },

    /**
     * LFU (least frequently used) algorithm &mdash; the line that has been accessed the fewest times is evicted
     */
    LFU {
        @Override
        void lineAccessed(List<CacheLine> set, int accessedIndex) {
            CacheLine accessed = set.get(accessedIndex);
            accessed.setMeta(accessed.getMeta() + 1);
        }
    },

    /**
     * Random algorithm &mdash; the line to be evicted is chosen at random
     */
    RANDOM {
        private final Random random = new Random();

        @Override
        int chooseLineToEvict(List<CacheLine> set) {
            return random.nextInt(set.size());
        }
    };

    /**
     * The default replacement algorithm (currently LRU).
     */
    public static final ReplacementAlgorithm DEFAULT = LRU;

    /**
     * Called when a line is loaded into cache so that line metas may be updated as appropriate.
     * (Default version: does nothing)
     * @param set the lines in this set
     * @param fetchedIndex the index in set of the line that was loaded
     */
    void lineLoaded(List<CacheLine> set, int fetchedIndex) {}

    /**
     * Called when a line already in the cache is accessed so that line metas may be updated as appropriate.
     * (Default version: does nothing)
     * @param set the lines in this set
     * @param accessedIndex the index in set of the line that was loaded
     */
    void lineAccessed(List<CacheLine> set, int accessedIndex) {}

    /**
     * Choose which line to evict from a set.
     * (Default version: the mapped line with the least meta)
     * @param set the lines to choose from
     * @return the index of the line to evict relative to the set parameter
     */
    int chooseLineToEvict(List<CacheLine> set) {
        int minIndex = 0;
        for (int i = 1; i < set.size(); ++i) {
            CacheLine line = set.get(i);
            if (line.isMapped() && line.getMeta() < set.get(minIndex).getMeta())
                minIndex = i;
        }

        return minIndex;
    }
}
