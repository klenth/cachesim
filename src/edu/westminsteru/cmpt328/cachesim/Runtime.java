package edu.westminsteru.cmpt328.cachesim;

import edu.westminsteru.cmpt328.cachesim.annotations.*;
import edu.westminsteru.cmpt328.cachesim.annotations.*;
import edu.westminsteru.cmpt328.memory.*;
import edu.westminsteru.cmpt328.memory.MainMemory;
import edu.westminsteru.cmpt328.memory.MemorySystem;
import edu.westminsteru.cmpt328.memory.MemoryValue;
import edu.westminsteru.cmpt328.memory.StackFrame;
import edu.westminsteru.cmpt328.memory.gui.MemorySystemConfiguration;
import edu.westminsteru.cmpt328.memory.gui.MemorySystemConfigurationChooser;
import edu.westminsteru.cmpt328.cachesim.annotations.Cache;

import javax.swing.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.prefs.*;

import java.util.*;

public final class Runtime {

    private static MemorySystem sys;

    private static Deque<StackFrame> stack = new LinkedList<>();

    private static String PREFERENCES_KEY_MEMORY_SYSTEM_CONFIGURATION = "memorySystemConfiguration";

    private Runtime() { }

    private static MemorySystem system() {
        if (sys == null) {
            throw new RuntimeException("No memory system has been configured. (Does your main class have @Memory? Is the program run with -javaagent?)");
        }
        return sys;
    }

    private static Map<Object, Integer> arrayAddresses = new IdentityHashMap<>();

    public static void viewStatistics() {
        system().viewStatistics();
    }

    public static void initMemorySystem(Class<?> initialClass) {
        var config = initialClass.getAnnotation(edu.westminsteru.cmpt328.cachesim.annotations.Memory.class);

        boolean configureInGui = initialClass.isAnnotationPresent(ConfigureMemoryInGui.class);
        //boolean configureInGui = initialClass.isAnnotationPresent(MemoryAware.class);
        if (!configureInGui && config == null)
            throw new RuntimeException("No memory configuration: class " + initialClass.getName() + " must either be marked with @ConfigureMemoryInGui or define a configuration in a @Memory annotation");

        if (configureInGui) {
            var chooser = new MemorySystemConfigurationChooser();
            var savedConfig = loadMemorySystemConfigurationFromPreferences();
            if (savedConfig != null)
                chooser.setConfiguration(savedConfig);
            boolean accepted = false;
            while (chooser.showDialog(null)) {
                var configuration = chooser.getConfiguration();
                saveMemorySystemConfigurationToPreferences(configuration);
                try {
                    sys = fromConfiguration(configuration);
                    accepted = true;
                    break;
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Invalid memory configuration", JOptionPane.ERROR_MESSAGE);
                }
            }

            if (!accepted) {
                System.out.println("Memory system configuration cancelled — exiting");
                System.exit(0);
            }
        } else {
            MainMemory ram = new MainMemory(config.ram().name(), config.ram().size(), config.ram().accessTime());
            edu.westminsteru.cmpt328.memory.Memory top = ram;
            for (Cache cache : config.caches()) {
                edu.westminsteru.cmpt328.memory.Cache.Builder builder = edu.westminsteru.cmpt328.memory.Cache.builder();
                builder.name(cache.name());
                builder.accessTime(cache.accessTime());
                builder.lineCount(cache.lines());

                if (cache.mapping() == MappingAlgorithm.Direct)
                    builder.directMapping();
                else if (cache.mapping() == MappingAlgorithm.FullyAssociative)
                    builder.fullyAssociative(cache.replacement());
                else if (cache.mapping() == MappingAlgorithm.SetAssociative) {
                    if (cache.ways() == Cache.Unspecified)
                        throw new RuntimeException(String.format("Cache %s: for set associative mapping, must specify number of ways (e.g. ways = 4)", cache.name()));
                    builder.setAssociative(cache.ways(), cache.replacement());
                }

                if (cache.accessTime() > top.getAccessTime())
                    System.err.printf("Warning: cache %s has an access time of %d cycles, which is higher than the memory below it (%s, access time %d cycles).\nMake sure your caches are in order from lowest (closest to RAM) to highest (closest to CPU)!",
                            cache.name(), cache.accessTime(), top.getName(), cache.accessTime());

                builder.drawingFrom(top);

                top = builder.build();
            }

            sys = new MemorySystem(top);
        }

        MemorySystem.setDefault(sys);
    }

    private static MemorySystemConfiguration loadMemorySystemConfigurationFromPreferences() {
        Preferences p = Preferences.userNodeForPackage(Runtime.class);
        String savedData = p.get(PREFERENCES_KEY_MEMORY_SYSTEM_CONFIGURATION, null);
        if (savedData == null)
            return null;
        var reader = new StringReader(savedData);

        try {
            return MemorySystemConfiguration.loadJson(reader);
        } catch (MemorySystemConfiguration.InvalidConfigurationException ex) {
            System.err.println("Unable to load memory system configuration from preferences:");
            ex.printStackTrace();
            return null;
        }
    }

    private static void saveMemorySystemConfigurationToPreferences(MemorySystemConfiguration config) {
        Preferences p = Preferences.userNodeForPackage(Runtime.class);
        var out = new StringWriter();
        config.saveJson(out);

        String valueToSave = out.toString();
        if (valueToSave.length() > Preferences.MAX_VALUE_LENGTH)
            System.err.println("Unable to save memory system configuration to preferences: value too long!");
        else
            p.put(PREFERENCES_KEY_MEMORY_SYSTEM_CONFIGURATION, out.toString());
    }

