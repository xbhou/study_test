# ThreadPoolExecutor Behavior Lab / 线程池行为实验

## 中文

### 问题

当任务到达速度高于处理速度时，`ThreadPoolExecutor` 会按照什么顺序使用：

1. core threads
2. work queue
3. 扩容到 `maximumPoolSize`
4. rejection policy

### 配置

```text
corePoolSize     = 2
maximumPoolSize  = 4
queueCapacity    = 2
submittedTasks   = 8
```

实验通过 `CountDownLatch` 故意阻塞正在运行的任务，从而让任务接纳顺序具有确定性。

### 预期顺序

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

关键规则：

> ThreadPoolExecutor 先使用核心线程，再进入队列，再扩容到 maximumPoolSize，最后才触发拒绝策略。

一个常见误解是线程池会先扩容到 `maximumPoolSize` 再排队，实际上不是。

### 运行

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.threadpool.ThreadPoolAdmissionDemo
```

### 观察重点

释放 latch 之前，大致应该看到：

```text
poolSize      = 4
activeCount   = 4
queueSize     = 2
rejectedCount = 2
```

释放 latch 后，队列中的任务开始执行，线程池最终关闭。

### 为什么这对后端系统重要

线程池设计不仅取决于 CPU 核数，Queue 和 Rejection Policy 共同决定系统过载时的行为。

生产设计需要问：

- 任务是 CPU-bound 还是 I/O-bound？
- Queue 是否必须有界？
- 排队任务可以容忍多少延迟？
- 过载时是否应该 fail fast？
- 是否应该使用 CallerRunsPolicy？
- 哪些 Metrics 应触发告警？
- 下游变慢是否可能导致线程池耗尽？

### 后续实验

- 对比 `AbortPolicy` 和 `CallerRunsPolicy`
- 有界队列 vs 无界队列
- Queue Waiting Time
- 下游延迟突增
- Thread Pool Metrics 与告警

---

## English

### Question

When tasks arrive faster than they can be processed, in what order does `ThreadPoolExecutor` use:

1. core threads
2. the work queue
3. extra threads up to `maximumPoolSize`
4. the rejection policy

### Configuration

```text
corePoolSize     = 2
maximumPoolSize  = 4
queueCapacity    = 2
submittedTasks   = 8
```

The demo deliberately blocks running tasks with a latch so the admission sequence is deterministic.

### Expected Admission Sequence

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

Key rule:

> ThreadPoolExecutor fills core threads first, then the queue, then grows toward maximumPoolSize, then rejects.

A common misunderstanding is to assume the pool always grows to `maximumPoolSize` before queueing. It does not.

### Run

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.threadpool.ThreadPoolAdmissionDemo
```

### What to Observe

Before the latch is released, the executor should report roughly:

```text
poolSize      = 4
activeCount   = 4
queueSize     = 2
rejectedCount = 2
```

After releasing the latch, queued tasks are allowed to execute and the pool eventually terminates.

### Why This Matters in Backend Systems

Thread-pool sizing is not only about CPU count. The queue and rejection policy determine overload behavior.

Questions to ask:

- Is the workload CPU-bound or I/O-bound?
- Should the queue be bounded?
- How much latency can queued work tolerate?
- Should overload fail fast?
- Should the caller execute work?
- What metrics should trigger alerts?
- Can downstream slowness cause thread-pool exhaustion?

### Next Experiments

- compare `AbortPolicy` and `CallerRunsPolicy`
- compare bounded vs unbounded queues
- measure queue waiting time
- simulate downstream latency spikes
- add pool metrics and alerts
