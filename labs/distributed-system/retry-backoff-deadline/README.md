# Retry + Exponential Backoff + Jitter + Deadline

## 中文

### 问题

下游调用失败后：

- 什么错误应该重试？
- 等多久再重试？
- 重试多少次？
- 什么时候必须停止？

生产级 Retry 远不只是：

```java
for (int i = 0; i < 3; i++) {
    tryAgain();
}
```

本实验把四个概念放在一起：

- 只重试 Retryable Failure
- Exponential Backoff
- Jitter
- Overall Deadline Budget

### 场景 1：临时故障最终成功

```text
attempt 1 -> transient failure
backoff   -> wait
attempt 2 -> transient failure
backoff   -> wait
attempt 3 -> success
```

### 场景 2：永久失败立即停止

```text
attempt 1 -> permanent failure
stop      -> no retry
```

### 场景 3：Deadline 不允许继续重试

```text
attempt 1 -> transient failure
backoff   -> wait

attempt 2 -> transient failure

remaining deadline < next backoff
stop -> DEADLINE_EXHAUSTED
```

### Exponential Backoff

没有 Jitter 时可能是：

```text
100 ms
200 ms
400 ms
800 ms
...
```

通常还会设置最大 Backoff，避免无限增长。

### 为什么需要 Jitter

假设 10,000 个客户端同时遇到同一个故障。

没有 Jitter：

```text
failure
   |
   +-- all retry at 100 ms
   +-- all retry at 200 ms
   +-- all retry at 400 ms
```

大量请求会再次同步打到下游，形成 Retry Storm。

本实验使用 **Full Jitter**：

```text
cap = min(maxBackoff, initialBackoff * 2^(retryNumber - 1))
actualDelay = random(0, cap)
```

为了让教学输出可复现，Demo 使用固定随机 seed。生产系统不应让所有客户端共享相同固定 seed。

### Deadline Budget

每次准备 sleep 前检查：

```text
remainingBudget > retryDelay
```

如果下一次 backoff 本身都会消耗掉剩余预算，则立即停止。

核心原则：

> Retry 必须服从原始请求 Deadline。

否则一个 300ms 请求可能因为重试被拖成几秒钟。

### Retryable 与 Non-Retryable

常见 Retryable：

- connection reset
- temporary network failure
- HTTP 429
- HTTP 502 / 503 / 504
- transient gRPC unavailable
- 某些 optimistic-lock conflict

常见 Non-Retryable：

- validation failure
- authentication / authorization failure
- malformed request
- insufficient balance
- deterministic business-rule rejection

最终分类必须依据 API Contract。

### 运行

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.retry.App
```

### 生产设计问题

启用 Retry 前至少问：

1. 操作是否幂等？
2. 哪些错误码可重试？
3. Overall Deadline 是多少？
4. Deadline 内能容纳几次尝试？
5. 是否使用 Exponential Backoff？
6. 是否使用 Jitter？
7. 下游是否已经内部重试？
8. 多层 Retry 是否会放大流量？
9. 下游部分故障时会发生什么？
10. Retry Count 和 Retry Latency 是否可观察？

### Retry Amplification

如果每层都重试 3 次：

```text
API Gateway: 3
Service A:   3
Service B:   3

worst case fan-out = 3 * 3 * 3 = 27 attempts
```

所以 Retry Ownership 必须明确。

### 下一步

下一主题是 **Idempotency（幂等）**，因为只有重复执行不会制造重复副作用时，Retry 才真正安全。

---

## English

### Question

When a downstream call fails, when should we retry, how long should we wait, and when must we stop?

A production retry policy is more than:

```java
for (int i = 0; i < 3; i++) {
    tryAgain();
}
```

This lab combines:

- retryable failure classification
- exponential backoff
- jitter
- overall deadline budgeting

### Scenario 1: Transient failure eventually succeeds

```text
attempt 1 -> transient failure
backoff   -> wait
attempt 2 -> transient failure
backoff   -> wait
attempt 3 -> success
```

### Scenario 2: Permanent failure stops immediately

```text
attempt 1 -> permanent failure
stop      -> no retry
```

### Scenario 3: Deadline prevents another retry

```text
attempt 1 -> transient failure
backoff   -> wait
attempt 2 -> transient failure
remaining deadline < next backoff
stop -> DEADLINE_EXHAUSTED
```

### Exponential Backoff

Without jitter:

```text
100 ms
200 ms
400 ms
800 ms
...
```

A maximum backoff is normally applied.

### Why Jitter Matters

When thousands of clients fail together, fixed retry schedules can synchronize them into a retry storm.

This lab uses **Full Jitter**:

```text
cap = min(maxBackoff, initialBackoff * 2^(retryNumber - 1))
actualDelay = random(0, cap)
```

A fixed seed is used only to make the teaching output reproducible.

### Deadline Budget

Before sleeping:

```text
remainingBudget > retryDelay
```

If the next backoff would consume the remaining request budget, retry stops.

> Retry must fit inside the original request deadline.

### Retryable vs Non-Retryable

Typical retryable failures include connection resets, temporary network errors, HTTP 429 / 502 / 503 / 504, transient gRPC unavailable, and selected optimistic-lock conflicts.

Typical non-retryable failures include validation, authentication, authorization, malformed requests, insufficient balance, and deterministic business-rule rejections.

The exact classification depends on the API contract.

### Run

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.retry.App
```

### Production Questions

Before enabling retry, ask:

1. Is the operation idempotent?
2. Which error codes are retryable?
3. What is the total deadline?
4. How many attempts fit into that deadline?
5. Is there exponential backoff?
6. Is there jitter?
7. Does the downstream already retry internally?
8. Could multiple retry layers multiply traffic?
9. What happens under partial downstream outage?
10. Are retry count and retry latency observable?

### Retry Amplification

```text
API Gateway: 3
Service A:   3
Service B:   3

worst case fan-out = 3 * 3 * 3 = 27 attempts
```

Retry ownership should therefore be explicit.

### Next Experiment

The natural follow-up is **idempotency**, because retries are only safe when repeated execution cannot create duplicate side effects.
