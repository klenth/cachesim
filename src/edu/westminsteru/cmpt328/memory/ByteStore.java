package edu.westminsteru.cmpt328.memory;

import java.util.Arrays;

class ByteStore {

    final byte[] data;

    ByteStore(int size) {
        this.data = new byte[size];
    }

    ByteStore(int[] bytes) {
        this.data = new byte[bytes.length];
        for (int i = 0; i < bytes.length; ++i)
            data[i] = (byte)(bytes[i] & 0xff);
    }

    int getSize() {
        return data.length;
    }

    int getByteAt(int address) {
        return ((int)data[address]) & 0xff;
    }

    void setByteAt(int address, int value) {
        data[address] = (byte)(value & 0xff);
    }

    int getIntAt(int address) {
        int x = 0;
        for (int i = Bits.INT_SIZE - 1; i >= 0; --i) {
            x = (x << 8) | (((int)data[address + i]) & 0xff);
        }
        return x;
    }

    void setIntAt(int address, int value) {
        for (int i = 0; i < Bits.INT_SIZE; ++i, value >>= 8) {
            data[address + i] = (byte)(value & 0xff);
        }
    }

    double getDoubleAt(int address) {
        long x = 0;
        for (int i = Bits.DOUBLE_SIZE - 1; i >= 0; --i) {
            x = (x << 8) | (((long)data[address + i]) & 0xff);
        }
        return Double.longBitsToDouble(x);
    }

    void setDoubleAt(int address, double value) {
        long longValue = Double.doubleToLongBits(value);
        for (int i = 0; i < Bits.DOUBLE_SIZE; ++i, longValue >>= 8) {
            data[address + i] = (byte)(longValue & 0xff);
        }
    }

    // Currently a pointer is the same as an int
    int getPointerAt(int address) {
        return getIntAt(address);
    }

    void setPointerAt(int address, int value) {
        setIntAt(address, value);
    }

    void clear() {
        Arrays.fill(data, (byte)0);
    }
}
