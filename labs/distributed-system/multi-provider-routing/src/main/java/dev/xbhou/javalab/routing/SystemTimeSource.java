package dev.xbhou.javalab.routing;

public class SystemTimeSource implements TimeSource {

    @Override
    public long nowMillis() {
        return System.currentTimeMillis();
    }
}
