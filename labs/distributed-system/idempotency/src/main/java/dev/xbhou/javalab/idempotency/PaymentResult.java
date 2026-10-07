package dev.xbhou.javalab.idempotency;

import java.math.BigDecimal;

public record PaymentResult(
        String paymentId,
        String orderId,
        BigDecimal amount
) {
}
