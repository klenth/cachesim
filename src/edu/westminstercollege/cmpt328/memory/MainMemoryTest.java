package edu.westminstercollege.cmpt328.memory;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

class MainMemoryTest {

    private MainMemory memory;

    @Before
    public void setUp() {
        memory = new MainMemory(16 * 8, 225);
    }

    @Test
    public void getByte() {
        ByteValue b = memory.getByte(100);
        assertEquals(0, b.get());
        b.set(25);
        assertEquals(25, b.get());

        b = memory.getByte(99);
        assertEquals(0, b.get());
        b.set(200);
        assertEquals(200, b.get());

        b = memory.getByte(101);
        assertEquals(0, b.get());
    }

    @Test
    public void getInt() {
        IntValue i = memory.getInt(36);
        assertEquals(0, i.get());
        i.set(1029384756);
        assertEquals(1029384756, i.get());
        i = memory.getInt(32);
        assertEquals(0, i.get());
        i = memory.getInt(40);
        assertEquals(0, i.get());
    }

    @Test
    public void getDouble() {
        DoubleValue d = memory.getDouble(120);
        assertEquals(0, d.get(), 0);
        d.set(Math.PI);
        assertEquals(Math.PI, d.get(), 0);
        d = memory.getDouble(55);
        d.set(-28734.23223232);
        assertEquals(-28734.23223232, d.get(), 0);
    }

    @Test
    public void getByteArray() {
        ByteArrayValue ba = memory.getByteArray(50, 25);
        for (int i = 0; i < ba.getLength(); ++i)
            assertEquals(0, ba.get(i));
        for (int i = 0; i < ba.getLength(); ++i)
            ba.set(i, 100 - i);
        for (int i = 0; i < ba.getLength(); ++i)
            assertEquals(100 - i, ba.get(i));
    }

    @Test
    public void getIntArray() {
        IntArrayValue ia = memory.getIntArray(100, 6);
        for (int i = 0; i < ia.getLength(); ++i)
            assertEquals(ia.get(i), 0);
        for (int i = 0; i < ia.getLength(); ++i)
            ia.set(i, 20000 * i - 102938);
        for (int i = ia.getLength() - 1; i >= 0; --i)
            assertEquals(20000 * i - 102938, ia.get(i));
    }

    @Test
    public void getDoubleArray() {
        final int seed = 5;
        DoubleArrayValue da = memory.getDoubleArray(64, 6);
        for (int i = 0; i < da.getLength(); ++i)
            assertEquals(da.get(i), 0, 0);
        Random rand = new Random(seed);
        for (int i = 0; i < da.getLength(); ++i)
            da.set(i, rand.nextDouble());
        rand = new Random(seed);
        for (int i = 0; i < da.getLength(); ++i)
            assertEquals(rand.nextDouble(), da.get(i), 0);
    }
}