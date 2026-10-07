package dev.xbhou.javalab.idempotency;

public record PaymentResponse(
        Status status,
        PaymentResult result,
        String message
) {
    public enum Status {
        CREATED,
        REPLAYED,
        IN_PROGRESS,
        CONFLICT
    }

    public static PaymentResponse created(PaymentResult result) {
        return new PaymentResponse(Status.CREATED, result, "side effect executed");
    }

    public static PaymentResponse replayed(PaymentResult result) {
        return new PaymentResponse(Status.REPLAYED, result, "cached result reused");
    }

    public static PaymentResponse inProgress() {
        return new PaymentResponse(Status.IN_PROGRESS, null, "original request is still processing");
    }

    public static PaymentResponse conflict() {
        return new PaymentResponse(Status.CONFLICT, null, "same key used with different request");
    }
}
