package dev.xbhou.javalab.lock;

public class FencedResource {

    private long highestAcceptedToken;
    private String value = "initial";

    public synchronized boolean write(
            long fencingToken,
            String newValue
    ) {
        if (fencingToken < highestAcceptedToken) {
            System.out.printf(
                    "reject stale write: token=%d, highestAccepted=%d%n",
                    fencingToken,
                    highestAcceptedToken
            );
            return false;
        }

        highestAcceptedToken = fencingToken;
        value = newValue;

        System.out.printf(
                "accept write: token=%d, value=%s%n",
                fencingToken,
                newValue
        );

        return true;
    }

    public synchronized String value() {
        return value;
    }
}
