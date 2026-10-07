# Concurrency

Experiments covering threads, executors, CompletableFuture, synchronization, cancellation, timeouts, and virtual threads.

## Labs

### CompletableFuture Quote Aggregator

Path: `completable-future-quote/`

Focus:

- parallel asynchronous calls
- timeout isolation
- result aggregation
- best-result selection

### CompletableFuture Deadline & Cancellation

Path: `completable-future-deadline/`

Focus:

- overall request deadlines
- exception isolation with `handle`
- partial results
- cancellation semantics
- why cancelling a CompletableFuture does not guarantee the underlying work stops

### ThreadPoolExecutor Behavior

Path: `thread-pool-behavior/`

Focus:

- core vs maximum pool size
- bounded queues
- task admission order
- saturation
- rejection behavior

### Thread Pool Sizing

Path: `thread-pool-sizing/`

Focus:

- CPU-bound vs I/O-bound workloads
- throughput measurement
- diminishing returns after CPU saturation
- why production sizing needs measurement instead of a magic formula
