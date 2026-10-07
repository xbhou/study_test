# Thread Pool Sizing Lab / 线程池容量设计实验

## 中文

### 问题

为什么 CPU-bound 和 I/O-bound 任务需要不同的线程池容量设计？

本实验用相同任务数量，对比多个线程数下两类 workload：

- CPU-bound：每个任务执行固定计算量
- I/O-bound：使用 `Thread.sleep` 模拟阻塞 I/O

输出总耗时和吞吐量。

### 核心原则

生产环境不存在一个适用于所有场景的“正确线程数公式”。

可以先建立这样的直觉：

```text
CPU-bound
  -> CPU 饱和后吞吐提升趋缓
  -> 更多线程主要增加调度和竞争

I/O-bound
  -> 大量任务在等待时，更多线程可能提升吞吐
  -> 直到其他资源成为瓶颈
```

真实 sizing 还取决于：

- Container CPU Quota
- 下游延迟
- DB / HTTP Connection Pool
- Queue Capacity
- Memory
- Timeout Budget
- 请求到达速率
- 可接受的 Tail Latency
- Rejection Strategy

### 为什么 CPU 实验使用固定计算量

如果让每个 CPU 任务“忙 80ms 墙钟时间”，会产生误导。

线程被抢占时，墙钟时间仍然继续，导致超量线程可能看起来也能快速完成。

所以本实验让每个 CPU 任务执行相同固定计算量，使线程数对比更有意义。

### 运行

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.threadpool.ThreadPoolSizingExperiment
```

### 如何看结果

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

不要跨机器比较绝对数字，应该比较趋势。

CPU-bound 通常在接近可用 CPU 并行度后出现明显收益递减；Blocking I/O 则可能随着更多 worker 继续提升吞吐。

### 生产线程池设计流程

1. 判断 workload 类型
2. 测量 Service Time 与 Blocking Time
3. 找到 DB / HTTP Connection Pool 等硬限制
4. 使用有界队列
5. Benchmark 多组线程数
6. 观察 Throughput 与 p95 / p99
7. 测试 Overload 和 Rejection
8. 上线后持续监控

### 关注指标

- Active Thread Count
- Pool Size
- Queue Depth
- Task Wait Time
- Task Execution Time
- Rejection Count
- Request p95 / p99
- Downstream Latency
- CPU Utilization
- GC Pressure

### 注意

这是教学实验，不是生产级 Benchmark。JIT warmup、CPU frequency、Container CPU quota、noisy neighbor 和 OS scheduling 都会影响结果。

---

## English

### Question

Why should CPU-bound and I/O-bound workloads use different thread-pool sizing strategies?

This lab compares the same number of tasks with several pool sizes under two workloads:

- CPU-bound: a fixed amount of computation per task
- I/O-bound: simulated blocking I/O using `Thread.sleep`

It reports total elapsed time and throughput.

### Important Principle

There is no universal production formula for the "correct" thread count.

```text
CPU-bound
  -> throughput usually stops improving once CPU capacity is saturated
  -> extra threads mainly add scheduling and contention

I/O-bound
  -> more threads can improve throughput while many tasks are blocked
  -> until another limit becomes the bottleneck
```

Real sizing also depends on CPU quotas, downstream latency, connection-pool size, queue capacity, memory, timeout budget, request arrival rate, tail latency, and rejection strategy.

### Why Fixed CPU Work Is Used

A benchmark that makes every CPU task spin for a fixed wall-clock duration is misleading under oversubscription.

This lab gives every CPU task the same fixed amount of computation, making comparisons more meaningful.

### Run

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.threadpool.ThreadPoolSizingExperiment
```

### Reading the Output

Compare trends, not exact numbers across machines.

CPU-bound workloads usually show diminishing returns after CPU capacity is saturated. Blocking I/O may keep benefiting from more workers while tasks spend time waiting.

### Production Sizing Workflow

1. classify the workload
2. measure service time and blocking time
3. identify hard limits such as connection pools
4. choose a bounded queue
5. benchmark several thread counts
6. observe throughput and p95 / p99 latency
7. test overload and rejection behavior
8. monitor continuously in production

### Metrics

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

### Caveat

This is a teaching experiment, not a production benchmark. JIT warmup, CPU frequency scaling, container quotas, noisy neighbors, and OS scheduling can affect results.
