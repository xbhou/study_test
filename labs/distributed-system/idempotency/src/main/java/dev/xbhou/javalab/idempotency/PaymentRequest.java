package dev.xbhou.javalab.idempotency;

import java.math.BigDecimal;

public record PaymentRequest(String orderId, BigDecimal amount) {

    public String fingerprint() {
        return orderId + "|" + amount.stripTrailingZeros().toPlainString();
    }
}
