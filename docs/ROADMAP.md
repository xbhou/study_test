# Java Lab Roadmap

## Phase 1 — Java & Concurrency

- [x] Preserve and clean legacy Java experiments
- [x] Establish the new `labs/` structure
- [x] CompletableFuture parallel quote aggregation
- [x] ThreadPoolExecutor sizing and rejection behavior
- [x] CPU-bound vs I/O-bound thread-pool sizing experiment
- [ ] CompletableFuture exception and cancellation behavior
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

- [ ] Idempotency
- [ ] Retry and backoff
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
