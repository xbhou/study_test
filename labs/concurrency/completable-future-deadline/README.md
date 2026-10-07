# CompletableFuture Deadline & Cancellation Lab

## Question

How should an aggregator behave when:

- one provider fails
- one provider is slow
- the whole request has an overall deadline
- some providers have already returned useful partial results
- we want to cancel work that is no longer useful

This lab extends the earlier quote aggregation experiment.

## Scenario

```text
Provider A   80 ms   success   100.12
Provider D  120 ms   failure
Provider B  220 ms   success    99.98
Provider C  800 ms   success    99.50

Overall request deadline = 300 ms
```

Expected result at roughly 300 ms:

```text
A -> success
D -> failure, isolated
B -> success
C -> cancellation requested because deadline expired

best available quote -> Provider B / 99.98
```

## Key Ideas

### 1. Provider failure should not necessarily fail the whole request

Each provider call is converted into a `ProviderOutcome` with `handle`.

That makes failure explicit data:

```text
success -> Quote
failure -> Throwable
```

The aggregator can still use successful providers.

### 2. Provider timeout and request deadline are different concepts

A provider timeout answers:

> How long may one dependency call run?

An overall deadline answers:

> How long may the user-facing operation run in total?

Real services often need both.

### 3. `orTimeout` does not cancel child futures

Applying `orTimeout` to `allOf(...)` only causes that aggregate future to complete exceptionally when the deadline is exceeded.

The provider futures keep running unless something else cancels or stops them.

### 4. `CompletableFuture.cancel(true)` is logical cancellation

For `CompletableFuture`, the `mayInterruptIfRunning` argument does not interrupt the running supplier.

Cancellation marks the future as cancelled, so its eventual result is ignored, but the underlying supplier may still finish.

This is why real cancellation often requires cooperation from the underlying client:

- HTTP request cancellation
- gRPC deadline / cancellation
- database query timeout
- interrupt-aware blocking API
- explicit cancellation token

## Run

Requires JDK 17+ and Maven.

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.deadline.App
```

## What to Observe

The aggregator returns around the 300 ms deadline with Provider B as the best available quote.

After that, Provider C may still print that it finished later. Its value does not become part of the aggregation result because its future was already cancelled.

That distinction is important:

```text
future cancelled
!=
underlying work definitely stopped
```

## Production Design Questions

When implementing this pattern for a real backend system, ask:

- What is the end-to-end request deadline?
- Does each downstream dependency also have its own tighter timeout?
- Are partial results acceptable?
- Which failures should be isolated?
- Which failures should fail the whole request?
- Does cancellation actually propagate to the client / RPC layer?
- How are late results discarded?
- What metrics distinguish timeout, cancellation, and dependency failure?

## Next Step

The natural next experiment is retry with exponential backoff and jitter, because retry must fit inside the same deadline budget.
