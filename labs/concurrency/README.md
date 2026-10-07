# Concurrency / 并发编程

## 中文

这里收录 Java 并发相关实验，包括线程、线程池、`CompletableFuture`、同步、取消、超时与后续的虚拟线程。

### Labs

#### CompletableFuture Quote Aggregator

路径：`completable-future-quote/`

重点：

- 并行异步调用
- 超时隔离
- 结果归集
- 最优结果选择

#### CompletableFuture Deadline & Cancellation

路径：`completable-future-deadline/`

重点：

- 整体请求 Deadline
- 使用 `handle` 做异常隔离
- 部分结果
- cancellation 语义
- 为什么取消 `CompletableFuture` 不等于底层任务一定停止

#### ThreadPoolExecutor Behavior

路径：`thread-pool-behavior/`

重点：

- corePoolSize 与 maximumPoolSize
- 有界队列
- 任务接纳顺序
- 饱和
- 拒绝策略

#### Thread Pool Sizing

路径：`thread-pool-sizing/`

重点：

- CPU-bound 与 I/O-bound
- 吞吐量测量
- CPU 饱和后的收益递减
- 为什么生产参数应该靠测量，而不是魔法公式

---

## English

Experiments covering threads, executors, CompletableFuture, synchronization, cancellation, timeouts, and virtual threads.

### Labs

#### CompletableFuture Quote Aggregator

Path: `completable-future-quote/`

Focus:

- parallel asynchronous calls
- timeout isolation
- result aggregation
- best-result selection

#### CompletableFuture Deadline & Cancellation

Path: `completable-future-deadline/`

Focus:

- overall request deadlines
- exception isolation with `handle`
- partial results
- cancellation semantics
- why cancelling a CompletableFuture does not guarantee the underlying work stops

#### ThreadPoolExecutor Behavior

Path: `thread-pool-behavior/`

Focus:

- core vs maximum pool size
- bounded queues
- task admission order
- saturation
- rejection behavior

#### Thread Pool Sizing

Path: `thread-pool-sizing/`

Focus:

- CPU-bound vs I/O-bound workloads
- throughput measurement
- diminishing returns after CPU saturation
- why production sizing needs measurement instead of a magic formula
