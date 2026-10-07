package dev.xbhou.javalab.idempotency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class FakePaymentGateway {

    private final AtomicInteger chargeCount = new AtomicInteger();
    private final CountDownLatch startedSignal;
    private final CountDownLatch releaseGate;

    public FakePaymentGateway() {
        this(null, null);
    }

    public FakePaymentGateway(
            CountDownLatch startedSignal,
            CountDownLatch releaseGate
    ) {
        this.startedSignal = startedSignal;
        this.releaseGate = releaseGate;
    }

    public PaymentResult charge(PaymentRequest request) {
        if (startedSignal != null) {
            startedSignal.countDown();
        }

        if (releaseGate != null) {
            try {
                releaseGate.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("payment interrupted", e);
            }
        }

        int sequence = chargeCount.incrementAndGet();

        return new PaymentResult(
                "payment-" + sequence,
                request.orderId(),
                request.amount()
        );
    }

    public int chargeCount() {
        return chargeCount.get();
    }
}
