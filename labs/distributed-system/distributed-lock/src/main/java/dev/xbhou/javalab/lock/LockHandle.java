package dev.xbhou.javalab.lock;

public record LockHandle(
        String key,
        String ownerToken,
        long fencingToken
) {
}
