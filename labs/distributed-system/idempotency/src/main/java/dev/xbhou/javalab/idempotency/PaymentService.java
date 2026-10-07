package dev.xbhou.javalab.idempotency;

import static dev.xbhou.javalab.idempotency.InMemoryIdempotencyStore.Action.CONFLICT;
import static dev.xbhou.javalab.idempotency.InMemoryIdempotencyStore.Action.IN_PROGRESS;
import static dev.xbhou.javalab.idempotency.InMemoryIdempotencyStore.Action.REPLAY_SUCCESS;
import static dev.xbhou.javalab.idempotency.InMemoryIdempotencyStore.Action.START;

public class PaymentService {

    private final InMemoryIdempotencyStore store;
    private final FakePaymentGateway gateway;

    public PaymentService(
            InMemoryIdempotencyStore store,
            FakePaymentGateway gateway
    ) {
        this.store = store;
        this.gateway = gateway;
    }

    public PaymentResponse pay(String idempotencyKey, PaymentRequest request) {
        InMemoryIdempotencyStore.BeginDecision decision =
                store.begin(idempotencyKey, request.fingerprint());

        if (decision.action() == REPLAY_SUCCESS) {
            return PaymentResponse.replayed(decision.entry().result());
        }

        if (decision.action() == IN_PROGRESS) {
            return PaymentResponse.inProgress();
        }

        if (decision.action() == CONFLICT) {
            return PaymentResponse.conflict();
        }

        if (decision.action() != START) {
            throw new IllegalStateException("unexpected action " + decision.action());
        }

        PaymentResult result = gateway.charge(request);
        store.completeSuccess(idempotencyKey, result);

        return PaymentResponse.created(result);
    }
}
