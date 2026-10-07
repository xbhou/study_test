package dev.xbhou.javalab.retry;

public class RetryableException extends RuntimeException {

    public RetryableException(String message) {
        super(message);
    }
}
