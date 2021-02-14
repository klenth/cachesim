package edu.westminstercollege.cmpt328.cachesim;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class Runtime {

    private static Map<Object, Integer> arrayAddresses = new IdentityHashMap<>();
    private static int loads, stores;
    private static int arrayLoads, arrayStores;

    public static void loadLocal(int index, int size) {
        //System.out.printf("Loading local variable %d–%d\n", index, index + size - 1)
        ++loads;
    }

    public static void storeLocal(int index, int size) {
        //System.out.printf("Storing local variable %d–%d\n", index, index + size - 1);
        ++stores;
    }

    public static void allocateArray(Object array) {
        if (!array.getClass().isArray())
            throw new RuntimeException("allocateArray(" + array + ") - not an array");
        arrayAddresses.put(array, 0);
    }

    public static void loadFromArray(Object array, int index) {
        if (!array.getClass().isArray())
            throw new RuntimeException("loadFromArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: loading from array that was not allocated by this code");
        ++arrayLoads;
    }

    public static void storeToArray(byte[] array, int index, byte value) {
        if (!array.getClass().isArray())
            throw new RuntimeException("storeToArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: storing to array that was not allocated by this code");
        array[index] = value;
        ++arrayStores;
    }

    public static void storeToArray(short[] array, int index, short value) {
        if (!array.getClass().isArray())
            throw new RuntimeException("storeToArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: storing to array that was not allocated by this code");
        array[index] = value;
        ++arrayStores;
    }

    public static void storeToArray(int[] array, int index, int value) {
        if (!array.getClass().isArray())
            throw new RuntimeException("storeToArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: storing to array that was not allocated by this code");
        array[index] = value;
        ++arrayStores;
    }

    public static void storeToArray(long[] array, int index, long value) {
        if (!array.getClass().isArray())
            throw new RuntimeException("storeToArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: storing to array that was not allocated by this code");
        array[index] = value;
        ++arrayStores;
    }

    public static void storeToArray(float[] array, int index, float value) {
        if (!array.getClass().isArray())
            throw new RuntimeException("storeToArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: storing to array that was not allocated by this code");
        array[index] = value;
        ++arrayStores;
    }

    public static void storeToArray(double[] array, int index, double value) {
        if (!array.getClass().isArray())
            throw new RuntimeException("storeToArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: storing to array that was not allocated by this code");
        array[index] = value;
        ++arrayStores;
    }

    public static void storeToArray(char[] array, int index, char value) {
        if (!array.getClass().isArray())
            throw new RuntimeException("storeToArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: storing to array that was not allocated by this code");
        array[index] = value;
        ++arrayStores;
    }

    public static void storeToArray(boolean[] array, int index, boolean value) {
        if (!array.getClass().isArray())
            throw new RuntimeException("storeToArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: storing to array that was not allocated by this code");
        array[index] = value;
        ++arrayStores;
    }

    public static void storeToArray(Object[] array, int index, Object value) {
        if (!array.getClass().isArray())
            throw new RuntimeException("storeToArray(" + array + ", " + index + ") - not an array");
        if (!arrayAddresses.containsKey(array))
            System.out.println("Warning: storing to array that was not allocated by this code");
        array[index] = value;
        ++arrayStores;
    }
}
