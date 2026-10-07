# ThreadPoolExecutor Behavior Lab

## Question

When tasks arrive faster than they can be processed, in what order does `ThreadPoolExecutor` use:

1. core threads
2. the work queue
3. extra threads up to `maximumPoolSize`
4. the rejection policy

## Configuration

```text
corePoolSize     = 2
maximumPoolSize  = 4
queueCapacity    = 2
submittedTasks   = 8
```

The demo deliberately blocks running tasks with a latch so the admission sequence is deterministic.

## Expected Admission Sequence

```text
Task 1 -> core thread
Task 2 -> core thread

Task 3 -> queue
Task 4 -> queue

Task 5 -> extra thread
Task 6 -> extra thread

Task 7 -> rejected
Task 8 -> rejected
```

This is the key rule:

> ThreadPoolExecutor fills core threads first, then the queue, then grows toward maximumPoolSize, then rejects.

A common misunderstanding is to assume the pool always grows to `maximumPoolSize` before queueing. It does not.

## Run

Requires JDK 17+ and Maven.

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.threadpool.ThreadPoolAdmissionDemo
```

## What to Observe

Before the latch is released, the executor should report roughly:

```text
poolSize      = 4
activeCount   = 4
queueSize     = 2
rejectedCount = 2
```

After releasing the latch, queued tasks are allowed to execute and the pool eventually terminates.

## Why This Matters in Backend Systems

Thread-pool sizing is not only about CPU count. The queue and rejection policy determine overload behavior.

Questions to ask in production design:

- Is the workload CPU-bound or I/O-bound?
- Should the queue be bounded?
- How much latency can queued work tolerate?
- Should overload fail fast?
- Should the caller execute work?
- What metrics should trigger alerts?
- Can downstream slowness cause thread-pool exhaustion?

## Next Experiments

- compare `AbortPolicy` and `CallerRunsPolicy`
- compare bounded vs unbounded queues
- measure queue waiting time
- simulate downstream latency spikes
- add pool metrics and alerts
