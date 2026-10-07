package dev.xbhou.javalab.deadline;

import java.math.BigDecimal;

public record Quote(String provider, String symbol, BigDecimal price, long latencyMs) {
}
