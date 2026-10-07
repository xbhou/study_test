# Thread Pool Sizing Lab

## Question

Why should CPU-bound and I/O-bound workloads use different thread-pool sizing strategies?

This lab compares the same number of tasks with several pool sizes under two workloads:

- CPU-bound: busy computation for a fixed duration
- I/O-bound: simulated blocking I/O using `Thread.sleep`

It reports total elapsed time and throughput for each pool size.

## Important Principle

There is no universal production formula for the "correct" thread count.

A useful mental model is:

```text
CPU-bound
  -> too many threads usually add scheduling overhead and contention

I/O-bound
  -> more threads may improve throughput while many tasks are blocked
```

But real sizing also depends on:

- CPU quota in containers
- downstream latency
- connection-pool size
- queue capacity
- memory
- timeout budget
- request arrival rate
- acceptable tail latency
- rejection strategy

## Run

Requires JDK 17+ and Maven.

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.threadpool.ThreadPoolSizingExperiment
```

## How to Read the Output

Example shape:

```text
Detected processors: 8

CPU_BOUND
pool=1  elapsed=... ms throughput=... tasks/s
pool=8  elapsed=... ms throughput=... tasks/s
pool=16 elapsed=... ms throughput=... tasks/s

IO_BOUND
pool=1  elapsed=... ms throughput=... tasks/s
pool=8  elapsed=... ms throughput=... tasks/s
pool=16 elapsed=... ms throughput=... tasks/s
```

Do not compare exact numbers across machines. Compare the trend.

## Production Sizing Workflow

A safer workflow than memorizing a formula:

1. classify the workload
2. measure service time and blocking time
3. identify hard limits such as DB/HTTP connection pools
4. choose a bounded queue
5. benchmark several thread counts
6. observe throughput and p95/p99 latency
7. test overload and rejection behavior
8. monitor continuously in production

## Metrics Worth Watching

- active thread count
- pool size
- queue depth
- task wait time
- task execution time
- rejection count
- request p95/p99 latency
- downstream latency
- CPU utilization
- GC pressure

## Relationship to the Previous Lab

`thread-pool-behavior` explains **how ThreadPoolExecutor admits tasks**.

This lab asks the next question:

> How many worker threads should a service actually have?

The answer should come from workload characteristics and measurements, not from a single magic number.
