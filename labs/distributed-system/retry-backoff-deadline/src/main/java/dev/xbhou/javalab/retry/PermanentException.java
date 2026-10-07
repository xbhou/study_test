package dev.xbhou.javalab.retry;

public class PermanentException extends RuntimeException {

    public PermanentException(String message) {
        super(message);
    }
}
