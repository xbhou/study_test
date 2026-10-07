package dev.xbhou.javalab.idempotency;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) throws Exception {
        responseLostThenRetry();
        concurrentDuplicate();
        sameKeyDifferentRequest();
    }

    private static void responseLostThenRetry() {
        System.out.println("=== Scenario 1: success happened, response was lost ===");

        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        FakePaymentGateway gateway = new FakePaymentGateway();
        PaymentService service = new PaymentService(store, gateway);

        PaymentRequest request =
                new PaymentRequest("order-1001", new BigDecimal("10.00"));

        PaymentResponse first = service.pay("key-order-1001", request);

        System.out.println("first response (pretend it was lost): " + first);
        System.out.println("client observes timeout and retries...");

        PaymentResponse retry = service.pay("key-order-1001", request);

        System.out.println("retry response: " + retry);
        System.out.println("actual charge count: " + gateway.chargeCount());
        System.out.println();
    }

    private static void concurrentDuplicate() throws Exception {
        System.out.println("=== Scenario 2: duplicate arrives while processing ===");

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        FakePaymentGateway gateway = new FakePaymentGateway(started, release);
        PaymentService service = new PaymentService(store, gateway);

        PaymentRequest request =
                new PaymentRequest("order-2001", new BigDecimal("20.00"));

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<PaymentResponse> firstFuture = executor.submit(
                    () -> service.pay("key-order-2001", request)
            );

            if (!started.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("first payment did not start");
            }

            PaymentResponse duplicate =
                    service.pay("key-order-2001", request);

            System.out.println("concurrent duplicate response: " + duplicate);

            release.countDown();

            PaymentResponse first = firstFuture.get(1, TimeUnit.SECONDS);
            PaymentResponse replay =
                    service.pay("key-order-2001", request);

            System.out.println("original response: " + first);
            System.out.println("request after success: " + replay);
            System.out.println("actual charge count: " + gateway.chargeCount());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }

        System.out.println();
    }

    private static void sameKeyDifferentRequest() {
        System.out.println("=== Scenario 3: same key, different request ===");

        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        FakePaymentGateway gateway = new FakePaymentGateway();
        PaymentService service = new PaymentService(store, gateway);

        PaymentRequest original =
                new PaymentRequest("order-3001", new BigDecimal("30.00"));

        PaymentRequest different =
                new PaymentRequest("order-3001", new BigDecimal("99.00"));

        PaymentResponse first =
                service.pay("key-order-3001", original);

        PaymentResponse conflict =
                service.pay("key-order-3001", different);

        System.out.println("original response: " + first);
        System.out.println("different payload response: " + conflict);
        System.out.println("actual charge count: " + gateway.chargeCount());
    }
}
