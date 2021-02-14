package edu.westminstercollege.cmpt328.memory;

import static org.junit.Assert.*;

class ByteStoreTest {

    private ByteStore store;
    private ByteStore empty;

    @org.junit.Before
    public void setUp() throws Exception {
        store = new ByteStore(new int[] {
                0x44, 0x17, 0x41, 0x54, 0xfb, 0x21, 0x09, 0x40
        });
        empty = new ByteStore(32);
    }

    @org.junit.Test
    public void getByteAt() {
        assertEquals(0x44, store.getByteAt(0));
        assertEquals(0xfb, store.getByteAt(4));
        assertEquals(0x40, store.getByteAt(7));
    }

    @org.junit.Test
    public void setByteAt() {
        empty.setByteAt(3, 0x96);
        assertEquals(0x96, empty.getByteAt(3));
        assertEquals(0, empty.getByteAt(2));
        assertEquals(0, empty.getByteAt(4));

        empty.setByteAt(4, 0x20);
        assertEquals(0x20, empty.getByteAt(4));
        assertEquals(0x96, empty.getByteAt(3));
        assertEquals(0, empty.getByteAt(5));
    }

    @org.junit.Test
    public void getIntAt() {
        assertEquals(0x54411744, store.getIntAt(0));
        assertEquals(0x400921fb, store.getIntAt(4));
        assertEquals(0xfb544117, store.getIntAt(1));
    }

    @org.junit.Test
    public void setIntAt() {
        empty.setIntAt(0, 0x10293847);
        assertEquals(0x10293847, empty.getIntAt(0));
        assertEquals(0, empty.getIntAt(4));
        empty.setIntAt(2, 0x99887766);
        assertEquals(0x99887766, empty.getIntAt(2));
        assertEquals(0x00009988, empty.getIntAt(4));
    }

    @org.junit.Test
    public void getDoubleAt() {
        assertEquals(3.1415926535, store.getDoubleAt(0), 0);
    }

    @org.junit.Test
    public void setDoubleAt() {
        empty.setDoubleAt(0, Math.E);
        assertEquals(Math.E, empty.getDoubleAt(0), 0);
        empty.setDoubleAt(0, 1 / Math.PI);
        assertEquals(1 / Math.PI, empty.getDoubleAt(0), 0);
    }
}