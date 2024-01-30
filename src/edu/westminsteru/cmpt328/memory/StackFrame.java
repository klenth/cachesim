package edu.westminsteru.cmpt328.memory;

public class StackFrame implements MemoryValue {

    private final int address;
    private final int size;

    StackFrame(int address, int size) {
        this.address = address;
        this.size = size;
    }

    @Override
    public int getMemorySize() {
        return size;
    }

    @Override
    public int getAddress() {
        return address;
    }
}
