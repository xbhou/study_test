package dev.xbhou.javalab.circuitbreaker;

public interface TimeSource {
    long nowMillis();
}
