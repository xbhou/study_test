package dev.xbhou.javalab.circuitbreaker;

public class ManualClock implements TimeSource {

    private long nowMillis;

    public ManualClock(long initialMillis) {
        this.nowMillis = initialMillis;
    }

    @Override
    public long nowMillis() {
        return nowMillis;
    }

    public void advanceMillis(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("millis must be >= 0");
        }
        nowMillis += millis;
    }
}
