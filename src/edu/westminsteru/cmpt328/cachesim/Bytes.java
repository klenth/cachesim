package edu.westminsteru.cmpt328.cachesim;

import java.util.LinkedList;
import java.util.List;

class Bytes {

    private interface ByteSpan {
        int size();
        int get(int i);
    }

    private List<ByteSpan> spans = new LinkedList<>();

    private Bytes add(ByteSpan span) {
        spans.add(span);
        return this;
    }

    public Bytes u8(final int b) {
        return add(new ByteSpan() {
            @Override
            public int size() {
                return 1;
            }

            @Override
            public int get(int i) {
                return b & 0xff;
            }
        });
    }

    public Bytes u16(int b) {
        return add(new ByteSpan() {
            @Override
            public int size() {
                return 2;
            }

            @Override
            public int get(int i) {
                return (i == 0) ? ((b >>> 8) & 0xff) : (b & 0xff);
            }
        });
    }

    public Bytes s16(int b) {
        return add(new ByteSpan() {
            @Override
            public int size() {
                return 2;
            }

            @Override
            public int get(int i) {
                return (i == 0) ? ((b >> 8) & 0xff) : (b & 0xff);
            }
        });
    }

    public byte[] build() {
        int numBytes = spans.stream().mapToInt(ByteSpan::size).sum();
        byte[] b = new byte[numBytes];
        int bi = 0;
        for (ByteSpan span : spans) {
            for (int i = 0; i < span.size(); ++i, ++bi)
                b[bi] = (byte)span.get(i);
        }

        return b;
    }
}
