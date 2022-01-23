package edu.westminstercollege.cmpt328.memory.gui;

import edu.westminstercollege.cmpt328.cachesim.annotations.Cache;
import edu.westminstercollege.cmpt328.memory.Bits;
import edu.westminstercollege.cmpt328.memory.MemorySystem;
import edu.westminstercollege.cmpt328.memory.ReplacementAlgorithm;
import edu.westminstercollege.klenth.json.Json;
import edu.westminstercollege.klenth.json.JsonArray;
import edu.westminstercollege.klenth.json.JsonObject;
import edu.westminstercollege.klenth.json.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class MemorySystemConfiguration {

    public static class CacheConfiguration {

        final int lineCount;
        final int accessTime;
        final int ways;
        final ReplacementAlgorithm replacement;

        CacheConfiguration(int lineCount, int accessTime, int ways, ReplacementAlgorithm replacement) {
            if (lineCount < 1)
                throw new IllegalArgumentException("lineCount must be at least 1");
            if (accessTime < 0)
                throw new IllegalArgumentException("accessTime must be at least 0");
            this.lineCount = lineCount;
            this.accessTime = accessTime;
            this.ways = ways;
            this.replacement = replacement;
        }

        public int getLineCount() {
            return lineCount;
        }

        public int getSize() {
            return lineCount * Bits.BLOCK_SIZE;
        }

        public MemorySize getMemorySize() {
            return MemorySize.of(getSize(), Unit.Byte);
        }

        public int getAccessTime() {
            return accessTime;
        }

        public int getWays() {
            return ways;
        }

        public ReplacementAlgorithm getReplacement() {
            return replacement;
        }

        public boolean isDirect() {
            return ways == 1;
        }

        public boolean isFullyAssociative() {
            return ways == lineCount;
        }

        public boolean isSetAssociative() {
            return ways > 1 && ways < lineCount;
        }

        public CacheConfiguration withLineCount(int newLineCount) {
            if (newLineCount == this.lineCount)
                return this;
            return new CacheConfiguration(newLineCount, this.accessTime, this.ways, this.replacement);
        }

        public CacheConfiguration withAccessTime(int newAccessTime) {
            if (newAccessTime == this.accessTime)
                return this;
            return new CacheConfiguration(this.lineCount, newAccessTime, this.ways, this.replacement);
        }

        public CacheConfiguration asDirect() {
            if (isDirect())
                return this;
            return new CacheConfiguration(this.lineCount, this.accessTime, 1, this.replacement);
        }

        public CacheConfiguration asAssociative(int newWays) {
            if (newWays == this.ways)
                return this;
            return new CacheConfiguration(this.lineCount, this.accessTime, newWays, this.replacement);
        }

        public CacheConfiguration withReplacement(ReplacementAlgorithm newReplacement) {
            if (newReplacement == this.replacement)
                return this;
            return new CacheConfiguration(this.lineCount, this.accessTime, this.ways, newReplacement);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lineCount, accessTime, ways, replacement);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof CacheConfiguration other)
                    && lineCount == other.lineCount
                    && accessTime == other.accessTime
                    && ways == other.ways
                    && replacement == other.replacement;
        }
    }

    final int ramSize;
    final int ramAccessTime;
    final CacheConfiguration[] caches;

    public MemorySystemConfiguration(int ramSize, int ramAccessTime) {
        this(ramSize, ramAccessTime, new CacheConfiguration[0]);
    }

    MemorySystemConfiguration(int ramSize, int ramAccessTime, CacheConfiguration[] caches) {
        if (ramSize < 64)
            throw new IllegalArgumentException("ramSize must be at least one block (64 B)");
        if (ramAccessTime < 0)
            throw new IllegalArgumentException("ramAccessTime must be at least 0");
        this.ramSize = ramSize;
        this.ramAccessTime = ramAccessTime;
        this.caches = caches;
    }

    private MemorySystemConfiguration cachesModified(Consumer<List<CacheConfiguration>> cacheOperation) {
        var modifiedCaches = new ArrayList<>(Arrays.asList(this.caches));
        cacheOperation.accept(modifiedCaches);
        return new MemorySystemConfiguration(ramSize, ramAccessTime, modifiedCaches.toArray(new CacheConfiguration[0]));
    }

    public int getRamSize() {
        return ramSize;
    }

    public MemorySize getRamMemorySize() {
        return MemorySize.of(getRamSize(), Unit.Byte);
    }

    public int getRamAccessTime() {
        return ramAccessTime;
    }

    public List<CacheConfiguration> getCaches() {
        return Collections.unmodifiableList(Arrays.asList(caches));
    }

    public MemorySystemConfiguration withRamSize(int newRamSize) {
        if (newRamSize == this.ramSize)
            return this;
        return new MemorySystemConfiguration(newRamSize, this.ramAccessTime, this.caches);
    }

    public MemorySystemConfiguration withRamAccessTime(int newRamAccessTime) {
        if (newRamAccessTime == this.ramAccessTime)
            return this;
        return new MemorySystemConfiguration(this.ramSize, newRamAccessTime, this.caches);
    }

    public MemorySystemConfiguration withCacheAtTop(CacheConfiguration c) {
        return cachesModified(caches -> caches.add(0, c));
    }

    public MemorySystemConfiguration withCacheAtBottom(CacheConfiguration c) {
        return cachesModified(caches -> caches.add(c));
    }

    public MemorySystemConfiguration insertingCache(int level, CacheConfiguration c) {
        return cachesModified(caches -> caches.add(level, c));
    }

    public MemorySystemConfiguration replacingCache(CacheConfiguration original, CacheConfiguration replacement) {
        if (original.equals(replacement))
            return this;
        return cachesModified(caches -> caches.replaceAll(c -> (c == original) ? replacement : c));
    }

    public CacheConfiguration directCache(int lineCount, int accessTime) {
        return new CacheConfiguration(lineCount, accessTime, 1, ReplacementAlgorithm.LRU);
    }

    public CacheConfiguration associativeCache(int lineCount, int accessTime, int ways, ReplacementAlgorithm replacement) {
        if (ways < 2)
            throw new IllegalArgumentException("ways must be at least 2 for an associative cache");
        if (replacement == null)
            throw new IllegalArgumentException("replacement cannot be null");
        return new CacheConfiguration(lineCount, accessTime, ways, replacement);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MemorySystemConfiguration other)
                && ramAccessTime == other.ramAccessTime
                && ramSize == other.ramSize
                && Objects.deepEquals(caches, other.caches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ramAccessTime, ramSize, Arrays.hashCode(caches));
    }

    public void saveJson(Path path) throws IOException {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            out.println("{");
            out.println("\t\"ram\": {");
            out.printf("\t\t\"size\": %d,\n", ramSize);
            out.printf("\t\t\"accessTime\": %d\n", ramAccessTime);
            out.println("\t},");
            out.println("\t\"caches\": [");

            for (int i = 0; i < caches.length; ++i) {
                var cache = caches[i];
                out.println("\t\t{");
                out.printf("\t\t\t\"lines\": %d,\n", cache.getLineCount());
                out.printf("\t\t\t\"accessTime\": %d,\n", cache.getAccessTime());
                out.printf("\t\t\t\"ways\": %d", cache.getWays());
                if (cache.getWays() > 1)
                    out.printf(",\n\t\t\t\"replacement\": \"%s\"", cache.getReplacement().toString());

                if (i + 1 < caches.length)
                    out.println("\n\t\t},");
                else
                    out.println("\n\t\t}");
            }

            out.println("\t]");
            out.println("}");
        }
    }

    public static class InvalidConfigurationException extends Exception {
        public InvalidConfigurationException() {
            super();
        }

        public InvalidConfigurationException(String message) {
            super(message);
        }

        public InvalidConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidConfigurationException(Throwable cause) {
            super(cause);
        }

        protected InvalidConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public static MemorySystemConfiguration loadJson(Path path) throws IOException, InvalidConfigurationException {
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            var parser = new JsonParser(in);
            var value = parser.parseValue().get();

            if (!(value instanceof Map<?, ?>))
                throw new InvalidConfigurationException("Unable to parse configuration: not a JSON object");
            var object = (Map<String, Object>)value; //.get();

            var objectKeys = object.keySet();
            if (!(objectKeys.contains("ram")))
                throw new InvalidConfigurationException("Unable to parse configuration: key 'ram' missing");
            var ram = (Map<String, Object>)object.get("ram");

            var ramKeys = ram.keySet();
            if (!(ramKeys.contains("size")))
                throw new InvalidConfigurationException("Unable to parse configuration: ram missing key 'size'");
            int ramSize = ((Number)ram.get("size")).intValue();

            if (!(ramKeys.contains("accessTime")))
                throw new InvalidConfigurationException("Unable to parse configuration: ram missing key 'ramAccessTime'");
            int ramAccessTime = ((Number)ram.get("accessTime")).intValue();

            if (!(objectKeys.contains("caches")))
                throw new InvalidConfigurationException("Unable to parse configuration: key 'caches' missing");
            var caches = (List<Object>)object.get("caches");

            CacheConfiguration[] parsedCaches = new CacheConfiguration[caches.size()];
            for (int i = 0; i < caches.size(); ++i) {
                var cache = (Map<String, Object>)caches.get(i);
                var cacheKeys = cache.keySet();
                if (!(cacheKeys.contains("lines")))
                    throw new InvalidConfigurationException(String.format("Unable to parse configuration: cache #%d missing key 'lines'", i));
                int lines = ((Number)cache.get("lines")).intValue();

                if (!(cacheKeys.contains("accessTime")))
                    throw new InvalidConfigurationException(String.format("Unable to parse configuration: cache %d missing key 'accessTime'", i));
                int accessTime = ((Number)cache.get("accessTime")).intValue();

                if (!(cacheKeys.contains("ways")))
                    throw new InvalidConfigurationException(String.format("Unable to parse configuration: cache %d missing key 'ways'", i));
                int ways = ((Number)cache.get("ways")).intValue();

                ReplacementAlgorithm replacement = ReplacementAlgorithm.LRU;
                if (cacheKeys.contains("replacement"))
                    replacement = ReplacementAlgorithm.valueOf(cache.get("replacement").toString());
                else if (ways > 1)
                    throw new InvalidConfigurationException(String.format("Unable to parse configuration: cache %d is associative but missing key 'replacement'", i));

                parsedCaches[i] = new CacheConfiguration(lines, accessTime, ways, replacement);
            }

            return new MemorySystemConfiguration(ramSize, ramAccessTime, parsedCaches);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidConfigurationException("Unable to parse configuration", e);
        }
    }
}
