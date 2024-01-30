package edu.westminsteru.cmpt328.memory;

/**
 * An exception thrown by the {@link MemorySystem} <code>allocate</code> methods when there is no more memory to allocate.
 */
public class MemoryExhaustedException extends RuntimeException {

    public MemoryExhaustedException() {
    }

    public MemoryExhaustedException(String message) {
        super(message);
    }

    public MemoryExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemoryExhaustedException(Throwable cause) {
        super(cause);
    }

    public MemoryExhaustedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