    private static MemorySystem fromConfiguration(MemorySystemConfiguration config) {
        var ram = new MainMemory("RAM", config.getRamSize(), config.getRamAccessTime());
        edu.westminsteru.cmpt328.memory.Memory top = ram;

        int level = config.getCaches().size();
        final var caches = config.getCaches();
        for (int i = caches.size() - 1; i >= 0; --i) {
        //for (var cache : config.getCaches().reversed()) {
            var cache = caches.get(i);
            var builder = edu.westminsteru.cmpt328.memory.Cache.builder()
                    .name("L" + level)
                    .drawingFrom(top)
                    .lineCount(cache.getLineCount())
                    .accessTime(cache.getAccessTime());
            if (cache.isDirect())
                builder = builder.directMapping();
            else {
                if (cache.getWays() == cache.getLineCount())
                    builder = builder.fullyAssociative(cache.getReplacement());
                else
                    builder = builder.setAssociative(cache.getWays(), cache.getReplacement());
            }

            top = builder.build();
            --level;
        }

        return new MemorySystem(top);
    }

    public static void enterMethod(int locals) {
        stack.push(system().allocateStackFrame(4 * locals));
    }

    public static void leaveMethod() {
        StackFrame frame = stack.pop();
        system().deallocateStackFrame(frame);
    }

    private static StackFrame topFrame() {
        return stack.getFirst();
    }

    public static void loadLocal(int index, int size) {
        int addr = (topFrame().getAddress() + 4 * index);
        addr -= addr % (4 * size);
        if (size == 1)
            system().getIntAt(addr).get();
        else
            system().getDoubleAt(addr).get();
    }

    public static void storeLocal(int index, int size) {
        int addr = topFrame().getAddress() + 4 * index;
        addr -= addr % (4 * size);
        if (size == 1)
            system().getIntAt(addr).set(0);
        else
            system().getDoubleAt(addr).set(0);
    }

    public static void allocateArray(Object array) {
        MemoryValue arrayValue;
        if (array instanceof byte[])
            arrayValue = system().allocateByteArray(((byte[])array).length);
        else if (array instanceof boolean[])
            arrayValue = system().allocateByteArray(((boolean[])array).length);
        else if (array instanceof short[])
            arrayValue = system().allocateIntArray(((short[])array).length);
        else if (array instanceof int[])
            arrayValue = system().allocateIntArray(((int[])array).length);
        else if (array instanceof float[])
            arrayValue = system().allocateIntArray(((float[])array).length);
        else if (array instanceof char[])
            arrayValue = system().allocateIntArray(((char[])array).length);
        else if (array instanceof Object[])
            arrayValue = system().allocateIntArray(((Object[])array).length);
        else if (array instanceof long[])
            arrayValue = system().allocateDoubleArray(((long[])array).length);
        else if (array instanceof double[])
            arrayValue = system().allocateDoubleArray(((double[])array).length);
        else
            throw new RuntimeException("allocateArray(" + array + ") - not an array");

        arrayAddresses.put(array, arrayValue.getAddress());
    }

    public static void loadFromArray(Object array, int index) {
        int addr = arrayAddresses.getOrDefault(array, -1);
        if (addr == -1)
            System.out.println("Warning: loading from array that was not allocated by this code");
        if (array instanceof byte[]
                || array instanceof boolean[])
            system().getByteAt(addr + index).get();
        else if (array instanceof short[]
                || array instanceof int[]
                || array instanceof float[]
                || array instanceof char[]
                || array instanceof Object[])
            system().getIntAt(addr + 4 * index).get();
        else if (array instanceof long[]
                || array instanceof double[])
            system().getDoubleAt(addr + 8 * index).get();
    }

    public static void storeToArray(byte[] array, int index, byte value) {
        trackStoreToArray(array, index, 1);
        array[index] = value;
    }

    public static void storeToArray(short[] array, int index, short value) {
        trackStoreToArray(array, index, 4);
        array[index] = value;
    }

    public static void storeToArray(int[] array, int index, int value) {
        trackStoreToArray(array, index, 4);
        array[index] = value;
    }

    public static void storeToArray(long[] array, int index, long value) {
        trackStoreToArray(array, index, 8);
        array[index] = value;
    }

    public static void storeToArray(float[] array, int index, float value) {
        trackStoreToArray(array, index, 4);
        array[index] = value;
    }

    public static void storeToArray(double[] array, int index, double value) {
        trackStoreToArray(array, index, 8);
        array[index] = value;
    }

    public static void storeToArray(char[] array, int index, char value) {
        trackStoreToArray(array, index, 4);
        array[index] = value;
    }

    public static void storeToArray(boolean[] array, int index, boolean value) {
        trackStoreToArray(array, index, 1);
        array[index] = value;
    }

    public static void storeToArray(Object[] array, int index, Object value) {
        trackStoreToArray(array, index, 4);
        array[index] = value;
    }

    private static void trackStoreToArray(Object array, int index, int elementSize) {
        int addr = arrayAddresses.getOrDefault(array, -1);
        if (addr == -1)
            System.err.println("Warning: storing to array that was not allocated by this code");
        if (elementSize == 1)
            system().getByteAt(addr + index).set(0);
        else if (elementSize == 4)
            system().getIntAt(addr + 4 * index).set(0);
        else if (elementSize == 8)
            system().getDoubleAt(addr + 8 * index).set(0);
    }
}
