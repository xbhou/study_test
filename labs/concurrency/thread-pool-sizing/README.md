# Thread Pool Sizing Lab

## Question

Why should CPU-bound and I/O-bound workloads use different thread-pool sizing strategies?

This lab compares the same number of tasks with several pool sizes under two workloads:

- CPU-bound: a fixed amount of computation per task
- I/O-bound: simulated blocking I/O using `Thread.sleep`

It reports total elapsed time and throughput for each pool size.

## Important Principle

There is no universal production formula for the "correct" thread count.

A useful mental model is:

```text
CPU-bound
  -> throughput usually stops improving once CPU capacity is saturated
  -> extra threads mainly add scheduling and contention

I/O-bound
  -> more threads can improve throughput while many tasks are blocked
  -> until another limit becomes the bottleneck
```

But real sizing also depends on:

- CPU quota in containers
- downstream latency
- DB / HTTP connection-pool size
- queue capacity
- memory
- timeout budget
- request arrival rate
- acceptable tail latency
- rejection strategy

## Why the CPU Work Uses Fixed Iterations

A bad benchmark would make every CPU task "spin for 80 ms of wall-clock time".

That is misleading because oversubscribed threads still finish when 80 ms of wall-clock time has passed, even if each thread received less actual CPU time.

This lab instead gives every CPU task the same fixed amount of computation. That makes pool-size comparisons more meaningful.

## Run

Requires JDK 17+ and Maven.

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.threadpool.ThreadPoolSizingExperiment
```

## How to Read the Output

Example shape:

```text
Detected processors: 4

CPU_BOUND
pool=1  elapsed=... ms throughput=... tasks/s
pool=4  elapsed=... ms throughput=... tasks/s
pool=8  elapsed=... ms throughput=... tasks/s

IO_BOUND
pool=1  elapsed=... ms throughput=... tasks/s
pool=4  elapsed=... ms throughput=... tasks/s
pool=8  elapsed=... ms throughput=... tasks/s
```

Do not compare exact numbers across machines. Compare the trend.

For CPU-bound work, increasing from one worker toward the available CPU capacity should help strongly. Increasing far beyond available CPU capacity should eventually show diminishing returns.

For blocking I/O, additional workers may continue to improve throughput because many threads spend time waiting instead of consuming CPU.

## Production Sizing Workflow

A safer workflow than memorizing a formula:

1. classify the workload
2. measure service time and blocking time
3. identify hard limits such as DB / HTTP connection pools
4. choose a bounded queue
5. benchmark several thread counts
6. observe throughput and p95 / p99 latency
7. test overload and rejection behavior
8. monitor continuously in production

## Metrics Worth Watching

- active thread count
- pool size
- queue depth
- task wait time
- task execution time
- rejection count
- request p95 / p99 latency
- downstream latency
- CPU utilization
- GC pressure

## Caveats

This is a teaching experiment, not a production benchmark.

JIT warmup, CPU frequency scaling, container CPU quotas, noisy neighbors, and OS scheduling can all affect the results.

## Relationship to the Previous Lab

`thread-pool-behavior` explains **how ThreadPoolExecutor admits tasks**.

This lab asks the next question:

> How many worker threads should a service actually have?

The answer should come from workload characteristics and measurements, not from a single magic number.
