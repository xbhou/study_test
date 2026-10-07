package dev.xbhou.javalab.quote;

import java.math.BigDecimal;

public record Quote(String provider, String symbol, BigDecimal price, long latencyMs) {
}
