# Retry + Exponential Backoff + Jitter + Deadline

## Question

When a downstream call fails, when should we retry, how long should we wait, and when must we stop?

A production retry policy is more than:

```java
for (int i = 0; i < 3; i++) {
    tryAgain();
}
```

This lab demonstrates four ideas together:

- retry only retryable failures
- exponential backoff
- jitter to avoid synchronized retry storms
- overall deadline budget

## Scenarios

The demo runs three scenarios.

### 1. Transient failure eventually succeeds

```text
attempt 1 -> transient failure
backoff   -> wait
attempt 2 -> transient failure
backoff   -> wait
attempt 3 -> success
```

### 2. Permanent failure stops immediately

```text
attempt 1 -> permanent failure
stop      -> no retry
```

### 3. Deadline budget prevents another retry

```text
attempt 1 -> transient failure
backoff   -> wait

attempt 2 -> transient failure

remaining deadline < next backoff
stop -> DEADLINE_EXHAUSTED
```

## Exponential Backoff

Without jitter, a simple exponential schedule could look like:

```text
100 ms
200 ms
400 ms
800 ms
...
```

A common cap is applied so it does not grow forever.

## Why Jitter Matters

Imagine 10,000 clients all receive the same error at the same time.

Without jitter:

```text
failure
   |
   +-- all retry at 100 ms
   +-- all retry at 200 ms
   +-- all retry at 400 ms
```

This can create a retry storm.

With jitter, retry times are spread across a window.

This lab uses **Full Jitter**:

```text
cap = min(maxBackoff, initialBackoff * 2^(retryNumber - 1))

actualDelay = random(0, cap)
```

For reproducible learning output, the demo uses a fixed random seed.

Production systems should not reuse one deterministic seed across all clients.

## Deadline Budget

The retry executor receives an overall deadline.

Before sleeping for the next retry, it checks:

```text
remainingBudget > retryDelay
```

If the backoff itself would consume the remaining budget, the retry stops.

This is the important design rule:

> Retry must fit inside the original request budget.

A retry policy that ignores the deadline can turn a 300 ms user request into a multi-second request.

## Retryable vs Non-Retryable Errors

Typical retryable failures:

- connection reset
- temporary network failure
- HTTP 429
- HTTP 502 / 503 / 504
- transient gRPC unavailable
- optimistic-lock conflict in some workflows

Typical non-retryable failures:

- validation failure
- authentication failure
- authorization failure
- malformed request
- insufficient balance
- deterministic business-rule rejection

The exact classification depends on the API contract.

## Run

Requires JDK 17+ and Maven.

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.retry.App
```

## Production Questions

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

## Retry Amplification

If every layer retries three times:

```text
API Gateway: 3
Service A:   3
Service B:   3

worst case fan-out = 3 * 3 * 3 = 27 attempts
```

This is why retry ownership should be explicit.

## Next Experiment

The natural follow-up is **idempotency**, because retries are only safe when repeated execution does not create duplicated side effects.
