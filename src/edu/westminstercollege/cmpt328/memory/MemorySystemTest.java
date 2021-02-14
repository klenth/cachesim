package edu.westminstercollege.cmpt328.memory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

class MemorySystemTest {

    private static Random random = new Random(0);

    public static void main(String... args) throws IOException {
        /*
        MainMemory ram = new MainMemory("RAM", Bits.NUM_ADDRESSES, 245);
        Cache cache = Cache.builder()
                .lineCount(64)
                .accessTime(5)
                .drawingFrom(ram)
                .setAssociative(2, ReplacementAlgorithm.LRU)
                .build();
        MemorySystem.setDefault(new MemorySystem(cache));
        */

        /*
        try (PrintWriter out = new PrintWriter("sort.csv")) {
            for (int n = 10; n < 1000; n += 10) {
                long cycles = testSort(n);
                out.printf("%d,%d\n", n, cycles);
            }
        }
        */

        //MemorySystem.MicroCoreI7 sys = new MemorySystem.MicroCoreI7();
        //MemorySystem.CoreI7 sys = new MemorySystem.CoreI7();
        //MemorySystem.setDefault(sys);

        MainMemory ram = new MainMemory(242);
        Cache cache = Cache.builder()
                .setAssociative(4, ReplacementAlgorithm.LRU)
                .drawingFrom(ram)
                .accessTime(4)
                .lineCount(32)
                .build();
        MemorySystem sys = new MemorySystem(cache);
        MemorySystem.setDefault(sys);

        PrintWriter out = new PrintWriter("sort.csv");
        //testSort(20000);
        for (int i = 10; i <= 2000; i += 10) {
            long cycles = testSort(i);
            out.printf("%d,%d\n", i, cycles);
        }
        out.close();

        sys.printStatistics();
    }

    private static long testSort(int n) {
        MemorySystem sys = MemorySystem.getDefault();
        sys.resetAll();
        DoubleArrayValue data = sys.allocateDoubleArray(n);
        IntValue i = sys.allocateInt();
        for (i.set(0); i.get() < data.getLength(); i.increment()) {
            //System.out.println(i.get());
            data.set(i.get(), random.nextInt(1000));
        }

//        data.forEach(x -> System.out.print(x + " "));
//        System.out.println();

        /*
        IntValue minIndex = sys.allocateInt();
        IntValue j = sys.allocateInt();
        DoubleValue tmp = sys.allocateDouble();
        for (i.set(0); i.get() + 1 < data.getLength(); i.increment()) {
            minIndex.set(i);
            for (j.set(i.get() + 1); j.get() < data.getLength(); j.increment()) {
                if (data.get(j) < data.get(minIndex))
                    minIndex.set(j);
            }

            tmp.set(data.get(minIndex));
            data.set(minIndex, data.get(i));
            data.set(i, tmp);
        }
        */

        quicksort(data);

//        data.forEach(x -> System.out.print(x + " "));
//        System.out.println();
        long cycles = sys.getTotalAccessTime();
        System.out.printf("%,d elements / %,d cycles\n", n, cycles);
        return cycles;
    }

    private static void quicksort(DoubleArrayValue data) {
        final MemorySystem sys = MemorySystem.getDefault();
        int n = data.getLength();
        if (n < 2)
            return;

        /*
        IntValue piv = sys.allocateInt(random.nextInt(data.getLength()));
        IntValue i = sys.allocateInt(0);
        DoubleValue tmp = sys.allocateDouble();
        swap(data, piv.get(), i.get(), tmp);
        piv.set(0);
        */

        IntValue piv = sys.allocateInt(0);
        IntValue i = sys.allocateInt();
        DoubleValue tmp = sys.allocateDouble();

        for (i.set(1); i.get() < data.getLength(); i.increment()) {
            if (data.get(i) < data.get(piv)) {
                swap(data, i.get(), piv.get() + 1, tmp);
                swap(data, piv.get(), piv.get() + 1, tmp);
                piv.increment();
            }
        }

//        quicksort(data.subArray(0, piv.get()));
//        quicksort(data.subArray(piv.get() + 1, data.getLength()));
    }

    private static void swap(DoubleArrayValue arr, int a, int b, DoubleValue tmp) {
        tmp.set(arr.get(a));
        arr.set(a, arr.get(b));
        arr.set(b, tmp);
    }
}
