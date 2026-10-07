# Java Lab Roadmap

> Open-source source-reading plan: [OPEN_SOURCE_LEARNING_PATH.md](./OPEN_SOURCE_LEARNING_PATH.md)
>
> Current recommended starting point: **Caffeine → Resilience4j → Nacos → gRPC Java**.

## Phase 1 — Java & Concurrency

- [x] Preserve and clean legacy Java experiments
- [x] Establish the new `labs/` structure
- [x] CompletableFuture parallel quote aggregation
- [x] ThreadPoolExecutor sizing and rejection behavior
- [x] CPU-bound vs I/O-bound thread-pool sizing experiment
- [x] CompletableFuture exception, deadline, and cancellation behavior
- [ ] Virtual threads comparison

## Phase 2 — JVM

- [ ] Class loading lifecycle
- [ ] Heap / stack / metaspace experiments
- [ ] GC observation with JDK tools
- [ ] Thread dump analysis

## Phase 3 — Spring

- [ ] Bean lifecycle
- [ ] AOP
- [ ] Transaction propagation
- [ ] Spring Boot auto-configuration

## Phase 4 — Backend Infrastructure

- [ ] MySQL index experiments
- [ ] Redis cache patterns
- [ ] Kafka consumer lag and retry
- [ ] gRPC timeout and retry

## Phase 5 — Distributed Systems

- [x] Idempotency
- [x] Retry, exponential backoff, jitter, and deadline budget
- [ ] Distributed lock
- [ ] Rate limiting
- [ ] Circuit breaker
- [ ] Multi-provider routing and aggregation

## Working Method

For every topic:

1. Define the question.
2. Build the smallest runnable experiment.
3. Observe behavior.
4. Explain why it happens.
5. Record trade-offs and failure cases.
