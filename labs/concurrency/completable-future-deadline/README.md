# CompletableFuture Deadline & Cancellation Lab / Deadline 与取消实验

## 中文

### 问题

当以下情况同时存在时，聚合服务应该如何处理？

- 一个 Provider 失败
- 一个 Provider 很慢
- 整体请求有 Deadline
- 部分 Provider 已经返回有效结果
- 希望取消已经没有价值的剩余任务

这个实验是在并行报价聚合实验上的进一步扩展。

### 场景

```text
Provider A   80 ms   success   100.12
Provider D  120 ms   failure
Provider B  220 ms   success    99.98
Provider C  800 ms   success    99.50

Overall request deadline = 300 ms
```

大约 300ms 时预期：

```text
A -> success
D -> failure, isolated
B -> success
C -> cancellation requested because deadline expired

best available quote -> Provider B / 99.98
```

### 核心概念

#### 1. Provider 失败不一定应该让整个请求失败

每个 Provider 调用通过 `handle` 转换成 `ProviderOutcome`：

```text
success -> Quote
failure -> Throwable
```

这样失败会成为显式数据，聚合器仍可使用其他成功结果。

#### 2. Provider Timeout 与 Request Deadline 是不同概念

Provider Timeout 回答：

> 一个下游调用最多允许执行多久？

Overall Deadline 回答：

> 整个用户请求最多允许执行多久？

真实系统通常两者都需要。

#### 3. `orTimeout` 不会自动取消子 Future

对 `allOf(...)` 使用 `orTimeout`，只是让这个聚合 Future 在超时后异常完成。

子 Provider Future 仍可能继续运行。

#### 4. `CompletableFuture.cancel(true)` 更接近逻辑取消

对于 `CompletableFuture`，`mayInterruptIfRunning` 并不会保证正在执行的 supplier 被中断。

Future 可以被标记为 cancelled，但底层任务仍可能执行完成。

因此真实取消通常需要底层客户端配合：

- HTTP request cancellation
- gRPC deadline / cancellation
- DB query timeout
- interrupt-aware API
- explicit cancellation token

### 运行

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.deadline.App
```

### 观察重点

聚合器会在大约 300ms Deadline 附近返回，Provider B 是当前最优报价。

Provider C 可能在之后仍打印执行完成，因为：

```text
future cancelled
!=
underlying work definitely stopped
```

### 生产设计问题

- End-to-end Deadline 是多少？
- 每个下游是否应该有更严格的独立 Timeout？
- 是否允许 Partial Result？
- 哪些失败可以隔离？
- 哪些失败必须让整体失败？
- Cancellation 是否真正传播到 RPC / HTTP Client？
- Late Result 如何丢弃？
- Timeout、Cancellation、Dependency Failure 如何分别监控？

---

## English

### Question

How should an aggregator behave when a provider fails, another is slow, the request has an overall deadline, partial results are already available, and unfinished work should be cancelled?

### Scenario

```text
Provider A   80 ms   success   100.12
Provider D  120 ms   failure
Provider B  220 ms   success    99.98
Provider C  800 ms   success    99.50

Overall request deadline = 300 ms
```

Expected result:

```text
A -> success
D -> failure, isolated
B -> success
C -> cancellation requested because deadline expired

best available quote -> Provider B / 99.98
```

### Key Ideas

#### 1. Provider failure should not necessarily fail the whole request

Each provider call becomes a `ProviderOutcome` through `handle`, making failure explicit data.

#### 2. Provider timeout and request deadline are different

A provider timeout limits one dependency call. An overall deadline limits the entire user-facing operation.

#### 3. `orTimeout` does not cancel child futures

Applying `orTimeout` to `allOf(...)` only completes that aggregate future exceptionally after the timeout. Child futures may keep running.

#### 4. `CompletableFuture.cancel(true)` is logical cancellation

Cancellation marks the future as cancelled, but the running supplier may still finish.

Real cancellation often requires support from HTTP clients, gRPC, database drivers, interrupt-aware APIs, or explicit cancellation tokens.

### Run

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.deadline.App
```

### What to Observe

The aggregator returns around the 300 ms deadline with Provider B as the best available quote.

Provider C may still finish later.

```text
future cancelled
!=
underlying work definitely stopped
```

### Production Design Questions

- What is the end-to-end deadline?
- Does each dependency need its own tighter timeout?
- Are partial results acceptable?
- Which failures should be isolated?
- Which failures should fail the whole request?
- Does cancellation propagate to the RPC / HTTP layer?
- How are late results discarded?
- How are timeout, cancellation, and dependency failures measured?
