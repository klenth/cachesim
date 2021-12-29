package edu.westminstercollege.cmpt328.memory.gui;

import org.junit.Test;

import static org.junit.Assert.*;

import static edu.westminstercollege.cmpt328.memory.gui.Unit.*;

public class MemorySizeTest {

    @Test
    public void of() {
        ofCase(1, Byte,1, Byte);
        ofCase(2, Byte,2, Byte);
        ofCase(17, Byte, 17, Byte);
        ofCase(1023, Byte, 1023, Byte);
        ofCase(1, Kibibyte, 1024, Byte);
        ofCase(4, Mebibyte, 4096, Kibibyte);
        ofCase(6, Mebibyte, 6144, Kibibyte);
        ofCase(1023, Yobibyte, 1023, Yobibyte);
        ofCase(1024, Yobibyte, 1024, Yobibyte);
        ofCase(1025, Yobibyte, 1025, Yobibyte);
        ofCase(4, Yobibyte, 4096, Zebibyte);
        ofCase(8192, Yobibyte, 8192 << 10, Zebibyte);
    }

    private void ofCase(int expectedSize, Unit expectedUnit, int size, Unit unit) {
        assertEquals(
                new MemorySize(expectedSize, expectedUnit),
                MemorySize.of(size, unit)
        );
    }

    @Test
    public void testToString() {
        assertEquals("1 B", new MemorySize(1, Byte).toString());
        assertEquals("84 kiB", new MemorySize(84, Kibibyte).toString());
        assertEquals("16 YiB", new MemorySize(16, Yobibyte).toString());
    }
}