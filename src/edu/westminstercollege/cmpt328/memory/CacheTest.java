package edu.westminstercollege.cmpt328.memory;

import java.util.Random;

class CacheTest {

    public static void main(String... args) {
        MainMemory ram = new MainMemory(Bits.NUM_ADDRESSES, 225);
        Cache cache = Cache.builder()
                .accessTime(5)
                .lineCount(4)
                .drawingFrom(ram)
                .fullyAssociative(ReplacementAlgorithm.FIFO)
                .build();

        Random rand = new Random(0);
        IntArrayValue memInts = ram.getIntArray(0, (int)(ram.getSize() / Bits.INT_SIZE));
        for (int i = 0; i < memInts.getLength(); ++i)
            memInts.set(i, rand.nextInt());

        int[] addrs = {
                0xab40, 0xcd40, 0xef40, 0x0040,
                0x1240, 0xcd48, 0x4040
        };
        for (int addr : addrs)
            cache.getByte(addr).get();

        //ram.printContents();
        cache.print();
    }
}
